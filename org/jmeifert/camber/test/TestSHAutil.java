package org.jmeifert.camber.test;

import org.jmeifert.camber.security.SHAutil;

public class TestSHAutil {
    public static void main(String[] args) {
        System.out.println("Testing SHAutil...");
        System.out.println(SHAutil.getHash("The quick brown fox jumped over the lazy dog."));
        System.out.println(SHAutil.getHash("The quick brown fox jumped over the lazy dog."));
    }
}
