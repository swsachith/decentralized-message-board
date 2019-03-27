package iu.e510.message.board.cluster;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestClusterManager extends BaseZKTest {
    @Test
    public void testInit() throws Exception {
        String ip = "192.168.1.2";
        ClusterManager clusterManager = new ClusterManager(ip);
        Assert.assertEquals(clusterManager.getNode(ip), ip);
    }
}
