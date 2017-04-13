package ru.spbau.lobanov.liteVCS;

import ru.spbau.lobanov.liteVCS.logic.DataManager.BrokenFileException;
import ru.spbau.lobanov.liteVCS.logic.DataManager.LostFileException;
import ru.spbau.lobanov.liteVCS.logic.DataManager.RecreatingRepositoryException;
import ru.spbau.lobanov.liteVCS.logic.DataManager.RepositoryNotInitializedException;
import ru.spbau.lobanov.liteVCS.logic.LiteVCS;
import ru.spbau.lobanov.liteVCS.logic.LiteVCS.*;
import ru.spbau.lobanov.liteVCS.logic.Logging;
import ru.spbau.lobanov.liteVCS.logic.VersionControlSystemException;
import ru.spbau.lobanov.liteVCS.primitives.Commit;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class ConsoleWorker {

    private static final String COMMIT_PLACE_HOLDER = "\"%s\" by %s (node: %s)\n";
    private static final String STATUS_PLACE_HOLDER = "        \"%s\"  status=%s\n";

    private final LiteVCS liteVCS;

    public ConsoleWorker(LiteVCS liteVCS) {
        this.liteVCS = liteVCS;
    }

    /**
     * Sugar to simplify checking count of arguments
     *
     * @param size expected arguments number
     * @param args array of arguments
     * @throws WrongNumberArgumentsException if length of array isn't equal to expected value
     */
    private static void checkArguments(int size, String[] args) throws WrongNumberArgumentsException {
        if (args.length != size) {
            throw new WrongNumberArgumentsException("Expected " + size + " arguments");
        }
    }

    public void execute(String command, String[] args) throws VersionControlSystemException,
            WrongNumberArgumentsException, IOException, UnknownCommandException {
        switch (command) {
            case "init":
                checkArguments(0, args);
                liteVCS.init();
                break;
            case "add":
                checkArguments(1, args);
                liteVCS.add(args[0]);
                break;
            case "commit":
                checkArguments(1, args);
                liteVCS.commit(args[0]);
                break;
            case "checkout":
                checkArguments(1, args);
                liteVCS.checkout(args[0]);
                break;
            case "clean":
                checkArguments(0, args);
                liteVCS.clean();
                break;
            case "status":
                checkArguments(0, args);
                printStatus(liteVCS.stageStatus(), liteVCS.workingCopyStatus());
                break;
            case "create_branch":
                checkArguments(1, args);
                liteVCS.createBranch(args[0]);
                break;
            case "remove_branch":
                checkArguments(1, args);
                liteVCS.removeBranch(args[0]);
                break;
            case "switch_branch":
                checkArguments(1, args);
                liteVCS.switchBranch(args[0]);
                break;
            case "merge_branch":
                checkArguments(2, args);
                liteVCS.mergeBranch(args[0], args[1]);
                break;
            case "reset":
                checkArguments(1, args);
                liteVCS.reset(args[0]);
                break;
            case "remove":
                checkArguments(1, args);
                liteVCS.remove(args[0]);
                break;
            case "uninstall":
                checkArguments(0, args);
                liteVCS.uninstall();
                break;
            case "logs":
                checkArguments(1, args);
                printHistory(liteVCS.history(args[0]));
                break;
            case "hello":
                checkArguments(1, args);
                liteVCS.hello(args[0]);
                break;
            default:
                throw new UnknownCommandException(command);
        }
    }

    public static void main(String... args) {
        try {
            Logging.setupLogging();
        } catch (Logging.LoggingException e) {
            System.out.println("Logging error: " + e.getMessage());
            System.out.println("Original message: " + e.getCause().getMessage());
            return;
        }
        if (args.length == 0) {
            System.out.println("Error: empty command");
            return;
        }
        String[] functionArgs = new String[args.length - 1];
        System.arraycopy(args, 1, functionArgs, 0, functionArgs.length);
        String targetPath = Paths.get(System.getProperty("user.dir")).toString();
        ConsoleWorker consoleWorker = new ConsoleWorker(new LiteVCS(targetPath));
        try {
            consoleWorker.execute(args[0], functionArgs);
        } catch (ConflictMergeException e) {
            System.out.println("Conflicts were found:");
            for (String path : e.getConflicts()) {
                System.out.println("    " + path);
            }
        } catch (LostFileException e) {
            System.out.println("Error: Looks, like important file disappeared: " + e.getExpectedFile().toString());
        } catch (BrokenFileException e) {
            System.out.println("Error: Looks, like file was badly changed: " + e.getBrokenFile().toString());
        } catch (UnknownBranchException e) {
            System.out.println("Error: branch doesn't exist");
        } catch (UncommittedChangesException e) {
            System.out.println("Error: commit or reset changes first");
        } catch (UnknownCommandException e) {
            System.out.println("Error: command doesn't exist");
        } catch (WrongNumberArgumentsException e) {
            System.out.println("Error: wrong number of arguments");
        } catch (RepositoryNotInitializedException e) {
            System.out.println("Error: Looks, like repository wasn't already created");
        } catch (RemoveActiveBranchException e) {
            System.out.println("Error: you cant remove active branch");
        } catch (LiteVCS.IllegalBranchToMergeException e) {
            System.out.println("Error: Looks, like you tried to branch with it-self");
        } catch (RecreatingRepositoryException e) {
            System.out.println("Error: Looks, like you tried to create repository second time");
        } catch (Throwable e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void printHistory(List<Commit> commits) {
        System.out.println("Local history:");
        for (int i = commits.size() - 1; i >= 0; i--) {
            Commit commit = commits.get(i);
            System.out.printf(COMMIT_PLACE_HOLDER, commit.getCommitMessage(), commit.getAuthor(),
                    commit.getContentDescriptorID());
        }
    }



    private static void printStatus(Map<String, StageStatus> stageStatus, Map<String, FileStatus> workingCopyStatus) {
        System.out.println("---------------------------------------------");
        System.out.println("Status of repository:");
        System.out.println("    Stage:");
        for (Entry<String, StageStatus> entry : stageStatus.entrySet()) {
            System.out.printf(STATUS_PLACE_HOLDER, entry.getKey(), entry.getValue());
        }
        System.out.println("    Working copy:");
        for (Entry<String, FileStatus> entry : workingCopyStatus.entrySet()) {
            System.out.printf(STATUS_PLACE_HOLDER, entry.getKey(), entry.getValue());
        }
        System.out.println("---------------------------------------------");
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
