package iu.e510.message.board.server;

import iu.e510.message.board.cluster.data.DataManager;
import iu.e510.message.board.dbserver.model.DMBPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ClientAPIImpl extends UnicastRemoteObject implements ClientAPI {
    private static Logger logger = LoggerFactory.getLogger(ClientAPI.class);
    private DataManager dataManager;

    public ClientAPIImpl(DataManager dataManager) throws RemoteException {
        super();
        this.dataManager = dataManager;
    }

    @Override
    public boolean post(String clientID, String topic, String title, String content) {
        logger.info("Received a post request from: " + clientID + "\tfor topic: " + topic + "\ttitle: " + title);
        return true;
    }

    @Override
    public void upvotePost(String clientID, String topic, String postID) {

    }

    @Override
    public void downvotePost(String clientID, String topic, String postID) {

    }

    @Override
    public void replyPost(String clientID, String topic, String postID, String content) {

    }

    @Override
    public void upvoteReply(String clientID, String topic, String postID, String replyID) {

    }

    @Override
    public void downvoteReply(String clientID, String topic, String postID, String replyID) {

    }

    @Override
    public DMBPost getPost(String clientID, String topic, String postID) {
        return null;
    }

    @Override
    public List<DMBPost> getPosts(String clientID, String topic) {
        return null;
    }
}
