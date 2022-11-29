package org.jmeifert.camber.main;

import org.jmeifert.camber.net.Client;

import java.util.Scanner;
import java.util.Vector;

/**
 * CamberStressTester is a stress tester for the server.
 */
public class CamberStressTester {

    public static final String DEFAULT_HOSTNAME = "127.0.0.1";
    public static final int DEFAULT_PORT = 7450;
    public static final String DEFAULT_GROUP = "default";
    public static final String DEFAULT_PASSWORD = "default";
    public static final String DEFAULT_NICKNAME = "Anonymous";

    public static void main(String[] args) throws Exception {
        Vector<Client> cs = new Vector<>();
        int nclients;
        int cmdstep;
        int maxiters;
        Scanner scanner = new Scanner(System.in);
        System.out.println("Number of clients to test with:");
        nclients = Integer.parseInt(scanner.nextLine());
        System.out.println("Command step (ms):");
        cmdstep = Integer.parseInt(scanner.nextLine());
        System.out.println("Iterations:");
        maxiters = Integer.parseInt(scanner.nextLine());

        System.out.println("Creating clients...");
        for(int i = 0; i < nclients; i++) {
            cs.add(new Client(DEFAULT_HOSTNAME, DEFAULT_PORT, DEFAULT_GROUP,
                    DEFAULT_PASSWORD, DEFAULT_NICKNAME + i));
            cs.get(i).open();
            Thread.sleep(cmdstep);
        }
        System.out.println("Running actions");
        for(int j = 0; j < maxiters; j++) {
            for(int i = 0; i < nclients; i++) {
                cs.get(i).sendMessage("test" + i);
                Thread.sleep(cmdstep);
            }

            for(int i = 0; i < nclients; i++) {
                cs.get(i).getAllMessages();
                Thread.sleep(cmdstep);
            }
        }
        System.out.println("Closing clients...");
        for(int i = 0; i < nclients; i++) {
            cs.get(i).close();
            Thread.sleep(cmdstep);
        }
    }
}
