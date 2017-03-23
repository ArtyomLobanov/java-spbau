package ru.spbau.lobanov.liteVCS.logic;

import org.jetbrains.annotations.NotNull;
import ru.spbau.lobanov.liteVCS.primitives.VersionNode;

import java.util.ArrayList;

public class PathManager extends DataManager {

    private final ArrayList<VersionNode> versionNodes = new ArrayList<>();

    PathManager(int size) {
        super("");
        try {
            versionNodes.add(Algorithms.createRootNode("0", ""));
            for (int i = 1; i < size; i++) {
                versionNodes.add(Algorithms.createVersionNode("", (i - 1) + "", this));
            }
        } catch (Exception ignored){}
    }

    @Override
    public VersionNode getVersionNode(@NotNull String id) {
        int i = Integer.parseInt(id);
        return versionNodes.get(i);
    }
}
