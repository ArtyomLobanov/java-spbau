package ru.spbau.lobanov.server;

import java.io.IOException;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 *
 */
public class Server {

    private volatile Thread thread;

    private final PrintStream logStream;


    public Server(PrintStream logStream) {
        this.logStream = logStream;
    }

    /**
     * Stop server, interrupt ServerRunner's thread
     *
     * @throws ServerException if server isn't running now
     */
    public synchronized void stop() throws ServerException {
        if (thread == null) {
            throw new ServerException("Try to stop server, but it isn't running now");
        }
        thread.interrupt();
        thread = null;
        logStream.println("Server successfully stopped");
    }

    /**
     * Should be called in case if ServerRunner
     * was stopped with error
     */
    private synchronized void aborted() {
        thread = null;
        logStream.println("Server aborted");
    }

    /**
     * Start server, run ServerRunner's thread
     *
     * @param port port which will be used by server
     * @throws ServerException if server is already running now
     */
    public synchronized void start(int port) throws ServerException {
        if (thread != null) {
            throw new ServerException("Try to start server, but it's already started");
        }
        thread = new Thread(new ServerRunner(port));
        thread.start();
        logStream.println("Server successfully started");
    }

    /**
     * Help class, which create ServerSocket, creates connections
     * and call CommandExecutor to process commands
     */
    private class ServerRunner implements Runnable {

        private final int port;

        ServerRunner(int port) {
            this.port = port;
        }

        @Override
        public void run() {
            ExecutorService threadPool = Executors.newCachedThreadPool();
            CommandExecutor commandExecutor = new CommandExecutor(logStream);
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                serverSocket.setSoTimeout(100);
                while (!Thread.interrupted()) {
                    try {
                        Socket connection = serverSocket.accept();
                        threadPool.execute(() -> {
                            commandExecutor.execute(connection);
                        });
                    } catch (SocketTimeoutException ignored) {
                    }
                }
            } catch (Exception exception) {
                logStream.println(exception.getMessage());
                aborted();
            } finally {
                threadPool.shutdown();
            }
        }
    }

    /**
     * @return true if server is active
     */
    public synchronized boolean isRunning() {
        return thread != null && thread.isAlive();
    }

    public class ServerException extends Exception {
        ServerException(String message) {
            super(message);
        }
    }
}
