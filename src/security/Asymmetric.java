/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package security;

import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import security.Utils;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

/**
 *
 * @author mikhail
 */
public class Asymmetric {
    Key[] keys;
    String encryptedText;
    byte [] encryptedBytes;
    
    String decryptedText;
    byte [] decryptedBytes;
    //Signature sig;
    Utils utils = new Utils();
    
    
    public Asymmetric() throws NoSuchAlgorithmException, NoSuchPaddingException{
        Security.addProvider(new BouncyCastleProvider());
        //sig = Signature.getInstance("SHA512WithRSA");
    }

    
    /**
     * Generates key pair and returns it
     * @return
     * @throws NoSuchAlgorithmException 
     */
    public static KeyPair generateKeys() throws NoSuchAlgorithmException{
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair pair = keyGen.generateKeyPair();
        return pair;
    }
    
    public String ApplySHA256(String plainText) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte [] hash = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
        String sha256hex = new String(Hex.encode(hash));
        return sha256hex;
    }
    
    public boolean compare(String message, String hash) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException{
        
        if (ApplySHA256(message).equals(hash))
        {
            return true;
        }
        else 
        {
            return false;
        }
    }
    
    /**
     * Encrypts the message using RSA algorithm in ECB mode with PKCS1 padding.
     * @param key the symmetric key used for encryption.
     * @param message the message to encrypt.
     * @return encrypted bytes of message.
     * @throws Exception 
     */
    public byte[] encrypt(Key key, byte[] message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        encryptedBytes = cipher.doFinal(message);
        encryptedText = getHexString(encryptedBytes);
        return encryptedBytes;
    }
    
    public static byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }
    
    /**
     * Decrypts the message using RSA algorithm in ECB mode with PKCS1 padding.
     * @param key the symmetric key used for decryption.
     * @param encrypted the encrypted bytes to decrypt. 
     * @return  decrypted bytes of message.
     * @throws Exception 
     */
    public byte[] decrypt(Key key, byte [] encrypted) throws Exception { // public static [] bytes decrypt(Key key, byte [] encrypted) throws Exception 
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", new org.bouncycastle.jce.provider.BouncyCastleProvider());
        cipher.init(Cipher.DECRYPT_MODE, key);
        decryptedBytes = cipher.doFinal(encrypted);
        decryptedText = new String (cipher.doFinal(encrypted), "UTF-8");
        
        return decryptedBytes;
    }
    
    
     /**
     * Returns the encrypted bytes of the message as a hexadecimal string.
     * @param b bytes to convert to a hex string.
     * @return the hex string of the converted bytes.
     * @throws Exception 
     */
    public String getHexString(byte[] b) throws Exception {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString( ( b[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return result;
    }

    //GETTERS//

    /**
     * Return an array of keys from a file
     * @param fileName
     * @return 
     */
    public Key[] getKeys(String fileName){
        keys = new Key[2];
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
        //Key[] key = getKeys(name);
        return (PublicKey) keys[0];
    }
    
    /**
     * Get the private key from a specified file
     * @param name
     * @return 
     */
    public PrivateKey getPrivateKey(String name){
        //Key[] key = getKeys(name);
        return (PrivateKey) keys[1];
    }
    
    /**
     * Returns the encrypted text in hex format.
     * @return encrypted text.
     */
    public String getEncryptedText() {
        return encryptedText;
    }

    /**
     * Returns the encrypted bytes.
     * @return encrypted bytes.
     */
    public byte[] getEncryptedBytes() {
        return encryptedBytes;
    }

    /**
     * Returns the decrypted text in UTF-8 format.
     * @return decrypted message.
     */
    public String getDecryptedText() {
        return decryptedText;
    }

    /**
     * Returns the decrypted bytes of the message.
     * @return decrypted bytes.
     */
    public byte[] getDecryptedBytes() {
        return decryptedBytes;
    }
    
    //SETTERS//
    
}

    

