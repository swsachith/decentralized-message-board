package iu.e510.message.board.server;

import iu.e510.message.board.db.model.DMBPost;

import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;
import java.util.Set;

/**
 * This is the client side API.
 * Will be served through a REST API.
 */
public interface ClientAPI extends Remote, Serializable {

    Set<String> getServersForTopic(String topic);

    // Posting, upvoting and downvoting posts
    Set<String> post(String clientID, String topic, String title, String content) throws RemoteException;

    Set<String> upvotePost(String clientID, String topic, int postID) throws RemoteException;

    Set<String> downvotePost(String clientID, String topic, int postID) throws RemoteException;

    // one-level-reply to posts, upvoting and downvoting replies
    Set<String> replyPost(String clientID, String topic, int postID, String content) throws RemoteException;

    Set<String> upvoteReply(String clientID, String topic, int postID, int replyID) throws RemoteException;

    Set<String> downvoteReply(String clientID, String topic, int postID, int replyID) throws RemoteException;

    // retrieval of data
    DMBPost getPost(String clientID, String topic, int postID) throws RemoteException;

    /**
     * A paginated version of the posts retrieval in a ranked manner.
     * @param topic
     * @return
     */
    List<DMBPost> getPosts(String clientID, String topic) throws RemoteException;
}
