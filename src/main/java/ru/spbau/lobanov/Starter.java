package ru.spbau.lobanov;

import org.jetbrains.annotations.NotNull;
import ru.spbau.lobanov.client.ClientConsoleApplication;
import ru.spbau.lobanov.gui.GraphicalApplication;
import ru.spbau.lobanov.server.ServerConsoleApplication;

/**
 * Class which run one of possible application
 */
public class Starter {
    public static void main(@NotNull String[] args) {
        if (args.length != 1) {
            System.out.println("Wrong number of arguments");
        }
        switch (args[0]) {
            case "server":
                ServerConsoleApplication.main(new String[0]);
                break;
            case "client":
                ClientConsoleApplication.main(new String[0]);
                break;
            case "gui":
                GraphicalApplication.main(new String[0]);
                break;
            default:
                System.out.println("Unknown module");
                break;
        }
    }
}
