package ru.spbau.lobanov;

import com.google.common.io.Files;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mockito;
import ru.spbau.lobanov.client.Client;
import ru.spbau.lobanov.client.FileDescriptor;
import ru.spbau.lobanov.server.Server;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

public class Tests {

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private String fillFile(String arg0, String... args) throws IOException {
        Path path = Paths.get(arg0, args);
        File file = folder.newFile(path.toString());
        PrintWriter out = new PrintWriter(file);
        out.println("lgevbhkwubrldf" + Math.random());
        out.close();
        return file.toPath().toString();
    }

    @Test
    public void commandListFiles() throws Server.ServerException, Client.ClientException, IOException {
        Server server = new Server(Mockito.mock(PrintStream.class));
        server.start(4000);
        Client client = new Client(Mockito.mock(PrintStream.class));
        client.setServer("localhost", 4000);
        File testFolder = folder.newFolder("test");
        fillFile("test", "a.txt");
        folder.newFolder("test", "test2");
        fillFile("test", "test2", "b.txt");
        FileDescriptor[] descriptors = client.listFiles(testFolder.getPath());
        assertEquals(2, descriptors.length);
        List<String> names = Arrays.stream(descriptors)
                .map(FileDescriptor::getName)
                .collect(Collectors.toList());
        assertTrue(names.contains("a.txt"));
        assertTrue(names.contains("test2"));
        long folders = Arrays.stream(descriptors)
                .filter(FileDescriptor::isFolder)
                .count();
        assertEquals(1, folders);
    }

    @Test
    public void commandGetFile() throws Server.ServerException, Client.ClientException, IOException {
        Server server = new Server(Mockito.mock(PrintStream.class));
        server.start(4000);
        Client client = new Client(Mockito.mock(PrintStream.class));
        client.setServer("localhost", 4000);
        folder.newFolder("test");
        String path = fillFile("test", "a.txt");
        folder.newFolder("test", "test2");
        String target = fillFile("test", "test2", "b.txt");
        File file = client.getFile(path, target);
        File realFile = Paths.get(path).toFile();
        assertEquals(realFile.length(), file.length());
        assertTrue(Files.equal(realFile, file));
    }

    @Test(expected = Client.ClientException.class)
    public void commandGetNotExistentFile() throws Server.ServerException, Client.ClientException, IOException {
        Server server = new Server(Mockito.mock(PrintStream.class));
        server.start(4000);
        Client client = new Client(Mockito.mock(PrintStream.class));
        client.setServer("localhost", 4000);
        client.getFile(".ougerbgho", "tmp.txt");
    }

    @Test
    public void statusTest() throws Server.ServerException {
        Server server = new Server(Mockito.mock(PrintStream.class));
        assertFalse(server.isRunning());
        server.start(4000);
        assertTrue(server.isRunning());
        server.stop();
        assertFalse(server.isRunning());
    }

    @Test(expected = Server.ServerException.class)
    public void restarting() throws Server.ServerException {
        Server server = new Server(Mockito.mock(PrintStream.class));
        server.start(4000);
        server.start(4000);
    }

    @Test(expected = Server.ServerException.class)
    public void badStopping() throws Server.ServerException {
        Server server = new Server(Mockito.mock(PrintStream.class));
        server.stop();
    }

    @Test(expected = Client.ClientException.class)
    public void badConnection() throws Client.ClientException {
        Client client = new Client(Mockito.mock(PrintStream.class));
        client.setServer("erljilerj", 2293);
        client.listFiles("src");
    }
}
