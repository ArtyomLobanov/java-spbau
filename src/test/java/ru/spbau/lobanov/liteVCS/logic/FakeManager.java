package ru.spbau.lobanov.liteVCS.logic;

import org.jetbrains.annotations.NotNull;
import ru.spbau.lobanov.liteVCS.primitives.VersionNode;

import java.util.HashMap;

/**
 * Created by Артём on 23.03.2017.
 */
public class FakeManager extends DataManager {
    private final HashMap<String, VersionNode> map = new HashMap<>();

    FakeManager() {
        super("");
    }

    public void addVersionNode(String parentID) throws BrokenFileException, LostFileException {
        VersionNode versionNode;
        if (parentID == null) {
            versionNode = Algorithms.createRootNode("0", "");
        } else {
            versionNode = Algorithms.createVersionNode("", parentID, this);
        }
        map.put(map.size() + "", versionNode);
    }

    @NotNull
    @Override
    public VersionNode getVersionNode(@NotNull String id) {
        return map.get(id);
    }
}
