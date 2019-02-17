package ru.spbau.lobanov.liteVCS.primitives;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * Special class which represent branch in liteVCS
 * It has name and link to VersionNode
 * Its easy to save branch in file
 */
public class Branch implements Serializable {
    private final String versionNodeID;
    private final String name;

    public Branch(@NotNull String versionNodeID, @NotNull String name) {
        this.versionNodeID = versionNodeID;
        this.name = name;
    }

    @NotNull
    public String getVersionNodeID() {
        return versionNodeID;
    }

    @NotNull
    public String getName() {
        return name;
    }
}
