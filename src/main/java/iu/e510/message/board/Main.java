package iu.e510.message.board;

import iu.e510.message.board.db.DBService;
import iu.e510.message.board.server.Server;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;

public class Main {
    public static void main(String[] args) throws Exception {

        Options options = new Options();

        options.addOption("host", true, "host");
        options.addOption("port", true, "port");

        CommandLineParser commandLineParser = new DefaultParser();
        CommandLine cmd = commandLineParser.parse(options, args);

        String host = cmd.getOptionValue("host", "localhost");
        String port = cmd.getOptionValue("port", "8083");
        String nodeID = String.format("%s:%s", host, port);

        DBService dbService = new DBService(nodeID);
        dbService.getConnection();
        Server server = new Server(nodeID);
        server.run();
    }
}
