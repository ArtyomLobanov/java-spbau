package ru.spbau.lobanov.liteVCS.logic;

import java.io.IOException;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Logging {

    public static void setupLogging() throws LoggingException {
        try {
            LogManager.getLogManager().readConfiguration(
                    Logging.class.getResourceAsStream("/logging.properties"));
        } catch (IOException e) {
            throw new LoggingException("Can't init logging", e);
        }
    }

    public static class LoggingException extends Exception {
        LoggingException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
