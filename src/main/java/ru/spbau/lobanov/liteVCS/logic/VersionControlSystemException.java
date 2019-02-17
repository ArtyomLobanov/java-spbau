package ru.spbau.lobanov.liteVCS.logic;

public class VersionControlSystemException extends Exception {

    VersionControlSystemException(String message) {
        super(message);
    }

    VersionControlSystemException(String message, Throwable cause) {
        super(message, cause);
    }

}
