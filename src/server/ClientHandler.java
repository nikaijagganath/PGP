package server;

//Project Imports:
import protocol.*;

//Java Imports:
import java.io.*;
import java.util.logging.*;
import java.net.*;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.NoSuchPaddingException;
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

    private Asymmetric asym = new Asymmetric();
    private Symmetric sym = new Symmetric();
    Utils utils = new Utils();

    //Constructor:
    
    /**
     * Creates new client handler thread to relay messages.
     * @param server server that client connects to and thread services
     * @param clientSocket socket on client side linking server to client
     */
    public ClientHandler(Server server, Socket clientSocket) throws NoSuchAlgorithmException, NoSuchPaddingException {
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
            testCompression(ClientProtocol.getEncryp(m));
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
    private void processLogin(Message m) throws IOException {
        String name= ClientProtocol.getLoginMessageUsername(m);
        String password= ClientProtocol.getLoginMessagePassword(m);
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
    
    private void testHash(Message m, byte[]received){
        utils.compareHash(m, received);
    }
    private void test(Message m) throws Exception{
        Key key = utils.getPrivateKey("server.keys");
        byte[] cipher = ClientProtocol.getEncryp(m);
        asym.decrypt(key, cipher);
        System.out.println("Decrypted" + asym.getDecryptedText());
    }
    
    private void testCompression(byte[] message){
        System.out.println(new String(utils.decompress(message)));
    }
}