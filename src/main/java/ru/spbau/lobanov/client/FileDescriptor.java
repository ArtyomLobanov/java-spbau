package ru.spbau.lobanov.client;

/**
 * Created by Артём on 27.04.2017.
 */
public class FileDescriptor {
    private final String name;
    private final String path;
    private final boolean isFolder;

    public FileDescriptor(String name, String path, boolean isFolder) {
        this.name = name;
        this.path = path;
        this.isFolder = isFolder;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public boolean isFolder() {
        return isFolder;
    }
}
