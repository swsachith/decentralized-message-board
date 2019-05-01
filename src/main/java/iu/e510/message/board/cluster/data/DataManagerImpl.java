package iu.e510.message.board.cluster.data;

import iu.e510.message.board.cluster.ClusterManager;
import iu.e510.message.board.cluster.data.beans.BaseBean;
import iu.e510.message.board.cluster.data.payloads.ClientDataPayload;
import iu.e510.message.board.cluster.data.payloads.DataRequestPayload;
import iu.e510.message.board.cluster.data.payloads.DataResponsePayload;
import iu.e510.message.board.cluster.zk.ZKManager;
import iu.e510.message.board.cluster.zk.ZKManagerImpl;
import iu.e510.message.board.db.DMBDatabase;
import iu.e510.message.board.db.model.DMBPost;
import iu.e510.message.board.tom.MessageService;
import iu.e510.message.board.tom.common.LamportClock;
import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.tom.common.payloads.BlockingPayload;
import iu.e510.message.board.tom.common.payloads.NonBlockingPayload;
import iu.e510.message.board.tom.common.payloads.Payload;
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
    private BlockingQueue<NonBlockingPayload> superNodeMsgQueue;
    private MesssageExecutor messsageExecutor;

    private AtomicBoolean consistent;

    private ClusterManager clusterManager;

    private DMBDatabase database;

    private LamportClock clock;


    public DataManagerImpl(String nodeID, MessageService messageService,
                           BlockingQueue<NonBlockingPayload> superNodeMsgQueue,
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

        this.messsageExecutor = new MesssageExecutor(this);

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

        this.clock = LamportClock.getClock();

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
    public byte[] requestData(String topic) {
        Set<String> nodes = clusterManager.getHashingNodes(topic);

        logger.debug("Requesting data for: " + topic + " from: " + nodes);
        for (String node : nodes) {
            if (node.equals(myNodeID)) {
                continue;
            }
            logger.debug("Talking to " + node);

            Message response = messageService.send_unordered(new DataRequestPayload(topic),
                    messageService.getUrl(node));

            logger.debug("Received data: " + new String(((DataResponsePayload)
                    response.getPayload()).getContent()));

            return (byte[]) response.getPayload().getContent();
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
    public void setConsistency(boolean consistency) {
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
            messageService.send_ordered(new ClientDataPayload(dataBean), nodes);

            return Collections.emptySet();
        } else {
            logger.info("Received unrelated topic: " + dataBean.getTopic());
            return nodes;
        }
    }

    @Override
    public DMBPost getPost(String clientID, String topic, int postID) throws Exception {
        if (isConsistent()) {
            return database.getPostDataByPostId(postID);
        } else {
            throw new Exception("Server is inconsistent. Unable to serve getPost request!");
        }
    }

    @Override
    public List<DMBPost> getPosts(String clientID, String topic) throws Exception {
        if (isConsistent()) {
            return database.getPostsDataByTopicArrayList(topic);
        } else {
            throw new Exception("Server is inconsistent. Unable to serve getPosts request!");
        }
    }

    @Override
    public List<DMBPost> searchPosts(String str) throws Exception {
        if (isConsistent()) {
            return database.getPostsDataByTitleDescriptionArrayList(str);
        } else {
            throw new Exception("Server is inconsistent. Unable to serve searchPosts request!");
        }
    }

    @Override
    public List<DMBPost> getTopPosts() throws Exception {
        if (isConsistent()) {
            return database.getTopPostsDataByPopularityArrayList();
        } else {
            throw new Exception("Server is inconsistent. Unable to serve getTopPosts request!");
        }
    }

    /**
     * Synchronous/ blocking processing of a payload
     */
    @Override
    public Message processPayload(BlockingPayload payload) {
        logger.debug("Processing message: " + payload);

        return payload.process(this);
    }

    /**
     * Asynchronous/ non-blocking processing of a payload
     */
    @Override
    public void queuePayload(NonBlockingPayload payload) {
        logger.debug("Saving message for async process: " + payload);
        try {
            superNodeMsgQueue.put(payload);
        } catch (InterruptedException e) {
            logger.error("Unable to access queue ", e);
            throw new RuntimeException("Unable to access queue ", e);
        }
    }


    @Override
    public int getLamportTimestamp() {
        return clock.get();
    }

    @Override
    public MessageService getMessageService() {
        return messageService;
    }


    @Override
    public DMBDatabase getDatabase() {
        return database;
    }


    class MesssageExecutor extends Thread {
        private boolean running = true;

        private DataManager dataManager;

        public MesssageExecutor(DataManager dataManager) {
            super();
            this.dataManager = dataManager;
        }

        void stopExecutor() {
            this.running = false;
        }

        @Override
        public void run() {
            while (running) {
                try {
                    NonBlockingPayload payload = superNodeMsgQueue.take();
                    setConsistency(false);

                    payload.process(dataManager);

                    setConsistency(true);

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
        private String nodeID;
        public ConcurrentSkipListSet<Message> messageQueue;
        public MessageHandlerImpl(String nodeID, ConcurrentSkipListSet<Message> messageQueue) {
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
            Payload payload = message.getPayload();

            logger.info("Received message: " + payload + " from: " + message.getNodeID());

            if (payload instanceof NonBlockingPayload) {
                queuePayload((NonBlockingPayload) payload);
                return null;
            } else {
                return processPayload((BlockingPayload) payload);
            }
        }

        private Message processMulticastMessage(Message message) {
            if (message.isRelease()) {
                // if message is a release request, deliver it, don't have to reply
                logger.debug("[nodeID:" + nodeID + "][clock:" + clock.get() + "] Received release request. " +
                        "Delivering multicast message: " + message.getRelease() + " with clock: " + message.getClock()
                        + " from: " + message.getNodeID());
                message.setId(message.getRelease());

                queuePayload((NonBlockingPayload) message.getPayload());

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
            Message ack = new Message(new Payload<>("Ack"), nodeID, sendClock, true);
            ack.setAck(message.getId());
            return ack;
        }


    }
}
