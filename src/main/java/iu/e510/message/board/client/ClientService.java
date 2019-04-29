package iu.e510.message.board.client;

import iu.e510.message.board.db.model.DMBPost;

import java.util.List;
import java.util.Set;

public interface ClientService {
    //Posting, upvoting and downvoting posts
    boolean post(String topic, String title, String content);

    boolean upvotePost(String topic, int postID);

    boolean downvotePost(String topic, int postID);

    // one-level-reply to posts, upvoting and downvoting replies
    boolean replyPost(String topic, int postID, String content);

    boolean upvoteReply(String topic, int postID, int replyID);

    boolean downvoteReply(String topic, int postID, int replyID);

    // retrieval of data
    DMBPost getPost(String topic, int postID);

    /**
     * A paginated version of the posts retrieval in a ranked manner.
     * @param topic
     * @return
     */
    List<DMBPost> getPosts(String topic);
}
