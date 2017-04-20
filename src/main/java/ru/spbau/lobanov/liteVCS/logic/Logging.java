package ru.spbau.lobanov.liteVCS.logic;

import sun.rmi.runtime.Log;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Logging {

    private static final Path LOG_STORAGE = Paths.get(".liteVCS", "logs");

    public static void setupLogging() throws LoggingException {
        try {
            Files.createDirectories(LOG_STORAGE);
            LogManager.getLogManager().readConfiguration(
                    Logging.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            throw new LoggingException("Can't init logging", e);
        }
        Logger.getLogger(Logger.class.getName()).fine("Logging was set up");
    }

    public static class LoggingException extends Exception {
        LoggingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
