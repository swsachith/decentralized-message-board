package iu.e510.message.board;

import iu.e510.message.board.server.Server;

public class Main {
    public static void main(String[] args) throws Exception {
        String nodeID = "localhost:8083";

        Server server = new Server(nodeID);
        server.run();
    }
}
