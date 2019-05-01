package iu.e510.message.board.cluster.data;

import iu.e510.message.board.cluster.data.beans.BaseBean;
import iu.e510.message.board.db.DMBDatabase;
import iu.e510.message.board.db.model.DMBPost;
import iu.e510.message.board.tom.MessageService;
import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.tom.common.payloads.BlockingPayload;
import iu.e510.message.board.tom.common.payloads.NonBlockingPayload;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface DataManager {
    void addTopicData(String path) throws Exception;

    byte[] requestData(String path);

    boolean hasData(String path);

    void deleteData(String path) throws Exception;

    Set<String> getAllTopics();

    void setConsistency(boolean consistency);

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


    DMBPost getPost(String clientID, String topic, int postID) throws Exception;

    List<DMBPost> getPosts(String clientID, String topic) throws Exception;

    Message processPayload(BlockingPayload payload);

    void queuePayload(NonBlockingPayload payload);

    int getLamportTimestamp();

    MessageService getMessageService();

    DMBDatabase getDatabase();
}
