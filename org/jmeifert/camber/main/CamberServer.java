package org.jmeifert.camber.main;

import org.jmeifert.camber.net.Server;
import java.util.Scanner;

/**
 * The server for Camber. Usage: java -jar camberserver.jar [port: optional, int]
 */
public class CamberServer {
    public static final String SPLASH_MESSAGE = "" +
            "x----------------------------------------x\n" +
            "| Welcome to Camber Server.              |\n" +
            "| You're using version 0.0 (Testing)     |\n" +
            "| For info and updates, please visit     |\n" +
            "| https://github.com/lavajuno/camber     |\n" +
            "x----------------------------------------x\n";
    public static final int DEFAULT_PORT = 7450;

    public static void main(String[] args) {
        int port = DEFAULT_PORT;

        // Handle args
        if(args.length > 1) {
            System.out.println("Usage: java -jar camberserver.jar [port: optional, int]");
            return;
        }
        if(args.length == 1) {
            try {
                port = Integer.parseInt(args[0]);
                if(port <= 1024) {
                    System.out.println("CamberServer: Invalid port.");
                    return;
                }
            } catch(NumberFormatException e) {
                System.out.println("Usage: java -jar camberserver.jar [port: optional, int]");
                return;
            }
        }

        // Splash
        System.out.println(SPLASH_MESSAGE);

        // Start server
        Scanner scanner = new Scanner(System.in);
        String userInput;
        Server sm = new Server(port);
        sm.start();
        System.out.println("-- Server started on port " + port + ".");
        System.out.println("-- Input 't' to terminate.");
        while(true) {
            userInput = scanner.nextLine();
            if(userInput.equals("t")) {
                System.out.println("-- Shutting down server...");
                sm.interrupt();
                sm.close();
                System.out.println("-- Server shut down.");
                System.exit(0); // Stop all processes
            }
        }
    }
}
