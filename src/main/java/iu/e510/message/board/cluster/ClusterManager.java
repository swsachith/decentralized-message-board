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

public class ClusterManager extends Thread {
    private static Logger logger = LoggerFactory.getLogger(ClusterManager.class);

    private HashRing hashRing;
    private ZKManager zkManager;
    private Config config;
    private String nodeID;
    private String clusterParentZK;

    public ClusterManager(String nodeID) throws Exception {
        this.config = new Config();
        this.nodeID = nodeID;
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
        // todo: add a lock here
        this.hashRing = new HashRing();
        List<String> allNodes = zkManager.getAllChildren(clusterParentZK);
        hashRing.addAll(allNodes);
    }

    private void addClusterChangeListner(PathChildrenCache cache) {
        logger.info("Cluster change detected");
        PathChildrenCacheListener listener = (curatorFramework, event) -> {
            switch (event.getType()) {
                case CHILD_ADDED:
                    updateRing();
                case CHILD_REMOVED:
                    updateRing();
                default:
                    break;
            }
        };
        cache.getListenable().addListener(listener);
    }

    public HashRing getRing() {
        return hashRing;
    }
}
