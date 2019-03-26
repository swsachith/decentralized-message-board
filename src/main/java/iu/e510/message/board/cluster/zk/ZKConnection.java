package iu.e510.message.board.cluster.zk;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class ZKConnection {
    private static ZooKeeper zoo;
    private static CountDownLatch connectionLatch = new CountDownLatch(1);

    public static ZooKeeper getConnection(String zooHost, int timeout) throws IOException, InterruptedException {
        if (zoo == null) {
            zoo = new ZooKeeper(zooHost, timeout, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    if (watchedEvent.getState() == Event.KeeperState.SyncConnected) {
                        connectionLatch.countDown();
                    }
                }
            });
            connectionLatch.await();
        }
        return zoo;
    }

    public static void closeConnection() throws InterruptedException {
        if (zoo != null) {
            zoo.close();
        }
    }
}
