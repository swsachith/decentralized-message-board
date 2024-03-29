package iu.e510.message.board.util;

public class Constants {
    public static final String CONFIG_FILE_NAME = "config.properties";

    // Communication parameters
    public static final String HOST_IP = "HOST_IP";
    public static final String HOST_PORT = "HOST_PORT";

    // Message Service Parameters
    public static final String SEND_TIMEOUT = "SEND_TIMEOUT";

    // ZooKeeper Constants
    public static final String ZK_CONN_STRING = "ZK_CONN_STRING";
    public static final String ZK_CONN_TIMEOUT = "ZK_CONN_TIMEOUT";
    public static final String ZK_CONN_MAXRETRY_COUNT = "ZK_CONN_MAXRETRY_COUNT";
    public static final String ZK_CONN_RETRY_INIT_WAIT = "ZK_CONN_RETRY_INIT_WAIT";
    public static final String ZK_SESSION_TIMEOUT = "ZK_SESSION_TIMEOUT";

    // Cluster configurations
    public static final String CLUSTER_RING_LOCATION = "CLUSTER_RING_LOCATION";
    public static final String NODE_REPLICAS = "NODE_REPLICAS";
    public static final String DATA_REPLICAS = "DATA_REPLICAS";
    public static final String DATA_LOCATION = "DATA_LOCATION";
    public static final String LEADER_PATH = "LEADER_PATH";
    public static final String LEADER_ELECTION_DELAY = "LEADER_ELECTION_DELAY";

    // RMI Registry configs
    public static final String RMI_REGISTRY_HOST = "RMI_REGISTRY_HOST";
    public static final String RMI_REGISTRY_PORT = "RMI_REGISTRY_PORT";

    // Client configs
    public static final String SUPER_NODE_LIST = "SUPER_NODE_LIST";
    public static final String CLIENT_ID = "CLIENT_ID";
    public static final String SUPER_NODE_RETRIES = "SUPER_NODE_RETRIES";

    // Database configurations
    public static final String DB_DRIVER = "DB_DRIVER";
    public static final String DB_CONNECTION_PREFIX = "DB_CONNECTION_PREFIX";
    public static final String DB_USER = "DB_USER";
    public static final String DB_PASSWORD = "DB_PASSWORD";
    public static final String CLEAN_DB_COMMAND = "DROP ALL OBJECTS;";
}