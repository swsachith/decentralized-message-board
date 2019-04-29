package iu.e510.message.board.cluster.data.beans;

public class PostVoteBean extends BaseBean {
    private boolean vote;
    private int postID;

    public PostVoteBean(String clientID, String topic, int postID, boolean vote) {
        super(clientID, topic);
        this.vote = vote;
        this.postID = postID;
    }

    public boolean isVote() {
        return vote;
    }

    public int getPostID() {
        return postID;
    }
}
