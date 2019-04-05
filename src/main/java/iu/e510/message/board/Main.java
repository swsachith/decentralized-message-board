package iu.e510.message.board;

import iu.e510.message.board.cluster.ClusterManager;
import iu.e510.message.board.cluster.data.DataManager;
import iu.e510.message.board.cluster.data.DataManagerImpl;

public class Main {
    public static void main(String[] args) {
        String ip = "192.168.7.5";
        try {
            ClusterManager clusterManager = new ClusterManager(ip);
            DataManager dataManager = new DataManagerImpl(ip);
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
