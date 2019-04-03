package iu.e510.message.board.cluster;

import iu.e510.message.board.cluster.zk.ZKManager;
import iu.e510.message.board.cluster.zk.ZKManagerImpl;
import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    private boolean leader;
    private int replicas;

    public ClusterManager(String nodeID) throws Exception {
        this.config = new Config();
        this.nodeID = nodeID;
        this.lock = new ReentrantReadWriteLock();
        this.readLock = this.lock.readLock();
        this.writeLock = this.lock.writeLock();
        this.hash = new Hash();
        this.zkManager = new ZKManagerImpl();
        this.leader = false;
        this.replicas = Integer.parseInt(config.getConfig(Constants.NODE_REPLICAS));
        initialize();
    }

    /**
     * Initializes the cluster with the following steps.
     * Init the zookeeper client
     * Create your ephemeral file at the /cluster location
     * Update your ring
     * Add a watch for the /cluster to get notified of node changes
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
            String eventNodeID = event.getData().getPath().substring(clusterParentZK.length() + 1);
            switch (event.getType()) {
                case CHILD_ADDED:
                    addToRing(eventNodeID);
                    break;
                case CHILD_REMOVED:
                    // todo: handle race condition if the leader dies and this gets hit before the leader message
                    removeFromRing(eventNodeID);
                    if (leader) {
                        initiateDataRedistribution(eventNodeID);
                    }
                    break;
                default:
                    break;
            }
        };
        cache.getListenable().addListener(listener);
    }

    /**
     * Returns the ip of the node in the ring for the given key
     * @param key
     * @return
     */
    public String getNode(String key) {
        try {
            readLock.lock();
            return hashRing.getValue(hash.getHash(key + "_0"));
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Get bucket nodes list for a given key. Returns all the replicas.
     * @param key
     * @return
     */
    public Set<String> getHashingNodes(String key) {
        try {
            readLock.lock();
            Set<String> replicaSet = new HashSet<>();
            for (int i = 0; i < replicas; i++) {
                replicaSet.add(hashRing.getValue(hashRing.getHashingNode(key)));
            }
            return replicaSet;
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Stops the cluster manager
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
        leader = true;
    }

    @Override
    public void notLeader() {
        logger.info("I'm not the leader");
        leader = false;
    }

    private void initiateDataRedistribution(String nodeID) throws Exception {
        String dataPath = config.getConfig(Constants.DATA_LOCATION) + "/" + nodeID;
        HashSet<String> lostNodeTopics = getLostNodeTopics(dataPath);
        for (String topic : lostNodeTopics) {
            logger.info("Re-distributing topic: " + topic);
        }
    }

    /**
     * Return lost node topics and delete that data node.
     * @param nodePath
     * @return
     * @throws Exception
     */
    private HashSet<String> getLostNodeTopics(String nodePath) throws Exception {
        HashSet<String> topics = SerializationUtils.deserialize(zkManager.getData(nodePath));
        zkManager.delete(nodePath);
        logger.info("Deleted data node for the lost node: " +
                nodePath.substring(config.getConfig(Constants.DATA_LOCATION).length() + 1));
        return topics;
    }
}
