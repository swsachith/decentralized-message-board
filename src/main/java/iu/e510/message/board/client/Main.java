package iu.e510.message.board.client;

public class Main {
    public static void main(String[] args) {
        String clientID = "sachith";
        ClientService clientService = new ClientServiceImpl(clientID);
        boolean post = clientService.post("bloomington", "weather", "it's nice outside");
        System.out.println(post);
    }
}