package iu.e510.message.board.cluster;

import iu.e510.message.board.cluster.zk.ZKManager;
import iu.e510.message.board.cluster.zk.ZKManagerImpl;
import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClusterManager {
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

    public ClusterManager(String nodeID) throws Exception {
        this.config = new Config();
        this.nodeID = nodeID;
        lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        hash = new Hash();
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
        zkManager = new ZKManagerImpl();
        // add myself to the cluster
        clusterParentZK = this.config.getConfig(Constants.CLUSTER_RING_LOCATION);
        zkManager.create(clusterParentZK + "/" + nodeID, SerializationUtils.serialize(nodeID), CreateMode.EPHEMERAL);
        // update the hash ring
        updateRing();
        childrenCache = zkManager.getPathChildrenCache(clusterParentZK);
        addClusterChangeListner(childrenCache);
        childrenCache.start();
        logger.info("Cluster initialization done!");
    }

    /**
     * Updates the Hash Ring with the latest changes.
     * @throws Exception
     */
    private void updateRing() throws Exception {
        logger.info("Cluster change detected! Updating my hash ring");
        try {
            writeLock.lock();
            this.hashRing = new HashRing(Integer.parseInt(config.getConfig(Constants.NUM_REPLICAS)));
            List<String> allNodes = zkManager.getAllChildren(clusterParentZK);
            hashRing.addAll(allNodes);
        } finally {
            writeLock.unlock();
        }
    }

    //todo: do not create a new ring. Update the current ring (adding and removing)
    private void addClusterChangeListner(PathChildrenCache cache) {
        PathChildrenCacheListener listener = (curatorFramework, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    updateRing();
                    break;
                case CHILD_REMOVED:
                    updateRing();
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
     * Get the closest node ip for a given key.
     * @param key
     * @return
     */
    //todo: hash values into replicas as well
    public String getClosestNode(String key) {
        try {
            readLock.lock();
            return hashRing.getValue(hashRing.getHashingNode(key));
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
}
