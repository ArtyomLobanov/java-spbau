package ru.spbau.lobanov.client;

import com.google.common.io.Files;
import org.jetbrains.annotations.NotNull;
import ru.spbau.lobanov.Connection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Class which provides an api to work as client
 */
public class Client {

    private final PrintStream logStream;
    private String host;
    private int port = -1;

    public Client(PrintStream logStream) {
        this.logStream = logStream;
    }

    /**
     * Method to set server's address
     *
     * @param host server's host
     * @param port server's port
     */
    public void setServer(@NotNull String host, int port) {
        this.host = host;
        this.port = port;
        logStream.println("Server address updated");
    }

    /**
     * Remove information about current server
     */
    void clearServer() {
        host = null;
        port = -1;
        logStream.println("Server address cleared");
    }

    /**
     * Open connection to current server, request list of files
     * in directory defined by path-argument
     *
     * @param path path to interesting folder
     * @return array of FileDescriptors contained information about files in interesting folder
     * @throws ClientException if some problems with connection appeared
     */
    @NotNull
    public FileDescriptor[] listFiles(@NotNull String path) throws ClientException {
        if (host == null || port == -1) {
            throw new ClientException("Target server wasn't set");
        }
        FileDescriptor[] descriptors;
        try (Connection connection = Connection.openConnection(host, port, Connection.Command.LIST_FILES, path)) {
            int count = connection.in.readInt();
            descriptors = new FileDescriptor[count];
            for (int i = 0; i < count; i++) {
                descriptors[i] = new FileDescriptor(connection.in.readUTF(), path, connection.in.readBoolean());
            }
        } catch (IOException e) {
            throw new ClientException("Failed to open connection to server");
        }
        logStream.println("ListFile-request successfully finished");
        return descriptors;
    }

    /**
     * Open connection to current server,
     * copy file from server to you computer
     *
     * @param path   path to interesting file
     * @param target expected path to copy
     * @return File associated with your copy
     * @throws ClientException if some problems with connection appeared
     */
    @NotNull
    public File getFile(@NotNull String path, @NotNull String target) throws ClientException {
        if (host == null && port == -1) {
            throw new ClientException("Target server wasn't set");
        }
        File file = new File(target);
        try {
            Files.createParentDirs(file);
            Files.touch(file);
        } catch (Exception e) {
            throw new ClientException("Failed to create target file");
        }
        try (FileOutputStream fileStream = new FileOutputStream(file);
             Connection connection = Connection.openConnection(host, port, Connection.Command.GET_FILE, path)) {
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
        ClientException(@NotNull String message) {
            super(message);
        }

        ClientException(@NotNull String message, @NotNull Throwable cause) {
            super(message, cause);
        }
    }
}
