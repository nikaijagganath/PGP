package client;

/**
 * Interface for interacting with server response listeners on client.
 */
public interface ServerResponseListener {
    
    /**
     * Used to process error message from server.
     * @param errCommand command from client which caused an error
     * @param errType type of error
     * @param errMessage error message from server
     */
    public void onError(String errCommand, String errType, String errMessage);
    
    /**
     * Used to process response to command from server.
     * @param initialCommand command from client which caused the response
     * @param respType type of response
     * @param response response message
     */
    public void onResponse(String initialCommand, String respType, String response);
    
}
