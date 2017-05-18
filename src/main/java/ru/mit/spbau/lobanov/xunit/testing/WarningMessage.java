package ru.mit.spbau.lobanov.xunit.testing;

import java.lang.reflect.Method;

/**
 * Class, which contains information about warnings
 * and not-critical problem occurred during tests running
 */
public class WarningMessage implements Message {
    private final String message;
    private final Method test;

    WarningMessage(String message, Method test) {
        this.message = message;
        this.test = test;
    }

    public String getMessage() {
        return message;
    }

    public Method getTest() {
        return test;
    }
}
