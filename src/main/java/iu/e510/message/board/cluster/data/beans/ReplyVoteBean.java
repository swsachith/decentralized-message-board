package iu.e510.message.board.cluster.data.beans;

import iu.e510.message.board.db.DMBDatabase;

public class ReplyVoteBean extends BaseBean {
    private boolean vote;
    private int postID;
    private int replyID;

    public ReplyVoteBean(String clientID, String topic, int postID, int replyID, boolean vote) {
        super(clientID, topic);
        this.vote = vote;
        this.postID = postID;
        this.replyID = replyID;
    }


    @Override
    public void processBean(DMBDatabase database) {
        if (vote) {
            database.upVoteReply(replyID, getClientID());
        }
    }

    @Override
    public String toString() {
        return "ReplyVoteBean{" +
                "vote=" + vote +
                ", postID=" + postID +
                ", replyID=" + replyID +
                '}';
    }
}
