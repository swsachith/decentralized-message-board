package iu.e510.message.board.cluster.data;

import iu.e510.message.board.cluster.zk.ZKManager;
import iu.e510.message.board.cluster.zk.ZKManagerImpl;
import iu.e510.message.board.tom.MessageService;
import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.tom.common.MessageType;
import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SuperNodeDataManagerImpl implements SuperNodeDataManager {
    private static Logger logger = LoggerFactory.getLogger(SuperNodeDataManagerImpl.class);
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
    private MsgExecutor superNodeMsgExecutor;

    private LocalDataManager localDataManager;

    private AtomicBoolean consistency;

    public SuperNodeDataManagerImpl(String nodeID, MessageService messageService,
                                    LocalDataManager localDataManager, BlockingQueue<Message> superNodeMsgQueue) throws Exception {
        logger.info("Initializing the Data Manager!");
        this.config = new Config();
        this.myNodeID = nodeID;
        this.zkMyTopicStore = config.getConfig(Constants.DATA_LOCATION) + "/" + myNodeID;

        this.consistency = new AtomicBoolean(true);
        this.messageService = messageService;
        this.superNodeMsgQueue = superNodeMsgQueue;
        this.localDataManager = localDataManager;

        this.superNodeMsgExecutor = new MsgExecutor(this,
                localDataManager, superNodeMsgQueue);

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

        this.superNodeMsgExecutor.start();
    }

    @Override
    public void addData(String path, String data) throws Exception {
        try {
            writeLock.lock();
            // write to ZK only if the topic is not already recorded
            if (!myTopics.contains(path)) {
                myTopics.add(path);
                zkManager.set(zkMyTopicStore, SerializationUtils.serialize(myTopics));
            }
            //todo: store data here
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean getData(String path) {
        // todo: send data from the DB. Remove the following stub
        return myTopics.contains(path);
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


    class MsgExecutor extends Thread {

        private SuperNodeDataManager superNodeDataManager;
        private BlockingQueue<Message> superNodeMsgQueue;
        private LocalDataManager localDataManager;

        MsgExecutor(SuperNodeDataManager superNodeDataManager,
                    LocalDataManager localDataManager,
                    BlockingQueue<Message> superNodeMsgQueue) {
            this.superNodeDataManager = superNodeDataManager;
            this.superNodeMsgQueue = superNodeMsgQueue;
            this.localDataManager = localDataManager;
        }

        @Override
        public void run() {
            try {
                Message message = this.superNodeMsgQueue.take();
                logger.info("Server is inconsistent");
                this.superNodeDataManager.setConsistency(false);

                if (message.getMessageType() == MessageType.SYNC) {
                    logger.info("Processing SYNC msg: " + message);
                    // todo: implement this --> take the topics from the message and get the
                    //  data using the datamanager to talk to relevant nodes

                    String topic = (String) message.getPayload().getContent();


                } else if (message.getMessageType() == MessageType.TRANSFER) {
                    logger.info("Processing TRANSFER msg: " + message);
                    // todo: implement this --> take the topics from the message and send the
                    //  relevant data to the destination node
                }

                this.superNodeDataManager.setConsistency(true);
                logger.info("Server consistent again!");

            } catch (InterruptedException e) {
                logger.error("Unable to access the queue: ", e);
            }
        }
    }


}
