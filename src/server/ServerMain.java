package server;

//Project Imports:
import constants.Constants;
import security.*;
//Java Imports:
import java.io.IOException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import javax.crypto.NoSuchPaddingException;
import security.Utils;

/**
 * Starts server application.
 */
public class ServerMain {

    /**
     * Utility object
     */

    /**
     * Opens server port, creates server and processes new connecting clients.
     *
     * @param args none required
     */
    public static void main(String[] args) throws NoSuchAlgorithmException, NoSuchPaddingException, Exception {
        try {
            Asymmetric asym = new Asymmetric();
            Utils utils = new Utils();
            if(!utils.checkFile("server.keys")){
                utils.writeToFile("server");
            }
            Server server = new Server(Constants.SERVER_PORT_NUM);
            server.runServer();
        } catch (IOException ex) {
            System.out.println("Connection Error: Couldn't connect to port " + Constants.SERVER_PORT_NUM + ".");
        }
    }

}
