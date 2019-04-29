package iu.e510.message.board.tom.common;

import java.io.Serializable;
import java.util.*;

public class Message implements Serializable, Comparable<Message> {
    private Payload payload;
    private String nodeID;
    private int clock;
    private boolean unicast;
    private String ack;
    private String release;
    private String id;
    private Set<String> recipients;
    private boolean allAcked;
    private MessageType messageType;

    public Message(Payload payload, String nodeID, int clock, boolean unicast,
                   MessageType messageType) {
        this.payload = payload;
        this.nodeID = nodeID;
        this.clock = clock;
        this.unicast = unicast;
        this.ack = "";
        this.release = "";
        this.id = UUID.randomUUID().toString();
        this.recipients = new HashSet<>();
        this.allAcked = false;
        this.messageType = messageType;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public boolean isAllAcked() {
        return allAcked;
    }

    public void setAllAcked(boolean allAcked) {
        this.allAcked = allAcked;
    }

    public Payload getPayload() {
        return payload;
    }

    public String getNodeID() {
        return nodeID;
    }

    public int getClock() {
        return clock;
    }

    public boolean isUnicast() {
        return unicast;
    }

    public boolean isAck() {
        return !ack.isEmpty();
    }

    public String getAck() {
        return ack;
    }

    public void setAck(String ack) {
        this.ack = ack;
    }

    public String getRelease() {
        return release;
    }

    public void setRelease(String release) {
        this.release = release;
    }

    public boolean isRelease() {
        return !release.isEmpty();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Set<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(Set<String> recipients) {
        this.recipients = recipients;
    }

    @Override
    public String toString() {
        return payload.getContent().toString() + " generated from pid=" + nodeID + " with " +
                "clock: " + clock + " " + "unicast: " + unicast;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return Objects.equals(id, message.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public int compareTo(Message o) {
        if (this.id.equals(o.getId())) {
            return 0;
        }
        // compare clock values first
        if (this.clock > o.clock) {
            return 1;
        } else if (this.clock < o.clock) {
            return -1;
        } else {
            // if clock values are equal, precedence given to process with the smallest nodeID
            return this.getNodeID().compareTo(o.getNodeID());
        }
    }
}
