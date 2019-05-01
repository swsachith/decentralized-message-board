package iu.e510.message.board.tom;

import iu.e510.message.board.tom.common.Message;
import iu.e510.message.board.tom.common.payloads.Payload;
import iu.e510.message.board.tom.core.MessageHandler;

import java.util.Set;

public interface MessageService {

    /**
     * Sends the message using totally ordered multicast.
     * @param message
     * @param recipients
     */
    void send_ordered(Payload message, Set<String> recipients);

    /**
     * Point to Point message passing.
     * @param message
     * @param recipient
     * @return The response to the message
     */
    Message send_unordered(Payload message, String recipient);

    /**
     * Stop the Message Service.
     */
    void stop_service();

    /**
     * Get the connection url for a given port and ip (id)
     */
    String getUrl(String id);

    /**
     * Set the message handler to process the messages
     */
    void init(MessageHandler messageHandler);
}
