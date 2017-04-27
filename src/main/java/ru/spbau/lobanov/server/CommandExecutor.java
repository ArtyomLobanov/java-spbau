package ru.spbau.lobanov.server;

import ru.spbau.lobanov.Connection;

import java.io.*;
import java.net.Socket;

/**
 * Class which help Server to execute commands
 */
public class CommandExecutor {

    public static final int LIST_COMMAND = 1;
    public static final int GET_COMMAND = 2;

    private final PrintStream errorStream;

    CommandExecutor(PrintStream errorStream) {
        this.errorStream = errorStream;
    }

    /**
     * Parse command from socket's input stream, execute it
     * and send by socket's output stream.
     *
     * @param socket socket connected with client
     */
    void execute(Socket socket) {
        try (Connection connection = new Connection(socket)) {
            int commandCode = connection.in.readInt();
            if (commandCode == LIST_COMMAND) {
                listCommand(connection.in.readUTF(), connection.out);
            } else if (commandCode == GET_COMMAND) {
                getCommand(connection.in.readUTF(), connection.out);
            } else {
                throw new WrongRequestFormatException("Unknown command code: " + commandCode);
            }
            connection.out.flush();
        } catch (IOException | WrongRequestFormatException e) {
            errorStream.println(e.getMessage());
        }
    }

    /**
     * Realize logic of listCommand: write down list of
     * files in interesting directory to outputStream
     *
     * @param path         path to interesting folder
     * @param outputStream stream to write down results
     * @throws IOException if some I/O problems occurred
     */
    private void listCommand(String path, DataOutputStream outputStream) throws IOException {
        File folder = new File(path);
        if (!folder.exists() || !folder.isDirectory()) {
            outputStream.writeInt(0);
            return;
        }
        File[] files = folder.listFiles();
        if (files == null) {
            throw new IOException("Unknown exception: can't get list of nested files");
        }
        outputStream.writeInt(files.length);
        for (File file : files) {
            outputStream.writeUTF(file.getName());
            outputStream.writeBoolean(file.isDirectory());
        }
    }

    /**
     * Realize logic of getCommand: copy content of
     * interesting file to outputStream
     *
     * @param path         path to interesting file
     * @param outputStream stream to write down results
     * @throws IOException if some I/O problems occurred
     */
    private void getCommand(String path, DataOutputStream outputStream) throws IOException {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            outputStream.writeInt(0);
            return;
        }
        outputStream.writeLong(file.length());
        FileInputStream fileStream = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        try {
            int count  = fileStream.read(buffer);
            while (count != -1) {
                outputStream.write(buffer, 0, count);
                count = fileStream.read(buffer);
            }
        } catch (IOException e) {
            errorStream.println("Error during get-command:" + e.getMessage());
            throw e;
        }
    }

    public class WrongRequestFormatException extends Exception {
        WrongRequestFormatException(String message) {
            super(message);
        }
    }
}
