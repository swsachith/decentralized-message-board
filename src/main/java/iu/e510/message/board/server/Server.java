package iu.e510.message.board.server;

import iu.e510.message.board.cluster.ClusterManager;
import iu.e510.message.board.cluster.data.DataManager;
import iu.e510.message.board.cluster.data.DataManagerImpl;
import iu.e510.message.board.tom.MessageService;
import iu.e510.message.board.tom.MessageServiceImpl;
import iu.e510.message.board.util.Config;

public class Server {
    private String id;

    private Config config;

    private ClusterManager clusterManager;

    private DataManager dataManager;

    private MessageService messageService;


    public Server(String nodeID) throws Exception {

        this.id = nodeID;
        this.config = new Config();

        this.messageService = new MessageServiceImpl("tcp://" + id, id);
        this.clusterManager = new ClusterManager(id, messageService);
        this.dataManager = new DataManagerImpl(id);


    }

    public void run() throws Exception {

        dataManager.addData("hapoi", "hi");

        dataManager.addData("IN", "hi");
        System.out.println();
        System.out.println();
        int i = 0;
        while (i < Integer.MAX_VALUE) {
            Thread.sleep(3000);
            i++;
        }
    }
}
