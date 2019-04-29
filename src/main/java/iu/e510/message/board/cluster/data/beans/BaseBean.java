package iu.e510.message.board.cluster.data.beans;

import iu.e510.message.board.db.DMBDatabase;

import java.io.Serializable;

public abstract class BaseBean implements Serializable {

    private static final long serialVersionUID = -6432207221940427864L;

    private String clientID;
    private String topic;

    public BaseBean(String clientID, String topic) {
        this.clientID = clientID;
        this.topic = topic;
    }

    public String getClientID() {
        return clientID;
    }

    public String getTopic() {
        return topic;
    }

    public abstract void processBean(DMBDatabase database);
}
