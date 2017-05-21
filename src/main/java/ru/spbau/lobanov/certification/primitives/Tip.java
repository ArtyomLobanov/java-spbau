package ru.spbau.lobanov.certification.primitives;

/**
 * Data-class, which contains message of some tip
 */
public class Tip {
    private final String message;

    public Tip(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
