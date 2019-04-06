package iu.e510.message.board.cluster.data;

import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.tom.common.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.BlockingQueue;

public class SuperNodeMsgExecutor extends Thread {
/*    private static Logger logger = LoggerFactory.getLogger(SuperNodeMsgExecutor.class);

    private SuperNodeDataManager superNodeDataManager;
    private BlockingQueue<String> superNodeMsgQueue;
    private Map<String, Message> superNodeMsgs;

    public SuperNodeMsgExecutor(SuperNodeDataManager superNodeDataManager,
                                BlockingQueue<String> superNodeMsgQueue,
                                Map<String, Message> superNodeMsgs) {
        this.superNodeDataManager = superNodeDataManager;
        this.superNodeMsgQueue = superNodeMsgQueue;
        this.superNodeMsgs = superNodeMsgs;
    }

    @Override
    public void run() {
        try {
            String messageId = this.superNodeMsgQueue.take();
            Message message = this.superNodeMsgs.remove(messageId);

            if (message.getMessageType() == MessageType.SYNC) {
                logger.info("Processing SYNC msg: " + message);
                // todo: implement this --> take the topics from the message and get the
                //  data using the datamanager to talk to relevant nodes
            } else if (message.getMessageType() == MessageType.TRANSFER) {
                logger.info("Processing TRANSFER msg: " + message);
                // todo: implement this --> take the topics from the message and send the
                //  relevant data to the destination node
            }

        } catch (InterruptedException e) {
            logger.error("Unable to access the queue: ", e);
        }
    }*/
}