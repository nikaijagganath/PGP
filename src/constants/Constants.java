package constants;

public class Constants {
    
    /*
    "localhost" specifies that the server is running on the same computer as the client.
    TO DO: change "localhost" to take a host address to connect to a remote server
    */
    public static final String SERVER_NAME= "localhost";
    public static final int SERVER_PORT_NUM= 1024;
    public static final String LOGINS= "logins.txt";
    
    public static final int SUCCES_NEW_USER= 0;
    public static final int SYMBOL_NEW_USER_ERR= 1;
    public static final int EXISTS_NEW_USER_ERR= 2;
}
