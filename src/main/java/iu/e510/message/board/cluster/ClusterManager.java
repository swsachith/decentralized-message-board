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

import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ClusterManager extends Thread {
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

    public ClusterManager(String nodeID) throws Exception {
        this.config = new Config();
        this.nodeID = nodeID;
        lock = new ReentrantReadWriteLock();
        readLock = lock.readLock();
        writeLock = lock.writeLock();
        hash = new Hash();
        initialize();
    }

    public void initialize() throws Exception {
        logger.info("Initializing the cluster");
        zkManager = new ZKManagerImpl();
        // add myself to the cluster
        clusterParentZK = this.config.getConfig(Constants.CLUSTER_RING_LOCATION);
        zkManager.create(clusterParentZK + "/" + nodeID, SerializationUtils.serialize(nodeID), CreateMode.EPHEMERAL);
        // update the hash ring
        updateRing();
        PathChildrenCache childrenCache = zkManager.getPathChildrenCache(clusterParentZK);
        addClusterChangeListner(childrenCache);
        childrenCache.start();
        logger.info("Cluster initialization done!");
    }

    private void updateRing() throws Exception {
        logger.info("Updating my hash ring");
        try {
            writeLock.lock();
            this.hashRing = new HashRing();
            List<String> allNodes = zkManager.getAllChildren(clusterParentZK);
            hashRing.addAll(allNodes);
        } finally {
            writeLock.unlock();
        }
    }

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

    public String getNode(String key) {
        try {
            readLock.lock();
            return hashRing.getValue(hash.getHash(key));
        } finally {
            readLock.unlock();
        }
    }

    public int getClosestNode(String key) {
        try {
            readLock.lock();
            return hashRing.getHashingNode(key);
        } finally {
            readLock.unlock();
        }
    }
}
