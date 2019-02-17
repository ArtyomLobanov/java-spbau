package ru.spbau.lobanov.liteVCS.primitives;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Special class which contains information about
 * files and theirs actual versions
 * Its possible to save it in file
 */
public class ContentDescriptor implements Serializable {
    public static final ContentDescriptor EMPTY = new ContentDescriptor(new TreeMap<>());

    // Map name of file (relative path from working directory)
    // to ID of its actual version
    private final TreeMap<String, String> files;

    private ContentDescriptor(@NotNull TreeMap<String, String> files) {
        this.files = files;
    }

    @NotNull
    public Map<String, String> getFiles() {
        return Collections.unmodifiableMap(files);
    }

    @NotNull
    public static Builder builder() {
        return new Builder();
    }

    /**
     * This class should help to create ContentDescriptors
     */
    public static class Builder {
        private final TreeMap<String, String> files = new TreeMap<>();

        @NotNull
        public Builder addFile(@NotNull String relativePath, @NotNull String fileID) {
            files.put(relativePath, fileID);
            return this;
        }

        @NotNull
        public Builder addAll(@NotNull ContentDescriptor contentDescriptor) {
            files.putAll(contentDescriptor.files);
            return this;
        }

        @NotNull
        public Builder addAll(@NotNull Stage stage) {
            files.putAll(stage.getChangedFiles());
            files.keySet().removeAll(stage.getRemovedFiles());
            return this;
        }

        @NotNull
        public ContentDescriptor build() {
            return new ContentDescriptor(files);
        }
    }
}
