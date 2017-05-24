package ru.spbau.lobanov;

import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Class which help to work with sockets
 * Contained wrappers under socket's input/output streams
 */
public class Connection implements AutoCloseable {

    public enum Command {
        LIST_FILES(1),
        GET_FILE(2);

        public final int protocolCode;

        Command(int protocolCode) {
            this.protocolCode = protocolCode;
        }
    }

    private final Socket socket;
    public final DataInputStream in;
    public final DataOutputStream out;

    public Connection(@NotNull Socket socket) throws IOException {
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    public static Connection openConnection(@NotNull String host, int port, @NotNull Command command,
                                            @NotNull String path) throws IOException {
        Socket socket = new Socket(host, port);
        Connection connection = new Connection(socket);
        try {
            connection.out.writeInt(command.protocolCode);
            connection.out.writeUTF(path);
            connection.out.flush();
        } catch (IOException e) {
            connection.close();
            throw e;
        }
        return connection;
    }

    /**
     * Close both streams and socket
     */
    @Override
    public void close() {
        if (in != null) {
            try {
                in.close();
            } catch (Exception ignored) {
            }
        }
        if (out != null) {
            try {
                out.close();
            } catch (Exception ignored) {
            }
        }
        if (socket != null) {
            try {
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
            } catch (Exception ignored) {
            }
        }
    }
}
