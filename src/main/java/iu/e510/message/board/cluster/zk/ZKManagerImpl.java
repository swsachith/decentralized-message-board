package iu.e510.message.board.cluster.zk;

import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;

public class ZKManagerImpl implements ZKManager {
    private CuratorFramework zkClient;
    private Config config;

    public ZKManagerImpl() throws IOException, InterruptedException {
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
        zkClient.create().withMode(mode).forPath(path, data);
    }

    @Override
    public byte[] getData(String path) throws Exception {
        return zkClient.getData().forPath(path);
    }

    @Override
    public Stat exists(String path) throws KeeperException, InterruptedException {
        return null;
    }

    @Override
    public void exists(String path, Watcher watcher, AsyncCallback.StatCallback callback) {

    }

    @Override
    public void set(String path, byte[] data) throws Exception {
        zkClient.setData().forPath(path, data);
    }

    @Override
    public void closeManager() throws InterruptedException {
        ZKConnection.closeConnection();
    }
}
