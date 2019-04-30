package iu.e510.message.board.cluster.data.payloads;

import iu.e510.message.board.cluster.data.DataManager;
import iu.e510.message.board.tom.MessageService;
import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.tom.common.NonBlockingCall;
import iu.e510.message.board.tom.common.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferPayload extends Payload<String> implements NonBlockingCall {
    private static Logger logger = LoggerFactory.getLogger(TransferPayload.class);

    public TransferPayload(String content) {
        super(content);
    }

    @Override
    public void process(DataManager dataManager) {
        logger.info("Processing TRANSFER payload: " + this);

        // take the topics from the message
        String nodeToTalk = getContent();
        logger.info("Master asks me to talk to: " + nodeToTalk + " and transfer " +
                "the relevant topics");

        TransferTopicsPayload transferTopicsPayload =
                new TransferTopicsPayload(dataManager.getNodeId());

        MessageService messageService = dataManager.getMessageService();

        Message response = messageService.send_unordered(transferTopicsPayload,
                messageService.getUrl(nodeToTalk));

        dataManager.queueMessage(response);
    }

    @Override
    public String toString() {
        return "TransferPayload{" +
                "content=" + getContent() +
                '}';
    }

}
