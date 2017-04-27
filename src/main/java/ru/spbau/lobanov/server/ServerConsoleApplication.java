package ru.spbau.lobanov.server;

import java.util.Scanner;

/**
 * Console application - wrapper under Server-class
 * Execute commands from standard input
 */
public class ServerConsoleApplication {
    public static void main(String[] args)  {
        Server server = new Server(System.out);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("server>");
            try {
                String command = scanner.nextLine().trim();
                if (command.equals("start")) {
                    System.out.print("port:");
                    server.start(scanner.nextInt());
                    scanner.nextLine();
                } else if (command.equals("stop")) {
                    server.stop();
                } else if (command.equals("exit")) {
                    if (server.isRunning()) {
                        System.out.println("Stop server first!");
                    } else {
                        scanner.close();
                        return;
                    }
                } else {
                    System.out.println("Unknown command or wrong format");
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
