package iu.e510.message.board.cluster.zk;

import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;

public class ZKManagerImpl implements ZKManager {
    private ZooKeeper zooKeeper;
    private Config config;

    public ZKManagerImpl() throws IOException, InterruptedException {
        config = new Config();
        zooKeeper = ZKConnection.getConnection(config.getConfig(Constants.ZK_HOST),
                Integer.parseInt(config.getConfig(Constants.ZK_CONN_TIMEOUT)));
    }

    @Override
    public void create(String path, byte[] data, CreateMode mode) throws KeeperException, InterruptedException {
        if (mode == null) {
            mode = CreateMode.PERSISTENT;
        }
        zooKeeper.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
    }

    @Override
    public byte[] getData(String path, boolean watchFlag) throws KeeperException, InterruptedException {
        return zooKeeper.getData(path, watchFlag, null);
    }

    @Override
    public Stat exists(String path, boolean watchFlag) throws KeeperException, InterruptedException {
        return zooKeeper.exists(path, watchFlag);
    }

    @Override
    public void set(String path, byte[] data) throws KeeperException, InterruptedException {
        zooKeeper.setData(path, data, exists(path, true).getVersion());
    }

    @Override
    public void closeManager() throws InterruptedException {
        ZKConnection.closeConnection();
    }
}
