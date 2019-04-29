package iu.e510.message.board.cluster.data;

import iu.e510.message.board.cluster.data.beans.BaseBean;

import java.util.Map;
import java.util.Set;

public interface DataManager {
    void addTopicData(String path) throws Exception;

    byte[] getData(String path);

    boolean hasData(String path);

    void deleteData(String path) throws Exception;

    Set<String> getAllTopics();

    void setConsistent(boolean consistency);

    String getNodeId();

    Set<String> getNodeIdsForTopic(String topic);

    boolean isConsistent();

    /**
     * Returns the topics to transfer and keep.
     * The "transfer" key of the map is the transfer topics (topics to be sent)
     * The "delete" index set is the topics to be deleted.
     * @param newNodeID
     * @return
     */
    Map<String, Set<String>> getTransferTopics(String newNodeID);


    Set<String> addData(BaseBean dataBean) throws Exception;

}
