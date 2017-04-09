package ru.spbau.lobanov.liteVCS.logic;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;

public class LiteVCSTest {

    private static final String workspace = "test_workspace";

    private static String randomWorkspace() {
        Path path;
        do {
            path = Paths.get(workspace, Double.toHexString(Math.random()));
        } while (path.toFile().exists());
        return path.toString();
    }

    private static void fillFile(String fullPath, String name) throws FileNotFoundException {
        PrintWriter out = new PrintWriter(Paths.get(fullPath, name).toFile());
        out.write(String.valueOf(Math.random()));
        out.write(String.valueOf(Math.random()));
        out.close();
    }

    private static String hash(String fullPath, String name) {
        String hash;
        try {
            hash = Files.hash(Paths.get(fullPath, name).toFile(), Hashing.sha256()).toString();
        } catch (IOException e) {
            throw new Error("Unknown exception during hash creating");
        }
        return hash;
    }


    @Test
    public void addBranchSwitchChangeSwitchMerge() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);
        liteVCS.init();
        dataManager.writeFile("a.txt", "versiona1");
        String initialState = dataManager.hash("a.txt");
        liteVCS.add("a.txt");
        liteVCS.commit("commit #1");
        liteVCS.createBranch("br");
        liteVCS.switchBranch("br");
        dataManager.writeFile("a.txt", "versiona2");
        String lastState = dataManager.hash("a.txt");
        liteVCS.add("a.txt");
        liteVCS.commit("commit2");
        liteVCS.switchBranch("master");
        assertEquals(initialState, dataManager.hash("a.txt"));
        liteVCS.mergeBranch("br", "mesage");
        liteVCS.reset();
        assertEquals(lastState, dataManager.hash("a.txt"));
    }

    @Test
    public void addBranchChangeSwitchChangeSwitchMerge() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);
        liteVCS.init();

        dataManager.writeFile("a.txt", "versiona1");
        String initialStateA = dataManager.hash("a.txt");

        dataManager.writeFile("b.txt", "versionb1");
        String initialStateB = dataManager.hash("b.txt");

        liteVCS.add("a.txt");
        liteVCS.add("b.txt");
        liteVCS.commit("commit #1");
        liteVCS.createBranch("br");


        dataManager.writeFile("a.txt", "versiona2");
        String resultStateA = dataManager.hash("a.txt");
        liteVCS.add("a.txt");
        liteVCS.commit("commit #2");

        liteVCS.switchBranch("br");

        dataManager.writeFile("b.txt", "versionb2");
        String resultStateB = dataManager.hash("b.txt");
        liteVCS.add("b.txt");
        liteVCS.commit("commit3");

        liteVCS.switchBranch("master");
        assertEquals(resultStateA, dataManager.hash("a.txt"));
        assertEquals(initialStateB, dataManager.hash("b.txt"));

        liteVCS.mergeBranch("br", "mesage");
        liteVCS.reset();


        assertEquals(resultStateA, dataManager.hash("a.txt"));
        assertEquals(resultStateB, dataManager.hash("b.txt"));
    }
}