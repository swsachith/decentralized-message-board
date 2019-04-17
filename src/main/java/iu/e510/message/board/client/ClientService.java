package iu.e510.message.board.client;

import iu.e510.message.board.dbserver.model.DMBPost;

import java.util.List;

public interface ClientService {
    // Posting, upvoting and downvoting posts
    boolean post(String topic, String title, String content);

    void upvotePost(String topic, String postID);

    void downvotePost(String topic, String postID);

    // one-level-reply to posts, upvoting and downvoting replies
    void replyPost(String topic, String postID, String content);

    void upvoteReply(String topic, String postID, String replyID);

    void downvoteReply(String topic, String postID, String replyID);

    // retrieval of data
    DMBPost getPost(String topic, String postID);

    /**
     * A paginated version of the posts retrieval in a ranked manner.
     * @param topic
     * @return
     */
    List<DMBPost> getPosts(String topic);
}
