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
    
    Key key;
    
    byte[] enc;
    
    byte[] mess;
    
    byte[] sharedKey;
    
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
    
    public Message(String cmd, String name, String type, String message, Key key, byte[] enc) {
        this.cmd = cmd;
        this.name = name;
        this.type = type;
        this.message = message;
        this.key = key;
        this.enc = enc;
    }
    
    public Message(String cmd, String name, String type, byte[] mess, byte[] sharedKey) {
        this.cmd = cmd;
        this.name = name;
        this.type = type;
        this.mess = mess;
        this.sharedKey = sharedKey;
    }
    
    public void setTest(Key key, byte[] enc){
        this.key = key;
        this.enc = enc;
    }
    

    
}
