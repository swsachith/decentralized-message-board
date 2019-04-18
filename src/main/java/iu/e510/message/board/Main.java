package iu.e510.message.board;

import iu.e510.message.board.db.DBService;
import iu.e510.message.board.server.Server;

public class Main {
    public static void main(String[] args) throws Exception {
        String nodeID = "localhost:8088";
        DBService dbService = new DBService(nodeID);
        dbService.getConnection();
        Server server = new Server(nodeID);
        server.run();
    }
}
