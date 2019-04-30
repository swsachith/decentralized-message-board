package iu.e510.message.board.client;

import iu.e510.message.board.db.model.DMBPost;
import iu.e510.message.board.util.Config;
import iu.e510.message.board.util.Constants;

import java.util.List;
import java.util.Scanner;

public class Main {
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
            else if (tokens.length < 1) {
                //todo: add a comprehensive input format
                System.out.println("Wrong input format.");
                continue;
            }
            String method = tokens[0].trim().toLowerCase();
            processInput(clientService, tokens, method);
        }
        scanner.close();
    }

    private static void processInput(ClientService clientService, String[] tokens, String method) {
        boolean successful = false;
        boolean wrongFormat = false;
        switch (method) {
            case "post":
                if (tokens.length != 4) {
                    wrongFormat = true;
                    break;
                }
                successful = clientService.post(tokens[1].trim().toLowerCase(), tokens[2].trim(), tokens[3].trim());
                break;
            case "reply":
                if (tokens.length != 4) {
                    wrongFormat = true;
                    break;
                }
                successful = clientService.replyPost(tokens[1].trim().toLowerCase(), Integer.parseInt(tokens[2].trim()), tokens[3].trim());
                break;
            case "uppost":
                if (tokens.length != 3) {
                    wrongFormat = true;
                    break;
                }
                successful = clientService.upvotePost(tokens[1].trim().toLowerCase(), Integer.parseInt(tokens[2].trim()));
                break;
            case "downpost":
                if (tokens.length != 3) {
                    wrongFormat = true;
                    break;
                }
                successful = clientService.downvotePost(tokens[1].trim().toLowerCase(), Integer.parseInt(tokens[2].trim()));
                break;
            case "upreply":
                if (tokens.length != 4) {
                    wrongFormat = true;
                    break;
                }
                successful = clientService.upvoteReply(tokens[1].trim().toLowerCase(), Integer.parseInt(tokens[2].trim()), Integer.parseInt(tokens[3].trim()));
                break;
           case "downreply":
               if (tokens.length != 4) {
                   wrongFormat = true;
                   break;
               }
                successful = clientService.downvoteReply(tokens[1].trim().toLowerCase(), Integer.parseInt(tokens[2].trim()), Integer.parseInt(tokens[3].trim()));
                break;
            case "getposts":
                if (tokens.length != 2) {
                    wrongFormat = true;
                    break;
                }
                List<DMBPost> posts = clientService.getPosts(tokens[1].trim().toLowerCase());
                for (DMBPost post : posts) {
                    System.out.println(post);
                }
                break;
            case "getpost":
                if (tokens.length != 3) {
                    wrongFormat = true;
                    break;
                }
                DMBPost post = clientService.getPost(tokens[1].trim().toLowerCase(), Integer.parseInt(tokens[2].trim()));
                if (post != null) {
                    System.out.println(post.getFullPostString());
                } else {
                    System.out.println("No such post found!");
                }
                break;
            default:
                break;
        }
        if (!(method.equals("getposts") || method.equals("getpost"))) {
            if (successful) {
                System.out.println("The request was successfully executed");
            } else {
                System.out.println("Posting failed. Please try again");
            }
        }
        if (wrongFormat) {
            System.out.println("The input is in a wrong format. Please try again.");
        }
    }
}