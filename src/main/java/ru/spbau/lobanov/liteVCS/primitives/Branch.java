package ru.spbau.lobanov.liteVCS.primitives;

import java.io.Serializable;

/**
 * Special class which represent branch in liteVCS
 * It has name and link to VersionNode
 * Its easy to save branch in file
 */
public class Branch implements Serializable {
    private final String versionNodeID;
    private final String name;

    public Branch(String versionNodeID, String name) {
        this.versionNodeID = versionNodeID;
        this.name = name;
    }

    public String getVersionNodeID() {
        return versionNodeID;
    }

    public String getName() {
        return name;
    }
}
