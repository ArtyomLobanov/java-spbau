package ru.spbau.lobanov.client;

import ru.spbau.lobanov.server.CommandExecutor;

import java.io.*;
import java.net.Socket;

/**
 * Created by Артём on 27.04.2017.
 */
public class Client {
    private Socket socket;

    public void connect(String host, int port) throws IOException {
        socket = new Socket(host, port);
    }

    public void disconnect() {
//        socket.shutdownOutput();
//        socket.shutdownInput();
//        socket.close();
    }

    public static void main(String[] args) throws IOException {
        Client client = new Client();
//        client.connect();
    }

}
