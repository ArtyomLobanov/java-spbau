package ru.spbau.lobanov;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Class which help to work with sockets
 * Contained wrappers under socket's input/output streams
 */
public class Connection implements AutoCloseable {

    private final Socket socket;
    public final DataInputStream in;
    public final DataOutputStream out;

    public Connection(Socket socket) throws IOException {
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    /**
     * Close both streams and socket
     */
    @Override
    public void close() {
        if (in != null) {
            try {
                in.close();
            } catch (Exception ignored){}
        }
        if (out != null) {
            try {
                out.close();
            } catch (Exception ignored){}
        }
        if (socket != null) {
            try {
                socket.shutdownInput();
                socket.shutdownOutput();
                socket.close();
            } catch (Exception ignored){}
        }
    }
}
