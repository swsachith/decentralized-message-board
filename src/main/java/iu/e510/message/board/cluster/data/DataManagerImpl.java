package iu.e510.message.board.cluster.data;

import iu.e510.message.board.cluster.ClusterManager;
import iu.e510.message.board.cluster.data.beans.BaseBean;
import iu.e510.message.board.cluster.zk.ZKManager;
import iu.e510.message.board.cluster.zk.ZKManagerImpl;
import iu.e510.message.board.db.DMBDatabase;
import iu.e510.message.board.db.model.DMBPost;
import iu.e510.message.board.tom.MessageService;
import iu.e510.message.board.tom.common.LamportClock;
import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.tom.common.MessageType;
import iu.e510.message.board.tom.common.Payload;
import iu.e510.message.board.tom.core.MessageHandler;
import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataManagerImpl implements DataManager {
    private static Logger logger = LoggerFactory.getLogger(DataManagerImpl.class);
    private ReadWriteLock lock;
    private Lock readLock;
    private Lock writeLock;
    private HashSet<String> myTopics;
    private ZKManager zkManager;
    private Config config;
    private String myNodeID;
    private String zkMyTopicStore;

    private MessageService messageService;
    private BlockingQueue<Message> superNodeMsgQueue;
    private MesssageExecutor messsageExecutor;

    private AtomicBoolean consistent;

    private ClusterManager clusterManager;

    private DMBDatabase database;

    public DataManagerImpl(String nodeID, MessageService messageService,
                           BlockingQueue<Message> superNodeMsgQueue,
                           ClusterManager clusterManager, DMBDatabase database,
                           ConcurrentSkipListSet<Message> messageQueue) throws Exception {
        logger.info("Initializing the Data Manager!");
        this.config = new Config();
        this.myNodeID = nodeID;
        this.zkMyTopicStore = config.getConfig(Constants.DATA_LOCATION) + "/" + myNodeID;

        this.consistent = new AtomicBoolean(true);
        this.messageService = messageService;
        this.superNodeMsgQueue = superNodeMsgQueue;
        this.clusterManager = clusterManager;
        this.database = database;

        this.messsageExecutor = new MesssageExecutor();

        this.lock = new ReentrantReadWriteLock();
        this.readLock = this.lock.readLock();
        this.writeLock = this.lock.writeLock();
        this.myTopics = new HashSet<>();
        this.zkManager = new ZKManagerImpl();
        MessageHandler messageHandler = new MessageHandlerImpl(nodeID, messageQueue);
        this.messageService.init(messageHandler);
        // Create my data node
        if (zkManager.exists(zkMyTopicStore) == null) {
            logger.info("No data store with my node ID found. Hence creating the new data store in ZK");
            zkManager.create(zkMyTopicStore, SerializationUtils.serialize(""), CreateMode.PERSISTENT);
        } else {
            logger.info("Existing topics found for my node ID. Hence restoring the configurations!");
            Object obj = SerializationUtils.deserialize(zkManager.getData(zkMyTopicStore));
            if (obj instanceof HashSet) {
                this.myTopics = SerializationUtils.deserialize(zkManager.getData(zkMyTopicStore));
            } else {
                this.myTopics = new HashSet<>();
            }
        }

        this.messsageExecutor.start();

        logger.info("Data Manager init done. My topics: " + myTopics.toString());
    }

    @Override
    public void addTopicData(String path) throws Exception {
        try {
            writeLock.lock();
            // write to ZK only if the topic is not already recorded
            if (!hasData(path)) {
                myTopics.add(path);
                zkManager.set(zkMyTopicStore, SerializationUtils.serialize(myTopics));
            }

        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean hasData(String path) {
        return myTopics.contains(path);
    }

    @Override
    public byte[] getData(String topic) {
        // todo: send data from the DB. Remove the following stub
        Set<String> nodes = clusterManager.getHashingNodes(topic);

        logger.debug("Requesting data for: " + topic + " from: " + nodes);
        for (String node : nodes) {
            logger.debug("Talking to " + node);

            Message response = messageService.send_unordered(new Payload<>(topic),
                    messageService.getUrl(node), MessageType.DATA_REQUEST);

            if (response != null) return (byte[]) response.getPayload().getContent();
        }

        return null;
    }

    @Override
    public void deleteData(String path) throws Exception {
        try {
            writeLock.lock();
            myTopics.remove(path);
            zkManager.set(zkMyTopicStore, SerializationUtils.serialize(myTopics));
            //todo: store data here
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public HashSet<String> getAllTopics() {
        try {
            readLock.lock();
            return myTopics;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public void setConsistent(boolean consistency) {
        this.consistent.set(consistency);
    }

    @Override
    public String getNodeId() {
        return myNodeID;
    }

    @Override
    public Set<String> getNodeIdsForTopic(String topic) {
        return clusterManager.getHashingNodes(topic);
    }

    @Override
    public boolean isConsistent() {
        return this.consistent.get();
    }

    @Override
    public Map<String, Set<String>> getTransferTopics(String newNodeID) {
        HashSet<String> myTopics = getAllTopics();
        Map<String, Set<String>> transferTopics = clusterManager.getTransferTopics(myNodeID, newNodeID, myTopics);
        // get topics to be deleted (myTopics - myNewTopics)
        myTopics.removeAll(transferTopics.get("oldNode"));
        Map<String, Set<String>> resultMap = new HashMap<>();
        resultMap.put("delete", myTopics);
        resultMap.put("transfer", transferTopics.get("newNode"));
        return resultMap;
    }


    @Override
    public Set<String> addData(BaseBean dataBean) throws Exception {
        Set<String> nodes = getNodeIdsForTopic(dataBean.getTopic());

        if (nodes.contains(myNodeID) && isConsistent()){
            logger.info("Sending multicast: " + dataBean.toString());
            messageService.send_ordered(new Payload<>(dataBean), nodes, MessageType.CLIENT_DATA);

            return Collections.emptySet();
        } else {
            logger.info("Received unrelated topic: " + dataBean.getTopic());
            return nodes;
        }
    }

    @Override
    public DMBPost getPost(String clientID, String topic, int postID) {
        return database.getPostDataByPostId(postID);
    }

    @Override
    public List<DMBPost> getPosts(String clientID, String topic) {
        return database.getPostsDataByTopicArrayList(topic);
    }


    class MesssageExecutor extends Thread {
        private boolean running = true;

        void stopExecutor() {
            this.running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    Message message = superNodeMsgQueue.take();
                    logger.info("Server is inconsistent");
                    setConsistent(false);

                    if (message.getMessageType() == MessageType.SYNC) {
                        logger.info("Processing SYNC msg: " + message);

                        // topics set which needs to be grabbed from the peers
                        Set<String> topics = (Set<String>) message.getPayload().getContent();

                        for (String topic : topics) {
                            if (hasData(topic)) {
                                logger.info("Nothing to do! Data available locally for: " + topic);
                            } else {
                                logger.info("Requesting data from the cluster for: " + topic);
                                byte[] data = getData(topic);
                                logger.debug("Received data: " + Arrays.toString(data));

                                // add data to topics set and zk
                                addTopicData(topic);
                                database.addPostsDataFromByteArray(data);
                            }
                        }
                    } else if (message.getMessageType() == MessageType.TRANSFER) {
                        logger.info("Processing TRANSFER msg: " + message);

                        // take the topics from the message
                        String nodeToTalk = (String) message.getPayload().getContent();
                        logger.info("Master asks me to talk to: " + nodeToTalk + " and transfer " +
                                "the relevant topics");

                        Message response = messageService.send_unordered(new Payload<>(myNodeID),
                                messageService.getUrl(nodeToTalk), MessageType.TRANSFER_TOPICS);

                        superNodeMsgQueue.add(response);

                    } else if (message.getMessageType() == MessageType.DELETE_TOPICS) {
                        logger.info("Delete topics received!");
                        Set<String> delTopics = (Set<String>) message.getPayload().getContent();

                        for (String topic : delTopics) {
                            deleteData(topic);
                            database.removePostsDataByTopic(topic);
                        }


                    } else if (message.getMessageType() == MessageType.CLIENT_DATA) {
                        logger.info("Client data received!");

                        Payload<BaseBean> payload = message.getPayload();
                        BaseBean bean = payload.getContent();

                        // add data to topics set and zk
                        addTopicData(bean.getTopic());

                        // process payload data bean and store the data in the db
                        bean.processBean(database);
                    }
                    else {
                        throw new RuntimeException("Unknown message type: " + message);
                    }

                    setConsistent(true);
                    logger.info("Server consistent again!");

                } catch (InterruptedException e) {
                    logger.error("Unable to access the queue: ", e);
                } catch (Exception e) {
                    throw new RuntimeException("Exception occurred ", e);
                }
            }
        }
    }
    protected class MessageHandlerImpl implements MessageHandler {
        private Logger logger = LoggerFactory.getLogger(MessageHandlerImpl.class);
        private LamportClock clock;
        private String nodeID;
        public ConcurrentSkipListSet<Message> messageQueue;
        public MessageHandlerImpl(String nodeID, ConcurrentSkipListSet<Message> messageQueue) {
            this.clock = LamportClock.getClock();
            this.nodeID = nodeID;
            this.messageQueue = messageQueue;
        }

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
            } else if (type.equals(MessageType.LOST_CONNECTION)) {
                //todo handle this, set inconsistent
                setConsistent(false);
                logger.info("Lost connection with Zookeeper, Network partitioned. Hence becoming inconsistent until connection");
                return null;
            } else {
                return blockingMessageProcessing(message);
            }
        }

        private Message processMulticastMessage(Message message) {
            if (message.isRelease()) {
                // if message is a release request, deliver it, don't have to reply
                logger.debug("[nodeID:" + nodeID + "][clock:" + clock.get() + "] Received release request. " +
                        "Delivering multicast message: " + message.getRelease() + " with clock: " + message.getClock()
                        + " from: " + message.getNodeID());
                message.setId(message.getRelease());

                nonBlockingMessageProcessing(message);

                messageQueue.remove(message);
                logger.debug("Message queue size: " + messageQueue.size());
                return null;
            }
            // add to the message queue
            messageQueue.add(message);
            logger.debug("Added multicast message to the queue. Current queue size: " + messageQueue.size());
            // updating the clock for the reply event
            int sendClock = clock.incrementAndGet();
            logger.debug("[pid:" + nodeID + "][clock:" + sendClock + "] Sending ack for message: "
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

                byte[] data = database.getPostsDataByTopicByteArray(topic);

                return new Message(new Payload<>(nodeID, data), nodeID, clock.get(), true,
                        MessageType.DATA_RESPONSE);
            } else if (message.getMessageType() == MessageType.TRANSFER_TOPICS) {
                String newNodeId = (String) message.getPayload().getContent();

                Map<String, Set<String>> transferTopics = getTransferTopics(newNodeId);

                Set<String> delete = transferTopics.get("delete");
                try {
                    if (!delete.isEmpty()) {
                        superNodeMsgQueue.put(new Message(new Payload<>(delete), myNodeID, clock.get(),
                                true, MessageType.DELETE_TOPICS));
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException("Unable to access the queue", e);
                }

                Set<String> transfer = transferTopics.get("transfer");

                return new Message(new Payload<>(transfer), myNodeID, clock.get(), true,
                        MessageType.SYNC);
            } else {
                throw new RuntimeException("Unknown message type for sync process: " + message);
            }
        }
    }
}
