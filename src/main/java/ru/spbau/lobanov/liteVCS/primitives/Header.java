package ru.spbau.lobanov.liteVCS.primitives;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Special class to save general information in file
 * It knows name of current branch and name of author
 */
public class Header implements Serializable {
    private final String author;
    private final String currentBranchName;

    public Header(@NotNull String author, @NotNull String currentBranchName) {
        this.author = author;
        this.currentBranchName = currentBranchName;
    }

    @NotNull
    public String getAuthor() {
        return author;
    }

    @NotNull
    public String getCurrentBranchName() {
        return currentBranchName;
    }
}
