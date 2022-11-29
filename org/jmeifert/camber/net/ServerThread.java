package org.jmeifert.camber.net;

import org.jmeifert.camber.data.Group;
import org.jmeifert.camber.data.Message;
import org.jmeifert.camber.data.ServerData;
import org.jmeifert.camber.file.Log;
import org.jmeifert.camber.security.AESSuite;
import org.jmeifert.camber.security.RSASuite;
import org.jmeifert.camber.util.ChatMap;
import org.jmeifert.camber.util.Format;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.GeneralSecurityException;

/**
 * ServerThread is a state machine that handles a single client connection and
 * syncs with the ServerData it is instantiated with.
 */
public class ServerThread extends Thread {
    final int INACTIVITY_TIMEOUT = ChatMap.INACTIVITY_TIMEOUT;

    public enum States {
        waitingForHandshake,
        waitingForClientKey,
        waitingForGroup,
        waitingForPassword,
        waitingForNickname,
        ready,
    }

    private States state = States.waitingForHandshake;
    private Socket socket;
    private Request clientReq;
    private Request serverReq;
    private ServerData serverData;
    private ObjectInputStream inFromClient;
    private ObjectOutputStream outToClient;
    private Group group;
    private String hashedGroupPassword;
    private String nickname;
    private String topic;
    private RSASuite rsaSuite;
    private AESSuite aesSuite;
    private String crBody;

    /**
     * Instantiates a ServerThread.
     * @param clientSocket Socket to communicate with client on
     * @param serverData ServerData instance to sync with
     */
    public ServerThread(Socket clientSocket, ServerData serverData) {
        this.socket = clientSocket;
        this.serverData = serverData;
    }

    @Override
    public void run() {
        try {
            socket.setSoTimeout(INACTIVITY_TIMEOUT);
            outToClient = new ObjectOutputStream(socket.getOutputStream());
            inFromClient = new ObjectInputStream(socket.getInputStream());

            Log.log("Connected " + socket.getInetAddress().toString() + "."); // DEBUG

            // Main Loop
            while(true) {
                clientReq = (Request) inFromClient.readObject(); // Get next input from client

                if (clientReq.getType() == Request.Types.goodbye) { // Handle graceful disconnect
                    serverReq = new Request(Request.Types.goodbyeResponse);
                    outToClient.writeObject(serverReq);
                    outToClient.flush();
                    Log.log("Disconnected " + socket.getInetAddress().toString() + "."); // DEBUG
                    close();
                    return;
                }

                if (clientReq.getType() == Request.Types.reset) { // Handle hard disconnect
                    Log.log("Forcefully disconnected " + socket.getInetAddress().toString() + "."); // DEBUG
                    close();
                    return;
                }

                // Act on current state
                switch (state) {
                    case waitingForHandshake: // Initial handshake - send response and advance
                        if (clientReq.getType() == Request.Types.handshake) {
                            serverReq = new Request(Request.Types.handshakeResponse);
                            state = States.waitingForClientKey;
                        } else {
                            serverReq = new Request(Request.Types.sequenceError);
                        }
                        outToClient.writeObject(serverReq);
                        outToClient.flush();
                        break;

                    case waitingForClientKey: // Client key - Set up encryption
                        if (clientReq.getType() == Request.Types.clientKey) {
                            rsaSuite = new RSASuite(clientReq.getBytes());
                            aesSuite = new AESSuite();
                            serverReq = new Request(Request.Types.serverKey, rsaSuite.encryptBytes(aesSuite.getKey()));
                            state = States.waitingForGroup;
                        } else {
                            serverReq = new Request(Request.Types.sequenceError);
                            state = States.waitingForHandshake;
                        }
                        outToClient.writeObject(serverReq);
                        outToClient.flush();
                        break;

                    case waitingForGroup: // Group - Set group (Encrypted)
                        if (clientReq.getType() == Request.Types.setGroup) {
                            crBody = aesSuite.decryptString(clientReq.getBytes());
                            if (Format.isValidName(crBody)) {
                                group = serverData.getGroup(crBody);
                                if (group != null) {
                                    serverReq = new Request(Request.Types.groupConfirm);
                                    state = States.waitingForPassword;
                                } else {
                                    serverReq = new Request(Request.Types.groupError);
                                    state = States.waitingForHandshake;
                                }
                            } else {
                                serverReq = new Request(Request.Types.groupError);
                                state = States.waitingForHandshake;
                            }
                        } else {
                            serverReq = new Request(Request.Types.sequenceError);
                            state = States.waitingForHandshake;
                        }
                        outToClient.writeObject(serverReq);
                        outToClient.flush();
                        break;

                    case waitingForPassword: // Password - Set Password (Encrypted)
                        if (clientReq.getType() == Request.Types.setPassword) {
                            crBody = aesSuite.decryptString(clientReq.getBytes());
                            if (Format.isValidPasswordHash(crBody)) {
                                if (group.verifyPassword(crBody)) {
                                    hashedGroupPassword = crBody;
                                    serverReq = new Request(Request.Types.passwordConfirm);
                                    state = States.waitingForNickname;
                                } else {
                                    serverReq = new Request(Request.Types.passwordError);
                                    state = States.waitingForHandshake;
                                }
                            } else {
                                serverReq = new Request(Request.Types.passwordError);
                                state = States.waitingForHandshake;
                            }
                        } else {
                            serverReq = new Request(Request.Types.sequenceError);
                            state = States.waitingForHandshake;
                        }
                        outToClient.writeObject(serverReq);
                        outToClient.flush();
                        break;

                    case waitingForNickname: // Nickname - Set Nickname (Encrypted)
                        if (clientReq.getType() == Request.Types.setNickname) {
                            crBody = aesSuite.decryptString(clientReq.getBytes());
                            if (Format.isValidName(crBody)) {
                                nickname = crBody;
                                state = States.ready;
                                topic = "default";
                                serverReq = new Request(Request.Types.nicknameConfirm);
                            } else {
                                serverReq = new Request(Request.Types.nicknameError);
                                state = States.waitingForHandshake;
                            }
                        } else {
                            serverReq = new Request(Request.Types.sequenceError);
                            state = States.waitingForHandshake;
                        }
                        outToClient.writeObject(serverReq);
                        outToClient.flush();
                        break;

                    case ready: // Ready - Normal operation (Encrypted)
                        // Act on request type
                        switch (clientReq.getType()) {
                            case sendMessage: // Send a message
                                crBody = aesSuite.decryptString(clientReq.getBytes());
                                if (Format.isValidMessage(crBody)) {
                                    group.addMessage(new Message(this.topic, nickname, crBody));
                                }
                                serverReq = new Request(Request.Types.messageConfirm);
                                break;

                            case getMessages: // Get messages in current topic
                                serverReq = new Request(Request.Types.messages,
                                        aesSuite.encryptString(group.getMessages(this.topic)));
                                break;

                            case getAllMessages: // Get messages from all topics
                                crBody = aesSuite.decryptString(clientReq.getBytes());
                                serverReq = new Request(Request.Types.messages,
                                        aesSuite.encryptString(group.getMessages()));
                                break;

                            case getTopics: // Get active topics
                                serverReq = new Request(Request.Types.topics,
                                        aesSuite.encryptString(group.getTopics()));
                                break;

                            case setTopic: // Change topic
                                crBody = aesSuite.decryptString(clientReq.getBytes());
                                if (Format.isValidName(crBody)) {
                                    this.topic = crBody;
                                    serverReq = new Request(Request.Types.topicConfirm);
                                } else {
                                    serverReq = new Request(Request.Types.topicError);
                                }
                                break;

                            default: // If request type is not valid
                                serverReq = new Request(Request.Types.illegalRequestError);
                                break;
                        }
                        outToClient.writeObject(serverReq); // Send response
                        outToClient.flush();
                }
            }
        } catch (SocketTimeoutException e) {
            Log.log("Connection to " + socket.getInetAddress().toString() + " timed out.", 1);
            close();
        } catch (IOException e) {
            Log.log("IOException serving " + socket.getInetAddress().toString() +
                    ". - " + e.getMessage(), 1);
            close();
        } catch (ClassNotFoundException e) {
            Log.log("ClassNotFoundException serving " + socket.getInetAddress().toString() +
                    ". - " + e.getMessage(), 2);
            close();
        } catch (GeneralSecurityException e) {
            Log.log("GeneralSecurityException serving " + socket.getInetAddress().toString() +
                    ". - " + e.getMessage(), 2);
            close();
        } catch (Exception e) {
            Log.log("Unexpected exception serving " + socket.getInetAddress().toString() +
                    ". - " + e.getMessage(), 2);
            close();
        }
    }

    /**
     * Attempts to close the socket gracefully. If it can't, force it to close.
     */
    public void close() {
        try {
            socket.close();
        } catch (IOException f) {
            socket = null;
        }
    }
}
