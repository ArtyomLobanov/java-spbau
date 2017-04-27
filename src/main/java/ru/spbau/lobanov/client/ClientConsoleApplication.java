package ru.spbau.lobanov.client;

import java.util.Scanner;

/**
 * Console application - wrapper under Client-class
 * Execute commands from standard input
 */
public class ClientConsoleApplication {
    public static void main(String[] args)  {
        Client client = new Client(System.out);
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            try {
                String command = scanner.next();
                if (command.equals("set_server")) {
                    client.setServer(scanner.next(), scanner.nextInt());
                } else if (command.equals("clear_server")) {
                    client.clearServer();
                } else if (command.equals("exit")) {
                    scanner.close();
                    return;
                } else if (command.equals("list_files")) {
                    FileDescriptor[] files = client.listFiles(scanner.nextLine().trim());
                    for (FileDescriptor descriptor : files) {
                        System.out.println("  -  " + descriptor.getName() + ", isFolder = " + descriptor.isFolder());
                    }
                } else if (command.equals("get_file")) {
                    client.getFile(scanner.next(), scanner.next());
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
