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
 * Created by Артём on 27.04.2017.
 */
public class Server {

    private volatile Thread thread;

    private final Object threadLock = new Object();

    private final PrintStream logStream;


    Server(PrintStream logStream) {
        this.logStream = logStream;
    }

    public void stop() throws ServerException {
        synchronized (threadLock) {
            if (thread == null) {
                throw new ServerException("Try to stop server, but it isn't running now");
            }
            thread.interrupt();
            thread = null;
            logStream.println("Server successfully stopped");
        }
    }

    private void aborted() {
        synchronized (threadLock) {
            thread = null;
            logStream.println("Server aborted");
        }
    }

    public void start(int port) throws ServerException {
        synchronized (threadLock) {
            if (thread != null) {
                throw new ServerException("Try to start server, but it's already started");
            }
            thread = new Thread(new ServerRunner(port));
            thread.start();
            logStream.println("Server successfully started");
        }
    }

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
                    } catch (SocketTimeoutException ignored) {}
                }
            } catch (Exception exception) {
                logStream.println(exception.getMessage());
                aborted();
            } finally {
                threadPool.shutdown();
            }
        }
    }

    public boolean isRunning() {
        return thread != null && thread.isAlive();
    }

    public class ServerException extends Exception {
        ServerException(String message) {
            super(message);
        }
    }
}
