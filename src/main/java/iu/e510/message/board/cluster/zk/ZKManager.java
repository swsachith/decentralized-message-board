package iu.e510.message.board.cluster.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;

public interface ZKManager {
    void create(String path, byte[] data, CreateMode mode) throws Exception;

    byte[] getData(String path) throws Exception;

    void set(String path, byte[] data) throws Exception;

    void closeManager() throws InterruptedException, IOException;

    Stat exists(String path) throws Exception;

    List<String> getAllChildren(String path) throws Exception;

    void delete(String path) throws Exception;

    /**
     * Used to monitor the children of a given path.
     * Ex: monitor changes in the /cluster
     * @param path
     * @return
     */
    PathChildrenCache getPathChildrenCache(String path);

    /**
     * Returns ZK Client
     */
    CuratorFramework getZKClient();
}
