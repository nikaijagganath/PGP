/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package security;

import java.security.InvalidKeyException;
import java.security.Key;
import security.Utils;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.Signature;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

/**
 *
 * @author mikhail
 */
public class Asymmetric {
    Key key;
    String encryptedText;
    byte [] encryptedBytes;
    
    String decryptedText;
    byte [] decryptedBytes;
    Signature sig;
    Utils utils = new Utils();
    
    public Asymmetric() throws NoSuchAlgorithmException, NoSuchPaddingException{
        Security.addProvider(new BouncyCastleProvider());
    }

    /**
     * Encrypts the message using RSA algorithm in ECB mode with PKCS1 padding.
     * @param key the symmetric key used for encryption.
     * @param message the message to encrypt.
     * @return encrypted bytes of message.
     * @throws Exception 
     */
    public byte[] encrypt(Key key, String message) throws Exception {
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        encryptedBytes = cipher.doFinal(message.getBytes());
        encryptedText = getHexString(encryptedBytes);
        return encryptedBytes;
    }
    
    /**
     * Decrypts the message using RSA algorithm in ECB mode with PKCS1 padding.
     * @param key the symmetric key used for decryption.
     * @param encrypted the encrypted bytes to decrypt. 
     * @return  decrypted bytes of message.
     * @throws Exception 
     */
    public byte[] decrypt(Key key, byte [] encrypted) throws Exception { // public static [] bytes decrypt(Key key, byte [] encrypted) throws Exception 
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
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

    

