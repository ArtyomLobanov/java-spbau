package ru.mit.spbau.lobanov.xunit.testing;

import java.lang.reflect.Method;
import java.security.MessageDigest;

/**
 * Created by Артём on 18.05.2017.
 */
public class ErrorMessage implements Message {
    private final String message;
    private final Throwable error;
    private final Method test;

    public ErrorMessage(String message, Throwable error, Method test) {
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
