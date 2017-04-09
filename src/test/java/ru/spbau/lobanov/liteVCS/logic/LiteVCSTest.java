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
        String folder = randomWorkspace();
        String fullPath = Paths.get(workspace, folder).toString();
        Files.createParentDirs(Paths.get(workspace, folder).toFile());
        LiteVCS liteVCS = new LiteVCS(fullPath);
        liteVCS.init();
        fillFile(fullPath, "a.txt");
        String initialState = hash(fullPath, "a.txt");
        liteVCS.add("a.txt");
        liteVCS.commit("commit #1");
        liteVCS.createBranch("br");
        liteVCS.switchBranch("br");
        fillFile(fullPath, "a.txt");
        String lastState = hash(fullPath, "a.txt");
        liteVCS.add("a.txt");
        liteVCS.commit("commit2");
        liteVCS.switchBranch("master");
        assertEquals(initialState, hash(fullPath, "a.txt"));
        liteVCS.mergeBranch("br", "mesage");
        liteVCS.reset();
        assertEquals(lastState, hash(fullPath, "a.txt"));
    }

    @Test
    public void addBranchChangeSwitchChangeSwitchMerge() throws Exception {
        String folder = randomWorkspace();
        String fullPath = Paths.get(workspace, folder).toString();
        Files.createParentDirs(Paths.get(workspace, folder).toFile());
        LiteVCS liteVCS = new LiteVCS(fullPath);
        liteVCS.init();
        String initialStateA, initialStateB;
        do {
            fillFile(fullPath, "a.txt");
            initialStateA = hash(fullPath, "a.txt");
            fillFile(fullPath, "b.txt");
            initialStateB = hash(fullPath, "b.txt");
        } while (initialStateA.equals(initialStateB));

        liteVCS.add("a.txt");
        liteVCS.add("b.txt");
        liteVCS.commit("commit #1");
        liteVCS.createBranch("br");


        fillFile(fullPath, "a.txt");
        String resultStateA = hash(fullPath, "a.txt");
        liteVCS.add("a.txt");
        liteVCS.commit("commit #2");

        liteVCS.switchBranch("br");

        fillFile(fullPath, "b.txt");
        String resultStateB = hash(fullPath, "b.txt");
        liteVCS.add("b.txt");
        liteVCS.commit("commit3");

        liteVCS.switchBranch("master");
        assertEquals(resultStateA, hash(fullPath, "a.txt"));
        assertEquals(initialStateB, hash(fullPath, "b.txt"));

        liteVCS.mergeBranch("br", "mesage");
        liteVCS.reset();


        assertEquals(resultStateA, hash(fullPath, "a.txt"));
        assertEquals(resultStateB, hash(fullPath, "b.txt"));
    }
}