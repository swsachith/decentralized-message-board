package iu.e510.message.board;

import iu.e510.message.board.server.Server;

public class Main {
    public static void main(String[] args) throws Exception {
        String nodeID = "localhost:8086";

        Server server = new Server(nodeID);
        server.run();
    }
}
