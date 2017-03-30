import com.google.common.io.Files;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import static org.junit.Assert.*;

public class HashMD5Test {

    private static final String workspace = "test_workspace";

    private static void fillFile(String fullPath, String name) throws IOException {
        File f = Paths.get(fullPath, name).toFile();
        Files.createParentDirs(f);
        PrintWriter out = new PrintWriter(f);
        out.write(String.valueOf(Math.random()));
        out.write(String.valueOf(Math.random()));
        out.close();
    }

    @Test
    public void constancyTest() throws IOException, NoSuchAlgorithmException {
        fillFile(workspace,"a.txt");
        byte[] result1 = HashMD5.hash(Paths.get(workspace, "a.txt").toFile());
        byte[] result2 = HashMD5.hash(Paths.get(workspace, "a.txt").toFile());
        assertArrayEquals(result1, result2);
    }

    @Test
    public void similarityTest() throws IOException, NoSuchAlgorithmException {
        fillFile(workspace,"a.txt");
        byte[] result1 = HashMD5.hash(Paths.get(workspace, "a.txt").toFile());
        byte[] result2 = HashMD5.hashParallel(Paths.get(workspace, "a.txt").toFile());
        assertArrayEquals(result1, result2);
    }

    @Test
    public void folderHashTest() throws IOException, NoSuchAlgorithmException {
        fillFile(workspace,"a.txt");
        fillFile(Paths.get(workspace, "folder").toString(),"a.txt");
        byte[] result1 = HashMD5.hash(Paths.get(workspace, "a.txt").toFile());
        byte[] result2 = HashMD5.hash(Paths.get(workspace, "folder", "a.txt").toFile());
        byte[] result3 = HashMD5.hashParallel(Paths.get(workspace, "folder", "a.txt").toFile());
        assertTrue(!Arrays.equals(result1, result2));
        assertArrayEquals(result2, result3);
    }
}