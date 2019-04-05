package iu.e510.message.board.tom;

import java.util.List;

public interface MessageService {

    /**
     * Sends the message using totally ordered multicast.
     * @param message
     * @param recipients
     */
    void send_ordered(String message, List<String> recipients);

    /**
     * Point to Point message passing.
     * @param message
     * @param recipient
     */
    void send_unordered(String message, String recipient);

    /**
     * Stop the Message Service.
     */
    void stop_service();

    /**
     * Get the connection url for a given port and ip (id)
     */
    String getUrl(String id);
}
