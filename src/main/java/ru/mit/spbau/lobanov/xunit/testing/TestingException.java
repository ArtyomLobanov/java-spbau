package ru.mit.spbau.lobanov.xunit.testing;

/**
 * Special type of exception, thrown by Tester
 */
public class TestingException extends Exception {
    TestingException(String message) {
        super(message);
    }

    TestingException(String message, Throwable cause) {
        super(message, cause);
    }
}
