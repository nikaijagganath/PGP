/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package security;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import javax.crypto.NoSuchPaddingException;
import org.bouncycastle.util.Arrays;

public class Utils {  
    
    /**
     * Utils constructor
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException 
     */
    public Utils() throws NoSuchAlgorithmException, NoSuchPaddingException{
        
    }
    
     /**
     * Return an array of keys from a file
     * @param fileName
     * @return 
     */
    public static Key[] getKeys(String fileName){
        Key [] keys = new Key[2];
        try{
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            keys[0] = (Key) objectIn.readObject();
            keys[1] = (Key) objectIn.readObject();
            objectIn.close();
        }
        catch(IOException | ClassNotFoundException e){
            System.out.println(e);
        }
        return keys;
    }
    
    
    /**
     * Generates key pair and then writes the private and public key to a specified file
     * @param filename
     * @param keys
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     */
    public void writeToFile(String filename, Key [] keys) throws IOException, NoSuchAlgorithmException{
        try{
            FileOutputStream fileOut = new FileOutputStream(filename);
            ObjectOutputStream objectOut = new ObjectOutputStream(fileOut);
            for (Key key : keys) {
                objectOut.writeObject(key);
            }
            objectOut.close(); 
            }
        catch(IOException e){
            System.out.println(e);
        }
    }
    
    /**
     * Add new keys to the public.keys file
     * @param filename
     * @param key
     * @throws IOException 
     * @throws java.lang.ClassNotFoundException 
     * @throws java.security.NoSuchAlgorithmException 
     */
    public void appendToFile(String filename, Key key) throws IOException, ClassNotFoundException, NoSuchAlgorithmException{
        
        //Read the server's public key stored in the files
        FileInputStream fileIn = new FileInputStream(filename);
        ObjectInputStream objectIn = new ObjectInputStream(fileIn);
        Key [] keys  = new Key[2];
        keys[0] = (Key) objectIn.readObject(); //The server's public key
        objectIn.close();
        
        keys[1] = key; //The client's public key
        
        //Write both the server's public key and client's public key to the file
        writeToFile(filename, keys);
    }

    /**
     * Checking to see if a file exists.
     * @param name file to check if it exists.
     * @return 
     */
    public boolean checkFile(String name){
        File tmpDir = new File(name);
        boolean exists = tmpDir.exists();
        return exists;
    }

    /**
     * Concatenate two byte arrays with the '^' symbol in the middle to separate them.
     * @param message
     * @param hash
     * @return concatenated byte arrays separated with a '^'.
     * @throws IOException 
     */
    public byte[] concatenate(byte[] message, byte[] hash) throws IOException{
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        String middle = "^";
        output.write(hash);
        output.write(middle.getBytes());
        output.write(message);
        
        byte[] out = output.toByteArray();
        return out;
    }
    
    /**
     * De-concatenate a byte array to get the message and encrypted hash byte arrays.
     * @param message
     * @return list of byte arrays for the message and encrypted hash byte arrays.
     * @throws UnsupportedEncodingException 
     */
    public List<byte []> deconcatenate(byte[] message) throws UnsupportedEncodingException{
        List<byte[]> list = new ArrayList<>();
        
        //Find the index of the '^' separating the encrypted hash and message
        int index = 0;
        
        for (int i = message.length-1; i >= 0; i--){
            if (message[i]=='^'){
                index = i;
                break;
            }
        }
        list.add(Arrays.copyOfRange(message, 0, index)); //Byte array of encrypted hash
        list.add(Arrays.copyOfRange(message, index+1, message.length)); //Byte array of message
        
        return list;
    }
    
    
    /**
     * Compresses the byte array using the zip algorithm
     * @param messageAndHash
     * @return array of compressed bytes
     * @throws IOException 
     */
    public byte[] compress(byte[] messageAndHash) throws IOException{
        
        Deflater dfl = new Deflater();
        dfl.setLevel(Deflater.BEST_COMPRESSION);
        dfl.setInput(messageAndHash);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dfl.finish();
        byte[] tmp = new byte[1024];
        try{
            while(!dfl.finished()){
                int size = dfl.deflate(tmp);
                baos.write(tmp, 0, size);
            }
        } catch (Exception ex){      
        }
        baos.close();
        return baos.toByteArray();
    }
    
    /**
     * Decompresses the compressed byte array using the zip algorithm
     * @param compressedMessage
     * @return array of decompressed bytes
     */
    public byte[] decompress(byte[] compressedMessage) throws IOException{
        
        Inflater iflr = new Inflater();
        iflr.setInput(compressedMessage);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(compressedMessage.length);
        byte[] tmp = new byte[1024];
        try{
            while(!iflr.finished()){
                int size = iflr.inflate(tmp);
                baos.write(tmp, 0, size);
            }
        } catch (DataFormatException ex){
            
        }
        baos.close();
        return baos.toByteArray();
    }
    
}
