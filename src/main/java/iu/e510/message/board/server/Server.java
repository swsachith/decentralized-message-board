package iu.e510.message.board.server;

import iu.e510.message.board.cluster.ClusterManager;
import iu.e510.message.board.cluster.data.DataAdapter;
import iu.e510.message.board.cluster.data.MapBasedDataAdapter;
import iu.e510.message.board.cluster.data.DistributedDataManager;
import iu.e510.message.board.tom.MessageService;
import iu.e510.message.board.tom.MessageServiceImpl;
import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.util.Config;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    private String id;

    private Config config;

    private ClusterManager clusterManager;

    /* This will be used to access the database level */
    private DataAdapter dataAdapter;

    /* this will be used to access data between the supernodes*/
    private DistributedDataManager dataManager;

    /* message service to handle comms within the supernodes */
    private MessageService messageService;

    private BlockingQueue<Message> internalMsgQueue;


    public Server(String nodeID) throws Exception {

        this.id = nodeID;
        this.config = new Config();

        this.internalMsgQueue = new LinkedBlockingQueue<>();

        this.dataAdapter = new MapBasedDataAdapter();

        this.messageService = new MessageServiceImpl("tcp://" + id, id,
                dataAdapter, internalMsgQueue);

        this.clusterManager = new ClusterManager(id, messageService);

        this.dataManager = new DistributedDataManager(id, messageService,
                dataAdapter, internalMsgQueue, clusterManager);

    }

    public void run() throws Exception {

        dataManager.addData("OH", "ohio".getBytes());

        dataManager.addData("IN", "indiana".getBytes());
        System.out.println();
        System.out.println();
        int i = 0;
        while (i < Integer.MAX_VALUE) {
            Thread.sleep(3000);
            i++;
        }
    }
}
