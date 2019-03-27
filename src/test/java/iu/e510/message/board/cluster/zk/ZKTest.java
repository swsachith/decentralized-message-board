package iu.e510.message.board.cluster.zk;

import iu.e510.message.board.util.Config;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.KeeperException;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import java.io.IOException;

public class ZKTest {
    private static ZKManager zooKeeper;
    private static TestingServer zkServer;

    @BeforeSuite
    public static void setup() throws Exception {
        Config config = new Config();
        int zkHostPort = Integer.parseInt(config.getConfig("ZK_PORT_TEST"));
        zkServer = new TestingServer(zkHostPort, true);
        zooKeeper = new ZKManagerImpl();
    }

    @Test
    public void testCreate() throws Exception {
        String value = "Hello World";
        String testPath = "/test";
        zooKeeper.create(testPath, SerializationUtils.serialize(value), null);
        String result = SerializationUtils.deserialize(zooKeeper.getData(testPath));
        Assert.assertEquals(result, value);
    }

    @Test
    public void testSet() throws Exception {
        String newValue = "Hello Distributed Systems!";
        String testPath = "/test";
        zooKeeper.set(testPath, SerializationUtils.serialize(newValue));
        String result = SerializationUtils.deserialize(zooKeeper.getData(testPath));
        Assert.assertEquals(result, newValue);
    }

    @AfterSuite
    public static void cleanup() throws InterruptedException, IOException {
        zooKeeper.closeManager();
        zkServer.stop();
    }
}
