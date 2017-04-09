package ru.spbau.lobanov.liteVCS.logic;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class DataManagerTest {
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
}