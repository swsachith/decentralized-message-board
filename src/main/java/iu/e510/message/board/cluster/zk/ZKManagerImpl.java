package iu.e510.message.board.cluster.zk;

import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.Stat;

import java.util.List;

public class ZKManagerImpl implements ZKManager {
    private CuratorFramework zkClient;
    private Config config;

    public ZKManagerImpl(){
        config = new Config();
        zkClient = ZKConnection.getConnection(config.getConfig(Constants.ZK_CONN_STRING),
                Integer.parseInt(config.getConfig(Constants.ZK_CONN_MAXRETRY_COUNT)),
                Integer.parseInt(config.getConfig(Constants.ZK_CONN_RETRY_INIT_WAIT)),
                Integer.parseInt(config.getConfig(Constants.ZK_CONN_TIMEOUT)),
                Integer.parseInt(config.getConfig(Constants.ZK_SESSION_TIMEOUT)));
    }

    @Override
    public void create(String path, byte[] data, CreateMode mode) throws Exception {
        if (mode == null) {
            mode = CreateMode.PERSISTENT;
        }
        zkClient.create().creatingParentContainersIfNeeded().withMode(mode).withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                .forPath(path, data);
    }

    @Override
    public byte[] getData(String path) throws Exception {
        return zkClient.getData().forPath(path);
    }

    @Override
    public void set(String path, byte[] data) throws Exception {
        zkClient.setData().forPath(path, data);
    }

    @Override
    public void closeManager() throws InterruptedException {
        ZKConnection.closeConnection();
    }

    @Override
    public Stat exists(String path) throws Exception {
        return zkClient.checkExists().forPath(path);
    }

    @Override
    public List<String> getAllChildren(String path) throws Exception{
        return zkClient.getChildren().forPath(path);
    }

    @Override
    public PathChildrenCache getPathChildrenCache(String path) {
        return new PathChildrenCache(zkClient, path, true);
    }
}
