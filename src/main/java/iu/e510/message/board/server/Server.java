package iu.e510.message.board.server;

import iu.e510.message.board.cluster.ClusterManager;
import iu.e510.message.board.cluster.data.SuperNodeDataManager;
import iu.e510.message.board.cluster.data.DataManagerImpl;
import iu.e510.message.board.tom.MessageService;
import iu.e510.message.board.tom.MessageServiceImpl;
import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.util.Config;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

public class Server {
    private String id;

    private Config config;

    private ClusterManager clusterManager;

    private SuperNodeDataManager superNodeDataManager;

    private MessageService messageService;

    private BlockingQueue<String> superNodeMsgQueue;
    private Map<String, Message> superNodeMsgs;


    public Server(String nodeID) throws Exception {

        this.id = nodeID;
        this.config = new Config();

        this.superNodeMsgQueue = new LinkedBlockingQueue<>();
        this.superNodeMsgs = new ConcurrentHashMap<>();

        this.messageService = new MessageServiceImpl("tcp://" + id, id, superNodeMsgQueue, superNodeMsgs);
        this.clusterManager = new ClusterManager(id, messageService);
        this.superNodeDataManager = new DataManagerImpl(id, messageService, superNodeMsgQueue, superNodeMsgs);


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
