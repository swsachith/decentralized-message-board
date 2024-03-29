package iu.e510.message.board.cluster;

import iu.e510.message.board.cluster.data.payloads.SyncPayload;
import iu.e510.message.board.cluster.data.payloads.TransferPayload;
import iu.e510.message.board.cluster.zk.ZKManager;
import iu.e510.message.board.cluster.zk.ZKManagerImpl;
import iu.e510.message.board.tom.MessageService;
import iu.e510.message.board.tom.common.payloads.ConnectionFailure;
import iu.e510.message.board.tom.common.payloads.Payload;
import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClusterManager implements LeaderLatchListener {
    private static Logger logger = LoggerFactory.getLogger(ClusterManager.class);

    private HashRing hashRing;
    private ZKManager zkManager;
    private Config config;
    private String nodeID;
    private String clusterParentZK;
    private ReadWriteLock lock;
    private Lock readLock;
    private Lock writeLock;
    private Hash hash;
    private PathChildrenCache childrenCache;
    private LeaderLatch leaderLatch;
    private AtomicBoolean leader;
    private int dataReplicas;
    private MessageService messageService;

    public ClusterManager(String nodeID, MessageService messageService) throws Exception {
        this.config = new Config();
        this.nodeID = nodeID;
        this.lock = new ReentrantReadWriteLock();
        this.readLock = this.lock.readLock();
        this.writeLock = this.lock.writeLock();
        this.hash = new Hash();
        this.zkManager = new ZKManagerImpl();
        this.leader = new AtomicBoolean();
        this.dataReplicas = Integer.parseInt(config.getConfig(Constants.DATA_REPLICAS));
        this.messageService = messageService;
        initialize();
    }

    /**
     * Initializes the cluster with the following steps.
     * Init the zookeeper client
     * Create your ephemeral file at the /cluster location
     * Update your ring
     * Add a watch for the /cluster to get notified of node changes
     *
     * @throws Exception
     */
    private void initialize() throws Exception {
        logger.info("Initializing the cluster. NodeID: " + nodeID);

        // add myself to the cluster
        clusterParentZK = this.config.getConfig(Constants.CLUSTER_RING_LOCATION);
        zkManager.create(clusterParentZK + "/" + nodeID, SerializationUtils.serialize(nodeID), CreateMode.EPHEMERAL);

        // update the hash ring
        initRing();

        // monitoring the node joining and removal
        childrenCache = zkManager.getPathChildrenCache(clusterParentZK);
        addClusterChangeListner(childrenCache);
        childrenCache.start();

        // adding the leader election latch
        leaderLatch = new LeaderLatch(zkManager.getZKClient(), config.getConfig(Constants.LEADER_PATH), nodeID);
        leaderLatch.addListener(this);
        leaderLatch.start();
        logger.info("Cluster initialization done!");
    }

    /**
     * Creates the Hash Ring with the current cluster configs.
     *
     * @throws Exception
     */
    private void initRing() throws Exception {
        logger.info("Cluster change detected! Updating my hash ring");
        try {
            writeLock.lock();
            this.hashRing = new HashRing(Integer.parseInt(config.getConfig(Constants.NODE_REPLICAS)));
            List<String> allNodes = zkManager.getAllChildren(clusterParentZK);
            hashRing.addAll(allNodes);
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * This is a watcher added to detect changes in the cluster. To capture nodes leaving and joining.
     */
    private void addClusterChangeListner(PathChildrenCache cache) {
        PathChildrenCacheListener listener = (curatorFramework, event) -> {
            if (event.getType() == PathChildrenCacheEvent.Type.CONNECTION_LOST ||
                    event.getType() == PathChildrenCacheEvent.Type.CONNECTION_SUSPENDED) {
                messageService.send_unordered(new ConnectionFailure("Connection lost or " +
                        "suspended"), messageService.getUrl(nodeID));
                return;
            }
            String eventNodeID = event.getData().getPath().substring(clusterParentZK.length() + 1);
            if (eventNodeID.equals(nodeID)) {
                return;
            }
            switch (event.getType()) {
                case CHILD_ADDED:
                    addToRing(eventNodeID);
                    if (leader.get()) {
                        redistributeDataNodeAdded(eventNodeID);
                    }
                    break;
                case CHILD_REMOVED:
                    removeFromRing(eventNodeID);
                    // if the leader went down, and cluster does not still have a new leader,
                    // wait till it elects a new leader
                    // this is to fix the race condition where the this listener is hit before the leader listener
                    if (leaderLatch.getLeader().getId().equals(eventNodeID)) {
                        Thread.sleep(Integer.parseInt(config.getConfig(Constants.LEADER_ELECTION_DELAY)));
                        logger.info("Master died. Waiting for new master to be elected.");
                    }
                    if (leader.get()) {
                        redistributeDataNodeLeft(eventNodeID);
                    }
                    break;
                default:
                    break;
            }
        };
        cache.getListenable().addListener(listener);
    }

    /**
     * Returns the ip of the node in the ring for the given ip
     *
     * @param ip
     * @return
     */
    public boolean exists(String ip) {
        try {
            readLock.lock();
            return hashRing.exists(ip);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Get bucket nodes list for a given topic. Returns all the dataReplicas.
     *
     * @param topic
     * @return
     */
    public Set<String> getHashingNodes(String topic) {
        try {
            readLock.lock();
            Set<String> replicaSet = new HashSet<>();
            for (int i = 0; i < dataReplicas; i++) {
                replicaSet.add(hashRing.getValue(hashRing.getHashingNode(topic + "_" + i)));
            }
            return replicaSet;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Returns a single bucket to which the value belongs to.
     *
     * @param value
     * @return
     */
    public String getPreviousNode(String value) {
        try {
            readLock.lock();
            return hashRing.getPreviousNode(value);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Stops the cluster manager
     *
     * @throws InterruptedException
     * @throws IOException
     */
    public void stop() throws InterruptedException, IOException {
        childrenCache.close();
        zkManager.closeManager();
    }


    private void addToRing(String nodeID) {
        try {
            writeLock.lock();
            logger.info("Adding Node: " + nodeID + " to the ring");
            this.hashRing.add(nodeID);
        } finally {
            writeLock.unlock();
        }
    }

    private void removeFromRing(String nodeID) {
        try {
            writeLock.lock();
            logger.info("Removing Node: " + nodeID + " from the ring");
            this.hashRing.remove(nodeID);
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void isLeader() {
        logger.info("I'm the leader");
        leader.compareAndSet(false, true);
    }

    @Override
    public void notLeader() {
        logger.info("I'm not the leader");
        leader.compareAndSet(true, false);
    }

    /**
     * When a node goes down, reads the topics of that node, and create a inverted index of
     * new ip -> topic combination.
     * Then send out the message to all the ips with the list of topics.
     *
     * @param nodeID
     * @throws Exception
     */
    private void redistributeDataNodeLeft(String nodeID) throws Exception {
        String dataPath = config.getConfig(Constants.DATA_LOCATION) + "/" + nodeID;
        HashSet<String> lostNodeTopics = getLostNodeTopics(dataPath);
        Map<String, Set<String>> invertedIndex = new HashMap<>();
        // For each topic, create an inverted index from ip -> topics
        for (String topic : lostNodeTopics) {
            Set<String> ips = getHashingNodes(topic);
            for (String ip : ips) {
                Set<String> topicsOfIP = invertedIndex.get(ip);
                if (topicsOfIP == null) {
                    topicsOfIP = new HashSet<>();
                }
                topicsOfIP.add(topic);
                invertedIndex.put(ip, topicsOfIP);
            }
        }
        for (String topicNodeID : invertedIndex.keySet()) {
            Payload syncReq = new SyncPayload(invertedIndex.get(topicNodeID));
            messageService.send_unordered(syncReq, messageService.getUrl(topicNodeID));
        }
        logger.info("New redistribution topic map: " + invertedIndex.toString());
    }

    private void redistributeDataNodeAdded(String eventNodeID) {
        logger.info("Node added. Hence redistributing data");
        String hashingNode = getPreviousNode(eventNodeID);
        logger.info(eventNodeID + " Would talk to " + hashingNode + " to get the required topics");
        messageService.send_unordered(new TransferPayload(hashingNode),
                messageService.getUrl(eventNodeID));
        logger.info("Ring: " + hashRing.getRing().toString());
    }

    /**
     * Return lost node topics and delete that data node.
     *
     * @param nodePath
     * @return
     * @throws Exception
     */
    private HashSet<String> getLostNodeTopics(String nodePath) throws Exception { // todo: handle empty
        HashSet<String> topics = SerializationUtils.deserialize(zkManager.getData(nodePath));
        zkManager.delete(nodePath);
        logger.info("Deleted data node for the lost node: " +
                nodePath.substring(config.getConfig(Constants.DATA_LOCATION).length() + 1));
        return topics;
    }

    /**
     * Returns the topics the old node and the new node should have.
     * This is used when transferring the data from one node to the other.
     *
     * @param oldNode
     * @param newNode
     * @param topicList
     * @return
     */
    public Map<String, Set<String>> getTransferTopics(String oldNode, String newNode, Set<String> topicList) {
        Set<String> oldNodeTopics = new HashSet<>();
        Set<String> newNodeTopics = new HashSet<>();
        for (String topic : topicList) {
            Set<String> hashingNodes = getHashingNodes(topic);
            for (String node : hashingNodes) {
                if (node.equals(oldNode)) {
                    oldNodeTopics.add(topic);
                }
                if (node.equals(newNode)) {
                    newNodeTopics.add(topic);
                }
            }
        }
        Map<String, Set<String>> resultMap = new HashMap<>();
        resultMap.put("newNode", newNodeTopics);
        resultMap.put("oldNode", oldNodeTopics);
        return resultMap;
    }
}
