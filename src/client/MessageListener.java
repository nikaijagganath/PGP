package client;

/**
 * Interface for interacting with message listeners on client.
 */
public interface MessageListener  {
    
    /**
     * Used to process text message from server.
     * @param sender sender of message
     * @param messageBody text message body
     */
    public void onDirectMessage(String sender, String messageBody);
    
}
