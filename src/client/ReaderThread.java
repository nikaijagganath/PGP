package client;

//Project Imports:
import protocol.*;

//Java Imports:
import java.io.*;
import java.net.SocketException;

/**
 * Thread which processes incoming server messages for a client.
 */
public class ReaderThread extends Thread {
    
    //Instance Variables:
    
    /**
     * Client which thread is processing messages for.
     */
    Client client;
    
    /**
     * Input stream of client, which is receiving messages from server.
     */
    ObjectInputStream reader;  //MESSAGE
    
    
    
    //Constructor:
    
    /**
     * Creates new reader thread for client to process incoming server messages.
     * @param client
     * @throws IOException 
     */
    public ReaderThread(Client client) throws IOException {
        this.client= client;
        reader= client.getReader();
    }
    
    
    
    //Run Method:
    
    /**
     * Runs thread, constantly reading in server messages and processing them
     * according to message type.
     */
    @Override
    public void run(){
        try {
            Message m = (Message) reader.readObject();  //MESSAGE
            
            while (m!= null){
                String command= ServerProtocol.getMessageCommand(m);
                switch (command) {
                    case ServerProtocol.MESSAGE_CMD: //Receive message
                        processMessage(m);
                        break;
                    case ServerProtocol.RESPONSE_CMD: //Receive server response to previous command
                        processResponse(m);
                        break;
                    case ServerProtocol.ERROR_CMD: //Receive error response to previous command
                        processError(m);
                        break;
                    default: //Unkown message type
                        System.err.println("Unknown Server Message: " + command);
                        break;
                }   
                m = (Message) reader.readObject();  //MESSAGE
            }
        } 
        catch (SocketException ex){
            System.out.println("Logged of: " + ex);
        } 
        catch (ClassNotFoundException | IOException ex){
            System.out.println("Exception: " + ex);
        }
    } 
    
    //Processing methods used in run:
    
    /**
     * Processes text message.
     * @param m message received from server
     * @throws IOException 
     */
    public void processMessage(Message m) throws IOException {
        //String sender= ServerProtocol.getDirectMessageSender(m);
        String message= ServerProtocol.getTextMessageMessage(m);
        client.messageListeners.forEach((listener) -> {
            listener.onDirectMessage("server", message);
        });        
    }
    
    /**
     * Process error response message from server.
     * @param m message received from server
     */
    public void processError(Message m) {
        String errCommand= ServerProtocol.getErrorMessageCommand(m);
        String errType= ServerProtocol.getErrorMessageType(m);
        String errMessage= ServerProtocol.getErrorMessageMessage(m);
        client.serverResponseListeners.forEach((listener) -> {
            listener.onError(errCommand, errType, errMessage);
        });
    }
    
    /**
     * Process response message from server.
     * @param m message received from server
     */
    public void processResponse(Message m) {
        String initialCommand= ServerProtocol.getResponseMessageCommand(m);
        String respType= ServerProtocol.getResponseMessageType(m);
        String message= ServerProtocol.getResponseMessageMessage(m);
        client.serverResponseListeners.forEach((listener) -> {
            listener.onResponse(initialCommand, respType, message);
        });
    }
    
}
