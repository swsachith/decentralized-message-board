package iu.e510.message.board.tom.core;

import iu.e510.message.board.tom.common.Message;

import java.util.concurrent.ConcurrentSkipListSet;

public interface MessageHandler {
    Message processMessage(Message message);

    void setMessageQueue(ConcurrentSkipListSet<Message> messageQueue);
}
