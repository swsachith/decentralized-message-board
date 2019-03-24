package iu.e510.message.board.tom;

import iu.e510.message.board.tom.common.LamportClock;
import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.tom.core.*;
import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

public class MessageServiceImpl implements MessageService {
    private static Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    private int processID;
    private String serverBindURI;
    private LamportClock clock;
    private MessageReceiver messageReceiver;
    private MessageSender messageSender;
    private MessageHandler messageHandler;
    private MessageDeliveryService messageDeliveryService;
    private DeliveryHandler deliveryHandler;
    private ConcurrentSkipListSet<Message> messageQueue;
    private Config config;

    public MessageServiceImpl(String serverBindURI) {
        config = new Config();
        clock = LamportClock.getClock();
        this.serverBindURI = serverBindURI;
        this.processID = getProcessID();
        this.messageQueue = new ConcurrentSkipListSet<>();
        ZContext context = new ZContext();
        this.messageSender = new MessageSender(context, Integer.parseInt(config.getConfig(Constants.SEND_TIMEOUT)));
        this.messageHandler = new MessageHandlerImpl();
        this.deliveryHandler = new DeliveryHandlerImpl();
        this.messageReceiver = new MessageReceiver(context, processID, this.serverBindURI, this.messageHandler);
        this.messageReceiver.start();
        this.messageDeliveryService = new MessageDeliveryService(this.messageQueue, processID, this.deliveryHandler);
        messageDeliveryService.start();
        logger.info("Started the messaging service with pid: " + processID);
    }

    @Override
    public void send_ordered(String message, List<String> recipients) {
        // add yourself to the recipient list
        recipients.add(serverBindURI);
        // multicast the message to all the recipients
        int clock = this.clock.incrementAndGet();
        Message msg = new Message(message, processID, clock, false);
        msg.setRecipients(recipients);
        for (String recipient : recipients) {
            Message response = messageSender.sendMessage(msg, recipient, this.clock.get());
            if (response != null) {
                messageHandler.processMessage(response);
            } else {
                // the message was not delivered
                logger.info("Multicast message was not delivered. Hence removing it from the recipient list!");
                recipients.remove(recipient);
                msg.setRecipients(recipients);
            }
        }
        // since it use tcp, we can guarantee that all the acks have been received
        msg.setAllAcked(true);
        // we replace the msg with the acked message
        messageQueue.remove(msg);
        messageQueue.add(msg);
    }

    @Override
    public void send_unordered(String message, String recipient) {
        int clock = this.clock.incrementAndGet();
        Message msg = new Message(message, processID, clock, true);
        Message response = messageSender.sendMessage(msg, recipient, this.clock.get());
        // null is returned only when it's a unicast message
        if (response != null) {
            messageHandler.processMessage(response);
        }
    }

    @Override
    public void stop_service() {
        if (messageReceiver.isRunning()) {
            messageReceiver.stopReceiver();
        }
        if (messageDeliveryService.isRunning()) {
            messageDeliveryService.stopDeliveryService();
        }
    }

    private int getProcessID() {
        String processName = ManagementFactory.getRuntimeMXBean().getName();
        return Integer.parseInt(processName.substring(0, processName.indexOf('@')));
    }

    protected class DeliveryHandlerImpl implements DeliveryHandler {

        @Override
        public void deliverReleaseMessage(Message message) {
            Message releaseMessage = new Message("Release", processID, clock.incrementAndGet(), false);
            releaseMessage.setRelease(message.getId());

            List<String> recipients = message.getRecipients();

            // deliver the message to yourself
            logger.info("[pid:" + processID + "][clock:" + clock.get() + "] Delivering message: "
                    + message.getId() + " to myself");

            // multicast the message to all the other recipients
            for (String recipient : recipients) {
                if (recipient.equals(serverBindURI)) {
                    continue;
                }
                messageSender.sendMessage(releaseMessage, recipient, clock.get());
            }
        }
    }

    protected class MessageHandlerImpl implements MessageHandler {
        private Logger logger = LoggerFactory.getLogger(MessageHandlerImpl.class);

        public Message processMessage(Message message) {
            int receivedClock = message.getClock();
            // clock update
            if (receivedClock > clock.get()) {
                clock.set(receivedClock);
                logger.info("[pid=" + processID + "] [ClockUpdate]Received message from: " + message.getProcessID() +
                        ". Updating clock to the receiver's clock: " + receivedClock);
            }
            // increment the clock for the message received.
            int receiveClock = clock.incrementAndGet();

            // if message is an ack, do not resend
            if (message.isAck()) {
                logger.info("[pid:" + processID + "][clock:" + clock.get() + "] Received Ack for: " + message.getAck() +
                        " from: " + message.getProcessID());
                return null;
            }

            // handling the unicast vs multicast
            boolean unicast = message.isUnicast();
            if (unicast) {
                return processUnicastMessage(message, receiveClock);
            } else {
                return processMulticastMessage(message);
            }
        }

        private Message processUnicastMessage(Message message, int receiveClock) {
            logger.info("[pid:" + processID + "][clock:" + receiveClock + "] Received unicast message: "
                    + message.getId() + " from: " + message.getProcessID());
            return null;
        }

        private Message processMulticastMessage(Message message) {
            if (message.isRelease()) {
                // if message is a release request, deliver it, don't have to reply
                logger.info("[pid:" + processID + "][clock:" + clock.get() + "] Received release request. " +
                        "Delivering multicast message: " + message.getRelease() + " with clock: " + message.getClock()
                        + " from: " + message.getProcessID());
                message.setId(message.getRelease());
                messageQueue.remove(message);
                logger.info("Message queue size: " + messageQueue.size());
                return null;
            }
            // add to the message queue
            messageQueue.add(message);
            logger.info("Added multicast message to the queue. Current queue size: " + messageQueue.size());
            // updating the clock for the reply event
            int sendClock = clock.incrementAndGet();
            logger.info("[pid:" + processID + "][clock:" + sendClock + "] Sending ack for message: "
                    + message.getId() + " to: " + message.getProcessID());
            Message ack = new Message("Ack", processID, sendClock, true);
            ack.setAck(message.getId());
            return ack;
        }
    }
}