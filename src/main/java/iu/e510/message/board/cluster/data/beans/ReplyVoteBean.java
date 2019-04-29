package iu.e510.message.board.cluster.data.beans;

public class ReplyVoteBean extends ReplyBean {
    private boolean vote;

    public ReplyVoteBean(String clientID, String topic, int postID, int replyID, boolean vote) {
        super(clientID, topic, postID, replyID);
        this.vote = vote;
    }

    public boolean isVote() {
        return vote;
    }
}
