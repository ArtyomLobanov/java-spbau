package ru.spbau.lobanov.liteVCS.primitives;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.*;

public class Stage implements Serializable {
    public static final Stage EMPTY = new Stage(new TreeMap<>(), new TreeSet<>());

    // Map name of file (relative path from working directory)
    // to ID of its actual version
    private final TreeMap<String, String> changedFiles;
    private final TreeSet<String> removedFiles;

    private Stage(TreeMap<String, String> changedFiles, TreeSet<String> removedFiles) {
        this.changedFiles = changedFiles;
        this.removedFiles = removedFiles;
    }

    @NotNull
    public Map<String, String> getChangedFiles() {
        return Collections.unmodifiableMap(changedFiles);
    }

    public TreeSet<String> getRemovedFiles() {
        return removedFiles;
    }

    public boolean isEmpty() {
        return changedFiles.isEmpty() && removedFiles.isEmpty();
    }

    @NotNull
    public static Builder builder() {
        return new Builder(Collections.emptyMap(), Collections.emptySet());
    }

    @NotNull
    public Builder change() {
        return new Builder(changedFiles, removedFiles);
    }


    /**
     * This class should help to create ContentDescriptors
     */
    public static class Builder {
        private final TreeMap<String, String> changedFiles;
        private final TreeSet<String> removedFiles;

        private Builder(Map<String, String> changedFiles, Set<String> removedFiles) {
            this.changedFiles = new TreeMap<>(changedFiles);
            this.removedFiles = new TreeSet<>(removedFiles);
        }

        @NotNull
        public Builder addFile(@NotNull String relativePath, @NotNull String fileID) {
            changedFiles.put(relativePath, fileID);
            removedFiles.remove(relativePath);
            return this;
        }

        @NotNull
        public Builder removeFile(@NotNull String relativePath) {
            removedFiles.add(relativePath);
            changedFiles.remove(relativePath);
            return this;
        }

        public Builder reset(String relationPath) {
            removedFiles.remove(relationPath);
            changedFiles.remove(relationPath);
            return this;
        }

        @NotNull
        public Stage build() {
            return new Stage(changedFiles, removedFiles);
        }
    }
}
