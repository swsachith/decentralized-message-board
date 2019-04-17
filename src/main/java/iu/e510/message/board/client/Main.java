package iu.e510.message.board.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static final String POST = "post";
    public static final String REPLY = "reply";

    public static void main(String[] args) {
        String clientID = "sachith";
        ClientService clientService = new ClientServiceImpl(clientID);
        boolean post = clientService.post("bloomington", "weather", "it's nice outside");
        System.out.println(post);

        // message processing from the standard in
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