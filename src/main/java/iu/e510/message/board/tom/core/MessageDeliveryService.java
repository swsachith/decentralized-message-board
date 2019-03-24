package iu.e510.message.board.tom.core;

import iu.e510.message.board.tom.common.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

public class MessageDeliveryService extends Thread {
    private static Logger logger = LoggerFactory.getLogger(MessageDeliveryService.class);
    private AtomicBoolean running = new AtomicBoolean(false);
    private ConcurrentSkipListSet<Message> messageQueue;
    private int myPid;
    private DeliveryHandler handler;

    public MessageDeliveryService(ConcurrentSkipListSet<Message> messageQueue, int processID, DeliveryHandler handler) {
        this.messageQueue = messageQueue;
        this.myPid = processID;
        this.handler = handler;
    }

    public boolean isRunning() {
        return running.get();
    }

    public void stopDeliveryService() {
        logger.info("Stopping the message delivery service!");
        running.set(false);
    }

    @Override
    public void run() {
        logger.info("Starting the message delivery service!");
        running.set(true);
        while (running.get()) {
            if (!messageQueue.isEmpty()) {
                // if this is my message, send release request
                Message first = null;
                try {
                    first = messageQueue.first();
                } catch (NoSuchElementException e) {
                    // race condition occurred. Was not empty before if condition, but was empty right after
                }
                if (first != null && first.getProcessID() == myPid && first.isAllAcked()) {
                    logger.info("My message at the head of the message queue, hence releasing! Current Queue: ");
                    logger.info(messageQueue.toString());
                    handler.deliverReleaseMessage(first);
                    messageQueue.remove(first);
                }
            }
        }
    }
}
