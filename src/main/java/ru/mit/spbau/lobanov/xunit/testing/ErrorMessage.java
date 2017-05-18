package ru.mit.spbau.lobanov.xunit.testing;

import java.lang.reflect.Method;
import java.security.MessageDigest;

/**
 * Class, which contains information about
 * failed tests and error occurred during tests running
 */
public class ErrorMessage implements Message {
    private final String message;
    private final Throwable error;
    private final Method test;

    ErrorMessage(String message, Throwable error, Method test) {
        this.message = message;
        this.error = error;
        this.test = test;
    }

    public Throwable getError() {
        return error;
    }

    public Method getTest() {
        return test;
    }

    public String getMessage() {
        return message;
    }
}
