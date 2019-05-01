package iu.e510.message.board.tom.common.payloads;

import iu.e510.message.board.cluster.data.DataManager;
import iu.e510.message.board.tom.common.Message;

public interface BlockingPayload {
    Message process(DataManager dataManager);
}
