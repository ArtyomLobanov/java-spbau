package ru.spbau.lobanov.liteVCS.logic;

import org.junit.Test;
import ru.spbau.lobanov.liteVCS.ConsoleWorker;
import ru.spbau.lobanov.liteVCS.primitives.Header;
import ru.spbau.lobanov.liteVCS.primitives.Stage;

import java.io.PrintStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ConsoleWorkerTest {

    @Test(expected = LiteVCS.UnknownBranchException.class)
    public void unknownBranchTest() throws Exception {
        System.setOut(mock(PrintStream.class));

        Header header = mock(Header.class);
        when(header.getCurrentBranchName()).thenReturn("master");
        Stage stage = mock(Stage.class);
        when(stage.isEmpty()).thenReturn(true);

        DataManager dataManager = mock(DataManager.class);
        when(dataManager.hasBranch("branch")).thenReturn(false);
        when(dataManager.getHeader()).thenReturn(header);
        when(dataManager.getStage()).thenReturn(stage);
        when(dataManager.isInitialized()).thenReturn(true);

        ConsoleWorker consoleWorker = new ConsoleWorker(new LiteVCS(dataManager));
        consoleWorker.execute("switch_branch", new String[]{"branch"});
    }

    @Test(expected = LiteVCS.UncommittedChangesException.class)
    public void notEmptyStageTest() throws Exception {
        System.setOut(mock(PrintStream.class));

        Header header = mock(Header.class);
        when(header.getCurrentBranchName()).thenReturn("master");
        Stage stage = mock(Stage.class);
        when(stage.isEmpty()).thenReturn(false);

        DataManager dataManager = mock(DataManager.class);
        when(dataManager.hasBranch("branch")).thenReturn(false);
        when(dataManager.getHeader()).thenReturn(header);
        when(dataManager.getStage()).thenReturn(stage);
        when(dataManager.isInitialized()).thenReturn(true);

        ConsoleWorker consoleWorker = new ConsoleWorker(new LiteVCS(dataManager));
        consoleWorker.execute("switch_branch", new String[]{"branch"});
    }

    @Test(expected = LiteVCS.SwitchOnCurrentBranchException.class)
    public void switchOnCurrentBranchTest() throws Exception {
        System.setOut(mock(PrintStream.class));

        Header header = mock(Header.class);
        when(header.getCurrentBranchName()).thenReturn("branch");
        Stage stage = mock(Stage.class);
        when(stage.isEmpty()).thenReturn(true);

        DataManager dataManager = mock(DataManager.class);
        when(dataManager.hasBranch("branch")).thenReturn(false);
        when(dataManager.getHeader()).thenReturn(header);
        when(dataManager.getStage()).thenReturn(stage);
        when(dataManager.isInitialized()).thenReturn(true);

        ConsoleWorker consoleWorker = new ConsoleWorker(new LiteVCS(dataManager));
        consoleWorker.execute("switch_branch", new String[]{"branch"});
    }

    @Test(expected = LiteVCS.RemoveActiveBranchException.class)
    public void removeActiveBranchTest() throws Exception {
        System.setOut(mock(PrintStream.class));

        Header header = mock(Header.class);
        when(header.getCurrentBranchName()).thenReturn("master");
        Stage stage = mock(Stage.class);
        when(stage.isEmpty()).thenReturn(true);

        DataManager dataManager = mock(DataManager.class);
        when(dataManager.hasBranch("master")).thenReturn(false);
        when(dataManager.getHeader()).thenReturn(header);
        when(dataManager.getStage()).thenReturn(stage);
        when(dataManager.isInitialized()).thenReturn(true);

        ConsoleWorker consoleWorker = new ConsoleWorker(new LiteVCS(dataManager));
        consoleWorker.execute("remove_branch", new String[]{"master"});
    }

    @Test(expected = LiteVCS.IllegalBranchToMergeException.class)
    public void selfMergeTest() throws Exception {
        System.setOut(mock(PrintStream.class));

        Header header = mock(Header.class);
        when(header.getCurrentBranchName()).thenReturn("master");

        DataManager dataManager = mock(DataManager.class);
        when(dataManager.hasBranch("master")).thenReturn(false);
        when(dataManager.getHeader()).thenReturn(header);
        when(dataManager.isInitialized()).thenReturn(true);

        ConsoleWorker consoleWorker = new ConsoleWorker(new LiteVCS(dataManager));
        consoleWorker.execute("merge_branch", new String[]{"master", "commit"});
    }

    @Test(expected = LiteVCS.ConflictNameException.class)
    public void cloneBranchTest() throws Exception {
        System.setOut(mock(PrintStream.class));

        Header header = mock(Header.class);
        when(header.getCurrentBranchName()).thenReturn("master");

        DataManager dataManager = mock(DataManager.class);
        when(dataManager.hasBranch("branch")).thenReturn(true);
        when(dataManager.getHeader()).thenReturn(header);
        when(dataManager.isInitialized()).thenReturn(true);

        ConsoleWorker consoleWorker = new ConsoleWorker(new LiteVCS(dataManager));
        consoleWorker.execute("create_branch", new String[]{"branch"});
    }
}
