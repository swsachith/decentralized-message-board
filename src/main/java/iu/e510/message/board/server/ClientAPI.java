package iu.e510.message.board.server;

import iu.e510.message.board.db.model.DMBPost;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * This is the client side API.
 * Will be served through a REST API.
 */
public interface ClientAPI extends Remote, Serializable {

    // Posting, upvoting and downvoting posts
    boolean post(String clientID, String topic, String title, String content) throws RemoteException;

    void upvotePost(String clientID, String topic, String postID) throws RemoteException;

    void downvotePost(String clientID, String topic, String postID) throws RemoteException;

    // one-level-reply to posts, upvoting and downvoting replies
    void replyPost(String clientID, String topic, String postID, String content) throws RemoteException;

    void upvoteReply(String clientID, String topic, String postID, String replyID) throws RemoteException;

    void downvoteReply(String clientID, String topic, String postID, String replyID) throws RemoteException;

    // retrieval of data
    DMBPost getPost(String clientID, String topic, String postID) throws RemoteException;

    /**
     * A paginated version of the posts retrieval in a ranked manner.
     * @param topic
     * @return
     */
    List<DMBPost> getPosts(String clientID, String topic) throws RemoteException;
}
