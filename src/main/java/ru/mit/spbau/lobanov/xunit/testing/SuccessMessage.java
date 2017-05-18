package ru.mit.spbau.lobanov.xunit.testing;

import java.lang.reflect.Method;

/**
 * Class, which contains information about
 * successful tests
 */
public class SuccessMessage implements Message {
    private final String message;
    private final Method test;
    private final long time;

    SuccessMessage(String message, long time, Method test) {
        this.message = message;
        this.test = test;
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public Method getTest() {
        return test;
    }

    public long getTime() {
        return time;
    }
}
