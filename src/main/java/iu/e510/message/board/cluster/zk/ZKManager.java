package iu.e510.message.board.cluster.zk;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.data.Stat;

public interface ZKManager {
    void create(String path, byte[] data, CreateMode mode) throws KeeperException, InterruptedException;

    byte[] getData(String path, boolean watchFlag) throws KeeperException, InterruptedException;

    Stat exists(String path, boolean watchFlag) throws KeeperException, InterruptedException;

    void set(String path, byte[] data) throws KeeperException, InterruptedException;

    void closeManager() throws InterruptedException;
}
