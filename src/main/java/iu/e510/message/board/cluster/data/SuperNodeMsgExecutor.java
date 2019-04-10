package iu.e510.message.board.cluster.data;

import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.tom.common.MessageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;

public class SuperNodeMsgExecutor extends Thread {
    private static Logger logger = LoggerFactory.getLogger(SuperNodeMsgExecutor.class);

    private SuperNodeDataManager superNodeDataManager;
    private BlockingQueue<Message> superNodeMsgQueue;
    private LocalDataManager localDataManager;

    SuperNodeMsgExecutor(SuperNodeDataManager superNodeDataManager,
                         LocalDataManager localDataManager,
                         BlockingQueue<Message> superNodeMsgQueue) {
        this.superNodeDataManager = superNodeDataManager;
        this.superNodeMsgQueue = superNodeMsgQueue;
        this.localDataManager = localDataManager;
    }

    @Override
    public void run() {
        try {
            Message message = this.superNodeMsgQueue.take();
            logger.info("Server is inconsistent");
            this.superNodeDataManager.setConsistency(false);

            if (message.getMessageType() == MessageType.SYNC) {
                logger.info("Processing SYNC msg: " + message);
                // todo: implement this --> take the topics from the message and get the
                //  data using the datamanager to talk to relevant nodes

                String topic = (String) message.getPayload().getContent();



            } else if (message.getMessageType() == MessageType.TRANSFER) {
                logger.info("Processing TRANSFER msg: " + message);
                // todo: implement this --> take the topics from the message and send the
                //  relevant data to the destination node
            }

            this.superNodeDataManager.setConsistency(true);
            logger.info("Server consistent again!");

        } catch (InterruptedException e) {
            logger.error("Unable to access the queue: ", e);
        }
    }
}