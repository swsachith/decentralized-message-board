package iu.e510.message.board.cluster.zk;

import iu.e510.message.board.cluster.BaseZKTest;
import org.apache.commons.lang3.SerializationUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ZKTest extends BaseZKTest {
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
}
