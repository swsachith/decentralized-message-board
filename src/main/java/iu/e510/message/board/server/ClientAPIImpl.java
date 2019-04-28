package iu.e510.message.board.server;

import iu.e510.message.board.cluster.data.DataManager;
import iu.e510.message.board.db.DMBDatabase;
import iu.e510.message.board.db.DMBDatabaseImpl;
import iu.e510.message.board.db.model.DMBPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ClientAPIImpl extends UnicastRemoteObject implements ClientAPI {
    private static Logger logger = LoggerFactory.getLogger(ClientAPI.class);
    private DataManager dataManager;
    private DMBDatabase database;

    public ClientAPIImpl(DataManager dataManager) throws RemoteException {
        super();
        this.dataManager = dataManager;
        this.database = new DMBDatabaseImpl("sample_id");
    }

    @Override
    public boolean post(String clientID, String topic, String title, String content) {
        logger.info("Received a post request from: " + clientID + "\tfor topic: " + topic + "\ttitle: " + title);
        database.addPostData(title, topic, clientID, content);
        return true;
    }

    @Override
    public void upvotePost(String clientID, String topic, int postID) {
        logger.info("Received a post upvote request from: " + clientID + "\tfor post id: " + postID);
        database.upVotePost(postID, clientID);
    }

    @Override
    public void downvotePost(String clientID, String topic, int postID) {
        logger.info("Received a post downvote request from: " + clientID + "\tfor post id: " + postID);
        database.downVotePost(postID, clientID);
    }

    @Override
    public void replyPost(String clientID, String topic, int postID, String content) {
        logger.info("Received a reply to post request from: " + clientID + "\tfor post id: " + postID);
        database.addReplyData(postID, clientID, content);
    }

    @Override
    public void upvoteReply(String clientID, String topic, int postID, int replyID) {
        logger.info("Received a reply upvote request from: " + clientID + "\tfor post id: " + postID
                + " and reply id: " + replyID);
        database.upVoteReply(replyID, clientID);
    }

    @Override
    public void downvoteReply(String clientID, String topic, int postID, int replyID) {
        logger.info("Received a reply downvote request from: " + clientID + "\tfor post id: " + postID
                + " and reply id: " + replyID);
        database.downVoteReply(replyID, clientID);
    }

    @Override
    public DMBPost getPost(String clientID, String topic, int postID) {
        logger.info("Received a get post by id request from: " + clientID + "\tfor post id: " + postID);
        return null;
    }

    @Override
    public List<DMBPost> getPosts(String clientID, String topic) {
        logger.info("Received a get posts by topic request from: " + clientID + "\tfor post topic: " + topic);
        return database.getPostsDataByTopicArrayList(topic);
    }
}
