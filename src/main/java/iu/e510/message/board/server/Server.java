package iu.e510.message.board.server;

import iu.e510.message.board.cluster.ClusterManager;
import iu.e510.message.board.cluster.data.LocalDataManager;
import iu.e510.message.board.cluster.data.LocalDataManagerImpl;
import iu.e510.message.board.cluster.data.SuperNodeDataManager;
import iu.e510.message.board.cluster.data.SuperNodeDataManagerImpl;
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
    private LocalDataManager localDataManager;

    /* this will be used to access data between the supernodes*/
    private SuperNodeDataManager superNodeDataManager;

    /* message service to handle comms within the supernodes */
    private MessageService messageService;

    private BlockingQueue<Message> superNodeMsgQueue;


    public Server(String nodeID) throws Exception {

        this.id = nodeID;
        this.config = new Config();

        this.superNodeMsgQueue = new LinkedBlockingQueue<>();

        this.localDataManager = new LocalDataManagerImpl();

        this.messageService = new MessageServiceImpl("tcp://" + id, id,
                localDataManager, superNodeMsgQueue);

        this.clusterManager = new ClusterManager(id, messageService);

        this.superNodeDataManager = new SuperNodeDataManagerImpl(id, messageService,
                localDataManager, superNodeMsgQueue);

    }

    public void run() throws Exception {

        superNodeDataManager.addData("hapoi", "hi");

        superNodeDataManager.addData("IN", "hi");
        System.out.println();
        System.out.println();
        int i = 0;
        while (i < Integer.MAX_VALUE) {
            Thread.sleep(3000);
            i++;
        }
    }
}
