package ru.spbau.lobanov.liteVCS.primitives;

import java.io.Serializable;

/**
 * Special class which allow to build tree of versions
 * Has link to associated Commit
 * Commit and VersionNode are separated to facilitate the version tree
 */
public class VersionNode implements Serializable {

    private final String commitID;

    // data for method of binary expansion
    private final int deepLevel;
    private final String[] parentsTable;

    public VersionNode(String commitID, int deepLevel, String[] parentsTable) {
        this.commitID = commitID;
        this.deepLevel = deepLevel;
        this.parentsTable = parentsTable;
    }

    public String getCommitID() {
        return commitID;
    }

    public int getDeepLevel() {
        return deepLevel;
    }

    /**
     * This method should be used for method
     * of binary expansion
     * Returned table will contains parents, located at
     * 1, 2, 4, 8... levels upper
     *
     * @return table of parents
     */
    public String[] getParentsTable() {
        return parentsTable;
    }
}
