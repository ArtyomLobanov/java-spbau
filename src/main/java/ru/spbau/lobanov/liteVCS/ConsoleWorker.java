package ru.spbau.lobanov.liteVCS;

import ru.spbau.lobanov.liteVCS.logic.VersionControlSystemException;
import ru.spbau.lobanov.liteVCS.logic.LiteVCS;
import ru.spbau.lobanov.liteVCS.logic.LiteVCS.ConflictMergeException;
import ru.spbau.lobanov.liteVCS.primitives.Commit;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class ConsoleWorker {

    /**
     * Sugar to simplify checking count of arguments
     *
     * @param size expected arguments number
     * @param args array of arguments
     *
     * @throws WrongNumberArgumentsException if length of array isn't equal to expected value
     */
    private static void checkArguments(int size, String[] args) throws WrongNumberArgumentsException {
        if (args.length != size) {
            throw new WrongNumberArgumentsException("Expected " + size + " arguments");
        }
    }

    private static void execute(String command, String[] args) throws VersionControlSystemException,
            WrongNumberArgumentsException, IOException, UnknownCommandException {
        String path = Paths.get(System.getProperty("user.dir")).toString();
//        String path = "C:\\workspace\\liteVCS";
        switch (command) {
            case "init":
                checkArguments(0, args);
                LiteVCS.init(path);
                break;
            case "add":
                checkArguments(1, args);
                LiteVCS.add(path, args[0]);
                break;
            case "commit":
                checkArguments(1, args);
                LiteVCS.commit(path, args[0]);
                break;
            case "checkout":
                checkArguments(1, args);
                LiteVCS.checkout(path, args[0]);
                break;
            case "clear":
                checkArguments(0, args);
                LiteVCS.clear(path);
                break;
            case "create_branch":
                checkArguments(1, args);
                LiteVCS.createBranch(path, args[0]);
                break;
            case "remove_branch":
                checkArguments(1, args);
                LiteVCS.removeBranch(path, args[0]);
                break;
            case "switch_branch":
                checkArguments(1, args);
                LiteVCS.switchBranch(path, args[0]);
                break;
            case "merge_branch":
                checkArguments(2, args);
                LiteVCS.mergeBranch(path, args[0], args[1]);
                break;
            case "reset":
                checkArguments(0, args);
                LiteVCS.reset(path);
                break;
            case "uninstall":
                checkArguments(0, args);
                LiteVCS.uninstall(path);
                break;
            case "logs":
                checkArguments(0, args);
                printLogs(LiteVCS.log(path, "100"));
                break;
            default:
                throw new UnknownCommandException(command);
        }
    }

    public static void main(String[] args) {
//        args = new String[]{"merge_branch",  "master", "commit4444+"};
        if (args.length == 0) {
            System.out.println("Error: empty command");
            return;
        }
        String[] functionArgs = new String[args.length - 1];
        System.arraycopy(args, 1, functionArgs, 0, functionArgs.length);
        try {
            execute(args[0], functionArgs);
        } catch (ConflictMergeException e) {
            System.out.println("Conflicts were found:");
            for (String path : e.getConflicts()) {
                System.out.println("    " + path);
            }
        } catch (Throwable e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static final String COMMIT_PLACE_HOLDER = "\"%s\" by %s (node: %s)";

    private static void printLogs(List<Commit> commits) {
        System.out.println("Local history:");
        for (int i = commits.size() - 1; i >= 0; i--) {
            Commit commit = commits.get(i);
            System.out.printf(COMMIT_PLACE_HOLDER, commit.getCommitMessage(), commit.getAuthor(),
                    commit.getContentDescriptorID());
        }
    }

    public static class WrongNumberArgumentsException extends Exception {
        WrongNumberArgumentsException(String message) {
            super(message);
        }
    }

    public static class UnknownCommandException extends Exception {
        UnknownCommandException(String message) {
            super(message);
        }
    }
}
