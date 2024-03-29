package iu.e510.message.board.server;

import iu.e510.message.board.cluster.data.DataManager;
import iu.e510.message.board.cluster.data.beans.*;
import iu.e510.message.board.db.model.DMBPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Set;

public class ClientAPIImpl extends UnicastRemoteObject implements ClientAPI {
    private static Logger logger = LoggerFactory.getLogger(ClientAPI.class);
    private DataManager dataManager;
    private String myNodeId;

    public ClientAPIImpl(String myNodeId, DataManager dataManager) throws RemoteException {
        super();
        this.dataManager = dataManager;
        this.myNodeId = myNodeId;
    }

    @Override
    public Set<String> getNodes(String topic) {
        return dataManager.getNodeIdsForTopic(topic);
    }

    @Override
    public Set<String> post(String clientID, String topic, String title, String content) throws
            RemoteException {
        logger.info("Received a post request from: " + clientID + "\tfor topic: " + topic +
                "\ttitle: " + title);
        return dataManagerAddData(new PostBean(clientID, topic, title, content));
    }

    @Override
    public Set<String> upvotePost(String clientID, String topic, int postID)
            throws RemoteException {
        logger.info("Received post upvote from: " + clientID + "\tfor topic: " + topic +
                "\tpostId: " + postID);
        return dataManagerAddData(new PostVoteBean(clientID, topic, postID, true));

    }

    @Override
    public Set<String> downvotePost(String clientID, String topic, int postID)
            throws RemoteException {
        logger.info("Received post downvote from: " + clientID + "\tfor topic: " + topic +
                "\tpostId: " + postID);
        return dataManagerAddData(new PostVoteBean(clientID, topic, postID, false));
    }

    @Override
    public Set<String> replyPost(String clientID, String topic, int postID, String content)
            throws RemoteException {
        logger.info("Received a post reply from: " + clientID + "\tfor topic: " + topic +
                "\tpostid: " + postID);
        return dataManagerAddData(new ReplyBean(clientID, topic, postID, content));
    }

    @Override
    public Set<String> upvoteReply(String clientID, String topic, int postID, int replyID)
            throws RemoteException {
        return dataManagerAddData(new ReplyVoteBean(clientID, topic, postID, replyID, true));
    }

    @Override
    public Set<String> downvoteReply(String clientID, String topic, int postID, int replyID)
            throws RemoteException {
        return dataManagerAddData(new ReplyVoteBean(clientID, topic, postID, replyID, false));
    }

    @Override
    public DMBPost getPost(String clientID, String topic, int postID) throws RemoteException {
        try {
            DMBPost post = dataManager.getPost(clientID, topic, postID);
            logger.info("Response: " + post.toString());
            return post;
        } catch (Exception e) {
            throw new RemoteException("Unable to get post", e);
        }
    }

    @Override
    public List<DMBPost> getPosts(String clientID, String topic) throws RemoteException{
        try {

            List<DMBPost> posts = dataManager.getPosts(clientID, topic);
            logger.info("Response: " + posts.toString());
            return posts;
        } catch (Exception e) {
            throw new RemoteException("Unable to get posts", e);
        }
    }

    @Override
    public List<DMBPost> searchPosts(String clientID, String topic, String str) throws RemoteException {
        try {

            List<DMBPost> posts = dataManager.searchPosts(clientID, topic, str);
            logger.info("Response: " + posts.toString());
            return posts;
        } catch (Exception e) {
            throw new RemoteException("Unable to serach posts", e);
        }
    }

    @Override
    public List<DMBPost> getTopPosts() throws RemoteException {
        try {

            List<DMBPost> posts = dataManager.getTopPosts();
            logger.info("Response: " + posts.toString());
            return posts;
        } catch (Exception e) {
            throw new RemoteException("Unable to get top posts", e);
        }
    }

    private Set<String> dataManagerAddData(BaseBean dataBean) throws RemoteException {
        try {
            // todo: return the set if this is inconsistent
            return dataManager.addData(dataBean);
        } catch (Exception e) {
            throw new RemoteException("Unable to publish post reply", e);
        }
    }
}
