package ru.spbau.lobanov.liteVCS.logic;

import org.junit.Test;
import ru.spbau.lobanov.liteVCS.ConsoleWorker;

import java.io.OutputStream;
import java.io.PrintStream;

import static org.mockito.Mockito.*;

public class ConsoleWorkerTest {
    @Test
    public void commandsTest() throws Exception {
        System.setOut(mock(PrintStream.class));
        ConsoleWorker.main("");
    }
}
