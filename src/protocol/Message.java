package protocol;

import java.io.*;
import java.security.Key;

/**
 * Message objects are used to send messages between client and server  sockets
 * and contain all relevant information to the message.
 */
public class Message implements Serializable{
    
    //Instance Variables:
    
    /**
     * The command type of the message.
     */
    String cmd;

    
    /**
     * The sender/receiver of a message.
     * Used in protocols to indicate receiver for message command, user name for login and new user commands.
     */
    String name;
    
    /**
     * The text of a message.
     * Used in protocols for text messages, password for login and new user commands.
     */
    String message;
    
    String type;
    
    byte[] encryptedMessageBytes;
    
    byte[] encryptedSharedKey;
    
    /**
     * Creates a message for sending between sockets.
     * @param cmd command
     * @param name name
     * @param type type
     * @param message message
     */
    public Message(String cmd, String name, String type, String message) {
        this.cmd = cmd;
        this.name = name;
        this.type = type;
        this.message = message;
    }
    
    /**
     * Creates the encrypted message to be sent.
     * @param cmd
     * @param name
     * @param encryptedMessageBytes
     * @param encryptedSharedKey 
     */
    public Message(String cmd, String name, byte[] encryptedMessageBytes, byte[] encryptedSharedKey) {
        this.cmd = cmd;
        this.name = name;
        this.encryptedMessageBytes = encryptedMessageBytes;
        this.encryptedSharedKey = encryptedSharedKey;
    }

    
}
