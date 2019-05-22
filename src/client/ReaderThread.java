package client;

//Project Imports:
import protocol.*;

//Java Imports:
import java.io.*;
import java.net.SocketException;
import java.security.Key;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import security.Utils;

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
        } catch (Exception ex) {
            Logger.getLogger(ReaderThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    } 
    
    //Processing methods used in run:
    
    /**
     * Processes and decrypts the text message sent from the server.
     * @param m message received from server
     * @throws IOException 
     */
    public void processMessage(Message m) throws IOException, Exception {
        //String message = ServerProtocol.getTextMessageMessage(m);
        
        //PGP PROCEDURE ON CLIENT SIDE
        System.out.println("\n--START: PGP PROCEDURE ON CLIENT--\n");
        
        //Get the encrypted message
        byte[] encryptedMessage = ServerProtocol.getEncryptedMessage(m);
        System.out.println("Encrypted message on client side: "+Base64.getEncoder().encodeToString(encryptedMessage));

        //Use the obtained shared key to decrypt the compressed message
        byte[] compressedMessageBytes = client.getSymmetric().decrypt(client.getSymmetric().getKey(), encryptedMessage);
        System.out.println("Compressed message on client side: "+ Base64.getEncoder().encodeToString(compressedMessageBytes));

        //Decompress the message
        byte[] decompressedMessage = client.getUtils().decompress(compressedMessageBytes);
        System.out.println("Decompressed message on client side: "+ Base64.getEncoder().encodeToString(decompressedMessage));

        //Deconcatenate the message into the encrypted hash and the message text (in this case the password)
        List<byte[]> password_hash = new ArrayList<>();
        password_hash = client.getUtils().deconcatenate(decompressedMessage);
        byte[] encryptedHash = password_hash.get(0);
        byte[] messageBytes = password_hash.get(1);

        System.out.println("Encrypted hash on client side: " + Base64.getEncoder().encodeToString(password_hash.get(0)));
        System.out.println("Message text on client side: " + new String (password_hash.get(1)) );

        //Convert the bytes of the message text (password) into a String
        String message = new String (messageBytes);

        //Get the server's public key which is the  first key stored in the public.keys file
        Key clientPublic = Utils.getKeys("public.keys")[0];

        //Decrypt the hash sent from the client using the client's public key
        byte[] sentHash = client.getAsymmetric().decrypt(clientPublic, encryptedHash);
        System.out.println("Hash of message sent from client on client side: "+ new String(sentHash));

        //Hash the message on the server side
        System.out.println("Hash of message on client side: " + client.getAsymmetric().ApplySHA256(message));

        //Compare the two hashes obtained
        boolean compare = client.getAsymmetric().compare(message, new String(sentHash));
        System.out.println("Compare hashes on client side: " + compare);

        System.out.println("\n--END: PGP PROCEDURE ON CLIENT--\n");
        
        if (compare){
            client.messageListeners.forEach((listener) -> {
            listener.onDirectMessage("server", message);
            });
        }
        else{
            client.messageListeners.forEach((listener) -> {
                listener.onDirectMessage("server", "authentication error occured ");
            });
        }        
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
