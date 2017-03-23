package ru.spbau.lobanov.liteVCS.primitives;

import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

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

    private ContentDescriptor(TreeMap<String, String> files) {
        this.files = files;
    }

    public Map<String, String> getFiles() {
        return Collections.unmodifiableMap(files);
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * This class should help to create ContentDescriptors
     */
    public static class Builder {
        private final TreeMap<String, String> files = new TreeMap<>();

        public Builder addFile(String relativePath, String fileID) {
            files.put(relativePath, fileID);
            return this;
        }

        public Builder addAllFiles(ContentDescriptor contentDescriptor) {
            files.putAll(contentDescriptor.files);
            return this;
        }

        public ContentDescriptor build() {
            return new ContentDescriptor(files);
        }
    }
}
