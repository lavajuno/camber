package org.jmeifert.camber.security;

import org.jmeifert.camber.util.ChatMap;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.security.*;

/**
 * AESSuite is a symmetric encryption suite.
 */
public class AESSuite {
    private final int AES_KEY_SIZE = ChatMap.AES_KEY_SIZE;
    private final String CHARSET = "UTF-8";

    private KeyGenerator keyGenerator;
    private SecretKey key;
    private IvParameterSpec iv;
    private Cipher encryptCipher;
    private Cipher decryptCipher;

    /**
     * Instantiates an AESSuite with a given 256-bit key.
     * @param keyToLoad Key to use
     * @throws IllegalArgumentException if key is invalid.
     */
    public AESSuite(byte[] keyToLoad) throws IllegalArgumentException {
        if (keyToLoad.length != (AES_KEY_SIZE / 8)) {
            throw new IllegalArgumentException("AESSuite - Wrong key size!");
        }
        this.keyGenerator = null;
        this.key = new SecretKeySpec(keyToLoad, "AES");
    }

    /**
     * Instantiates an AESSuite.
     */
    public AESSuite() {
        try {
            this.keyGenerator = KeyGenerator.getInstance("AES");
            this.keyGenerator.init(AES_KEY_SIZE);
            this.key = keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Algorithm 'AES' not found! - You should NOT be seeing this error!");
        }
    }

    /**
     * Encrypts given bytes and returns the result as bytes.
     * @param plaintextBytes Bytes to encrypt
     * @return Encrypted ciphertext (bytes)
     */
    public byte[] encryptBytes(byte[] plaintextBytes) {
        try {
            encryptCipher = Cipher.getInstance("AES");
            encryptCipher.init(Cipher.ENCRYPT_MODE, this.key);
            return encryptCipher.doFinal(plaintextBytes);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException("AESSuite: Illegal block size on encrypt.");
        } catch (BadPaddingException e) {
            throw new RuntimeException("AESSuite: Bad padding on encrypt.");
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException("AESSuite: No such padding 'NoPadding' - You should NOT be seeing this error.");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AESSuite: No such algorithm 'AES' - You should NOT be seeing this error.");
        } catch (InvalidKeyException e) {
            throw new RuntimeException("AESSuite: Invalid key on encrypt.");
        }
    }

    /**
     * Encrypts given String and returns the result as bytes.
     * @param plaintext Plaintext to encrypt
     * @return Encrypted ciphertext (bytes)
     */
    public byte[] encryptString(String plaintext) {
        byte[] plaintextBytes;
        try {
            plaintextBytes = plaintext.getBytes(CHARSET);
        } catch(UnsupportedEncodingException e) {
            System.err.println("Encoding does not exist. (You should NOT be seeing this error).");
            plaintextBytes = new byte[0];
        }
        return encryptBytes(plaintextBytes);
    }

    /**
     * Decrypts given bytes and returns the result as bytes.
     * @param ciphertext Ciphertext to decrypt
     * @return Decrypted plaintext (Bytes)
     * @throws GeneralSecurityException Throws a GeneralSecurityException if key is invalid.
     */
    public byte[] decryptBytes(byte[] ciphertext) throws GeneralSecurityException {
        try {
            decryptCipher = Cipher.getInstance("AES");
            decryptCipher.init(Cipher.DECRYPT_MODE, this.key);
            return decryptCipher.doFinal(ciphertext);
        } catch (IllegalBlockSizeException e) {
            throw new RuntimeException("AESSuite: Illegal block size on decrypt.");
        } catch (BadPaddingException e) {
            throw new RuntimeException("AESSuite: Bad padding on decrypt.");
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException("AESSuite: No such padding 'NoPadding' - You should NOT be seeing this error.");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("AESSuite: No such algorithm 'AES' - You should NOT be seeing this error.");
        } catch (InvalidKeyException e) {
            throw new GeneralSecurityException("AESSuite: Invalid key on decrypt.");
        }
    }

    /**
     * Decrypts given bytes and returns the result as a string.
     * @param ciphertext Ciphertext to decrypt
     * @return Decrypted plaintext (String)
     * @throws GeneralSecurityException Throws a GeneralSecurityException if key is invalid.
     */
    public String decryptString(byte[] ciphertext) throws GeneralSecurityException {
        try {
            return new String(decryptBytes(ciphertext), CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("AESSuite: Unsupported encoding: " +
                    "'UTF-8' - You should NOT be seeing this error.");
        }
    }

    public byte[] getKey() {
        return key.getEncoded();
    }

    public void loadKey(byte[] keyToLoad) {
        key = new SecretKeySpec(keyToLoad, "AES");
    }
}
