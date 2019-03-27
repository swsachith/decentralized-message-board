package iu.e510.message.board.cluster.zk;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

public class ZKConnection {

    private static CuratorFramework client;

    public static CuratorFramework getConnection(String connString, int maxRetryCount, int retryInitialWait,
                                                 int connectionTimeout, int sessionTimeout) {
        if (client == null) {
            CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder().connectString(connString)
                    .retryPolicy(new ExponentialBackoffRetry(retryInitialWait, maxRetryCount))
                    .connectionTimeoutMs(connectionTimeout).sessionTimeoutMs(sessionTimeout);
            client = builder.build();
            client.start();
        }
        return client;
    }

    public static void closeConnection() throws InterruptedException {
        if (client != null) {
            client.close();
        }
    }
}
