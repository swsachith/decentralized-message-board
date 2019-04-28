package iu.e510.message.board.server;

import iu.e510.message.board.cluster.data.DataManager;
import iu.e510.message.board.db.model.DMBPost;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
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
    public Set<String> getServersForTopic(String topic) {
        return dataManager.getNodeIdsForTopic(topic);
    }

    @Override
    public Set<String> post(String clientID, String topic, String title, String content) throws RemoteException {
        logger.info("Received a post request from: " + clientID + "\tfor topic: " + topic + "\ttitle: " + title);
        Set<String> servers = getServersForTopic(topic);
        if (servers.contains(myNodeId)) {
            // todo: save the infomation

            return Collections.emptySet();
        }

        return servers;
    }

    @Override
    public Set<String> upvotePost(String clientID, String topic, int postID) {
        return Collections.emptySet();

    }

    @Override
    public Set<String> downvotePost(String clientID, String topic, int postID) {
        return Collections.emptySet();

    }

    @Override
    public Set<String> replyPost(String clientID, String topic, int postID, String content) {
        return Collections.emptySet();

    }

    @Override
    public Set<String> upvoteReply(String clientID, String topic, int postID, int replyID) {
        return Collections.emptySet();
    }

    @Override
    public Set<String> downvoteReply(String clientID, String topic, int postID, int replyID) {
        return Collections.emptySet();
    }

    @Override
    public DMBPost getPost(String clientID, String topic, int postID) {
        return null;
    }

    @Override
    public List<DMBPost> getPosts(String clientID, String topic) {
        return null;
    }
}
