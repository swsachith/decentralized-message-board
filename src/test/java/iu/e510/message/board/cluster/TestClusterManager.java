package iu.e510.message.board.cluster;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestClusterManager extends BaseZKTest {
    @Test
    public void testInit() throws Exception {
        String id = "192.168.1.2:8081";
        ClusterManager clusterManager = new ClusterManager(id, null);
        Assert.assertTrue(clusterManager.exists(id));
    }
}
