
package security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.SignatureException;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.encoders.Hex;

/**
 *
 * @author mikhail
 */
public class Asymmetric {
    
    //INSTANCE VARIABLES
    
    /**
     * Array of public-private key pair
     */
    Key[] keys;
    
    /**
     * The encrypted bytes and corresponding String
     */
    String encryptedText;
    byte [] encryptedBytes;
    
    /**
     * The decrypted bytes and corresponding String
     */
    String decryptedText;
    byte [] decryptedBytes;
    
    
    //CONSTRUCTORS
    
    /**
     * Constructor which takes in a public-private keys.
     * @param keys public-private key pair
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IOException 
     */
    public Asymmetric(Key[] keys) throws NoSuchAlgorithmException, NoSuchPaddingException, IOException{
        this.keys = new Key[2];
        this.keys = keys;
        
        Security.addProvider(new BouncyCastleProvider());
    }
    
    /**
     * Constructor which takes in the filename from which the public-private keys can be read.
     * @param filename file from which the key-pair is obtained.
     * @throws NoSuchAlgorithmException
     * @throws NoSuchPaddingException
     * @throws IOException 
     */
    public Asymmetric(String filename) throws NoSuchAlgorithmException, NoSuchPaddingException, IOException{
        this.keys = new Key[2];
        this.keys = Utils.getKeys(filename);
        Security.addProvider(new BouncyCastleProvider());
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
    
    /**
     * Creates a hash of a text message.
     * @param plainText
     * @return
     * @throws NoSuchAlgorithmException 
     */
    public String ApplySHA256(String plainText) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte [] hash = digest.digest(plainText.getBytes(StandardCharsets.UTF_8));
        String sha256hex = new String(Hex.encode(hash));
        return sha256hex;
    }
    
    /**
     * Compares the hash sent with a hash of the message received.
     * @param message
     * @param hash
     * @return true if the hash sent and the hash calculated match, else false.
     * @throws InvalidKeyException
     * @throws SignatureException
     * @throws NoSuchAlgorithmException 
     */
    public boolean compare(String message, String hash) throws InvalidKeyException, SignatureException, NoSuchAlgorithmException{
        
        if (ApplySHA256(message).equals(hash)) return true;

        return false;
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
        encryptedText = Base64.getEncoder().encodeToString(encryptedBytes);
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
        Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding", new org.bouncycastle.jce.provider.BouncyCastleProvider());
        cipher.init(Cipher.DECRYPT_MODE, key);
        decryptedBytes = cipher.doFinal(encrypted);
        decryptedText = new String (cipher.doFinal(encrypted), "UTF-8");
        
        return decryptedBytes;
    }

    //GETTERS//

        /**
     * Get the public key from a specified file
     * @return 
     */
    public PublicKey getPublicKey(){
        return (PublicKey) keys[0];
    }
    
    /**
     * Get the private key from a specified file
     * @return 
     */
    public PrivateKey getPrivateKey(){
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

    

