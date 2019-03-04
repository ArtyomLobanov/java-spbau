package ru.spbau.lobanov.server;

import org.jetbrains.annotations.NotNull;
import ru.spbau.lobanov.Connection;
import ru.spbau.lobanov.Connection.Command;

import java.io.*;
import java.net.Socket;

/**
 * Class which help Server to execute commands
 */
public class CommandExecutor {

    private final PrintStream errorStream;

    CommandExecutor(@NotNull PrintStream errorStream) {
        this.errorStream = errorStream;
    }

    /**
     * Parse command from socket's input stream, execute it
     * and send by socket's output stream.
     *
     * @param socket socket connected with client
     */
    void execute(@NotNull Socket socket) {
        try (Connection connection = new Connection(socket)) {
            int commandCode = connection.in.readInt();
            if (commandCode == Command.LIST_FILES.protocolCode) {
                listCommand(connection.in.readUTF(), connection.out);
            } else if (commandCode == Command.GET_FILE.protocolCode) {
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
    private void listCommand(@NotNull String path, @NotNull DataOutputStream outputStream) throws IOException {
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
    private void getCommand(@NotNull String path, @NotNull DataOutputStream outputStream) throws IOException {
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
        WrongRequestFormatException(@NotNull String message) {
            super(message);
        }
    }
}
