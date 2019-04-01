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
    public static final String NUM_REPLICAS = "NUM_REPLICAS";
    public static final String DATA_LOCATION = "DATA_LOCATION";
}
