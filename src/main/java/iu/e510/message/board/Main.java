package iu.e510.message.board;

import iu.e510.message.board.cluster.ClusterManager;
import iu.e510.message.board.cluster.data.DataManager;
import iu.e510.message.board.cluster.data.DataManagerImpl;
import iu.e510.message.board.tom.MessageService;
import iu.e510.message.board.tom.MessageServiceImpl;
import iu.e510.message.board.util.Config;

public class Main {
    public static void main(String[] args) {
        String id = "localhost:8085";
        Config config = new Config();
        try {
            MessageService messageService = new MessageServiceImpl("tcp://" + id, id);
            ClusterManager clusterManager = new ClusterManager(id, messageService);
            DataManager dataManager = new DataManagerImpl(id);
            dataManager.addData("hapoi","hi");
            dataManager.addData("IN","hi");
            System.out.println();
            System.out.println();
            int i = 0;
            while (i < Integer.MAX_VALUE) {
                Thread.sleep(3000);
                i++;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
