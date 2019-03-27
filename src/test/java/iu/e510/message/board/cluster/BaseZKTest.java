package iu.e510.message.board.cluster;

import iu.e510.message.board.cluster.zk.ZKManager;
import iu.e510.message.board.cluster.zk.ZKManagerImpl;
import iu.e510.message.board.util.Config;
import org.apache.curator.test.TestingServer;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import java.io.IOException;

public class BaseZKTest {
    protected static ZKManager zooKeeper;
    protected static TestingServer zkServer;
    @BeforeSuite
    public static void setup() throws Exception {
        Config config = new Config();
        int zkHostPort = Integer.parseInt(config.getConfig("ZK_PORT_TEST"));
        zkServer = new TestingServer(zkHostPort, true);
        zooKeeper = new ZKManagerImpl();
    }
    @AfterSuite
    public static void cleanup() throws InterruptedException, IOException {
        zooKeeper.closeManager();
        zkServer.stop();
    }
}
