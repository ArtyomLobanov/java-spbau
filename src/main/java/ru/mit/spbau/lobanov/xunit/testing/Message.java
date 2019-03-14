package ru.mit.spbau.lobanov.xunit.testing;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Method;

/**
 * General type for all messages
 */
public interface Message {
    @NotNull
    String getMessage();
}
