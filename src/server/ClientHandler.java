package server;

//Project Imports:
import protocol.*;

//Java Imports:
import java.io.*;
import java.util.logging.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
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

    private Asymmetric asym;
    private Symmetric sym;
    Utils utils = new Utils();

    //Constructor:
    
    /**
     * Creates new client handler thread to relay messages.
     * @param server server that client connects to and thread services
     * @param clientSocket socket on client side linking server to client
     */
    public ClientHandler(Server server, Socket clientSocket) throws NoSuchAlgorithmException, NoSuchPaddingException {
        asym = new Asymmetric();
        sym = new Symmetric();
        asym.getKeys("server.keys");
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
            //test(m);
            //testCompression(ClientProtocol.getEncryp(m));
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
     * Checks for correct login command format then checks that user name and password match up and correspond to stored details.
     * If details are correct the login is allowed.
     * If details are incorrect the login is denied and user is informed.
     * @param m client message
     */
    private void processLogin(Message m) throws IOException, Exception {
        String name= ClientProtocol.getLoginMessageUsername(m);
        byte[] encryptedPassword= ClientProtocol.getLoginMessagePassword(m);
        byte[] encryptedSharedKey = ClientProtocol.getSharedKey(m);
        //Decrypt Password and SharedKey
        byte[] sharedKeyBytes= asym.decrypt(asym.getPrivateKey("WHATEVER"), encryptedSharedKey);
        Key sharedKey = new SecretKeySpec(sharedKeyBytes, 0, sharedKeyBytes.length, "AES");
        sym.setKey(sharedKey);
        System.out.println("shared key on server side: "+Base64.getEncoder().encodeToString(sharedKey.getEncoded()));
        
        //Using shared key to decrypt message
        byte[] messageBytes = sym.decrypt(sharedKey, encryptedPassword);
        System.out.println("message bytes  on server side = "+ Base64.getEncoder().encodeToString(messageBytes));
        
        //Decompress
        byte[] decompressedMessage = utils.decompress(messageBytes);
        System.out.println("decompressed message on server side = "+ Base64.getEncoder().encodeToString(decompressedMessage));
       
        
        //Deconcatenate 
        List<byte[]> password_hash = new ArrayList<>();
        password_hash = utils.deconcatenate(decompressedMessage);
        byte[] encryptedHash = password_hash.get(0);
        byte[] passwordBytes = password_hash.get(1);
        
        String s = Base64.getEncoder().encodeToString(password_hash.get(0));
        String t = new String (password_hash.get(1));
        System.out.println("encrypted hash on server side =" + s);
        System.out.println("plain message on server side =" + t);
        
        
        String password = new String (passwordBytes);
        
        //System.out.println("password "+new String(password,"UTF-8"));
        System.out.println("hashed password on server side = " + asym.ApplySHA256(password));
        
        
        // Need additional asymmetric object containing client keys --> FIX to get piublic keys from public.keys file
        //Compare hashes
        Asymmetric asymc = new Asymmetric();
        Key[] keysc = asymc.getKeys("client.keys");
        
        byte[] sentHash = asym.decrypt(keysc[0], encryptedHash);
        System.out.println("hash sent from client on server side = "+ Base64.getEncoder().encodeToString(sentHash) );
        boolean compare = asym.compare(password, new String(sentHash));
        System.out.println("compare hashes on server side = " + compare);


        
        System.out.println("User attempted login: " + name + " " + password);
        
        if (server.checkLogin(name, password)) {
            this.userName= name;        //set username
            System.out.println("User logged in succesfully: " + userName);
            Message msg= ServerProtocol.createResponseMessage(ClientProtocol.LOGIN_CMD, ServerProtocol.SUCCESS_MSG, "Successful Login");    //MESSAGE
            writer.writeObject(msg);
            
        } 
        else {  //incorrect login details
            Message msg= ServerProtocol.createResponseMessage(ClientProtocol.LOGIN_CMD, ServerProtocol.FAIL_MSG, "Incorrect username or password.");   //MESSAGE
            writer.writeObject(msg);    //MESSAGE
            System.err.println("ERROR: Login Failed. " + name + " " + new String(password));
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
     * Processes a message from client.
     * @param m client message
     * @throws IOException 
     */
    private void processMessage(Message m) throws IOException {
        String receiver= ClientProtocol.getMessageReceiver(m);
        String message= ClientProtocol.getMessageMessage(m);
        
        ClientHandler h = server.getHandler(receiver);
        if (h!=null) {  //receiver exists/is online
            Message msg = ServerProtocol.createDirectTextMessage("server@ "+message);   //MESSAGE
            h.send(msg);    //send message
            
            Message msg2= ServerProtocol.createResponseMessage(ClientProtocol.MESSAGE_CMD, ServerProtocol.SUCCESS_MSG, "Sent direct message.");   //MESSAGE
            send(msg2); //send success response message
            System.out.println("User, " + userName + ", sent '" + message +"' to server with reply back to " + receiver + " from server");
        }
        
        else {  //receiver doesn't exist/not online
            Message msg= ServerProtocol.createResponseMessage(ClientProtocol.MESSAGE_CMD, ServerProtocol.FAIL_MSG, receiver + ", not online. Could not send message: " + message);   //MESSAGE
            send(msg);  //send failed response message
            System.err.println("Error: User, " + userName + ", attempted to send message to server and then went offline");
        }        
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