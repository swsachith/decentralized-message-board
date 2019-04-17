package iu.e510.message.board.server;

import iu.e510.message.board.dbserver.model.DMBPost;

import java.util.List;

public class ClientAPIImpl implements ClientAPI {

    @Override
    public void post(String topic, String title, String content) {

    }

    @Override
    public void upvotePost(String topic, String postID) {

    }

    @Override
    public void downvotePost(String topic, String postID) {

    }

    @Override
    public void replyPost(String topic, String postID, String content) {

    }

    @Override
    public void upvoteReply(String topic, String postID, String replyID) {

    }

    @Override
    public void downvoteReply(String topic, String postID, String replyID) {

    }

    @Override
    public DMBPost getPost(String topic, String postID) {
        return null;
    }

    @Override
    public List<DMBPost> getPosts(String topic) {
        return null;
    }
}
