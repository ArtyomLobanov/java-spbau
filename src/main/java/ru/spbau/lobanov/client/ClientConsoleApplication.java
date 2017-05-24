package ru.spbau.lobanov.client;

import org.jetbrains.annotations.Nullable;

import java.util.Scanner;

/**
 * Console application - wrapper under Client-class
 * Execute commands from standard input
 */
public class ClientConsoleApplication {
    public static void main(@Nullable String[] args) {
        Client client = new Client(System.out);
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("client>");
            try {
                String command = scanner.nextLine().trim();
                switch (command) {
                    case "set_server":
                        System.out.print("host:");
                        String host = scanner.nextLine().trim();
                        System.out.print("port:");
                        client.setServer(host, scanner.nextInt());
                        scanner.nextLine();
                        break;
                    case "clear_server":
                        client.clearServer();
                        break;
                    case "exit":
                        scanner.close();
                        return;
                    case "list_files": {
                        System.out.print("path:");
                        String path = scanner.nextLine().trim();
                        FileDescriptor[] files = client.listFiles(path);
                        System.out.println("File at folder:" + path);
                        for (FileDescriptor descriptor : files) {
                            System.out.println("  -  " + descriptor.getName() + ", isFolder = " + descriptor.isFolder());
                        }
                        System.out.println();
                        break;
                    }
                    case "get_file": {
                        System.out.print("path to file:");
                        String path = scanner.nextLine().trim();
                        System.out.print("path to copy:");
                        String copy = scanner.nextLine().trim();
                        client.getFile(path, copy);
                        break;
                    }
                    default:
                        System.out.println("Unknown command or wrong format");
                        System.out.println("Possible commands: set_server, clear_server, list_files, get_file, exit");
                        break;
                }
            } catch (Exception e) {
                System.out.println("Bad command or argument. Error:" + e.getMessage());
            }
        }
    }
}
