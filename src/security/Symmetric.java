
package security;

import java.security.*;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.spec.IvParameterSpec;
/**
 *
 * @author Nikai Jagganath
 */
public class Symmetric {
    
    Key key;
    String encryptedText;
    byte [] encryptedBytes;
    
    String decryptedText;
    byte [] decryptedBytes;
    
    //CBC requires IV for CBC mode
    String INITIALIZATION_VECTOR = "AODVNUASDNVVAOVF"; //16 bytes

    //Default constructor
    public Symmetric() {
    }

    /**
     * Generates a shared key to be used for symmetric encryption between the client and server.
     * @return key used for symmetric encryption.
     * @throws NoSuchAlgorithmException 
     */
    public Key buildKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
        key  = keyGenerator.generateKey();
        return key;
    }

    /**
     * Encrypts the message using AES algorithm in CBC mode with PKCS5 padding.
     * @param key the symmetric key used for encryption.
     * @param message the message to encrypt.
     * @return encrypted bytes of message.
     * @throws Exception 
     */
    public byte[] encrypt(Key key, byte [] message) throws Exception {
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(INITIALIZATION_VECTOR.getBytes()));
        
        encryptedBytes = cipher.doFinal(message);
        encryptedText = getHexString(encryptedBytes);
        
        return encryptedBytes;
    }
    
    /**
     * Decrypts the message using AES algorithm in CBC mode with PKCS5 padding.
     * @param key the symmetric key used for decryption.
     * @param encrypted the encrypted bytes to decrypt. 
     * @return  decrypted bytes of message.
     * @throws Exception 
     */
    public byte[] decrypt(Key key, byte [] encrypted) throws Exception { // public static [] bytes decrypt(Key key, byte [] encrypted) throws Exception 
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding", new org.bouncycastle.jce.provider.BouncyCastleProvider());
        cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(INITIALIZATION_VECTOR.getBytes()));
        
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
     * Returns the symmetric key.
     * @return key used for symmetric encryption.
     */
    public Key getKey() {
        return key;
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

    /**
     * Set the symmetric key to be used.
     * @param key 
     */
    public void setKey(Key key) {
        this.key = key;
    }
    
}
