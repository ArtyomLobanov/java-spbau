package ru.spbau.lobanov.liteVCS.primitives;

import java.io.Serializable;

/**
 * Special class to save general information in file
 * It knows name of current branch and name of author
 */
public class Header implements Serializable {
    private final String author;
    private final String currentBranchName;

    public Header(String author, String currentBranchName) {
        this.author = author;
        this.currentBranchName = currentBranchName;
    }

    public String getAuthor() {
        return author;
    }

    public String getCurrentBranchName() {
        return currentBranchName;
    }
}
