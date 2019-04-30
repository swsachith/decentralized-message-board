package iu.e510.message.board.cluster.data.payloads;

import iu.e510.message.board.cluster.data.DataManager;
import iu.e510.message.board.tom.common.BlockingCall;
import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.tom.common.Payload;

import java.util.Map;
import java.util.Set;

public class TransferTopicsPayload extends Payload<String> implements BlockingCall {
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
            Message delTopics = new Message(new DeleteTopicsPayload(delete), nodeId, timestamp,
                    true);
            dataManager.queueMessage(delTopics);
        }

        Set<String> transfer = transferTopics.get("transfer");

        return new Message(new SyncPayload(transfer), nodeId, timestamp, true);
    }

    @Override
    public String toString() {
        return "TransferTopicsPayload{" +
                "content=" + getContent() +
                '}';
    }

}
