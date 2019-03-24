package iu.e510.message.board.tom.core;

import iu.e510.message.board.tom.common.Message;

public interface DeliveryHandler {
    void deliverReleaseMessage(Message message);
}
