package org.jmeifert.camber.test;

import org.jmeifert.camber.security.RSASuite;

public class TestRSASuite {
    public static void main(String[] args) {
        System.out.println("Testing RSASuite...");
        System.out.println("Testing instantiation...");
        RSASuite a = new RSASuite();
        RSASuite b = new RSASuite(a.getPublicKey());
        System.out.println("Testing encryption...");

        byte[] ct = new byte[0];
        try {
            ct = b.encryptString("The quick brown fox jumped over the lazy dog.");
        } catch(Exception e) {
            System.err.println(e.getMessage());
            return;
        }
        String dc = "";
        try {
            dc = a.decryptString(ct);
        } catch(Exception e) {
            System.err.println(e.getMessage());
            return;
        }
        if(!dc.equals("The quick brown fox jumped over the lazy dog.")) {
            System.err.println("Encryption and decryption test failed.");
            return;
        }
        System.out.println("Test of RSASuite passed.");
    }
}
