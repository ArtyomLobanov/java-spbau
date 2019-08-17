package ru.spbau.lobanov.liteVCS.logic;

import org.jetbrains.annotations.NotNull;

import java.io.File;

public class VirtualFile extends File {

    private String value;

    public VirtualFile(@NotNull String value) {
        super("");
        this.value = value;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj.getClass() != VirtualFile.class)
            return false;
        VirtualFile vf = (VirtualFile) obj;
        return value.equals(vf.value);
    }

    @Override
    public boolean exists() {
        return true;
    }

    @Override
    public boolean isFile() {
        return true;
    }

    public String getValue() {
        return value;
    }
}
