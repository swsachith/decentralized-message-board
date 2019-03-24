package iu.e510.message.board.tom.core;

import iu.e510.message.board.tom.common.Message;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class MessageSender {
    private static Logger logger = LoggerFactory.getLogger(MessageSender.class);

    private ZContext context;
    private int sendTimeout;

    public MessageSender(ZContext context, int sendTimeout) {
        this.context = context;
        this.sendTimeout = sendTimeout;
    }

    public Message sendMessage(Message message, String recipient, int clock) {
        String type = (message.isUnicast()) ? "unicast" : "multicast";
        if (message.isRelease()) {
            logger.info("[pid:" + message.getProcessID() + "][clock:" + clock + "] Sending release message for: "
                    + message.getRelease() + "\tto: " + recipient);
        } else {
            logger.info("[pid:" + message.getProcessID() + "][clock:" + clock + "] Sending " + type +
                    " message " + message.getId() + "\tto: " + recipient);
        }
        ZMQ.Socket socket = context.createSocket(SocketType.REQ);
        socket.setReceiveTimeOut(sendTimeout);
        try {
            socket.connect(recipient);
            socket.send(SerializationUtils.serialize(message));
            byte[] response = socket.recv(0);
            return SerializationUtils.deserialize(response);
        } catch (Exception e) {
            logger.error("[pid:" + message.getProcessID() + "][clock:" + clock + "] Error sending message: "
                    + message.getId() + "\tto: " + recipient + " " + e.getMessage(), e);
        }
        return null;
    }
}
