package ru.mit.spbau.lobanov.xunit.testing;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

/**
 * Class, which contains information about warnings
 * and not-critical problem occurred during tests running
 */
public class WarningMessage implements Message {
    private final String message;
    private final Method test;

    WarningMessage(@NotNull String message, @Nullable Method test) {
        this.message = message;
        this.test = test;
    }

    @NotNull
    public String getMessage() {
        return message;
    }

    @Nullable
    public Method getTest() {
        return test;
    }
}
