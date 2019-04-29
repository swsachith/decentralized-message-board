package iu.e510.message.board.cluster.data.beans;

public class ReplyBean extends BaseBean {
    private int postID;
    private String content;


    public ReplyBean(String clientID, String topic, int postID, String content) {
        super(clientID, topic);
        this.postID = postID;
        this.content = content;
    }

    public int getPostID() {
        return postID;
    }

    public String getContent() {
        return content;
    }
}
