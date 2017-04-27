package ru.spbau.lobanov.server;

import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Created by Артём on 27.04.2017.
 */
public class Connection implements AutoCloseable {

    private final Socket socket;
    final DataInputStream in;
    final DataOutputStream out;

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
