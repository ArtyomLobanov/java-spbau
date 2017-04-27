package ru.spbau.lobanov.server;

import ru.spbau.lobanov.Connection;

import java.io.*;
import java.net.Socket;


public class CommandExecutor {

    public static final int LIST_COMMAND = 1;
    public static final int GET_COMMAND = 2;

    private final PrintStream errorStream;

    CommandExecutor(PrintStream errorStream) {
        this.errorStream = errorStream;
    }

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

    private void listCommand(String path, DataOutputStream outputStream) throws IOException {
        File folder = new File(path);
        System.out.println(folder.getAbsolutePath());
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

    private void getCommand(String path, DataOutputStream outputStream) throws IOException {
        File file = new File(path);
        if (!file.exists() || !file.isFile()) {
            outputStream.writeInt(0);
            return;
        }
        System.out.println(file.length());
        outputStream.writeLong(file.length());
        FileInputStream fileStream = new FileInputStream(file);
        byte[] buffer = new byte[1024];
        int count;
        try {
            do {
                count = fileStream.read(buffer);
                outputStream.write(buffer, 0, count);
            } while (count != 0);
        }catch (Exception e) {
            System.out.println("e:" + e.getMessage());
        }
    }

    public class WrongRequestFormatException extends Exception {
        WrongRequestFormatException(String message) {
            super(message);
        }
    }
}
