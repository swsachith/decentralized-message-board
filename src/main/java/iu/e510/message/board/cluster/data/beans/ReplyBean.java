package iu.e510.message.board.cluster.data.beans;

public class ReplyBean extends PostBean {
    private int replyID;

    public ReplyBean(String clientID, String topic, int postID, int replyID) {
        super(clientID, topic, postID);
        this.replyID = replyID;
    }

    public int getReplyID() {
        return replyID;
    }
}
