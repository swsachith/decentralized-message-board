package iu.e510.message.board.cluster.data.beans;

public class PostBean extends BaseBean {
    private String  title;
    private String content;

    public PostBean(String clientID, String topic, String title, String content) {
        super(clientID, topic);
        this.title = title;
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }
}
