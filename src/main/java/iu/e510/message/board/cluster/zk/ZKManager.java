package iu.e510.message.board.cluster.zk;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.data.Stat;

public interface ZKManager {
    void create(String path, byte[] data, CreateMode mode) throws Exception;

    byte[] getData(String path) throws Exception;

    Stat exists(String path) throws KeeperException, InterruptedException;

    void exists(String path, Watcher watcher, AsyncCallback.StatCallback callback);

    void set(String path, byte[] data) throws Exception;

    void closeManager() throws InterruptedException;
}
