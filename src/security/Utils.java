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
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import protocol.ClientProtocol;
import protocol.Message;

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
    
    /**
     * Initialize KeyPair
     */
    KeyPair keyPair;
    
    /**
     * Initialize signature
     */
    Signature sig;
    

    public Utils() throws NoSuchAlgorithmException, NoSuchPaddingException{
        sig = Signature.getInstance("SHA512WithRSA");
    }
    /**
     * Generates key pair and then writes the private and public key to a specified file
     * @param name
     * @throws IOException
     * @throws NoSuchAlgorithmException 
     */
    public void writeToFile(String name) throws IOException, NoSuchAlgorithmException{
        keyPair = generateKeys();
        try{
            String fileName = name + ".keys";
            fileOut = new FileOutputStream(fileName);
            objectOut = new ObjectOutputStream(fileOut);
            objectOut.writeObject(keyPair.getPublic());
            objectOut.writeObject(keyPair.getPrivate());
            objectOut.close(); 
            }
        catch(Exception e){
            System.out.println(e);
        }
    }
    
    /**
     * Generates key pair and returns it
     * @return
     * @throws NoSuchAlgorithmException 
     */
    public KeyPair generateKeys() throws NoSuchAlgorithmException{
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        return pair;
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
     * Return an array of keys from a file
     * @param fileName
     * @return 
     */
    public Key[] getKeys(String fileName){
        Key[] keys = new Key[2];
        try{
            PublicKey keyP;
            PrivateKey keyV;
            FileInputStream fileIn = new FileInputStream(fileName);
            ObjectInputStream objectIn = new ObjectInputStream(fileIn);
            keyP = (PublicKey) objectIn.readObject();
            keyV = (PrivateKey) objectIn.readObject();
            keys[0] = keyP;
            keys[1] = keyV;
            objectIn.close();
        }
        catch(Exception e){
            System.out.println(e);
        }
        return keys;
    }
    
    /**
     * Get the public key from a specified file
     * @param name
     * @return 
     */
    public PublicKey getPublicKey(String name){
        Key[] key = getKeys(name);
        return (PublicKey) key[0];
    }
    
    /**
     * Get the private key from a specified file
     * @param name
     * @return 
     */
    public PrivateKey getPrivateKey(String name){
        Key[] key = getKeys(name);
        return (PrivateKey) key[1];
    }
    
    /**
     * Hash the message using SHA512
     * @param m
     * @return
     * @throws UnsupportedEncodingException
     * @throws SignatureException
     * @throws InvalidKeyException
     * @throws NoSuchAlgorithmException 
     */
    public byte[] signMessage(String message) throws UnsupportedEncodingException, SignatureException, InvalidKeyException, NoSuchAlgorithmException{
        sig.initSign(getPrivateKey("client.keys"));
        sig.update(message.getBytes());
        byte[] signature = sig.sign();
        return signature;      
    }
    
    /**
     * Authenticate message sent from client
     * @param message
     * @param hashReceived
     * @return
     * @throws InvalidKeyException
     * @throws SignatureException 
     */
    public boolean compareHashes(String message, byte[] hashReceived) throws InvalidKeyException, SignatureException{
        sig.initVerify(getPublicKey("client.keys"));
        sig.update(message.getBytes());
        if (sig.verify(hashReceived)) {
            return true;
        }
        else{
            return false;
        } 
           
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
        output.write(message);
        output.write(middle.getBytes());
        output.write(hash);
        
        byte[] out = output.toByteArray();
        return out;
    }
    
    /**
     * Deconcatenate a byte array to get the message and hash byte arrays
     * @param message
     * @return
     * @throws UnsupportedEncodingException 
     */
    public String[] deconcatenate(byte[] message) throws UnsupportedEncodingException{
        String[] messages = new String[2];
        String bigStr = new String(message);
        int mid = bigStr.indexOf("^");
        messages[0] = bigStr.substring(0, mid);
        messages [1] = bigStr.substring(mid+1, bigStr.length());
        return messages;
        //
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
        ByteArrayOutputStream baos = null;
        Deflater dfl = new Deflater();
        dfl.setLevel(Deflater.BEST_COMPRESSION);
        dfl.setInput(messageAndHash);
        dfl.finish();
        baos = new ByteArrayOutputStream();
        byte[] tmp = new byte[4*1024];
        try{
            while(!dfl.finished()){
                int size = dfl.deflate(tmp);
                baos.write(tmp, 0, size);
            }
        } catch (Exception ex){
             
        } finally {
            try{
                if(baos != null) baos.close();
            } catch(Exception ex){}
        }
         
        return baos.toByteArray();
    }
    
    /**
     * Decompresses the compressed byte array using the zip algorithm
     * @param compressedMessage
     * @return 
     */
    public byte[] decompress(byte[] compressedMessage){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Inflater iflr = new Inflater();
        iflr.setInput(compressedMessage);
        byte[] tmp = new byte[4*1048];
        try{
            while(!iflr.finished()){
                int size = iflr.inflate(tmp);
                baos.write(tmp, 0, size);
            }
        } catch (DataFormatException ex){
             
        } finally {
            try{
                if(baos != null){ 
                    baos.close();
                }
            } 
            catch(IOException ex){
            }
        }
         
        return baos.toByteArray();
    }
    
}
