package org.jmeifert.camber.main;

import org.jmeifert.camber.net.Client;
import java.io.IOException;
import java.util.Scanner;

/**
 * The console-based client for Camber. Usage: java -jar camber.jar [hostname: optional, string] [port: optional, int]
 */
public class CamberClient {
    public static final String SPLASH_MESSAGE = "" +
            "x----------------------------------------x\n" +
            "| Welcome to Camber.                     |\n" +
            "| You're using version 0.0.0 (Testing)   |\n" +
            "| For info and updates, please visit     |\n" +
            "| https://github.com/lavajuno/camber     |\n" +
            "x----------------------------------------x\n";
    public static final String DEFAULT_HOSTNAME = "127.0.0.1";
    public static final int DEFAULT_PORT = 7450;
    public static final String DEFAULT_GROUP = "default";
    public static final String DEFAULT_PASSWORD = "default";
    public static final String DEFAULT_NICKNAME = "Anonymous";

    public static void main(String[] args) { // args: hostname, port
        String hostname = DEFAULT_HOSTNAME;
        int port = DEFAULT_PORT;
        String group = DEFAULT_GROUP;
        String groupPassword = DEFAULT_PASSWORD;
        String nickname = DEFAULT_NICKNAME;

        // Handle args
        if(args.length > 2) {
            System.out.println("Usage: java -jar camber.jar [hostname: optional, string] [port: optional, int]");
            return;
        }
        if(args.length == 2) {
            try {
                hostname = args[0];
                port = Integer.parseInt(args[1]);
                if(port <= 1024) {
                    System.out.println("Camber: Invalid port.");
                    return;
                }
            } catch(NumberFormatException e) {
                System.out.println("Usage: java -jar camber.jar [hostname: optional, string] [port: optional, int]");
                return;
            }
        }
        if(args.length == 1) {
            try {
                hostname = args[0];
            } catch(NumberFormatException e) {
                System.out.println("Usage: java -jar camber.jar [hostname: optional, string] [port: optional, int]");
                return;
            }
        }

        // Splash
        System.out.println(SPLASH_MESSAGE);

        // Configure client
        Scanner scanner = new Scanner(System.in);
        String userOption;
        String userMessage;

        System.out.println("Using hostname '" + hostname + "'.");
        System.out.println("Using port " + port + ".\n");
        System.out.println("Enter group name (leave blank for default - '" + DEFAULT_GROUP + "'):");
        userOption = scanner.nextLine();
        if(!userOption.equals("")) {
            group = userOption;
        }

        System.out.println("Enter group password (leave blank for default - '" + DEFAULT_PASSWORD + "'):");
        userOption = scanner.nextLine();
        if(!userOption.equals("")) {
            groupPassword = userOption;
        }

        System.out.println("Enter nickname (leave blank for default - '" + DEFAULT_NICKNAME + "'):");
        userOption = scanner.nextLine();
        if(!userOption.equals("")) {
            nickname = userOption;
        }

        System.out.println("Connecting...");
        Client c = new Client(hostname, port, group, groupPassword, nickname);
        try {
            c.open();
            System.out.println("Connected!");
        } catch(IOException e) {
            System.err.println("Failed to connect.");
            return;
        }

        // Connected - Main Menu
        while(true) {
            showMessages(c);
            System.out.println("Enter your message below. (Type '/help' for help)");
            userMessage = scanner.nextLine();
            if(userMessage.equals("/help")) {
                System.out.println("-- Help:\n" +
                        "-- Press ENTER at the message prompt to refresh recent messages.\n" +
                        "-- '/lm' - List messages from all topics.\n" +
                        "-- '/lt' - List active topics.\n" +
                        "-- '/st' - Set active topic. (Will prompt you for it)\n" +
                        "-- '/quit' - Disconnect from the server and exit the program.\n");
            } else if(userMessage.equals("")) { // Refresh messages
                showMessages(c);
            } else if(userMessage.equals("/lm")) { // Get messages from all topics
                showAllMessages(c);
            }  else if(userMessage.equals("/lt")) { // List active topics
                System.out.println("-- Active topics: ");
                System.out.println(c.getTopics());
            } else if(userMessage.equals("/st")) { // Set active topic
                System.out.println("-- Enter topic to change to: ");
                userOption = scanner.nextLine();
                if(c.setTopic(userOption)) {
                    System.out.println("-- New topic set.");
                } else {
                    System.out.println("-- Failed to set new topic.");
                }
                showMessages(c);
            } else if(userMessage.equals("/quit")) { // Disconnect
                System.out.println("-- Disconnecting...");
                try {
                    c.close();
                } catch(IOException e) {
                    System.err.println("-- Failed to disconnect gracefully. Forcing disconnect...");
                }
                System.out.println("-- Disconnected.");
                return;
            } else { // Send message
                c.sendMessage(userMessage);
                showMessages(c);
            }
        }
    }

    private static void showMessages(Client c) {
        System.out.println("-- Showing recent messages:");
        System.out.println(c.getMessages());
        System.out.println("-- Recent messages displayed above.");
    }

    private static void showAllMessages(Client c) {
        System.out.println("-- Showing ALL recent messages:");
        System.out.println(c.getAllMessages());
        System.out.println("-- ALL recent messages displayed above.");
    }
}
