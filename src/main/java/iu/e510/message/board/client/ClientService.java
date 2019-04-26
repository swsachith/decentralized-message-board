package iu.e510.message.board.client;

import iu.e510.message.board.db.model.DMBPost;

import java.util.List;

public interface ClientService {
    // Posting, upvoting and downvoting posts
    boolean post(String topic, String title, String content);

    void upvotePost(String topic, int postID);

    void downvotePost(String topic, int postID);

    // one-level-reply to posts, upvoting and downvoting replies
    void replyPost(String topic, int postID, String content);

    void upvoteReply(String topic, int postID, int replyID);

    void downvoteReply(String topic, int postID, int replyID);

    // retrieval of data
    DMBPost getPost(String topic, int postID);

    /**
     * A paginated version of the posts retrieval in a ranked manner.
     * @param topic
     * @return
     */
    List<DMBPost> getPosts(String topic);
}
