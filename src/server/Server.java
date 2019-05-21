package server;

//Project Imports:
import constants.*;

//Java Imports:
import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.NoSuchPaddingException;

/**
 * Server used to connect clients to one another.
 */
public class Server {
    
    //Instance Variables:
    
    /**
     * Port number on which server socket listens for client connections.
     */
    private final int portNo;
    
    /**
     * Server socket which clients connect to.
     */
    private final ServerSocket serverSocket;
    
    /**
     * List of client handlers, each handling one client.
     */
    private final CopyOnWriteArrayList<ClientHandler> handlerList;
    
    /**
     * List of registered usernames and passwords.
     */
    private final ConcurrentHashMap loginList;
    
    //Constructor:
    
    /**
     * Creates new server, listening on specified port number.
     * @param portNo port number which server socket listens on
     * @throws java.io.IOException
     */
    public Server(int portNo) throws IOException {
        this.portNo= portNo;
        serverSocket= new ServerSocket(this.portNo);
        handlerList= new CopyOnWriteArrayList<>();
        loginList= new ConcurrentHashMap();
        readInLogins(); //read in list of saved login details
    }
    
    
    
    //Run Server Method:
    
    /**
     * Opens server socket on server's port number and continuously accepts clients, creating new client handler threads and adding them to the handler list.
     */
    public void runServer() throws NoSuchAlgorithmException {
        try {
            while(true) {
                System.out.println("Connecting to client...");
                Socket clientSocket= serverSocket.accept();
                System.out.println("Accepted connection: client " + clientSocket);
                try {
                    addHandler(clientSocket);
                } catch (NoSuchPaddingException ex) {
                    Logger.getLogger(Server.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } 
        catch (IOException ex) {
            System.out.println("Error connecting to client: " + ex);
        }
    }
    
    
    
    //Helper Methods:
    
    /**
     * Creates new client handler thread connecting the specified client socket with the server and adds handler to the handler list.
     * @param clientSocket client socket on the chat client 
     */
    public void addHandler(Socket clientSocket) throws NoSuchAlgorithmException, NoSuchPaddingException {
        ClientHandler handler= new ClientHandler(this, clientSocket);
        handlerList.add(handler);
        handler.start();
    }
    
    /**
     * Removes a client handler from the handler list.(when client logs off)
     * @param handler client handler thread to be removed
     */
    public void removeHandler(ClientHandler handler) {
        handlerList.remove(handler);
    }
    
    /**
     * Gets a list of all current handlers on server.
     * @return list of all online client handlers
     */
    public List<ClientHandler> getHandlerList() {
        return handlerList;
    }
    
    
    /**
     * Gets a handler of client with specified user name.
     * @param userName user name of client
     * @return client handler if client of provided name exists, otherwise null
     */
    public ClientHandler getHandler(String userName) {
        for(ClientHandler h: handlerList) {
           if (userName.equals(h.getUserName())) {
               return h;
            }
        }
        return null;
    }
    
    /**
     * Reads in username and passwords from stored login details text file.
     * @throws FileNotFoundException 
     */
    private void readInLogins() throws FileNotFoundException {
        try (Scanner fileReader= new Scanner(new FileReader(Constants.LOGINS))) {
            while(fileReader.hasNextLine()) {   //get all login detail sets
                String line= fileReader.nextLine();
                String [] tokens= line.split(" ");
                String name= tokens[0];
                String password= tokens[1];
                loginList.put(name, password);
            }
        }
    }
    
    /**
     * Checks whether user name exists in stored details and if password matches if it does exist.
     * @param username user name of user to login
     * @param password corresponding password
     * @return true if matches and exist and false otherwise
     */
    public boolean checkLogin(String username, String password) {
        for (String l : (Set<String>)loginList.keySet()) {
            if(username.equalsIgnoreCase(l)) {
                return ((String) loginList.get(username)).equalsIgnoreCase(password);
            }
        }
        return false;
    }
    
    /**
     * Attempts to add new user and tells user if add was successful or not.
     * @param username new user name to add
     * @param password corresponding password
     * @return true if added successfully otherwise false
     * @throws IOException 
     */
    public synchronized boolean addUser(String username, String password) throws IOException {
        for (String l : (Set<String>)loginList.keySet()) {
            if(username.equalsIgnoreCase(l)) {
                return false;
            }
        }
        
        //else {
            loginList.put(username, password);
            try (PrintWriter w = new PrintWriter(new FileWriter(Constants.LOGINS, true))) {
                w.println(username + " " + password);   //add to stored login details file
            }
            return true;
        //}
    }
    
}
