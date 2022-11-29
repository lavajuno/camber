package org.jmeifert.camber.security;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A class providing a one-off to get the SHA-256 hash of a String in hex form
 */
public class SHAutil {
    private static final String CHARSET = "UTF-8";

    /**
     * Gets the SHA-256 hash of a string.
     * @param s String to hash
     * @return The SHA-256 hash of the string as bytes
     */
    public static String getHash(String s) {
        try {
            String result = "";
            byte[] b = s.getBytes(CHARSET);
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(b);
            for(byte i : messageDigest.digest()) {
                result += String.format("%02X", i);
            }
            return result;
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Encoding 'UTF-8' does not exist - You should NOT be seeing this error!");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Encoding 'SHA-256' does not exist - You should NOT be seeing this error!");
        }
    }
}
