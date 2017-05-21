package ru.spbau.lobanov.certification.primitives;

/**
 * Data-class, which contains information (name and some
 * description) about one of argument of some command
 */
public class ArgumentInfo {
    private final String argumentName;
    private final String description;

    public ArgumentInfo(String argumentName, String description) {
        this.argumentName = argumentName;
        this.description = description;
    }

    public String getArgumentName() {
        return argumentName;
    }

    public String getDescription() {
        return description;
    }
}
