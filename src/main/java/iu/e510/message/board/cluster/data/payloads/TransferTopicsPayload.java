package iu.e510.message.board.cluster.data.payloads;

import iu.e510.message.board.cluster.data.DataManager;
import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.tom.common.payloads.BlockingPayload;
import iu.e510.message.board.tom.common.payloads.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class TransferTopicsPayload extends Payload<String> implements BlockingPayload {
    private static final Logger logger = LoggerFactory.getLogger( TransferTopicsPayload.class );

    public TransferTopicsPayload(String content) {
        super(content);
    }

    @Override
    public Message process(DataManager dataManager) {
        String newNodeId = getContent();

        Map<String, Set<String>> transferTopics = dataManager.getTransferTopics(newNodeId);

        Set<String> delete = transferTopics.get("delete");

        String nodeId = dataManager.getNodeId();
        int timestamp = dataManager.getLamportTimestamp();

        if (!delete.isEmpty()) {
            logger.info("Deleting topics: " + delete);
            dataManager.queuePayload(new DeleteTopicsPayload(delete));
        }
        logger.info("Current topics: " + dataManager.getAllTopics());

        Set<String> transfer = transferTopics.get("transfer");

        logger.info("Node " + nodeId + " needs to transfer topics: " + transfer);
        return new Message(new SyncPayload(transfer), nodeId, timestamp, true);
    }

    @Override
    public String toString() {
        return "TransferTopicsPayload{" +
                "content=" + getContent() +
                '}';
    }

}
