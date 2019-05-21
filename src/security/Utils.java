/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package security;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import javax.crypto.NoSuchPaddingException;
import org.bouncycastle.util.Arrays;

/**
 *
 * @author mikhail
 */
public class Utils {

    /**
     * Initialize FileOutputStream
     */
    FileOutputStream fileOut;
    /**
     * Initialize ObjectOutputStream
     */
    ObjectOutputStream objectOut;
    /**
     * Initialize FileOutput Stream
     */
    FileOutputStream fOut;
    /**
     * Initialize ObjectOutputStream
     */
    ObjectOutputStream oOut;
    
    

    public Utils() throws NoSuchAlgorithmException, NoSuchPaddingException{
        
    }
    /**
     * Generates key pair and then writes the private and public key to a specified file
     * @param name
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     */
    public void writeToFile(String name) throws IOException, NoSuchAlgorithmException{
        KeyPair keyPair = Asymmetric.generateKeys();
        try{
            String fileName = name + ".keys";
            fileOut = new FileOutputStream(fileName);
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(keyPair.getPublic());
            objectOut.writeObject(keyPair.getPrivate());
            objectOut.close(); 
            //TODO: public file
            }
        catch(Exception e){
            System.out.println(e);
        }
    }
    

    /**
     * Checking to see if a file exists
     * @param name
     * @return 
     */
    public boolean checkFile(String name){
        //check if file exists
        File tmpDir = new File(name);
        boolean exists = tmpDir.exists();
        return exists;
    }

    /**
     * Concatenate two byte arrays with a symbol in the middle
     * @param message
     * @param hash
     * @return
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
     * Deconcatenate a byte array to get the message and hash byte arrays
     * @param message
     * @return
     * @throws UnsupportedEncodingException 
     */
    public List<byte []> deconcatenate(byte[] message) throws UnsupportedEncodingException{
        List<byte[]> list = new ArrayList<>();
        
        //Getting the index of the '^'
        int index = 0;
        
        for (int i =message.length-1; i>=0; i--)
        {
            if (message[i]=='^')
            {
                index = i;
                break;
            }
        }
        list.add(Arrays.copyOfRange(message, 0, index));
        list.add(Arrays.copyOfRange(message, index+1, message.length));
        
        return list;
    }
    
    
    
    /**
     * Returns byte array of string
     * @param message
     * @return 
     */
    public byte[] getByteArray(String string){
        return string.getBytes();
    }
    
    
    /**
     * Compresses the byte array using the zip algorithm
     * @param messageAndHash
     * @return
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
     * @return 
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
