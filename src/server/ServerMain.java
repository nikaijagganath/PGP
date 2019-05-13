package server;

//Project Imports:
import constants.Constants;

//Java Imports:
import java.io.IOException;

/**
 * Starts server application.
 */
public class ServerMain 
    {
    
    /**
     * Opens server port, creates server and processes new connecting clients.
     * @param args none required
     */
    public static void main(String[] args) {
        try {
            Server server= new Server(Constants.SERVER_PORT_NUM);
            server.runServer();
        } 
        catch (IOException ex) {
            System.out.println("Connection Error: Couldn't connect to port " + Constants.SERVER_PORT_NUM + ".");
        }
    }
    
    
}
