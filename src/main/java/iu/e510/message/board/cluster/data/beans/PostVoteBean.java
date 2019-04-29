package iu.e510.message.board.cluster.data.beans;

public class PostVoteBean extends PostBean {
    private boolean vote;

    public PostVoteBean(String clientID, String topic, int postID, boolean vote) {
        super(clientID, topic, postID);
        this.vote = vote;
    }

    public boolean isVote() {
        return vote;
    }
}
