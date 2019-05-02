package iu.e510.message.board.cluster.data.payloads;

import iu.e510.message.board.cluster.data.DataManager;
import iu.e510.message.board.tom.MessageService;
import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.tom.common.payloads.NonBlockingPayload;
import iu.e510.message.board.tom.common.payloads.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransferPayload extends Payload<String> implements NonBlockingPayload {
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

        if (!dataManager.isConsistent()) {
            logger.info("Server is now consistent!");
            dataManager.setConsistency(true);
        }

        TransferTopicsPayload transferTopicsPayload =
                new TransferTopicsPayload(dataManager.getNodeId());

        MessageService messageService = dataManager.getMessageService();

        Message syncResponse = messageService.send_unordered(transferTopicsPayload,
                messageService.getUrl(nodeToTalk));

        logger.info("Need to sync topics: " + syncResponse.getPayload().getContent());
        dataManager.queuePayload((NonBlockingPayload) syncResponse.getPayload());
    }

    @Override
    public String toString() {
        return "TransferPayload{" +
                "content=" + getContent() +
                '}';
    }

}
