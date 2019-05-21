package protocol;

import java.security.Key;


/**
 * Contains static methods used by client to create messages in the Protocol
 * format and used by the server to process messages from the client
 * according to the protocol format.
 */
public class ClientProtocol {
    
    //Constants:
    
    /**
     * Command for log off message.
     */
    public static final String LOGOFF_CMD= "logoff";
    
    /**
     * Command for log in message.
     */
    public static final String LOGIN_CMD= "login";
    
    /**
     * Command for adding new user.
     */
    public static final String NEW_USER_CMD= "newuser";
    
    /**
     * Command for sending a message.
     */
    public static final String MESSAGE_CMD= "msg";
    
    
    
    //Sending message on client to server: Creating messages.
    
    /**
     * Creates a login message.
     * @param userName username to login with
     * @param password password to login with
     * @return message to send to server
     */
    public static Message createLoginMessage(String userName, String password) {
        return new Message(LOGIN_CMD, userName, null, password);
    }
    
    /**
     * Creates a log off message.
     * @return message to send to server
     */
    public static Message createLogoffMessage() {
        return new Message(LOGOFF_CMD, null, null, null);
    }

    
    /**
     * Creates a message to send direct text message information.
     * @param receiver person to send message to
     * @param messageBody text message to send
     * @return 
     */
    public static Message createDirectTextMessage(String receiver, String messageBody) {
        return new Message(MESSAGE_CMD, receiver, null, messageBody);
    }
    
    //Receiving message on server from client:
    
    /**
     * Gets the command of message being received.
     * @param m client message 
     * @return command name
     */
    public static String getMessageCommand(Message m) {
        return m.cmd;
    }
    
    //Login messages:
    
    /**
     * Gets the user name from a login message.
     * @param m client message 
     * @return user name
     */
    public static String getLoginMessageUsername(Message m) {
        return m.name;
    }
    
    /**
     * Gets the password from a login message.
     * @param m client message 
     * @return password
     */
    public static String getLoginMessagePassword(Message m) {
        return m.message;
    }
    
    //Messages:
    
    /**
     * Gets receiver for message sent.
     * @param m client message 
     * @return receiver of message
     */
    public static String getMessageReceiver(Message m) {
        return m.name;
    }
    
    /**
     * Gets message for text message.
     * @param m client message 
     * @return message text
     */
    public static String getMessageMessage(Message m) {
        return m.message;
    }
    
    public static Key getKey(Message m) {
        return m.key;
    }
    
    public static byte[] getEncryp(Message m) {
        return m.enc;
    }
}
