package client;

//Project Imports:
import protocol.*;
import security.*;
//Java Imports:
import java.io.*;
import java.net.*;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.util.*;
import javax.crypto.NoSuchPaddingException;
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
     * Creates a new client, by connecting to server, setting up streams and
     * creating listener lists and message storage map.
     * @param serverName name/IP address of the server
     * @param portNo the port number on which server is listening
     * @throws IOException 
     * @throws java.security.NoSuchAlgorithmException 
     * @throws javax.crypto.NoSuchPaddingException 
     * @throws java.lang.ClassNotFoundException 
     */
    public Client(String serverName, int portNo) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, ClassNotFoundException {
        
        //Create utils object
        utils = new Utils();
        
        //Create client.keys file if it does not exist
        if(!utils.checkFile("client.keys")){ 
            KeyPair keyPair = Asymmetric.generateKeys();
            Key [] keys = new Key [2];
            keys[0] = keyPair.getPublic();
            keys[1] = keyPair.getPrivate();
            utils.writeToFile("client.keys", keys); //Write these keys to the server.keys file
            utils.appendToFile("public.keys", keys[0]); //Append the client's public key to the public.keys file
            
            asym = new Asymmetric(keys); //Initialise asymmetric object with created keys
        }
        else{
            asym = new Asymmetric("client.keys"); //Initialise asymmetric object with existing keys
        }
        
        //Initialise encryption objects
        sym = new Symmetric();
        

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
    public boolean login(String userName, String password) throws IOException, ClassNotFoundException, UnsupportedEncodingException, SignatureException, InvalidKeyException, NoSuchAlgorithmException, Exception  {

        //PGP PROCEDURE ON CLIENT SIDE
        System.out.println("\n--START: PGP PROCEDURE ON CLIENT--\n");
        
        //Password on client side
        System.out.println("Password on client side: "+password);
        
        //Create a hash of the message (in this case the password)
        String hash = asym.ApplySHA256(password);
        System.out.println("Hash of message on client side: "+hash);
        
        //Encrypt the hash using the client's private key
        byte[] encryptedHash = asym.encrypt(asym.getPrivateKey(), hash.getBytes());
        System.out.println("Encrypted hash on client side: "+ Base64.getEncoder().encodeToString(encryptedHash));
        
        //Concatenate the encrypted hash with the text to be sent to form a message
        byte[] concatenated = utils.concatenate(password.getBytes(), encryptedHash);
        System.out.println("Concatenated message on client side: "+ Base64.getEncoder().encodeToString(concatenated));
        
        //Compress the message using zip compression
        byte[] zip = utils.compress(concatenated);
        System.out.println("Compressed message on client side: "+ Base64.getEncoder().encodeToString(zip));
        
        //Create shared key to use for the session with the server
        Key sharedKey = sym.buildKey();
        System.out.println("Shared key created on client side: "+Base64.getEncoder().encodeToString(sharedKey.getEncoded()));
        
        //Encrypt the compressed message with the shared key
        byte[] cipher1 = sym.encrypt(sharedKey, zip);
        System.out.println("Encrypted compressed message using shared key on client side: "+Base64.getEncoder().encodeToString(cipher1));
        
        //Get the server's public key which is the first key stored in the public.keys file
        Key serverPublic = Utils.getKeys("public.keys")[0]; 
        
        //Encrypt the shared key with the server's public key to send to the server
        byte[] cipher2 = asym.encrypt(serverPublic, sharedKey.getEncoded());
        System.out.println("Encrypted shared key using server's public key on client side: "+Base64.getEncoder().encodeToString(cipher2));
        
        System.out.println("\n--END: PGP PROCEDURE ON CLIENT--\n");
        
        //Send the message to the server
        Message msg = ClientProtocol.createLoginMessage(userName, cipher1, cipher2);
        sendMessage(msg); 
        
        Message response = (Message) reader.readObject();   //Check for whether correctly logged in at server.
        if (ServerProtocol.isSuccessResponse(response)) {   
            this.userName= userName; //Set username
        }
        return ServerProtocol.isSuccessResponse(response);
    }
    
    /**
     * Log off client, closing all streams and sockets.
     * @throws IOException 
     */
    public void logoff() throws IOException, UnsupportedEncodingException, SignatureException, InvalidKeyException, NoSuchAlgorithmException, Exception {
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
     * Encrypt and send the text message to the server.
     * @param message
     * @throws IOException
     * @throws UnsupportedEncodingException
     * @throws SignatureException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException
     * @throws Exception 
     */
    public void sendDirectTextMessage(String message) throws IOException, UnsupportedEncodingException, SignatureException, InvalidKeyException, NoSuchAlgorithmException, Exception {
        
        //PGP PROCEDURE ON CLIENT SIDE
        System.out.println("\n--START: PGP PROCEDURE ON CLIENT--\n");
        
        //Message on client side
        System.out.println("Message on client side: "+message);
        
        //Create a hash of the message (in this case the password)
        String hash = asym.ApplySHA256(message);
        System.out.println("Hash of message on client side: "+hash);
        
        //Encrypt the hash using the client's private key
        byte[] encryptedHash = asym.encrypt(asym.getPrivateKey(), hash.getBytes());
        System.out.println("Encrypted hash on client side: "+ Base64.getEncoder().encodeToString(encryptedHash));
        
        //Concatenate the encrypted hash with the text to be sent to form a message
        byte[] concatenated = utils.concatenate(message.getBytes(), encryptedHash);
        System.out.println("Concatenated message on client side: "+ Base64.getEncoder().encodeToString(concatenated));
        
        //Compress the message using zip compression
        byte[] zip = utils.compress(concatenated);
        System.out.println("Compressed message on client side: "+ Base64.getEncoder().encodeToString(zip));

        //Encrypt the compressed message with the shared key
        byte[] encryptedMessage = sym.encrypt(sym.getKey(), zip);
        System.out.println("Encrypted compressed message using shared key on client side: "+Base64.getEncoder().encodeToString(encryptedMessage));
        
        System.out.println("\n--END: PGP PROCEDURE ON CLIENT--\n");
        
        Message m = ClientProtocol.createDirectTextMessage(userName, encryptedMessage);
        sendMessage(m);
    }
    
    /**
     * Sends message to server, flushing writer after send.
     * @param m message to send
     * @throws IOException 
     */
    public void sendMessage(Message m) throws IOException, UnsupportedEncodingException, SignatureException, InvalidKeyException, NoSuchAlgorithmException, Exception {
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
    
    /**
     * Return the instance of the Utils object.
     * @return Utils object.
     */
    Utils getUtils(){
        return utils;
    }
    
    /**
     * Return the instance of the Asymmetric object.
     * @return Asymmetric object.
     */
    Asymmetric getAsymmetric(){
        return asym;
    }
    
    /**
     * Return the instance of the Symmetric object.
     * @return Symmetric object.
     */
    Symmetric getSymmetric(){
        return sym;
    }
    
}
