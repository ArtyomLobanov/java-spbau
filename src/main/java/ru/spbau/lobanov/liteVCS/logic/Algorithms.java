package ru.spbau.lobanov.liteVCS.logic;
import org.jetbrains.annotations.NotNull;

import ru.spbau.lobanov.liteVCS.logic.DataManager.BrokenFileException;
import ru.spbau.lobanov.liteVCS.logic.DataManager.LostFileException;
import ru.spbau.lobanov.liteVCS.primitives.VersionNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class Algorithms {

    private static final int CACHE_SIZE_LIMIT = 16;
    private static final int LONGEST_JUMP_LENGTH = 1 << (CACHE_SIZE_LIMIT - 1);

    /**
     * Special method which create root VersionNode.
     * It's important, because we have to know its own
     * id before create instance
     *
     * @param expectedID id of parent of VersionNode, which will  be created
     * @param commitID   id of commit, associated with VersionNode which will be created
     * @return created versionNode
     */
    @NotNull
    static VersionNode createRootNode(@NotNull String expectedID, @NotNull String commitID) {
        String[] parentsTable = new String[CACHE_SIZE_LIMIT];
        Arrays.fill(parentsTable, expectedID);
        return new VersionNode(commitID, 1, parentsTable);
    }

    /**
     * Special method which create VersionNode.
     * It's important, because we have to calculate table of parents,
     * before create VersionNode
     *
     * @param commitID id of commit, associated with VersionNode which will be created
     * @param parentID id of parent of VersionNode, which will  be created
     * @param manager  dataManager, which provides access to the files
     * @return created versionNode
     * @throws LostFileException   if file contained one of interesting Node was corrupted
     * @throws BrokenFileException if file contained one of interesting Node was not found
     */
    @NotNull
    static VersionNode createVersionNode(@NotNull String commitID, @NotNull String parentID,
                                         @NotNull DataManager manager) throws LostFileException, BrokenFileException {
        String[] parentsTable = new String[CACHE_SIZE_LIMIT];
        parentsTable[0] = parentID;
        for (int i = 1; i < CACHE_SIZE_LIMIT; i++) {
            parentsTable[i] = getParent(parentsTable[i - 1], i - 1, manager);
        }
        VersionNode parent = manager.fetchVersionNode(parentID);
        return new VersionNode(commitID, parent.getDeepLevel() + 1, parentsTable);
    }

    /**
     * Method which go up from node and write down its parents
     *
     * @param versionID   id of interesting node
     * @param limit       limit of recorded ancestors
     * @param dataManager dataManager, which provides access to the files
     * @return List of closed parents
     * @throws LostFileException   if file contained one of interesting Node was corrupted
     * @throws BrokenFileException if file contained one of interesting Node was not found
     */
    @NotNull
    static List<VersionNode> getAllParents(@NotNull String versionID, int limit,
                                           @NotNull DataManager dataManager) throws BrokenFileException, LostFileException {
        String currentVersionID = versionID;
        ArrayList<VersionNode> list = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            VersionNode currentNode = dataManager.fetchVersionNode(currentVersionID);
            list.add(currentNode);
            if (currentVersionID.equals(currentNode.getParentsTable()[0])) {
                break;
            }
            currentVersionID = currentNode.getParentsTable()[0];
        }
        return list;
    }

    /**
     * Method which find LCA of given nodes by algorithm
     * of binary expansion. In usual case, if deep of nodes
     * lower than 2^CACHE_SIZE_LIMIT it work O(log(deep)) times
     *
     * @param nodeID1 id of one interesting node
     * @param nodeID2 id of another interesting node
     * @param manager dataManager, which provides access to the files
     * @return id of LCA-node
     * @throws LostFileException   if file contained one of interesting Node was corrupted
     * @throws BrokenFileException if file contained one of interesting Node was not found
     */
    @NotNull
    static String findLowestCommonAncestor(@NotNull String nodeID1, @NotNull String nodeID2,
                                           @NotNull DataManager manager) throws LostFileException, BrokenFileException {
        String currentNodeID1 = nodeID1;
        String currentNodeID2 = nodeID2;
        int deepDelta = getDeepLevel(nodeID1, manager) - getDeepLevel(nodeID2, manager);
        if (deepDelta > 0) {
            currentNodeID1 = jump(currentNodeID1, deepDelta, manager);
        } else if (deepDelta < 0) {
            currentNodeID2 = jump(currentNodeID2, -deepDelta, manager);
        }
        if (currentNodeID1.equals(currentNodeID2)) {
            return currentNodeID1;
        }
        while (!isAncestorsEqual(currentNodeID1, currentNodeID2, CACHE_SIZE_LIMIT - 1, manager)) {
            currentNodeID1 = getParent(currentNodeID1, CACHE_SIZE_LIMIT - 1, manager);
            currentNodeID2 = getParent(currentNodeID2, CACHE_SIZE_LIMIT - 1, manager);
        }
        for (int level = CACHE_SIZE_LIMIT - 1; level >= 0; level--) {
            if (!isAncestorsEqual(currentNodeID1, currentNodeID2, level, manager)) {
                currentNodeID1 = getParent(currentNodeID1, level, manager);
                currentNodeID2 = getParent(currentNodeID2, level, manager);
            }
        }
        return getParent(currentNodeID1, 0, manager);
    }

    /**
     * Check if parents, remote from interesting nodes on
     * 2^level distance, are the same
     * Expected, that nodes has the same deep level
     *
     * @param nodeID1 id of one interesting node
     * @param nodeID2 id of another interesting node
     * @param level   number, which define distance between nodes
     *                and their interesting parents
     * @param manager dataManager, which provides access to the files
     * @return true, if parents are the same
     * @throws BrokenFileException if file contained one of interesting Node was corrupted
     * @throws LostFileException   if file contained one of interesting Node was not found
     */
    private static boolean isAncestorsEqual(@NotNull String nodeID1, @NotNull String nodeID2, int level,
                                            @NotNull DataManager manager) throws BrokenFileException, LostFileException {
        String ancestorID1 = manager.fetchVersionNode(nodeID1).getParentsTable()[level];
        String ancestorID2 = manager.fetchVersionNode(nodeID2).getParentsTable()[level];
        return ancestorID1.equals(ancestorID2);
    }

    /**
     * Method which find parent of interesting node, located at length levels
     * upper or the root of tree if such parent doesn't exist
     * It works O(length / (2^CACHE_SIZE_LIMIT) + log(length)) time.
     *
     * @param nodeID  id of interesting node
     * @param length  expected distance between interesting node and its interesting parent
     * @param manager dataManager, which provides access to the files
     * @return id of interesting parent
     * @throws BrokenFileException if file contained interesting Node was corrupted
     * @throws LostFileException   if file contained interesting Node was not found
     */
    @NotNull
    private static String jump(@NotNull String nodeID, int length,
                               @NotNull DataManager manager) throws LostFileException, BrokenFileException {
        String currentNodeID = nodeID;
        int residualLength = length;
        while (residualLength > LONGEST_JUMP_LENGTH) {
            currentNodeID = getParent(currentNodeID, CACHE_SIZE_LIMIT - 1, manager);
            residualLength -= LONGEST_JUMP_LENGTH;
        }
        for (int i = CACHE_SIZE_LIMIT - 1; i >= 0; i--) {
            int jumpLength = 1 << i;
            if (residualLength >= jumpLength) {
                currentNodeID = getParent(currentNodeID, i, manager);
                residualLength -= jumpLength;
            }
        }
        return currentNodeID;
    }

    /**
     * Sugar to simplify getting parents of VersionNode by id
     * Its return parent of interesting node, located at (2^level) levels
     * upper or the root of tree if such parent doesn't exist
     *
     * @param nodeID  id of interesting node
     * @param level   distance between interesting node and returned parent will be 2^level
     * @param manager dataManager, which provides access to the files
     * @return id of interesting parent
     * @throws BrokenFileException if file contained interesting Node was corrupted
     * @throws LostFileException   if file contained interesting Node was not found
     */
    @NotNull
    private static String getParent(@NotNull String nodeID, int level,
                                    @NotNull DataManager manager) throws BrokenFileException, LostFileException {
        return manager.fetchVersionNode(nodeID).getParentsTable()[level];
    }

    /**
     * Sugar to simplify getting the deep level by VersionNode id
     *
     * @param nodeID  id of interesting node
     * @param manager dataManager, which provides access to the files
     * @return deep level of VersionNode associated this nodeID
     * @throws BrokenFileException if file contained interesting Node was corrupted
     * @throws LostFileException   if file contained interesting Node was not found
     */
    private static int getDeepLevel(@NotNull String nodeID, @NotNull DataManager manager) throws BrokenFileException,
            LostFileException {
        return manager.fetchVersionNode(nodeID).getDeepLevel();
    }
}
