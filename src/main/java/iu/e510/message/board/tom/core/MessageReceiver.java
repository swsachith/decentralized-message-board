package iu.e510.message.board.tom.core;

import iu.e510.message.board.tom.common.Message;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.util.concurrent.atomic.AtomicBoolean;

public class MessageReceiver extends Thread {
    private static Logger logger = LoggerFactory.getLogger(MessageReceiver.class);

    private AtomicBoolean running = new AtomicBoolean(false);
    private MessageHandler messageHandler;
    private String bindURL;
    private ZContext context;
    private int processID;

    public MessageReceiver(ZContext context, int processID, String bindURL, MessageHandler messageHandler) {
        this.bindURL = bindURL;
        this.messageHandler = messageHandler;
        this.context = context;
        this.processID = processID;
    }

    public boolean isRunning() {
        return running.get();
    }

    public void stopReceiver() {
        logger.info("Stopping the Message Receiver!");
        running.set(false);
    }

    @Override
    public void run() {
        logger.info("Starting the message receiver with URI: " + bindURL);
        ZMQ.Socket socket = this.context.createSocket(SocketType.REP);
        socket.bind(this.bindURL);
        running.set(true);
        while (running.get()) {
            byte[] message = socket.recv(0);
            Message response = messageHandler.processMessage(SerializationUtils.deserialize(message));
            socket.send(SerializationUtils.serialize(response), 0);
        }
    }
}
