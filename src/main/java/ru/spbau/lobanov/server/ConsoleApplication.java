package ru.spbau.lobanov.server;

import java.util.Scanner;

/**
 * Created by Артём on 27.04.2017.
 */
public class ConsoleApplication {
    public static void main(String[] args)  {
        Server server = new Server(System.out);
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            try {
                String command = scanner.next();
                if (command.equals("start") && scanner.hasNextInt()) {
                    server.start(scanner.nextInt());
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
                    scanner.nextLine();
                }
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
