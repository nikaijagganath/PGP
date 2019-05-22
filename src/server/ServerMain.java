package server;

//Project Imports:
import constants.Constants;
import security.*;
//Java Imports:
import java.io.IOException;
import java.security.Key;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import javax.crypto.NoSuchPaddingException;
import security.Utils;

/**
 * Starts server application.
 */
public class ServerMain {

    /**
     * Opens server port, creates server and processes new connecting clients.
     *
     * @param args none required
     * @throws java.security.NoSuchAlgorithmException
     */
    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException  {
        try {
            setUpKeys();
            Server server = new Server(Constants.SERVER_PORT_NUM);
            server.runServer();
        } catch (IOException ex) {
            System.out.println("Connection Error: Couldn't connect to port " + Constants.SERVER_PORT_NUM + ".");
        }
    }
    
    /**
     * Create the private-public key-pair for the server and adds the public key to the public.keys file.
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IOException 
     */
    private static void setUpKeys() throws NoSuchAlgorithmException, NoSuchPaddingException, IOException{
        //Asymmetric asym = new Asymmetric();
        Utils utils = new Utils();
        if(!utils.checkFile("server.keys")){
            KeyPair keyPair = Asymmetric.generateKeys(); //Generate a public-private key pair for the server
            Key [] keys1 = new Key [2];
            keys1[0] = keyPair.getPublic();
            keys1[1] = keyPair.getPrivate();
            utils.writeToFile("server.keys", keys1); //Write these keys to the server.keys file
            
            Key [] keys2 = new Key [1];
            keys2[0] = keyPair.getPublic();
            utils.writeToFile("public.keys", keys2); //Write the server's public key to the published, accessable  public keys file.
        }
    
    }

}
