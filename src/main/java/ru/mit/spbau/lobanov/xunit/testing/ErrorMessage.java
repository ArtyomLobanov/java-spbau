package ru.mit.spbau.lobanov.xunit.testing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    ErrorMessage(@NotNull String message, @Nullable Throwable error, @Nullable Method test) {
        this.message = message;
        this.error = error;
        this.test = test;
    }

    @Nullable
    public Throwable getError() {
        return error;
    }

    @Nullable
    public Method getTest() {
        return test;
    }

    @NotNull
    public String getMessage() {
        return message;
    }
}
