package org.jmeifert.camber.net;

import org.jmeifert.camber.data.ServerData;
import org.jmeifert.camber.file.Log;
import org.jmeifert.camber.util.ChatMap;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.*;

/**
 * Server accepts client connections and manages the data sent and received by
 * individual server threads. In addition, it handles the creation,
 * synchronization, and destruction of these threads.
 */
public class Server extends Thread {
    private final int THREAD_POOL_SIZE = ChatMap.MAX_CONCURRENT_CONNECTIONS;

    private int port;
    ServerSocket serverSocket = null;
    ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
    ServerData serverData = new ServerData();

    /**
     * Creates an instance of ServerManager on a specified port.
     * @param port The port to listen on.
     * @throws IllegalArgumentException If the port specified is invalid.
     */
    public Server(int port) throws IllegalArgumentException {
        if (port <= 1024 || port > 65535) {
            throw new IllegalArgumentException("Server: Invalid port.");
        }
        this.port = port;
        this.serverData.createGroup("default", "default");
    }

    @Override
    public void run() {
        Log.log("ServerInterface: Created.");
        try {
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            System.err.println("Server: Failed to create ServerSocket.");
            throw new RuntimeException("Server: Failed to create ServerSocket.");
        }
        while (true) {
            try {
                threadPool.submit(new ServerThread(serverSocket.accept(), serverData));
            } catch (IOException e) {
                System.err.println("Failed to accept connection (IOException).");
            } catch (NullPointerException e) {
                System.err.println("Failed to accept connection (NullPointerException).");
            } catch (RejectedExecutionException e) {
                System.err.println("Failed to accept connection (Thread pool full).");
            }
        }
    }

    /**
     * Shuts down the server.
     */
    public void close() {
        Log.log("Server: Closing...");
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(10, TimeUnit.SECONDS)) {
                Log.log("Server: Graceful shutdown is taking too long, forcing hard shutdown...");
                threadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            Log.log("Server: Encountered exception during graceful shutdown, forcing hard shutdown...");
            threadPool.shutdownNow();
        }
        Log.log("Server: Closed.");
    }
}