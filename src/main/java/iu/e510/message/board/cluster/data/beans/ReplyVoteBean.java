package iu.e510.message.board.cluster.data.beans;

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

    public boolean isVote() {
        return vote;
    }

    public int getPostID() {
        return postID;
    }

    public int getReplyID() {
        return replyID;
    }
}
