package server;

//Project Imports:
import protocol.*;

//Java Imports:
import java.io.*;
import java.util.logging.*;
import java.net.*;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import security.*;

/**
 * ClientHandler thread processes all commands sent from client to server,
 * relaying messages and updating client status.
 */
public class ClientHandler extends Thread {
    
    //Instance Variables:
    
    /**
     * Server which the handler thread services.
     */
    private Server server;
    
    /**
     * Socket on client who's communications this thread is managing.
     */
    private Socket clientSocket;
    
    
    /**
     * Input stream for server socket (reader) which reads in client messages.
     */
    private ObjectInputStream reader;   
    
    /**
     * Output stream for server socket (writer) which writes messages to client.
     */
    private ObjectOutputStream writer;  
    
    
    /**
     * User name of client that this thread manages.
     */
    private String userName;

    /**
     * Object for asymmetric RSA encryption/decryption  
     */
    private Asymmetric asym;
    
    /**
     * Object for symmetric AES encryption/decryption  
     */
    private Symmetric sym;
    
    /**
     * Object containing utility functions needed for encryption/decryption purposes
     */
    private Utils utils;

    
    //Constructor:
    
    /**
     * Creates new client handler thread to relay messages.
     * @param server server that client connects to and thread services
     * @param clientSocket socket on client side linking server to client
     * @throws java.security.NoSuchAlgorithmException
     * @throws javax.crypto.NoSuchPaddingException
     * @throws java.io.IOException
     */
    public ClientHandler(Server server, Socket clientSocket) throws NoSuchAlgorithmException, NoSuchPaddingException, IOException {
        
        //Initialise encryption objects
        asym = new Asymmetric("server.keys");
        //asym.getKeys("server.keys");
        
        sym = new Symmetric();
        
        //Create utils object
        utils = new Utils();
        
        this.server = server;
        this.clientSocket = clientSocket;
        userName = null;
        try {
            OutputStream os = clientSocket.getOutputStream();
            writer = new ObjectOutputStream(os);   //MESSAGE
            InputStream is = clientSocket.getInputStream();
            reader = new ObjectInputStream(is); //MESSAGE
        } 
        catch (IOException ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    
    //Run method:
    
    @Override
    /**
     * Starts processing client commands/messages.Reads in messages/commands
     * from client and processes them according to chat protocols.
     * @throws IOException
     */
    public void run() {
        try {
            Message m = (Message) reader.readObject();   //MESSAGE
            String command;
            ONLINE:
            while (m!= null){
                command = ClientProtocol.getMessageCommand(m);
                switch (command) 
                    {
                    case ClientProtocol.LOGIN_CMD:  //received login message
                        processLogin(m);
                        break;
                    case ClientProtocol.LOGOFF_CMD: //received logoff message
                        processLogoff();
                        break ONLINE;
                    case ClientProtocol.MESSAGE_CMD:    //received message
                        processMessage(m);
                        break;
                    default:    //received unknown command
                        Message msg = ServerProtocol.createErrorMessage(command, ServerProtocol.UNKNOWN_CMD_ERR, "Unknown Command!"); //MESSAGE
                        writer.writeObject(msg);    //MESSAGE
                        System.err.println("ERROR: Unkown command, " + command + ", from user, " + userName);
                        break;
                    }
                writer.flush();
                m = (Message) reader.readObject();
                //test(m);
            }
            clientSocket.close();
        }
        
        catch (IOException | ClassNotFoundException ex) {
            System.out.println("Error: " + ex);
        } catch (Exception ex) {
            Logger.getLogger(ClientHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Checks for correct login command format then checks that user name and decrypted password match up and correspond to stored details.
     * The password is also authenticated.
     * If details are correct the login is allowed.
     * If details are incorrect the login is denied and user is informed.
     * @param m client message
     */
    private void processLogin(Message m) throws IOException, Exception {
        
        //PGP PROCEDURE ON SERVER SIDE
        System.out.println("\n--START: PGP PROCEDURE ON SERVER--\n");
        
        //Get the encrypted message
        byte[] encryptedMessage = ClientProtocol.getEncryptedMessage(m);
        System.out.println("Encrypted message on server side: "+Base64.getEncoder().encodeToString(encryptedMessage));
        
        //Get the encrypted shared key
        byte[] encryptedSharedKey = ClientProtocol.getEncryptedSharedKey(m);
        System.out.println("Encrypted shared key on server side: "+Base64.getEncoder().encodeToString(encryptedSharedKey));
        
        //Decrypt sharedKey using the server's private key
        byte[] sharedKeyBytes = asym.decrypt(asym.getPrivateKey(), encryptedSharedKey);
        Key sharedKey = new SecretKeySpec(sharedKeyBytes, 0, sharedKeyBytes.length, "AES");
        sym.setKey(sharedKey);
        System.out.println("Shared key on server side: "+Base64.getEncoder().encodeToString(sharedKey.getEncoded()));
        
        //Use the obtained shared key to decrypt the compressed message
        byte[] messageBytes = sym.decrypt(sharedKey, encryptedMessage);
        System.out.println("Compressed message on server side: "+ Base64.getEncoder().encodeToString(messageBytes));
        
        //Decompress the message
        byte[] decompressedMessage = utils.decompress(messageBytes);
        System.out.println("Decompressed message on server side: "+ Base64.getEncoder().encodeToString(decompressedMessage));
        
        //Deconcatenate the message into the encrypted hash and the message text (in this case the password)
        List<byte[]> password_hash = new ArrayList<>();
        password_hash = utils.deconcatenate(decompressedMessage);
        byte[] encryptedHash = password_hash.get(0);
        byte[] passwordBytes = password_hash.get(1);
        
        System.out.println("Encrypted hash on server side: " + Base64.getEncoder().encodeToString(password_hash.get(0)));
        System.out.println("Message text on server side: " + new String (password_hash.get(1)) );
        
        //Convert the bytes of the message text (password) into a String
        String password = new String (passwordBytes);
        
        //Get the client's public key which is the  second key stored in the public.keys file
        Key clientPublic = Utils.getKeys("public.keys")[1];
        
        //Decrypt the hash sent from the client using the client's public key
        byte[] sentHash = asym.decrypt(clientPublic, encryptedHash);
        System.out.println("Hash of message sent from client on server side: "+ new String(sentHash));//Base64.getEncoder().encodeToString(sentHash));
        
        //Hash the message on the server side
        System.out.println("Hash of message on server side: " + asym.ApplySHA256(password));
        
        //Compare the two hashes obtained
        boolean compare = asym.compare(password, new String(sentHash));
        System.out.println("Compare hashes on server side: " + compare);

        System.out.println("\n--END: PGP PROCEDURE ON SERVER--\n");
        
        //Check if login is valid and authenticated
        String name = ClientProtocol.getLoginMessageUsername(m);
        
        System.out.println("User attempted login: " + name + " " + password);
        
        if (server.checkLogin(name, password) && compare) {
            this.userName= name;        //set username
            System.out.println("User logged in succesfully: " + userName);
            Message msg= ServerProtocol.createResponseMessage(ClientProtocol.LOGIN_CMD, ServerProtocol.SUCCESS_MSG, "Successful Login");    //MESSAGE
            writer.writeObject(msg);
            
        } 
        else {  //incorrect login details
            Message msg= ServerProtocol.createResponseMessage(ClientProtocol.LOGIN_CMD, ServerProtocol.FAIL_MSG, "Incorrect username or password.");   //MESSAGE
            writer.writeObject(msg);    //MESSAGE
            System.err.println("ERROR: Login Failed. " + name + " " + password);
        }
    }
    
    //Methods to process clients messages:
    
    /**
     * Processes a client's logging off by removing the client's corresponding handler thread from the server's list of handlers.
     * The client socket is then closed.
     * @throws IOException 
     */
    private void processLogoff() throws IOException {
        server.removeHandler(this); //remove clientHandler thread associated with this client
        
        writer.close();
        reader.close();
        clientSocket.close();
        System.out.println("Closed client connection: " + clientSocket.toString());
    }
    
    
    /**
     * Processes a message from client using the PGP security procedure for authentication and confidentiality.
     * @param m client message
     * @throws IOException 
     */
    private void processMessage(Message m) throws IOException, Exception {
        
        String receiver= ClientProtocol.getMessageReceiver(m);
        
        ClientHandler h = server.getHandler(receiver);
        if (h!=null) {  //receiver exists/is online
            
            //PGP PROCEDURE ON SERVER SIDE
            System.out.println("\n--START: PGP PROCEDURE ON SERVER--\n");

            //Get the encrypted message
            byte[] encryptedMessage = ClientProtocol.getEncryptedMessage(m);
            System.out.println("Encrypted message on server side: "+Base64.getEncoder().encodeToString(encryptedMessage));

            //Use the obtained shared key to decrypt the compressed message
            byte[] compressedMessageBytes = sym.decrypt(sym.getKey(), encryptedMessage);
            System.out.println("Compressed message on server side: "+ Base64.getEncoder().encodeToString(compressedMessageBytes));

            //Decompress the message
            byte[] decompressedMessage = utils.decompress(compressedMessageBytes);
            System.out.println("Decompressed message on server side: "+ Base64.getEncoder().encodeToString(decompressedMessage));

            //Deconcatenate the message into the encrypted hash and the message text (in this case the password)
            List<byte[]> password_hash = new ArrayList<>();
            password_hash = utils.deconcatenate(decompressedMessage);
            byte[] encryptedHash = password_hash.get(0);
            byte[] messageBytes = password_hash.get(1);

            System.out.println("Encrypted hash on server side: " + Base64.getEncoder().encodeToString(password_hash.get(0)));
            System.out.println("Message text on server side: " + new String (password_hash.get(1)) );

            //Convert the bytes of the message text (password) into a String
            String message = new String (messageBytes);

            //Get the client's public key which is the  second key stored in the public.keys file
            Key clientPublic = Utils.getKeys("public.keys")[1];

            //Decrypt the hash sent from the client using the client's public key
            byte[] sentHash = asym.decrypt(clientPublic, encryptedHash);
            System.out.println("Hash of message sent from client on server side: "+ new String(sentHash));

            //Hash the message on the server side
            System.out.println("Hash of message on server side: " + asym.ApplySHA256(message));

            //Compare the two hashes obtained
            boolean compare = asym.compare(message, new String(sentHash));
            System.out.println("Compare hashes on server side: " + compare);

            System.out.println("\n--END: PGP PROCEDURE ON SERVER--\n");
            
            if (compare) {
                //Create encrypted message from server
                Message msg = createPGPResponse("server@"+message);
                //Message msg = ServerProtocol.createDirectTextMessage("server@ "+message);   //MESSAGE
                h.send(msg);    //send message

                Message msg2= ServerProtocol.createResponseMessage(ClientProtocol.MESSAGE_CMD, ServerProtocol.SUCCESS_MSG, "Sent direct message.");   //MESSAGE
                send(msg2); //send success response message
                System.out.println("User, " + userName + ", sent '" + message +"' to server with reply back to " + receiver + " from server");
            }
            
        }
        
        else {  //receiver doesn't exist/not online
            Message msg= ServerProtocol.createResponseMessage(ClientProtocol.MESSAGE_CMD, ServerProtocol.FAIL_MSG, receiver + ", not online. Could not send message.");   //MESSAGE
            send(msg);  //send failed response message
            System.err.println("Error: User, " + userName + ", attempted to send message to server and then went offline");
        }        
    }
    
    private Message createPGPResponse(String message) throws NoSuchAlgorithmException, Exception{
        
        //PGP PROCEDURE ON CLIENT SIDE
        System.out.println("\n--START: PGP PROCEDURE ON SERVER--\n");
        
        //Message on client side
        System.out.println("Message on server side: "+message);
        
        //Create a hash of the message (in this case the password)
        String hash = asym.ApplySHA256(message);
        System.out.println("Hash of message on server side: "+hash);
        
        //Encrypt the hash using the server's private key
        byte[] encryptedHash = asym.encrypt(asym.getPrivateKey(), hash.getBytes());
        System.out.println("Encrypted hash on server side: "+ Base64.getEncoder().encodeToString(encryptedHash));
        
        //Concatenate the encrypted hash with the text to be sent to form a message
        byte[] concatenated = utils.concatenate(message.getBytes(), encryptedHash);
        System.out.println("Concatenated message on server side: "+ Base64.getEncoder().encodeToString(concatenated));
        
        //Compress the message using zip compression
        byte[] zip = utils.compress(concatenated);
        System.out.println("Compressed message on server side: "+ Base64.getEncoder().encodeToString(zip));

        //Encrypt the compressed message with the shared key
        byte[] encryptedMessage = sym.encrypt(sym.getKey(), zip);
        System.out.println("Encrypted compressed message using shared key on server side: "+Base64.getEncoder().encodeToString(encryptedMessage));
        
        System.out.println("\n--END: PGP PROCEDURE ON SERVER--\n");
        
        Message m = ServerProtocol.createDirectTextMessage(encryptedMessage);
        
        return m;
    }
    
    
    //Helper methods:
    
    /**
     * Sends message to client if client is still logged in.
     * @param m message to send
     */
    void send(Message m) throws IOException {
        if (userName!= null) {   //logged in
            writer.writeObject(m);
        }
    }
    
    /**
     * Gets client's user name.
     * @return client's user name
     */
    
    public String getUserName() {
        return userName;
    }

}