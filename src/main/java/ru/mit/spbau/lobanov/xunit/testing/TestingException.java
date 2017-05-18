package ru.mit.spbau.lobanov.xunit.testing;

import org.jetbrains.annotations.NotNull;

/**
 * Special type of exception, thrown by Tester
 */
public class TestingException extends Exception {
    TestingException(@NotNull String message) {
        super(message);
    }

    TestingException(@NotNull String message, @NotNull Throwable cause) {
        super(message, cause);
    }
}
