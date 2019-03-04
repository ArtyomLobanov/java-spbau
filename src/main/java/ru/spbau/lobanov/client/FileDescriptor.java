package ru.spbau.lobanov.client;

import org.jetbrains.annotations.NotNull;

/**
 * Class contained information about file on server
 */
public class FileDescriptor {
    private final String name;
    private final String path;
    private final boolean isFolder;

    public FileDescriptor(@NotNull String name, @NotNull String path, boolean isFolder) {
        this.name = name;
        this.path = path;
        this.isFolder = isFolder;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public String getPath() {
        return path;
    }

    public boolean isFolder() {
        return isFolder;
    }
}
