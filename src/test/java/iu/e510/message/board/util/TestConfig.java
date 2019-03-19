package iu.e510.message.board.util;

import org.testng.Assert;
import org.testng.annotations.Test;

public class TestConfig {
    @Test
    public void testGetConfig() {
        Config config = new Config();
        String hostIp = config.getConfig("HOST_IP");
        Assert.assertEquals(hostIp, "127.0.0.1");
    }

    @Test
    public void testSetConfig() {
        Config config = new Config();
        config.setConfig("HOST_IP", "localhost");
        String hostIp = config.getConfig("HOST_IP");
        Assert.assertEquals(hostIp, "localhost");
    }
}
