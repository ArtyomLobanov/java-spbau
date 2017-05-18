package ru.mit.spbau.lobanov.xunit.testing;

import java.lang.reflect.Method;

/**
 * Created by Артём on 18.05.2017.
 */
public class WarningMessage implements Message {
    private final String message;
    private final Method test;

    public WarningMessage(String message, Method test) {
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
