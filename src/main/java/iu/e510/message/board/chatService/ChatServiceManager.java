package iu.e510.message.board.chatService;

import iu.e510.message.board.cluster.ClusterManager;
import iu.e510.message.board.cluster.zk.ZKManager;
import iu.e510.message.board.cluster.zk.ZKManagerImpl;
import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.framework.recipes.queue.*;
import org.apache.curator.framework.state.ConnectionState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChatServiceManager {
    private static Logger logger = LoggerFactory.getLogger(ClusterManager.class);

    private String nodeID;
    private ZKManager zkManager;
    private Config config;
    private PathChildrenCache subcriberListenerCache;

    public ChatServiceManager(String id) {
        this.nodeID = id;
        this.zkManager = new ZKManagerImpl();
        this.config = new Config();
    }

    private void initialize() {
        String queueLocation = config.getConfig(Constants.MESSAGE_QUEUE_LOCATION);
        String myQueue = queueLocation + "/" + nodeID;

        // create queue
        CuratorFramework zkClient = zkManager.getZKClient();
        ChatQueueConsumer queueConsumer = new ChatQueueConsumer();
        QueueBuilder builder = QueueBuilder.builder(zkClient, queueConsumer, new ChatMessageSerializer(), myQueue);
        DistributedQueue messageQueue = builder.buildQueue();

        //DistributedQueue<String> distributedQueue = new DistributedQueue<String>(zkClient, "hell");
        // create watcher for the children of topic
        // create watcher for your queue
    }

    private void initSubscribers(String queuePath, String queueParent) throws Exception {
        subcriberListenerCache = zkManager.getPathChildrenCache(queuePath);
        PathChildrenCacheListener listener = (curatorFramework, event) -> {
            String eventNodeID = event.getData().getPath().substring(queueParent.length() + 1);
            if (eventNodeID.equals(nodeID)) {
                return;
            }
            switch (event.getType()) {
                case CHILD_ADDED:
                    break;
                case CHILD_REMOVED:
                    break;
                default:
                    break;
            }
        };
        subcriberListenerCache.getListenable().addListener(listener);
        subcriberListenerCache.start();
    }

    protected class ChatQueueConsumer implements QueueConsumer {

        @Override
        public void consumeMessage(Object o) throws Exception {

        }

        @Override
        public void stateChanged(CuratorFramework curatorFramework, ConnectionState connectionState) {

        }

        @Override
        public boolean doNotDecorate() {
            return false;
        }
    }

    protected class ChatMessageSerializer implements QueueSerializer {
        @Override
        public byte[] serialize(Object o) {
            if (o instanceof ChatMessage) {
                return SerializationUtils.serialize((ChatMessage) o);
            } else {
                return null;
            }
        }

        @Override
        public Object deserialize(byte[] bytes) {
            return SerializationUtils.deserialize(bytes);
        }
    }
}
