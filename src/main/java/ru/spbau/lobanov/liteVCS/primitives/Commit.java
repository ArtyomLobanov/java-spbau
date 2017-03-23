package ru.spbau.lobanov.liteVCS.primitives;

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

    public Commit(String contentDescriptorID, String commitMessage, long time, String author) {
        this.contentDescriptorID = contentDescriptorID;
        this.commitMessage = commitMessage;
        this.time = time;
        this.author = author;
    }

    public String getContentDescriptorID() {
        return contentDescriptorID;
    }

    public String getCommitMessage() {
        return commitMessage;
    }

    public long getTime() {
        return time;
    }

    public String getAuthor() {
        return author;
    }
}
