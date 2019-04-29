package iu.e510.message.board.client;

import iu.e510.message.board.db.model.DMBPost;
import iu.e510.message.board.server.ClientAPI;
import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
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
        return handleClientRequestRecursively(ClientAPIMethodsEnum.POST, new String[]{clientID, topic, title, content});
    }

    private Method getMethod(ClientAPIMethodsEnum methodEnum) throws NoSuchMethodException {
        Method method = null;
        switch (methodEnum) {
            case POST:
                method = ClientAPI.class.getMethod("post", String.class, String.class, String.class, String.class);
                break;
            case REPLY:
                method = ClientAPI.class.getMethod("replyPost", String.class, String.class, int.class, String.class);
                break;
            default:
                break;
        }
        return method;
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
        return handleClientRequestRecursively(ClientAPIMethodsEnum.REPLY, new Object[]{clientID, topic, postID, content});
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
        List<DMBPost> results = new ArrayList<>();
        try {
            // if the cache has a topic client mapping, use that. Else use the current clientAPI
            ClientAPI topicClient = topicClientMap.get(topic);
            if (topicClient != null) {
                results = topicClient.getPosts(clientID, topic);
            } else {
                Set<String> nodes = clientAPI.getNodes(topic);
                if (nodes != null) {
                    topicClientMap.remove(topic);
                    // if the contacted node does not have that topic, retry with the retry list.
                    ClientAPI topicClientAPI = getClientAPI(nodes);
                    results = topicClientAPI.getPosts(clientID, topic);
                    if (!results.isEmpty()) {
                        topicClientMap.put(topic, topicClientAPI);
                        return results;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error executing the method." + e.getMessage(), e);
            return results;
        }
        return results;
    }

    /**
     * Returns a reference to a random working super node in the hash ring for a given client list.
     *
     * @param clientIDSet
     * @return
     */
    private ClientAPI getClientAPI(Set<String> clientIDSet) {
        ClientAPI clientAPI;
        // shuffling to get a random super node
        List<String> clientIDList = new ArrayList<>(clientIDSet);
        Collections.shuffle(clientIDList, new Random());
        for (String clientID : clientIDList) {
            logger.info("Trying to connect to: " + clientID);
            try {
                clientAPI = (ClientAPI) registry.lookup(clientID);
                return clientAPI;
            } catch (Exception e) {
                logger.info(clientID + " cannot be reached. Hence trying the next node");
            }
        }
        throw new RuntimeException("Cannot connect to any of the super nodes.");
    }

    private boolean handleClientRequestRecursively(ClientAPIMethodsEnum methodName, Object[] parameters) {
        Method method;
        Set<String> results;
        try {
            method = getMethod(methodName);
            // if the cache has a topic client mapping, use that. Else use the current clientAPI
            String topic = (String) parameters[1];
            ClientAPI topicClient = topicClientMap.get(topic);
            if (topicClient != null) {
                results = (Set<String>) method.invoke(topicClient, parameters);
            } else {
                results = (Set<String>) method.invoke(clientAPI, parameters);
            }
            if (results != null) {
                if (results.isEmpty()) {
                    return true;
                } else {
                    topicClientMap.remove(topic);
                    // if the contacted node does not have that topic, retry with the retry list.
                    ClientAPI topicClientAPI = getClientAPI(results);
                    Set<String> newResult =
                            (Set<String>) method.invoke(topicClientAPI, parameters);
                    if (newResult.isEmpty()) {
                        topicClientMap.put(topic, topicClientAPI);
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error executing the method." + e.getMessage(), e);
            return false;
        }
        return false;
    }
}
