package iu.e510.message.board.cluster.data.beans;

import iu.e510.message.board.db.DMBDatabase;

public class PostBean extends BaseBean {
    private String  title;
    private String content;

    public PostBean(String clientID, String topic, String title, String content) {
        super(clientID, topic);
        this.title = title;
        this.content = content;
    }

    @Override
    public void processBean(DMBDatabase database) {
        database.addPostData(title, getTopic(), getClientID(), content);
    }

    @Override
    public String toString() {
        return "PostBean{" +
                "title='" + title + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
