package ru.spbau.lobanov.liteVCS.primitives;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Special class which represent commits in liteVCS
 * It contains message, time of creating, its authors
 * name and link to ContentDescriptor which contains information about files
 */
public class Commit implements Serializable {

    private final String contentDescriptorID;
    private final String commitMessage;
    private final long time;
    private final String author;

    public Commit(@NotNull String descriptorID, @NotNull String commitMessage, long time, @NotNull String author) {
        this.contentDescriptorID = descriptorID;
        this.commitMessage = commitMessage;
        this.time = time;
        this.author = author;
    }

    @NotNull
    public String getContentDescriptorID() {
        return contentDescriptorID;
    }

    @NotNull
    public String getCommitMessage() {
        return commitMessage;
    }

    public long getTime() {
        return time;
    }

    @NotNull
    public String getAuthor() {
        return author;
    }
}
