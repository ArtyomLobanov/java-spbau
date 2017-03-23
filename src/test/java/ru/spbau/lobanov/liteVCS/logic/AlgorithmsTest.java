package ru.spbau.lobanov.liteVCS.logic;

import org.junit.Test;
import ru.spbau.lobanov.liteVCS.primitives.Commit;
import ru.spbau.lobanov.liteVCS.primitives.VersionNode;

import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;
import static ru.spbau.lobanov.liteVCS.logic.Algorithms.*;

public class AlgorithmsTest {
    // its longer then length of max jump in binary expansion
    private static final PathManager pathExample = new PathManager(100_000);

    // just usual tree
    private static final FakeManager treeExample = new FakeManager();

    static {
        try {
            treeExample.addVersionNode((String) null); // 0
            treeExample.addVersionNode("0");   // 1
            treeExample.addVersionNode("0");   // 2
            treeExample.addVersionNode("2");   // 3
            treeExample.addVersionNode("3");   // 4
            treeExample.addVersionNode("3");   // 5
            treeExample.addVersionNode("4");   // 6
            treeExample.addVersionNode("1");   // 7
            treeExample.addVersionNode("7");   // 8
            treeExample.addVersionNode("1");   // 9
            treeExample.addVersionNode("2");   // 10
            treeExample.addVersionNode("5");   // 11
            treeExample.addVersionNode("4");   // 12
            treeExample.addVersionNode("6");   // 13
            treeExample.addVersionNode("8");   // 14
            treeExample.addVersionNode("4");   // 15
        } catch (Exception ignored) {}
    }

    @Test
    public void smallDataLCATest() throws Exception {
        assertEquals("0", findLowestCommonAncestor("14", "13", treeExample));
        assertEquals("4", findLowestCommonAncestor("12", "13", treeExample));
        assertEquals("3", findLowestCommonAncestor("13", "3", treeExample));
        assertEquals("0", findLowestCommonAncestor("1", "15", treeExample));
        assertEquals("0", findLowestCommonAncestor("0", "15", treeExample));
        assertEquals("2", findLowestCommonAncestor("10", "11", treeExample));
        assertEquals("1", findLowestCommonAncestor("14", "9", treeExample));
        assertEquals("8", findLowestCommonAncestor("8", "8", treeExample));
    }

    @Test
    public void bigDataLCATest() throws Exception {
        assertEquals("0", findLowestCommonAncestor("0", "99999", pathExample));
        assertEquals("7", findLowestCommonAncestor("7", "99435", pathExample));
        assertEquals("34", findLowestCommonAncestor("34", "9934", pathExample));
        assertEquals("53", findLowestCommonAncestor("53", "99977", pathExample));
    }

    @Test
    public void allParentsSmallDataTest() throws DataManager.LostFileException, DataManager.BrokenFileException {
        List<VersionNode> parents = getAllParents("13", 7, treeExample);
        VersionNode[] answer = {treeExample.getVersionNode("13"), treeExample.getVersionNode("6"),
                treeExample.getVersionNode("4"), treeExample.getVersionNode("3"),
                treeExample.getVersionNode("2"), treeExample.getVersionNode("0")};
        assertEquals(answer.length, parents.size());
        for (int i = 0; i < answer.length; i++) {
            assertSame(answer[i], parents.get(i));
        }

        List<VersionNode> parents2 = getAllParents("14", 4, treeExample);
        VersionNode[] answer2 = {treeExample.getVersionNode("14"), treeExample.getVersionNode("8"),
                treeExample.getVersionNode("7"), treeExample.getVersionNode("1")};
        assertEquals(answer2.length, parents2.size());
        for (int i = 0; i < answer2.length; i++) {
            assertSame(answer2[i], parents2.get(i));
        }
    }
}