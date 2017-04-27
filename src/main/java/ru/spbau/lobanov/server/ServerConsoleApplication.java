package ru.spbau.lobanov.server;

import org.jetbrains.annotations.Nullable;

import java.util.Scanner;

/**
 * Console application - wrapper under Server-class
 * Execute commands from standard input
 */
public class ServerConsoleApplication {
    public static void main(@Nullable String[] args)  {
        Server server = new Server(System.out);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("server>");
            try {
                String command = scanner.nextLine().trim();
                switch (command) {
                    case "start":
                        System.out.print("port:");
                        server.start(scanner.nextInt());
                        scanner.nextLine();
                        break;
                    case "stop":
                        server.stop();
                        break;
                    case "exit":
                        if (server.isRunning()) {
                            System.out.println("Stop server first!");
                        } else {
                            scanner.close();
                            return;
                        }
                        break;
                    default:
                        System.out.println("Unknown command or wrong format");
                        break;
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
