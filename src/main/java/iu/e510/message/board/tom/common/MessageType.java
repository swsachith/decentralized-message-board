package iu.e510.message.board.tom.common;

public enum MessageType {
    // sent by master - unicast
    TRANSFER,
    SYNC,

    // sent by workers in response to master's messages - unicast
    TRANSFER_TOPICS,
    DELETE_TOPICS,
    DATA_REQUEST,
    DATA_RESPONSE,


    // sent by workers related to client data -
    CLIENT_DATA
}
