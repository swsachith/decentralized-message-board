package iu.e510.message.board.util;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class TestConfigReading {
    @Test
    public void testGetConfig() throws IOException {
        Config config = new Config();
        String hostIp = config.getConfig("HOST_IP");
        Assert.assertEquals(hostIp, "127.0.0.1");
    }
}
