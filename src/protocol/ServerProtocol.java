package protocol;


/**
 * Contains static methods used by server to create messages in the protocol
 * format and used by the client to process messages from the server
 * according to the protocol format.
 * @author talajross
 */
public class ServerProtocol {
    
    //Constants:
    
    /**
     * Command for online message.
     */
    public static final String ONLINE_CMD= "online";
    
    /**
     * Command for offline message.
     */
    public static final String OFFLINE_CMD= "offline";
    
    /**
     * Command for sending message.
     */
    public static final String MESSAGE_CMD= "msg";
    
    /**
     * Command for error message.
     */
    public static final String ERROR_CMD= "err";
    
    /**
     * Command for response message.
     */
    public static final String RESPONSE_CMD= "confirm";
    
    
    
    /**
     * Response to successful client command.
     */
    public static final String SUCCESS_MSG= "success";
    
    /**
     * Response to failed client command.
     */
    public static final String FAIL_MSG= "fail";
    
    
    
    /**
     * Error type for unknown command.
     */
    public static final String UNKNOWN_CMD_ERR= "unknown";
    
    
    //Sending message on server to client:
    
    /**
     * Creates error message.
     * @param cmd command from client which caused error
     * @param type error type
     * @param errMessage message from server
     * @return server message
     */
    public static Message createErrorMessage(String cmd, String type, String errMessage) {
        return new Message(ERROR_CMD, cmd, type, errMessage);
    }
    
    /**
     * Creates response message.
     * @param cmd command from client which caused response
     * @param type response type
     * @param message message from server
     * @return server message
     */
    public static Message createResponseMessage(String cmd, String type, String message) {
        return new Message(RESPONSE_CMD, cmd, type, message);
    }
    
    /**
     * Creates message for sending direct text message.
     * @param encryptedMessage text to send
     * @return server message
     */
    public static Message createDirectTextMessage(byte [] encryptedMessage) {
        return new Message(MESSAGE_CMD,  null, encryptedMessage, null);
    }

    
    //Receiving message on client from server:
    
    /**
     * Gets message command.
     * @param m server message
     * @return name of command
     */
    public static String getMessageCommand(Message m) {
        return m.cmd;
    }
    
    //Error messages:
    
    /**
     * Checks if it's an error message.
     * @param m server message
     * @return true if is error message and false otherwise
     */
    public static boolean isErrorMessage(Message m) {
        String cmd= m.cmd;
        return cmd.equals(ERROR_CMD);
    }
    
    /**
     * Gets command which caused error.
     * @param m server message
     * @return command name
     */
    public static String getErrorMessageCommand(Message m) {
        return m.name;
    }
    
    /**
     * Gets type of error.
     * @param m server message
     * @return error type
     */
    public static String getErrorMessageType(Message m) {
        return m.type;
    }
    
    /**
     * Gets error message.
     * @param m server message
     * @return error message
     */
    public static String getErrorMessageMessage(Message m) {
        return m.message;
    }
    
    //Response messages:
    
    /**
     * Gets response message causing command.
     * @param m server message
     * @return command
     */
    public static String getResponseMessageCommand(Message m) {
        return m.name;
    }
    
    /**
     * Gets response type.
     * @param m server message
     * @return response type
     */
    public static String getResponseMessageType(Message m) {
        return m.type;
    }
    
    /**
     * Gets response message.
     * @param m server message
     * @return response message
     */
    public static String getResponseMessageMessage(Message m) {
        return m.message;
    }
    
    /**
     * Checks if response is a success.
     * @param m server message
     * @return true if success and false if failed
     */
    public static boolean isSuccessResponse(Message m) {
        String type= m.type;
        return type.equals(SUCCESS_MSG);
    }
    
    
    /**
     * Gets text of text message.
     * @param m server message
     * @return message body
     */
    /*
    public static String getTextMessageMessage(Message m) {
        return m.message;
    }*/
    
    public static byte[] getEncryptedMessage(Message m) {
        return m.encryptedMessageBytes;
    }
}
