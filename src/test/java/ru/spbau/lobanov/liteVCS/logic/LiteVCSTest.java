package ru.spbau.lobanov.liteVCS.logic;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.junit.Test;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.DoubleSummaryStatistics;
import java.util.Random;

import static org.junit.Assert.*;

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
        LiteVCS.init(fullPath);
        fillFile(fullPath, "a.txt");
        String initialState = hash(fullPath, "a.txt");
        LiteVCS.add(fullPath, "a.txt");
        LiteVCS.commit(fullPath, "commit #1");
        LiteVCS.createBranch(fullPath, "br");
        LiteVCS.switchBranch(fullPath, "br");
        fillFile(fullPath, "a.txt");
        String lastState = hash(fullPath, "a.txt");
        LiteVCS.add(fullPath, "a.txt");
        LiteVCS.commit(fullPath, "commit2");
        LiteVCS.switchBranch(fullPath, "master");
        assertEquals(initialState, hash(fullPath, "a.txt"));
        LiteVCS.mergeBranch(fullPath,"br", "mesage");
        LiteVCS.reset(fullPath);
        assertEquals(lastState, hash(fullPath, "a.txt"));
    }

    @Test
    public void addBranchChangeSwitchChangeSwitchMerge() throws Exception {
        String folder = randomWorkspace();
        String fullPath = Paths.get(workspace, folder).toString();
        Files.createParentDirs(Paths.get(workspace, folder).toFile());
        LiteVCS.init(fullPath);

        fillFile(fullPath, "a.txt");
        String initialStateA = hash(fullPath, "a.txt");
        fillFile(fullPath, "b.txt");
        String initialStateB = hash(fullPath, "b.txt");

        LiteVCS.add(fullPath, "a.txt");
        LiteVCS.add(fullPath, "b.txt");
        LiteVCS.commit(fullPath, "commit #1");
        LiteVCS.createBranch(fullPath, "br");


        fillFile(fullPath, "a.txt");
        String resultStateA = hash(fullPath, "a.txt");
        LiteVCS.add(fullPath, "a.txt");
        LiteVCS.commit(fullPath, "commit #2");

        LiteVCS.switchBranch(fullPath, "br");

        fillFile(fullPath, "b.txt");
        String resultStateB = hash(fullPath, "b.txt");
        LiteVCS.add(fullPath, "b.txt");
        LiteVCS.commit(fullPath, "commit3");

        LiteVCS.switchBranch(fullPath, "master");
        assertEquals(resultStateA, hash(fullPath, "a.txt"));
        assertEquals(initialStateB, hash(fullPath, "b.txt"));

        LiteVCS.mergeBranch(fullPath,"br", "mesage");
        LiteVCS.reset(fullPath);


        assertEquals(resultStateA, hash(fullPath, "a.txt"));
        assertEquals(resultStateB, hash(fullPath, "b.txt"));
    }
}