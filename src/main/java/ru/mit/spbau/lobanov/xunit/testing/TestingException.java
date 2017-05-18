package ru.mit.spbau.lobanov.xunit.testing;

/**
 * Created by Артём on 18.05.2017.
 */
public class TestingException extends Exception {
    public TestingException(String message) {
        super(message);
    }

    public TestingException(String message, Throwable cause) {
        super(message, cause);
    }
}
