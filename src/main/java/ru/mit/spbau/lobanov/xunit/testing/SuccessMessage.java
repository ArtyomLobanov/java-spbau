package ru.mit.spbau.lobanov.xunit.testing;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * Class, which contains information about
 * successful tests
 */
public class SuccessMessage implements Message {
    private final String message;
    private final Method test;
    private final long time;

    SuccessMessage(@NotNull String message, long time, @NotNull Method test) {
        this.message = message;
        this.test = test;
        this.time = time;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    @NotNull
    public Method getTest() {
        return test;
    }

    public long getTime() {
        return time;
    }
}
