package iu.e510.message.board.tom.core;

import iu.e510.message.board.tom.common.Message;

public interface MessageHandler {
    Message processMessage(Message message);
}
