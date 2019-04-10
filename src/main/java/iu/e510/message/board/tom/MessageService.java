package iu.e510.message.board.tom;

import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.tom.common.MessageType;
import iu.e510.message.board.tom.common.Payload;

import java.util.List;

public interface MessageService {

    /**
     * Sends the message using totally ordered multicast.
     * @param message
     * @param recipients
     */
    void send_ordered(String message, List<String> recipients, MessageType messageType);

    /**
     * Point to Point message passing.
     * @param message
     * @param recipient
     * @return The response to the message
     */
    Message send_unordered(Payload message, String recipient, MessageType messageType);

    /**
     * Stop the Message Service.
     */
    void stop_service();

    /**
     * Get the connection url for a given port and ip (id)
     */
    String getUrl(String id);
}
