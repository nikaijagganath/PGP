package client;

//Project Imports:
import protocol.*;
import constants.*;

//Java Imports:
import java.io.*;
import java.net.*;
import java.util.*;

public class Client {
    
    //Instance Variables:
    
    /**
     * Name/IP address of the server socket.(Either "localhost" or
     * "IP_address>").
     */
    private final String serverName;
    
    /**
     * Port number on which server is listening.
     */
    private final int portNo;
    
    /**
     * This client's socket which is used to communicate with the server.
     */
    private final Socket clientSocket;
    
    /**
     * This client's username.
     */
    private String userName;
    
    /**
     * List of threads used to process all incoming messages from the server
     * socket(usually only 1).
     */
    private ArrayList<Thread> readerThreadList;
    
    
    /**
     * Stream used to read in messages from the server.
     */
    private final ObjectOutputStream writer;
    
    /**
     * Stream used to write messages to the server.
     */
    private final ObjectInputStream reader;
    
    /**
     * List of message listeners, checking for messages from the server.
     */
    ArrayList<MessageListener> messageListeners;
    
    /**
     * List of server response listeners, checking for success/fail responses from server.
     */
    ArrayList<ServerResponseListener> serverResponseListeners; 
    
    
    //Constructor:
    
    /**
     * Creates a new client, by connecting to server, setting up streams and
     * creating listener lists and message storage map.
     * @param serverName name/IP address of the server
     * @param portNo the port number on which server is listening
     * @throws IOException 
     */
    public Client(String serverName, int portNo) throws IOException {
        //Connect to server:
        this.serverName= serverName;
        this.portNo= portNo;
        clientSocket= new Socket(serverName, portNo);
        System.out.println("Client port is: " + clientSocket.getLocalPort());
        
        //Set up streams:
        InputStream is= clientSocket.getInputStream();
        reader= new ObjectInputStream(is); //MESSAGE
        OutputStream os= clientSocket.getOutputStream();
        writer= new ObjectOutputStream(os);   //MESSAGE
        
        //Create listener lists:
        messageListeners = new ArrayList<>();
        serverResponseListeners = new ArrayList<>();

    }

    
    //Methods used by GUI:
    
    /**
     * Starts new thread for reading in and processing messages from server.
     * @throws IOException 
     */
    public void startReaderThread() throws IOException {
        Thread readerThread= new ReaderThread(this);
        readerThreadList= new ArrayList<>();
        readerThreadList.add(readerThread);
        readerThread.start();
    }
    
    /**
     * Checks username format given and then checks for correct corresponding
     * details at server, processing login if all details are correct.
     * @param userName user name of user to log in
     * @param password password of user to log in
     * @return true if log in successfully otherwise false
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public boolean login(String userName, String password) throws IOException, ClassNotFoundException  {
        //Incorrect formatting:    
        if(userName.contains(" ") || userName.contains("@") || userName.contains("#")) {
            return false;
        }
        //Correct formatting:
        else {
            Message msg= ClientProtocol.createLoginMessage(userName, password);
            sendMessage(msg);   //tell server you want to log in

            Message response= (Message) reader.readObject();   //check for whether correctly logged in at server
            if (ServerProtocol.isSuccessResponse(response)) {   //set username
                this.userName= userName;
            }
            return ServerProtocol.isSuccessResponse(response);
        }
    }
    
    /**
     * Check formatting and if correct send new user details to server and then
     * checks server response for successful/ failed add.
     * @param userName user name to add
     * @param password linked password
     * @return 0 for success, 1 for wrong symbols or space and 2 for already exists
     * @throws IOException
     * @throws ClassNotFoundException 
     */
    public int addUser(String userName, String password) throws IOException, ClassNotFoundException {
        //Incorrecxt format:
        if (userName.contains(" ") || userName.contains("@") || userName.contains("#")) {
            return Constants.SYMBOL_NEW_USER_ERR;
        }
        
        //Correct format:
        Message msg= ClientProtocol.createNewUserMessage(userName, password);   
        sendMessage(msg);   //ask server to add new user
        
        Message response= (Message) reader.readObject();   //check for server response
        if (!ServerProtocol.isSuccessResponse(response)) {
            return Constants.EXISTS_NEW_USER_ERR;    //failed add
        }
        return Constants.SUCCES_NEW_USER;    //successful add
    }
    
    /**
     * Log off client, closing all streams and sockets.
     * @throws IOException 
     */
    public void logoff() throws IOException {
        Message m= ClientProtocol.createLogoffMessage();   //tell server that client is logging off
        sendMessage(m);
        readerThreadList.clear();
        writer.close();
        reader.close();
        clientSocket.close();
    }
    
    /**
     * Add listener to message listener list.
     * @param listener message listener to add
     */
    
    public void addMessageListener(MessageListener listener) {
        messageListeners.add(listener);
    }
    
    /**
     * Remove listener from message listener list.
     * @param listener message listener to remove
     */
    
    public void removeMessageListener(MessageListener listener) {
        messageListeners.remove(listener);
    }

    /**
     * Add listener to server response listener list.
     * @param listener server response listener to add
     */
    public void addServerResponseListener(ServerResponseListener listener) {
        serverResponseListeners.add(listener);
    }
    
    /**
     * Remove listener from server response listener list.
     * @param listener server response listener to remove
     */
    public void removeServerResponseListener(ServerResponseListener listener) {
        serverResponseListeners.remove(listener);
    }
    
    /**
     * Sends message to server, flushing writer after send.
     * @param m message to send
     * @throws IOException 
     */
    public void sendMessage(Message m) throws IOException {
        writer.writeObject(m);
        writer.flush();
    }
    
    
    
    //Getters and setters:
    
    /**
     * Gets client socket.
     * @return client socket
     */
    public Socket getSocket() {
        return clientSocket;
    }
    
    /**
     * Gets client's output stream/ writer.
     * @return output stream/ writer
     */
    public ObjectOutputStream getWriter() {
        return writer;
    }   
    
    /**
     * Gets client's input stream/ reader.
     * @return input stream/ reader
     */
    public ObjectInputStream getReader() {
        return reader;
    }
    
    /**
     * Gets client's user name.
     * @return input stream/ reader
     */
    public String getUserName() {
        return userName;
    }
    
    
}
