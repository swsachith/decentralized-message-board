package iu.e510.message.board.cluster.data.payloads;

import iu.e510.message.board.cluster.data.DataManager;
import iu.e510.message.board.tom.common.BlockingCall;
import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.tom.common.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataRequestPayload extends Payload<String> implements BlockingCall {
    private static final Logger logger = LoggerFactory.getLogger(DataRequestPayload.class);

    public DataRequestPayload(String content) {
        super(content);
    }

    @Override
    public Message process(DataManager dataManager) {
        String topic = getContent();
        logger.info("Data request received for topic: " + topic);

        byte[] data = dataManager.getDatabase().getPostsDataByTopicByteArray(topic);

        return new Message(new DataResponsePayload(data), dataManager.getNodeId(),
                dataManager.getLamportTimestamp(), true);

    }

    @Override
    public String toString() {
        return "DataReqPayload{" +
                "content=" + getContent() +
                '}';
    }

}
