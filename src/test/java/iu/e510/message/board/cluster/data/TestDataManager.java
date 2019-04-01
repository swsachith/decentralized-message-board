package iu.e510.message.board.cluster.data;

import iu.e510.message.board.cluster.BaseZKTest;
import iu.e510.message.board.util.Constants;
import org.apache.commons.lang3.SerializationUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;

public class TestDataManager extends BaseZKTest {
    @Test
    public void setData() throws Exception {
        String ip = "192.168.1.2";
        DataManager dataManager = new DataManagerImpl(ip);
        dataManager.addData("bloomington", "hi");
        dataManager.addData("iu", "hi");
        HashSet<String> dataManagerAllTopics = dataManager.getAllTopics();

        String dataStore = config.getConfig(Constants.DATA_LOCATION) + "/" + ip;
        HashSet<String> resultSet = SerializationUtils.deserialize(zooKeeper.getData(dataStore));

        Assert.assertEquals(resultSet, dataManagerAllTopics);
    }

    @Test (dependsOnMethods = { "setData" })
    public void testInitExisting() throws Exception {
        String ip = "192.168.1.2";
        DataManager diffDataManager = new DataManagerImpl(ip);
        HashSet<String> dataManagerAllTopics = diffDataManager.getAllTopics();
        String dataStore = config.getConfig(Constants.DATA_LOCATION) + "/" + ip;
        HashSet<String> resultSet = SerializationUtils.deserialize(zooKeeper.getData(dataStore));
        Assert.assertEquals(resultSet, dataManagerAllTopics);
    }
}
