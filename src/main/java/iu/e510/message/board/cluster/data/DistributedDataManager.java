package iu.e510.message.board.cluster.data;

import iu.e510.message.board.cluster.ClusterManager;
import iu.e510.message.board.cluster.zk.ZKManager;
import iu.e510.message.board.cluster.zk.ZKManagerImpl;
import iu.e510.message.board.tom.MessageService;
import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.tom.common.MessageType;
import iu.e510.message.board.tom.common.Payload;
import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DistributedDataManager implements DataManager {
    private static Logger logger = LoggerFactory.getLogger(DistributedDataManager.class);
    private ReadWriteLock lock;
    private Lock readLock;
    private Lock writeLock;
    private HashSet<String> myTopics;
    private ZKManager zkManager;
    private Config config;
    private String myNodeID;
    private String zkMyTopicStore;

    private MessageService messageService;
    private BlockingQueue<Message> internalMessageQueue;
    private MesssageExecutor messsageExecutor;

    private DataAdapter dataAdapter;

    private AtomicBoolean consistency;

    private ClusterManager clusterManager;

    public DistributedDataManager(String nodeID, MessageService messageService,
                                  DataAdapter dataAdapter,
                                  BlockingQueue<Message> internalMessageQueue,
                                  ClusterManager clusterManager) throws Exception {
        logger.info("Initializing the Data Manager!");
        this.config = new Config();
        this.myNodeID = nodeID;
        this.zkMyTopicStore = config.getConfig(Constants.DATA_LOCATION) + "/" + myNodeID;

        this.consistency = new AtomicBoolean(true);
        this.messageService = messageService;
        this.internalMessageQueue = internalMessageQueue;
        this.dataAdapter = dataAdapter;
        this.clusterManager = clusterManager;

        this.messsageExecutor = new MesssageExecutor();

        initialize();

        logger.info("Data Manager init done. My topics: " + myTopics.toString());
    }

    private void initialize() throws Exception {
        this.lock = new ReentrantReadWriteLock();
        this.readLock = this.lock.readLock();
        this.writeLock = this.lock.writeLock();
        this.myTopics = new HashSet<>();
        this.zkManager = new ZKManagerImpl();
        // Create my data node
        if (zkManager.exists(zkMyTopicStore) == null) {
            logger.info("No data store with my node ID found. Hence creating the new data store in ZK");
            zkManager.create(zkMyTopicStore, SerializationUtils.serialize(""), CreateMode.PERSISTENT);
        } else {
            logger.info("Existing topics found for my node ID. Hence restoring the configurations!");
            this.myTopics = SerializationUtils.deserialize(zkManager.getData(zkMyTopicStore));
        }

        this.messsageExecutor.start();
    }

    @Override
    public void addData(String path, byte[] data) throws Exception {
        try {
            writeLock.lock();
            // write to ZK only if the topic is not already recorded
            if (!myTopics.contains(path)) {
                myTopics.add(path);
                zkManager.set(zkMyTopicStore, SerializationUtils.serialize(myTopics));
            }

            // store data here
            dataAdapter.putDataDump(path, data);

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

            Message response = messageService.send_unordered(new Payload<>(topic), node,
                    MessageType.DATA_REQUEST);

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
    public void setConsistency(boolean consistency) {
        this.consistency.set(consistency);
    }

    @Override
    public boolean getConsistency() {
        return this.consistency.get();
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


    class MesssageExecutor extends Thread {
        @Override
        public void run() {
            try {
                Message message = internalMessageQueue.take();
                logger.info("Server is inconsistent");
                setConsistency(false);

                if (message.getMessageType() == MessageType.SYNC) {
                    logger.info("Processing SYNC msg: " + message);

                    String topic = (String) message.getPayload().getContent();

                    if (hasData(topic)) {
                        logger.info("Nothing to do! Data available locally for: " + topic);
                    } else {
                        logger.info("Requesting data from the cluster for: " + topic);
                        byte[] data = getData(topic);
                        dataAdapter.putDataDump(topic, data);
                    }


                } else if (message.getMessageType() == MessageType.TRANSFER) {
                    logger.info("Processing TRANSFER msg: " + message);
                    // todo: implement this --> take the topics from the message and send the
                    //  relevant data to the destination node


                } else {
                    throw new RuntimeException("Unknown message type: " + message);
                }

                setConsistency(true);
                logger.info("Server consistent again!");

            } catch (InterruptedException e) {
                logger.error("Unable to access the queue: ", e);
            }
        }
    }


}
