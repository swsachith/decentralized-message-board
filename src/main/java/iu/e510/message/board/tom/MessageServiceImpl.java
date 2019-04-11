package iu.e510.message.board.tom;

import iu.e510.message.board.cluster.data.DataAdapter;
import iu.e510.message.board.tom.common.LamportClock;
import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.tom.common.MessageType;
import iu.e510.message.board.tom.common.Payload;
import iu.e510.message.board.tom.core.*;
import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;

import java.util.List;
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

    private BlockingQueue<Message> superNodeMsgQueue;
    private DataAdapter dataAdapter;

    public MessageServiceImpl(String serverBindURI, String nodeID, DataAdapter dataAdapter,
                              BlockingQueue<Message> superNodeMsgQueue) {
        config = new Config();
        clock = LamportClock.getClock();
        this.serverBindURI = serverBindURI;
        this.nodeID = nodeID;
        this.messageQueue = new ConcurrentSkipListSet<>();
        ZContext context = new ZContext();
        this.messageSender = new MessageSender(context, Integer.parseInt(config.getConfig(Constants.SEND_TIMEOUT)));
        this.messageHandler = new MessageHandlerImpl();
        this.deliveryHandler = new DeliveryHandlerImpl();

        this.messageReceiver = new MessageReceiver(context, this.serverBindURI, this.messageHandler);
        this.messageDeliveryService = new MessageDeliveryService(this.messageQueue, nodeID, this.deliveryHandler);

        this.dataAdapter = dataAdapter;
        this.superNodeMsgQueue = superNodeMsgQueue;

        this.messageReceiver.start();
        this.messageDeliveryService.start();
        logger.info("Started the messaging service with pid: " + nodeID);
    }

    @Override
    public void send_ordered(String message, List<String> recipients, MessageType messageType) {
        // add yourself to the recipient list
        recipients.add(serverBindURI);
        // multicast the message to all the recipients
        int clock = this.clock.incrementAndGet();
        Message msg = new Message(new Payload<>(message), nodeID, clock, false, messageType);
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
    public Message send_unordered(Payload message, String recipient, MessageType messageType) {
        int clock = this.clock.incrementAndGet();
        Message msg = new Message(message, nodeID, clock, true, messageType);
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
            Message releaseMessage = new Message(new Payload<>("Release"), nodeID,
                    clock.incrementAndGet(), false, message.getMessageType());
            releaseMessage.setRelease(message.getId());

            List<String> recipients = message.getRecipients();

            //todo: add delivering into data manager
            // deliver the message to yourself
            logger.info("[pid:" + nodeID + "][clock:" + clock.get() + "] Delivering message: "
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
                logger.debug("[nodeID=" + nodeID + "] [ClockUpdate]Received message from: " + message.getNodeID() +
                        ". Updating clock to the receiver's clock: " + receivedClock);
            }
            // increment the clock for the message received.
            clock.incrementAndGet();

            // if message is an ack, do not resend
            if (message.isAck()) {
                logger.debug("[nodeID:" + nodeID + "][clock:" + clock.get() + "] Received Ack for: " + message.getAck() +
                        " from: " + message.getNodeID());
                return null;
            }
            //todo: deliver messages here
            // handling the unicast vs multicast
            boolean unicast = message.isUnicast();
            if (unicast) {
                return processUnicastMessage(message);
            } else {
                return processMulticastMessage(message);
            }
        }

        private Message processUnicastMessage(Message message) {
            logger.info("Received message: " + message.getPayload() + " from: " + message.getNodeID() + " of type: " +
                    message.getMessageType());

            MessageType type = message.getMessageType();

            if (type.equals(MessageType.TRANSFER) || type.equals(MessageType.SYNC)) {
                nonBlockingMessageProcessing(message);
                return null;
            } else {
                return blockingMessageProcessing(message);
            }
        }

        private Message processMulticastMessage(Message message) {
            if (message.isRelease()) {
                // if message is a release request, deliver it, don't have to reply
                logger.info("[nodeID:" + nodeID + "][clock:" + clock.get() + "] Received release request. " +
                        "Delivering multicast message: " + message.getRelease() + " with clock: " + message.getClock()
                        + " from: " + message.getNodeID());
                message.setId(message.getRelease());

                nonBlockingMessageProcessing(message);

                messageQueue.remove(message);
                logger.info("Message queue size: " + messageQueue.size());
                return null;
            }
            // add to the message queue
            messageQueue.add(message);
            logger.info("Added multicast message to the queue. Current queue size: " + messageQueue.size());
            // updating the clock for the reply event
            int sendClock = clock.incrementAndGet();
            logger.info("[pid:" + nodeID + "][clock:" + sendClock + "] Sending ack for message: "
                    + message.getId() + " to: " + message.getNodeID());
            Message ack = new Message(new Payload<>("Ack"), nodeID, sendClock, true,
                    message.getMessageType());
            ack.setAck(message.getId());
            return ack;
        }

        private void nonBlockingMessageProcessing(Message message) {
            logger.debug("Saving message for async process: " + message.toString());
            try {
                superNodeMsgQueue.put(message);
            } catch (InterruptedException e) {
                logger.error("Unable to access queue ", e);
                throw new RuntimeException("Unable to access queue ", e);
            }
        }

        private Message blockingMessageProcessing(Message message) {
            logger.debug("Processing message: " + message.toString());

            if (message.getMessageType() == MessageType.DATA_REQUEST) {
                String topic = (String) message.getPayload().getContent();
                logger.debug("Data request received for topic: " + topic);
                byte[] data = dataAdapter.getDataDump(topic);

                return new Message(new Payload<>(nodeID, data), nodeID, clock.get(), true,
                        MessageType.DATA_RESPONSE);
            } else {
                throw new RuntimeException("Unknown message type for sync process: " + message);
            }
        }
    }
}