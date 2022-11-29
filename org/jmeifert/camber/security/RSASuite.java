package org.jmeifert.camber.security;

import org.jmeifert.camber.util.ChatMap;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSAsuite is an asymmetric encryption suite.
 */
public class RSASuite {
    public final String CHARSET = "UTF-8";
    public final int RSA_KEY_SIZE = ChatMap.RSA_KEY_SIZE;

    private KeyPairGenerator keyPairGenerator;
    private Cipher encryptCipher;
    private Cipher decryptCipher;
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private boolean encryptOnly;

    /**
     * Initializes an RSAsuite with the given public key (Encrypt only)
     * @param publicKeyBytes Public key
     * @throws IllegalArgumentException If the given key is invalid
     */
    public RSASuite(byte[] publicKeyBytes) throws IllegalArgumentException {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            X509EncodedKeySpec pks = new X509EncodedKeySpec(publicKeyBytes);
            this.publicKey = keyFactory.generatePublic(pks);
            this.privateKey = null;
            encryptOnly = true;
            encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
            decryptCipher = null;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("You should NOT be seeing this error! (Algorithm 'RSA' not found!)");
        } catch (NoSuchPaddingException e) {
            throw new RuntimeException("You should NOT be seeing this error! (Padding 'RSA' not found!)");
        } catch (InvalidKeyException | InvalidKeySpecException e) {
            throw new IllegalArgumentException("RSAsuite initialized with invalid public key!");
        }
    }

    /**
     * Instantiates an RSAsuite.
     */
    public RSASuite() {
        try {
            encryptOnly = false;
            keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(RSA_KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            this.publicKey = keyPair.getPublic();
            this.privateKey = keyPair.getPrivate();
            encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, this.publicKey);
            decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, this.privateKey);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            throw new RuntimeException("Algorithm/Padding 'RSA' not found - You should NOT be seeing this error!");
        } catch (InvalidKeyException e) {
            throw new RuntimeException("The key we just generated is invalid - You should NOT be seeing this error!");
        }

    }

    /**
     * Encrypts given bytes and returns the result as bytes.
     * @param plaintextBytes Bytes to encrypt
     * @return Encrypted ciphertext (bytes)
     * @throws IOException Throws an IOException if input is malformed.
     */
    public byte[] encryptBytes(byte[] plaintextBytes) throws IOException {
        try {
            return encryptCipher.doFinal(plaintextBytes);
        } catch (IllegalBlockSizeException e) {
            System.err.println("encryptString: Illegal block size.");
            throw new IOException("encryptString: Illegal block size.");
        } catch (BadPaddingException e) {
            System.err.println("encryptString: Bad padding.");
            throw new IOException("encryptString: Bad padding.");
        }
    }

    /**
     * Encrypts given String and returns the result as bytes.
     * @param plaintext Plaintext to encrypt
     * @return Encrypted ciphertext (bytes)
     * @throws IOException Throws an IOException if input is malformed.
     */
    public byte[] encryptString(String plaintext) throws IOException {
        byte[] plaintextBytes;
        try {
            plaintextBytes = plaintext.getBytes(CHARSET);
        } catch(UnsupportedEncodingException e) {
            System.err.println("Encoding 'UTF-8' does not exist - You should NOT be seeing this error!");
            plaintextBytes = new byte[0];
        }
        return encryptBytes(plaintextBytes);
    }

    /**
     * Decrypts given bytes and returns the result as bytes.
     * @param ciphertext Ciphertext to decrypt
     * @return Decrypted plaintext (Bytes)
     * @throws IOException Throws an IOException if input is malformed.
     */
    public byte[] decryptBytes(byte[] ciphertext) throws IOException {
        if (encryptOnly) { return new byte[0]; }
        try {
            return decryptCipher.doFinal(ciphertext);
        } catch (IllegalBlockSizeException e) {
            System.err.println("decryptBytes: Illegal block size.");
            throw new IOException("decryptBytes: Illegal block size.");
        } catch (BadPaddingException e) {
            System.err.println("decryptBytes: Bad padding.");
            throw new IOException("decryptBytes: Bad padding.");
        }
    }

    /**
     * Decrypts given bytes and returns the result as a string.
     * @param ciphertext Ciphertext to decrypt
     * @return Decrypted plaintext (String)
     * @throws IOException Throws an IOException if input is malformed.
     */
    public String decryptString(byte[] ciphertext) throws IOException {
        if (encryptOnly) { return ""; }
        return new String(decryptBytes(ciphertext), CHARSET);
    }

    /**
     * @return True if the instance is encrypt-only.
     */
    public boolean isEncryptOnly() { return encryptOnly; }

    /**
     * @return Public key as bytes
     */
    public byte[] getPublicKey() {
        return publicKey.getEncoded();
    }
}
