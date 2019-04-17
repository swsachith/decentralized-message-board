package iu.e510.message.board.client;

import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static final String POST = "post";
    public static final String REPLY = "reply";

    public static void main(String[] args) {
        Config config = new Config();
        String clientID = config.getConfig(Constants.CLIENT_ID);
        // todo: remove this when building the final jar, this is for testing only
        clientID = "sachith";

        ClientService clientService = new ClientServiceImpl(clientID);

        // input processing from the standard in
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNextLine()) {
            String[] tokens = scanner.nextLine().split(",");
            if (tokens.length == 1 && tokens[0].equals("stop")) {
                System.exit(0);
            }
            else if (tokens.length < 3) {
                System.out.println("Wrong input format. Correct Format: <unicast/multicast>, message, recipient");
                continue;
            }
            String method = tokens[0];
            switch (method) {
                case POST:
                    boolean result = clientService.post(tokens[1].trim(), tokens[2].trim(), tokens[3].trim());
                    System.out.println(result);
                    break;
                case REPLY:
                    break;
                default:
                    break;
            }
        }
        scanner.close();
    }
}