package iu.e510.message.board.cluster.data.payloads;

import iu.e510.message.board.cluster.data.DataManager;
import iu.e510.message.board.tom.common.NonBlockingPayload;
import iu.e510.message.board.tom.common.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class DeleteTopicsPayload extends Payload<Set<String>> implements NonBlockingPayload {
    private static Logger logger = LoggerFactory.getLogger(DeleteTopicsPayload.class);

    public DeleteTopicsPayload(Set<String> content) {
        super(content);
    }

    @Override
    public void process(DataManager dataManager) {

        logger.info("Delete topics received!");
        Set<String> delTopics = getContent();

        for (String topic : delTopics) {
            try {
                dataManager.deleteData(topic);
                dataManager.getDatabase().removePostsDataByTopic(topic);
            } catch (Exception e) {
                throw new RuntimeException("unable to process DEL TOPICS payload", e);
            }
        }
    }

    @Override
    public String toString() {
        return "DelTopicsPayload{" +
                "content=" + getContent() +
                '}';
    }
}
