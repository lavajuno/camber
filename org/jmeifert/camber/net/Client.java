package org.jmeifert.camber.net;

import org.jmeifert.camber.file.Log;
import org.jmeifert.camber.security.AESSuite;
import org.jmeifert.camber.security.RSASuite;
import org.jmeifert.camber.security.SHAutil;
import org.jmeifert.camber.util.Format;
import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.GeneralSecurityException;

/**
 * Client handles the client side of the connection and provides functions for basic application features such as
 * sending and getting messages, switching and getting topics, and closing the connection.
 */
public class Client {
    boolean open = false;
    Socket socket;
    String hostname;
    String group;
    String topic = "default";
    String hashedGroupPassword;
    String nickname;
    int port;
    ObjectInputStream inFromServer;
    ObjectOutputStream outToServer;
    Request clientReq;
    Request serverReq;
    RSASuite rsaSuite;
    AESSuite aesSuite;
    String srBody;

    /**
     * Instantiates a Client.
     * @param hostname The server hostname to connect to
     * @param port The server port to connect to
     * @param group The group name to join
     * @param groupPassword The group's password
     * @param nickname The user's nickname in the group
     * @throws IllegalArgumentException Throws an IllegalArgumentException if the hostname or port is invalid
     */
    public Client(String hostname, int port, String group, String groupPassword,
                  String nickname) throws IllegalArgumentException {
        // Check args
        if (port <= 1024 || port > 65535) {
            throw new IllegalArgumentException("Client: Invalid port.");
        }
        if (hostname == null) {
            throw new IllegalArgumentException("Client: Invalid hostname.");
        }
        if (!Format.isSafeAscii(group) || !Format.isSafeAscii(nickname) ||
                !Format.isSafeAscii(groupPassword)) {
            throw new IllegalArgumentException("Client: Unsafe string parameter(s).");
        }
        // Instantiate
        this.hostname = hostname;
        this.port = port;
        this.group = group;
        this.nickname = nickname;
        this.hashedGroupPassword = SHAutil.getHash(groupPassword);
    }

    /**
     * Opens a connection to the server.
     * @throws IOException Throws an IOException if opening the connection fails.
     */
    public void open() throws IOException {
        try {
            // Set up socket
            Log.log("Opening connection to " + hostname + "...");
            socket = new Socket(hostname, port);
            inFromServer = new ObjectInputStream(socket.getInputStream());
            outToServer = new ObjectOutputStream(socket.getOutputStream());
            rsaSuite = new RSASuite();

            // Step 1 - Handshake
            clientReq = new Request(Request.Types.handshake);
            outToServer.writeObject(clientReq);
            outToServer.flush();
            serverReq = (Request) inFromServer.readObject();
            if (serverReq.getType() == Request.Types.handshakeResponse) {
                Log.log("Handshake completed.");
            } else if (serverReq.getType() == Request.Types.sequenceError) {
                System.err.println("Connection establishment error - Sequence (Handshake).");
                throw new IOException("Connection establishment error - Sequence (Handshake).");
            } else {
                System.err.println("Connection establishment error - Unexpected response.");
                throw new IOException("Connection establishment error - Unexpected response.");
            }

            // Step 2 - Encryption
            clientReq = new Request(Request.Types.clientKey, rsaSuite.getPublicKey());
            outToServer.writeObject(clientReq);
            outToServer.flush();
            serverReq = (Request) inFromServer.readObject();
            if (serverReq.getType() == Request.Types.serverKey) {
                aesSuite = new AESSuite(rsaSuite.decryptBytes(serverReq.getBytes()));
                Log.log("Encryption setup completed.");
            } else if (serverReq.getType() == Request.Types.sequenceError) {
                System.err.println("Connection establishment error - Sequence (Encryption).");
                throw new IOException("Connection establishment error - Sequence (Encryption).");
            } else {
                System.err.println("Connection establishment error - Unexpected response.");
                throw new IOException("Connection establishment error - Unexpected response.");
            }

            // Step 3 - Group
            clientReq = new Request(Request.Types.setGroup, aesSuite.encryptString(group));
            outToServer.writeObject(clientReq);
            outToServer.flush();
            serverReq = (Request) inFromServer.readObject();
            if (serverReq.getType() == Request.Types.groupConfirm) {
                Log.log("Group setup completed.");
            } else if (serverReq.getType() == Request.Types.groupError) {
                System.err.println("Connection establishment error - Group not found.");
                throw new IOException("Connection establishment error - Group not found.");
            } else if (serverReq.getType() == Request.Types.sequenceError) {
                System.err.println("Connection establishment error - Sequence (Group).");
                throw new IOException("Connection establishment error - Sequence (Group).");
            } else {
                System.err.println("Connection establishment error - Unexpected response.");
                throw new IOException("Connection establishment error - Unexpected response.");
            }

            // Step 4 - Group Password
            clientReq = new Request(Request.Types.setPassword, aesSuite.encryptString(hashedGroupPassword));
            outToServer.writeObject(clientReq);
            outToServer.flush();
            serverReq = (Request) inFromServer.readObject();
            if (serverReq.getType() == Request.Types.passwordConfirm) {
                Log.log("Credentials setup completed.");
            } else if (serverReq.getType() == Request.Types.passwordError) {
                System.err.println("Connection establishment error - Invalid password.");
                throw new IOException("Connection establishment error - Invalid password.");
            } else if (serverReq.getType() == Request.Types.sequenceError) {
                System.err.println("Connection establishment error - Sequence (Group Password).");
                throw new IOException("Connection establishment error - Sequence (Group Password).");
            } else {
                System.err.println("Connection establishment error - Unexpected response.");
                throw new IOException("Connection establishment error - Unexpected response.");
            }

            // Step 5 - Nickname
            clientReq = new Request(Request.Types.setNickname, aesSuite.encryptString(nickname));
            outToServer.writeObject(clientReq);
            outToServer.flush();
            serverReq = (Request) inFromServer.readObject();
            if (serverReq.getType() == Request.Types.nicknameConfirm) {
                Log.log("Nickname setup completed.");
            } else if (serverReq.getType() == Request.Types.nicknameError) {
                System.err.println("Connection establishment error - Invalid nickname.");
                throw new IOException("Connection establishment error - Invalid nickname.");
            } else if (serverReq.getType() == Request.Types.sequenceError) {
                System.err.println("Connection establishment error - Sequence (Group).");
                throw new IOException("Connection establishment error - Sequence (Group).");
            } else {
                System.err.println("Connection establishment error - Unexpected response.");
                throw new IOException("Connection establishment error - Unexpected response.");
            }

            Log.log("Connected to " + hostname + ".");
            open = true;

        } catch (UnknownHostException e) {
            System.err.println("Could not connect to " + hostname + ":" + port + ": Unknown host.");
            throw new IOException("Could not connect to " + hostname + ":" + port + ": Unknown host.");
        } catch (IOException e) {
            System.err.println("Could not connect to " + hostname + ":" + port + ": IOException encountered.");
            throw new IOException("Could not connect to " + hostname + ":" + port + ": IOException encountered.");
        } catch (ClassNotFoundException e) {
            System.err.println("Could not connect to " + hostname + ":" + port + ": " +
                    "ClassNotFoundException encountered.");
            throw new IOException("Could not connect to " + hostname + ":" + port +
                    ": ClassNotFoundException encountered.");
        }
    }

    /**
     * Closes the connection to the server.
     * @throws IOException Throws an IOException if closing the connection fails.
     */
    public void close() throws IOException {
        try {
            try {
                clientReq = new Request(Request.Types.goodbye);
                outToServer.writeObject(clientReq);
                outToServer.flush();
                serverReq = (Request) inFromServer.readObject();
                if (serverReq.getType() != Request.Types.goodbyeResponse) {
                    System.err.println("Error terminating connection gracefully.");
                }
            } catch (ClassNotFoundException e) {
                System.err.println("Error terminating connection gracefully.");
            }
            outToServer.close();
            inFromServer.close();
            socket.close();
            open = false;
        } catch (IOException e) {
            System.err.println("Could not disconnect from " + hostname + ":" + port + ": IOException encountered.");
            open = false;
            throw new IOException("Could not disconnect from " + hostname + ":" + port + ": IOException encountered.");
        }
    }

    private void putRequest(Request request) {
        try {
            outToServer.writeObject(request);
            outToServer.flush();
        } catch(IOException e) {
            System.err.println("Client: Failed to write to connection");
        }

    }

    private Request getRequest() {
        try {
            return (Request) inFromServer.readObject();
        } catch(IOException e) {
            System.err.println("Client: Failed to read from connection.");
            return null;
        } catch(ClassNotFoundException e) {
            System.err.println("Client: Class not found.");
            return null;
        }
    }

    /**
     * Sends a message.
     * @param message Message to send
     * @return Returns true if sending the message succeeds
     */
    public boolean sendMessage(String message) throws IllegalArgumentException {
        if (!open) { throw new IllegalStateException(); }
        if (!Format.isValidMessage(message)) {
            throw new IllegalArgumentException("sendMessage: Invalid message.");
        }
        putRequest(new Request(Request.Types.sendMessage, aesSuite.encryptString(message)));
        serverReq = getRequest();
        if (serverReq == null) {
            System.err.println("sendMessage: Bad response.");
            return false;
        }
        if (serverReq.getType() == Request.Types.messageConfirm) {
            return true;
        } else if (serverReq.getType() == Request.Types.messageError) {
            System.err.println("sendMessage: Server rejected message.");
            return false;
        } else {
            System.err.println("sendMessage: Unexpected response - " + serverReq.getType());
            return false;
        }
    }

    /**
     * Gets recent messages from the current topic.
     * @return Recent messages. Returns "" if recent messages cannot be retrieved
     */
    public String getMessages() {
        if (!open) { throw new IllegalStateException(); }
        putRequest(new Request(Request.Types.getMessages));
        serverReq = getRequest();
        if (serverReq == null) {
            System.err.println("getMessages: Bad response.");
            return "";
        }
        if (serverReq.getType() == Request.Types.messages) {
            try {
                srBody = aesSuite.decryptString(serverReq.getBytes());
            } catch (GeneralSecurityException e) {
                throw new RuntimeException("getMessages: GeneralSecurityException");
            }
            return srBody;
        } else {
            System.err.println("sendMessage: Unexpected response - " + serverReq.getType());
            return "";
        }

    }

    /**
     * Gets recent messages from all topics.
     * @return Recent messages. Returns "" if recent messages cannot be retrieved
     */
    public String getAllMessages() {
        if (!open) { throw new IllegalStateException(); }
        putRequest(new Request(Request.Types.getAllMessages));
        serverReq = getRequest();
        if (serverReq == null) {
            System.err.println("getAllMessages: Bad response.");
            return "";
        }
        if (serverReq.getType() == Request.Types.messages) {
            try {
                srBody = aesSuite.decryptString(serverReq.getBytes());
            } catch (GeneralSecurityException e) {
                throw new RuntimeException("getAllMessages: GeneralSecurityException");
            }
            return srBody;
        } else {
            System.err.println("sendMessage: Unexpected response - " + serverReq.getType());
            return "";
        }

    }
    /**
     * Sets the active topic.
     * @param newTopic Topic to change to
     * @return Returns true if setting the topic is successful
     */
    public boolean setTopic(String newTopic) throws IllegalArgumentException {
        if (!open) { throw new IllegalStateException(); }
        if (!Format.isValidName(newTopic)) {
            throw new IllegalArgumentException("sendMessage: Invalid topic.");
        }
        putRequest(new Request(Request.Types.setTopic, aesSuite.encryptString(newTopic)));
        serverReq = getRequest();
        if (serverReq == null) {
            System.err.println("sendMessage: Bad response.");
            return false;
        }
        if (serverReq.getType() == Request.Types.topicConfirm) {
            this.topic = newTopic;
            return true;
        } else if (serverReq.getType() == Request.Types.topicError) {
            System.err.println("sendMessage: Server rejected topic.");
            return false;
        } else {
            System.err.println("sendMessage: Unexpected response - " + serverReq.getType());
            return false;
        }
    }

    /**
     * Gets active topics.
     * @return Active topics. Returns "" if active topics cannot be retrieved
     */
    public String getTopics() {
        if (!open) { throw new IllegalStateException(); }
        putRequest(new Request(Request.Types.getTopics));
        serverReq = getRequest();
        if (serverReq == null) {
            System.err.println("getMessages: Bad response.");
            return "";
        }
        if (serverReq.getType() == Request.Types.topics) {
            try {
                srBody = aesSuite.decryptString(serverReq.getBytes());
            } catch (GeneralSecurityException e) {
                throw new RuntimeException("getMessages: GeneralSecurityException");
            }
            return srBody;
        } else {
            System.err.println("sendMessage: Unexpected response - " + serverReq.getType());
            return "";
        }
    }
}
