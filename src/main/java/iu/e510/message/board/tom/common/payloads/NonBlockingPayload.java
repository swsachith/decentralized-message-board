package iu.e510.message.board.tom.common.payloads;

import iu.e510.message.board.cluster.data.DataManager;

public interface NonBlockingPayload {
    void process(DataManager dataManager);
}
