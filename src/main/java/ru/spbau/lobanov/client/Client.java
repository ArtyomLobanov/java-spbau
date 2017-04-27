package ru.spbau.lobanov.client;

import com.google.common.io.Files;
import ru.spbau.lobanov.Connection;
import ru.spbau.lobanov.server.CommandExecutor;

import java.io.*;
import java.net.Socket;

public class Client {
    private final PrintStream logStream;
    private String host;
    private int port;

    public Client(PrintStream logStream) {
        this.logStream = logStream;
    }

    public synchronized void setServer(String host, int port) throws ClientException, IOException {
        this.host = host;
        this.port = port;
        logStream.println("Server address updated");
    }

    public synchronized void clearServer() throws ClientException, IOException {
        host = null;
        port = -1;
        logStream.println("Server address cleared");
    }

    public synchronized FileDescriptor[] listFiles(String path) throws ClientException, IOException {
        FileDescriptor[] descriptors;
        try (Socket socket = new Socket(host, port);
             Connection connection = new Connection(socket)) {
            connection.out.writeInt(CommandExecutor.LIST_COMMAND);
            connection.out.writeUTF(path);
            connection.out.flush();
            int count = connection.in.readInt();
            descriptors = new FileDescriptor[count];
            for (int i = 0; i < count; i++) {
                descriptors[i] = new FileDescriptor(connection.in.readUTF(), path, connection.in.readBoolean());
            }
        }
        logStream.println("ListFile-request successfully finished");
        return descriptors;
    }

    public synchronized File getFile(String path, String target) throws ClientException {
        File file = new File(target);
        try {
            Files.createParentDirs(file);
            Files.touch(file);
        } catch (Exception e) {
            throw new ClientException("Failed to create target file");
        }
        try (FileOutputStream fileStream = new FileOutputStream(file);
             Socket socket = new Socket(host, port);
             Connection connection = new Connection(socket)) {
            connection.out.writeInt(CommandExecutor.GET_COMMAND);
            connection.out.writeUTF(path);
            connection.out.flush();
            long count = connection.in.readLong();
            if (count == 0) {
                throw new ClientException("Server hasn't found requested file");
            }
            byte[] buffer = new byte[1024];
            while (count > 0) {
                int bytesRead = connection.in.read(buffer);
                fileStream.write(buffer, 0, bytesRead);
                count -= bytesRead;
            }
        } catch (IOException e) {
            throw new ClientException("Exception occurred during get command", e);
        }
        logStream.println("GetFile-request successfully finished");
        return file;
    }

    public class ClientException extends Exception {
        public ClientException(String message) {
            super(message);
        }

        public ClientException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
