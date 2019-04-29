package iu.e510.message.board.client;

import iu.e510.message.board.db.model.DMBPost;
import iu.e510.message.board.server.ClientAPI;
import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class ClientServiceImpl implements ClientService {
    private static Logger logger = LoggerFactory.getLogger(ClientServiceImpl.class);
    private Config config;
    private ClientAPI clientAPI;
    private Registry registry;
    private List<String> superNodeList;
    private String clientID;
    private Map<String, ClientAPI> topicClientMap;

    public ClientServiceImpl(String clientID) {
        this.clientID = clientID;
        this.config = new Config();
        String RMI_HOST = config.getConfig(Constants.RMI_REGISTRY_HOST);
        int RMI_PORT = Integer.parseInt(config.getConfig(Constants.RMI_REGISTRY_PORT));
        this.superNodeList = getSuperNodeList();
        this.topicClientMap = new HashMap<>();
        try {
            registry = LocateRegistry.getRegistry(RMI_HOST, RMI_PORT);
            clientAPI = getWorkingClientAPI();
        } catch (RemoteException e) {
            logger.error("Error occurred while getting the registry!");
        }
    }

    /**
     * Returns a working Supernode RMI object from the provided Super node list.
     *
     * @return
     */
    private ClientAPI getWorkingClientAPI() {
        ClientAPI clientAPI;
        for (String superNode : superNodeList) {
            logger.info("Trying to connect to: " + superNode);
            try {
                clientAPI = (ClientAPI) registry.lookup(superNode);
                logger.info("Connected to Super Node: " + superNode);
                return clientAPI;
            } catch (Exception e) {
                logger.info(superNode + " cannot be reached. Hence trying the next node");
            }
        }
        throw new RuntimeException("Cannot connect to any of the supernodes. Please verify the list");
    }

    /**
     * If a server is not reachable, then updates and points to a working server in the given list.
     */
    private void serverConnectionRefresh() {
        logger.error("Error occurred trying to access the Super Node! Please retry the command");
        getWorkingClientAPI();
    }

    /**
     * Reads the list of super nodes from the configs and return an ArrayList
     *
     * @return
     */
    private List<String> getSuperNodeList() {
        String superNodes = this.config.getConfig(Constants.SUPER_NODE_LIST);
        return Arrays.asList(superNodes.split(","));
    }

    @Override
    public boolean post(String topic, String title, String content) {
        topic = topic.toLowerCase().trim();
        Set<String> results;
        try {
            // if the cache has a topic client mapping, use that. Else use the current clientAPI
            ClientAPI topicClient = topicClientMap.get(topic);
            if (topicClient != null) {
                results = topicClient.post(clientID, topic, title, content);
            } else {
                results = clientAPI.post(clientID, topic, title, content);
            }
            if (results != null) {
                if (results.isEmpty()) {
                    return true;
                } else {
                    // if the contacted node does not have that topic, retry with the retry list.
                    ClientAPI topicClientAPI = getClientAPI(results);
                    Set<String> newResult = topicClientAPI.post(clientID, topic, title, content);
                    if (newResult.isEmpty()) {
                        topicClientMap.put(topic, topicClientAPI);
                        return true;
                    }
                }
            }
        } catch (RemoteException e) {
            logger.error("Error executing the method." + e.getMessage(), e);
            return false;
        }
        return false;
    }

    @Override
    public boolean upvotePost(String topic, int postID) {
        return false;
    }

    @Override
    public boolean downvotePost(String topic, int postID) {
        return false;
    }

    @Override
    public boolean replyPost(String topic, int postID, String content) {
        topic = topic.toLowerCase().trim();
        Set<String> results;
        try {
            // if the cache has a topic client mapping, use that. Else use the current clientAPI
            ClientAPI topicClient = topicClientMap.get(topic);
            if (topicClient != null) {
                results = topicClient.replyPost(clientID, topic, postID, content);
            } else {
                results = clientAPI.replyPost(clientID, topic, postID, content);
            }
            if (results != null) {
                if (results.isEmpty()) {
                    return true;
                } else {
                    // if the contacted node does not have that topic, retry with the retry list.
                    ClientAPI topicClientAPI = getClientAPI(results);
                    Set<String> newResult = topicClientAPI.replyPost(clientID, topic, postID, content);
                    if (newResult.isEmpty()) {
                        topicClientMap.put(topic, topicClientAPI);
                        return true;
                    }
                }
            }
        } catch (RemoteException e) {
            logger.error("Error executing the method." + e.getMessage(), e);
            return false;
        }
        return false;
    }

    @Override
    public boolean upvoteReply(String topic, int postID, int replyID) {
        return false;
    }

    @Override
    public boolean downvoteReply(String topic, int postID, int replyID) {
        return false;
    }

    @Override
    public DMBPost getPost(String topic, int postID) {
        return null;
    }

    @Override
    public List<DMBPost> getPosts(String topic) {
        return null;
    }

    /**
     * Returns a reference to a working client in the hash ring for a given client list.
     *
     * @param clientIDList
     * @return
     */
    private ClientAPI getClientAPI(Set<String> clientIDList) {
        //todo: return a random api
        ClientAPI clientAPI;
        for (String clientID : clientIDList) {
            logger.info("Trying to connect to: " + clientID);
            try {
                clientAPI = (ClientAPI) registry.lookup(clientID);
                return clientAPI;
            } catch (Exception e) {
                logger.info(clientID + " cannot be reached. Hence trying the next node");
            }
        }
        throw new RuntimeException("Cannot connect to any of the clients.");
    }
}
