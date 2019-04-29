package iu.e510.message.board.cluster.data.beans;

import iu.e510.message.board.db.DMBDatabase;

public class ReplyBean extends BaseBean {
    private int postID;
    private String content;

    public ReplyBean(String clientID, String topic, int postID, String content) {
        super(clientID, topic);
        this.postID = postID;
        this.content = content;
    }


    @Override
    public void processBean(DMBDatabase database) {
        database.addReplyData(postID, getClientID(), content);
    }

    @Override
    public String toString() {
        return "ReplyBean{" +
                "postID=" + postID +
                ", content='" + content + '\'' +
                '}';
    }
}
