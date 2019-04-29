package iu.e510.message.board.cluster.data.beans;

import iu.e510.message.board.db.DMBDatabase;

public class PostVoteBean extends BaseBean {
    private boolean vote;
    private int postID;

    public PostVoteBean(String clientID, String topic, int postID, boolean vote) {
        super(clientID, topic);
        this.vote = vote;
        this.postID = postID;
    }

    @Override
    public void processBean(DMBDatabase database) {
        if (vote){
            database.upVotePost(postID, getClientID());
        } else {
            database.downVotePost(postID, getClientID());
        }
    }

    @Override
    public String toString() {
        return "PostVoteBean{" +
                "vote=" + vote +
                ", postID=" + postID +
                '}';
    }
}
