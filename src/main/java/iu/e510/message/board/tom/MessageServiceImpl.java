package iu.e510.message.board.tom;

import iu.e510.message.board.tom.common.LamportClock;
import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.tom.common.NonBlockingPayload;
import iu.e510.message.board.tom.common.Payload;
import iu.e510.message.board.tom.core.*;
import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;

public class MessageServiceImpl implements MessageService {
    private static Logger logger = LoggerFactory.getLogger(MessageServiceImpl.class);

    private String nodeID;
    private String serverBindURI;
    private LamportClock clock;
    private MessageReceiver messageReceiver;
    private MessageSender messageSender;
    private MessageHandler messageHandler;
    private MessageDeliveryService messageDeliveryService;
    private DeliveryHandler deliveryHandler;
    private ConcurrentSkipListSet<Message> messageQueue;
    private Config config;
    private ZContext context;

    private BlockingQueue<NonBlockingPayload> superNodeMsgQueue;


    public MessageServiceImpl(String serverBindURI, String nodeID,
                              ConcurrentSkipListSet<Message> messageQueue,
                              BlockingQueue<NonBlockingPayload> superNodeMsgQueue) {
        config = new Config();
        clock = LamportClock.getClock();
        this.serverBindURI = serverBindURI;
        this.nodeID = nodeID;
        this.messageQueue = messageQueue;
        this.superNodeMsgQueue = superNodeMsgQueue;

        this.context = new ZContext();
        this.messageSender = new MessageSender(context, Integer.parseInt(config.getConfig(Constants.SEND_TIMEOUT)));
        this.deliveryHandler = new DeliveryHandlerImpl();
        this.messageDeliveryService = new MessageDeliveryService(this.messageQueue, nodeID, this.deliveryHandler);
        this.messageDeliveryService.start();
        logger.info("Started the messaging service with pid: " + nodeID);
    }

    @Override
    public void init(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
        this.messageReceiver = new MessageReceiver(context, this.serverBindURI, this.messageHandler);
        this.messageReceiver.start();
    }

    @Override
    public void send_ordered(Payload message, Set<String> recipients) {
        // add yourself to the recipient list
        recipients.add(nodeID);
        // multicast the message to all the recipients
        int clock = this.clock.incrementAndGet();
        Message msg = new Message(message, nodeID, clock, false);
        msg.setRecipients(recipients);
        for (String recipient : recipients) {
            Message response = messageSender.sendMessage(msg, getUrl(recipient),
                    this.clock.get());
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
    public Message send_unordered(Payload message, String recipient) {
        int clock = this.clock.incrementAndGet();
        Message msg = new Message(message, nodeID, clock, true);
        Message response = messageSender.sendMessage(msg, recipient, this.clock.get());

        // if response received, update the clock
        if (response != null) {
            int responseClock = response.getClock();
            if (responseClock > this.clock.get()) {
                this.clock.set(responseClock);
            }
            // increment the clock for the message received.
            this.clock.incrementAndGet();
        }
        return response;
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

    @Override
    public String getUrl(String id) {
        return "tcp://" + id;
    }

    protected class DeliveryHandlerImpl implements DeliveryHandler {

        @Override
        public void deliverReleaseMessage(Message message) {
            Message releaseMessage = new Message(message.getPayload(), nodeID,
                    clock.incrementAndGet(), false);
            releaseMessage.setRelease(message.getId());

            Set<String> recipients = message.getRecipients();

            // Add delivering into data manager by putting the message into the queue
            // deliver the message to yourself
            logger.debug("[pid:" + nodeID + "][clock:" + clock.get() + "] Delivering message: "
                    + message.getId() + " to myself");
            logger.debug("Saving message for async process: " + message.toString());
            try {
                superNodeMsgQueue.put((NonBlockingPayload) message.getPayload());
            } catch (InterruptedException e) {
                logger.error("Unable to access queue ", e);
                throw new RuntimeException("Unable to access queue ", e);
            }

            // multicast the message to all the other recipients
            for (String recipient : recipients) {
                if (recipient.equals(nodeID)) {
                    continue;
                }
                messageSender.sendMessage(releaseMessage, getUrl(recipient), clock.get());
            }
        }
    }
}