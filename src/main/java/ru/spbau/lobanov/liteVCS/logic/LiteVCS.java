package ru.spbau.lobanov.liteVCS.logic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.lobanov.liteVCS.logic.DataManager.BrokenFileException;
import ru.spbau.lobanov.liteVCS.logic.DataManager.LostFileException;
import ru.spbau.lobanov.liteVCS.logic.DataManager.RecreatingRepositoryException;
import ru.spbau.lobanov.liteVCS.logic.DataManager.RepositoryNotInitializedException;
import ru.spbau.lobanov.liteVCS.primitives.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Special class which provides all the main
 * functionality of library by static methods
 */
public class LiteVCS {

    private final DataManager dataManager;

    public LiteVCS(@NotNull String path) {
        dataManager = new DataManager(path);
    }

    public LiteVCS(@NotNull DataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * This method allows to set author's name.
     * That name will be mentioned in following commits.
     *
     * @param author chosen name
     * @throws LostFileException     if file contained one of interesting object was corrupted
     * @throws BrokenFileException   if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException if repository was not initialized
     */
    public void hello(@NotNull String author) throws BrokenFileException,
            LostFileException, RepositoryNotInitializedException {
        String branchName = dataManager.getHeader().getCurrentBranchName();
        dataManager.putHeader(new Header(author, branchName));
    }

    /**
     * Wrapper for init-method of DataManager
     *
     * @throws RecreatingRepositoryException if repository was already created
     */
    public void init() throws RecreatingRepositoryException,
            RepositoryNotInitializedException {
        dataManager.initRepository();
    }

    /**
     * Method which add file to stage (list of files to commit).
     *
     * @param fileName relative path to target file
     * @throws LostFileException     if file contained one of interesting object was corrupted
     * @throws BrokenFileException   if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException if repository was not initialized
     */
    public void add(@NotNull String fileName)
            throws RepositoryNotInitializedException, LostFileException, BrokenFileException {
        File file = dataManager.getFile(fileName);
        ContentDescriptor stage = dataManager.getStage();
        ContentDescriptor updatedStage;
        if (!file.isFile()) {
            throw new Error("Possible to add files only");
        }
        String fileID = dataManager.addFile(file);
        updatedStage = ContentDescriptor.builder()
                .addAllFiles(stage)
                .addFile(fileName, fileID)
                .build();
        dataManager.putStage(updatedStage);
    }

    /**
     * Method which get list of changed from stage,
     * create Commit and add it to head of current branch
     *
     * @param message text which explain changes which was made in this commit
     * @throws LostFileException     if file contained one of interesting object was corrupted
     * @throws BrokenFileException   if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException if repository was not initialized
     */
    public void commit(@NotNull String message) throws BrokenFileException,
            LostFileException, RepositoryNotInitializedException {
        Header header = dataManager.getHeader();
        Branch currentBranch = dataManager.fetchBranch(header.getCurrentBranchName());
        VersionNode currentVersion = dataManager.fetchVersionNode(currentBranch.getVersionNodeID());
        Commit lastCommit = dataManager.fetchCommit(currentVersion.getCommitID());
        ContentDescriptor currentDescriptor = dataManager.fetchContentDescriptor(lastCommit.getContentDescriptorID());
        ContentDescriptor stage = dataManager.getStage();

        ContentDescriptor updatedDescriptor = ContentDescriptor.builder()
                .addAllFiles(currentDescriptor)
                .addAllFiles(stage)
                .build();
        String descriptorID = dataManager.addContentDescriptor(updatedDescriptor);
        Commit newCommit = new Commit(descriptorID, message, System.currentTimeMillis(), header.getAuthor());
        String commitID = dataManager.addCommit(newCommit);
        VersionNode newVersion = Algorithms.createVersionNode(commitID, currentBranch.getVersionNodeID(), dataManager);
        String versionID = dataManager.addVersionNode(newVersion);
        Branch updatedBranch = new Branch(versionID, currentBranch.getName());
        dataManager.addBranch(updatedBranch);
        dataManager.putStage(ContentDescriptor.EMPTY);
    }

    /**
     * Method which return list of node's parents sorted by increasing distance
     *
     * @param lengthLimit limit size of returned List
     * @throws LostFileException   if file contained one of interesting object was corrupted
     * @throws BrokenFileException if file contained one of interesting object was not found
     */
    @NotNull
    public List<Commit> history(@NotNull String lengthLimit)
            throws BrokenFileException, LostFileException {
        int limit;
        try {
            limit = Integer.parseInt(lengthLimit);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Cant parse length limit", e);
        }
        Header header = dataManager.getHeader();
        Branch currentBranch = dataManager.fetchBranch(header.getCurrentBranchName());
        List<VersionNode> versions = Algorithms.getAllParents(currentBranch.getVersionNodeID(), limit, dataManager);
        List<Commit> commits = new ArrayList<>();
        for (VersionNode versionNode : versions) {
            commits.add(dataManager.fetchCommit(versionNode.getCommitID()));
        }
        return commits;
    }

    /**
     * Method create new Branch which current version
     * equal to version of active branch
     *
     * @param branchName name of new branch
     * @throws LostFileException     if file contained one of interesting object was corrupted
     * @throws BrokenFileException   if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException if repository was not initialized
     * @throws ConflictNameException if branch with equal name is already exist
     */
    public void createBranch(@NotNull String branchName)
            throws BrokenFileException, LostFileException, ConflictNameException, RepositoryNotInitializedException {
        Header header = dataManager.getHeader();
        Branch currentBranch = dataManager.fetchBranch(header.getCurrentBranchName());
        if (dataManager.hasBranch(branchName)) {
            throw new ConflictNameException("Branch with the same name is already exist");
        }
        Branch newBranch = new Branch(currentBranch.getVersionNodeID(), branchName);
        dataManager.addBranch(newBranch);
    }

    /**
     * Method removes record about given branch
     *
     * @param branchName name of branch to remove
     * @throws LostFileException           if file contained one of interesting object was corrupted
     * @throws BrokenFileException         if file contained one of interesting object was not found
     * @throws RemoveActiveBranchException if user try to remove active(current) branch
     * @throws UnknownBranchException      if branch with the same name wasn't found
     */
    public void removeBranch(@NotNull String branchName)
            throws BrokenFileException, LostFileException,
            RemoveActiveBranchException, UnknownBranchException {
        Header header = dataManager.getHeader();
        if (header.getCurrentBranchName().equals(branchName)) {
            throw new RemoveActiveBranchException("Cant remove active branch");
        }
        if (!dataManager.hasBranch(branchName)) {
            throw new UnknownBranchException("Branch to remove doesn't exist");
        }
        dataManager.removeBranch(branchName);
    }

    /**
     * Method merges given branch into current branch
     * Conflicts can be resolved only if there is no file,
     * which was changed in both branches since theirs last common version
     * Stage have to be empty before merging
     *
     * @param branchName name of branch to remove
     * @param message    text ehich will be used in commit
     * @throws LostFileException     if file contained one of interesting object was corrupted
     * @throws BrokenFileException   if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException if repository was not initialized
     * @throws IllegalBranchToMergeException if it's logically impossible to merge thar branches
     * @throws UnknownBranchException        if branch with the same name wasn't found
     * @throws UncommittedChangesException   if stage not empty
     * @throws ConflictMergeException        if unresolvable conflict was found
     */
    public void mergeBranch(@NotNull String branchName, @NotNull String message)
            throws BrokenFileException, LostFileException, IllegalBranchToMergeException, UnknownBranchException,
            UncommittedChangesException, ConflictMergeException, RepositoryNotInitializedException {
        Header header = dataManager.getHeader();
        if (header.getCurrentBranchName().equals(branchName)) {
            throw new IllegalBranchToMergeException("Cant merge branch with itself");
        }
        if (!dataManager.hasBranch(branchName)) {
            throw new UnknownBranchException("Branch to merge doesn't exist");
        }
        if (!dataManager.getStage().getFiles().isEmpty()) {
            throw new UncommittedChangesException("Commit changes before merge");
        }
        String activeVersionID = dataManager.fetchBranch(header.getCurrentBranchName()).getVersionNodeID();
        String sideVersionID = dataManager.fetchBranch(branchName).getVersionNodeID();
        String lcaVersionID = Algorithms.findLowestCommonAncestor(activeVersionID, sideVersionID, dataManager);
        ContentDescriptor activeContent = toContentDescriptor(activeVersionID);
        ContentDescriptor sideContent = toContentDescriptor(sideVersionID);
        ContentDescriptor lcaContent = toContentDescriptor(lcaVersionID);
        List<String> conflicts = checkConflicts(activeContent, sideContent, lcaContent);
        if (!conflicts.isEmpty()) {
            throw new ConflictMergeException("Conflicts was found", conflicts);
        }
        ContentDescriptor mergedDescriptor = mergeContent(activeContent, sideContent, lcaContent);
        String descriptorID = dataManager.addContentDescriptor(mergedDescriptor);
        Commit commit = new Commit(descriptorID, message, System.currentTimeMillis(), header.getAuthor());
        String commitID = dataManager.addCommit(commit);
        VersionNode versionNode = Algorithms.createVersionNode(commitID, activeVersionID, dataManager);
        String versionNodeID = dataManager.addVersionNode(versionNode);
        Branch updatedBranch = new Branch(versionNodeID, header.getCurrentBranchName());
        dataManager.addBranch(updatedBranch);
    }

    /**
     * Method which merge ContentDescriptors.
     * Result of merging - ContentDescriptor contained changes from both descriptors
     */
    @NotNull
    private static ContentDescriptor mergeContent(@NotNull ContentDescriptor descriptor1,
                                                  @NotNull ContentDescriptor descriptor2,
                                                  @NotNull ContentDescriptor lcaDescriptor) {
        ContentDescriptor.Builder builder = ContentDescriptor.builder();
        Map<String, String> files1 = descriptor1.getFiles();
        Map<String, String> files2 = descriptor2.getFiles();
        Map<String, String> lcaFiles = lcaDescriptor.getFiles();

        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(files1.keySet());
        allFiles.addAll(files2.keySet());

        for (String path : allFiles) {
            String resultVersion = mergeFileVersions(files1.get(path), files2.get(path), lcaFiles.get(path));
            if (resultVersion != null) {
                builder.addFile(path, resultVersion);
            }
        }
        return builder.build();
    }

    /**
     * Check if these versions cause unresolvable conflict
     */
    private static boolean hasConflict(@Nullable String version1, @Nullable String version2,
                                       @Nullable String lcaVersion) {
        return !Objects.equals(version1, version2) && !Objects.equals(version1, lcaVersion)
                && !Objects.equals(version2, lcaVersion);
    }

    /**
     * Choose result version of file, based on it's version in different VersionNodes and in their LCA
     */
    @Nullable
    private static String mergeFileVersions(@Nullable String version1, @Nullable String version2,
                                            @Nullable String lcaVersion) {
        return Objects.equals(version1, lcaVersion) ? version2 : version1;
    }

    /**
     * Method which finds all unresolvable conflicts between different versions
     *
     * @param descriptor1   version of file in one Node
     * @param descriptor2   version of file in another Node
     * @param lcaDescriptor version of file in their LCA
     */
    @NotNull
    private static List<String> checkConflicts(@NotNull ContentDescriptor descriptor1,
                                               @NotNull ContentDescriptor descriptor2,
                                               @NotNull ContentDescriptor lcaDescriptor) {
        List<String> conflicts = new ArrayList<>();

        Map<String, String> files1 = descriptor1.getFiles();
        Map<String, String> files2 = descriptor2.getFiles();
        Map<String, String> lcaFiles = lcaDescriptor.getFiles();

        Set<String> allFiles = new HashSet<>();
        allFiles.addAll(files1.keySet());
        allFiles.addAll(files2.keySet());

        Set<String> folders = new HashSet<>();

        for (String path : allFiles) {
            if (hasConflict(files1.get(path), files2.get(path), lcaFiles.get(path))) {
                conflicts.add(path);
                continue;
            }
            if (mergeFileVersions(files1.get(path), files2.get(path), lcaFiles.get(path)) == null) {
                continue;
            }
            Path folder = Paths.get(path).getParent();
            while (folder != null) {
                folders.add(folder.toString());
                folder = folder.getParent();
            }
        }

        for (String path : allFiles) {
            if (!hasConflict(files1.get(path), files2.get(path), lcaFiles.get(path))
                    && mergeFileVersions(files1.get(path), files2.get(path), lcaFiles.get(path)) != null
                    && folders.contains(path)) {
                conflicts.add(path);
            }
        }

        return conflicts;
    }

    /**
     * Sugar to simplify getting ContentDescriptor from ID of VersionNode
     *
     * @param versionID   id of VersionNode
     * @return loaded ContentDescriptor
     * @throws LostFileException   if file contained one of interesting object was corrupted
     * @throws BrokenFileException if file contained one of interesting object was not found
     */
    @NotNull
    private ContentDescriptor toContentDescriptor(@NotNull String versionID)
            throws BrokenFileException, LostFileException {
        VersionNode versionNode = dataManager.fetchVersionNode(versionID);
        Commit commit = dataManager.fetchCommit(versionNode.getCommitID());
        return dataManager.fetchContentDescriptor(commit.getContentDescriptorID());
    }

    /**
     * @param branchName name of interesting branch
     * @throws LostFileException     if file contained one of interesting object was corrupted
     * @throws BrokenFileException   if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException if repository was not initialized
     * @throws SwitchOnCurrentBranchException if you try to switch on your current branch
     * @throws UncommittedChangesException    if stage isn't empty before change branch
     * @throws UnknownBranchException         if such branch wasn't found
     * @throws IOException                    in case of some IO problems
     */
    public void switchBranch(@NotNull String branchName)
            throws BrokenFileException, LostFileException, SwitchOnCurrentBranchException,
            UncommittedChangesException, UnknownBranchException, IOException, RepositoryNotInitializedException {
        Header header = dataManager.getHeader();
        if (header.getCurrentBranchName().equals(branchName)) {
            throw new SwitchOnCurrentBranchException("This branch is already chosen");
        }
        ContentDescriptor contentDescriptor = dataManager.getStage();
        if (!contentDescriptor.getFiles().isEmpty()) {
            throw new UncommittedChangesException("Commit changes before switch branch");
        }
        if (!dataManager.hasBranch(branchName)) {
            throw new UnknownBranchException("Branch wasn't found");
        }
        Branch branch = dataManager.fetchBranch(branchName);
        VersionNode versionNode = dataManager.fetchVersionNode(branch.getVersionNodeID());
        Commit lastCommit = dataManager.fetchCommit(versionNode.getCommitID());
        checkout(lastCommit.getContentDescriptorID());
        Header updatedHeader = new Header(header.getAuthor(), branchName);
        dataManager.putHeader(updatedHeader);
    }

    /**
     * Restore saved copies from current Branch
     *
     * @throws LostFileException     if file contained one of interesting object was corrupted
     * @throws BrokenFileException   if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException if repository was not initialized
     * @throws IOException         in case of some IO problems
     */
    public void reset() throws BrokenFileException, LostFileException, IOException,
            RepositoryNotInitializedException {
        dataManager.putStage(ContentDescriptor.EMPTY);
        Header header = dataManager.getHeader();
        Branch branch = dataManager.fetchBranch(header.getCurrentBranchName());
        VersionNode versionNode = dataManager.fetchVersionNode(branch.getVersionNodeID());
        Commit lastCommit = dataManager.fetchCommit(versionNode.getCommitID());
        checkout(lastCommit.getContentDescriptorID());
    }

    /**
     * Restore saved file from given ContentDescriptor
     *
     * @param descriptorID id of interesting description
     * @throws LostFileException   if file contained one of interesting object was corrupted
     * @throws BrokenFileException if file contained one of interesting object was not found
     * @throws IOException         in case of some IO problems
     */
    public void checkout(@NotNull String descriptorID)
            throws IOException, BrokenFileException, LostFileException {
        dataManager.clearWorkingCopy();
        dataManager.loadFiles(descriptorID);
    }

    /**
     * This method remove all file from working directory
     */
    public void clear() {
        dataManager.clearWorkingCopy();
    }

    /**
     * Remove repository, but doesn't touch files in working copy
     *
     * @throws RepositoryNotInitializedException if there is nothing to delete
     */
    public void uninstall() throws RepositoryNotInitializedException {
        dataManager.uninstallRepository();
    }

    public static class SwitchOnCurrentBranchException extends VersionControlSystemException {
        SwitchOnCurrentBranchException(String message) {
            super(message);
        }
    }

    public static class UncommittedChangesException extends VersionControlSystemException {
        UncommittedChangesException(String message) {
            super(message);
        }
    }

    public static class UnknownBranchException extends VersionControlSystemException {
        UnknownBranchException(String message) {
            super(message);
        }
    }

    public static class ConflictNameException extends VersionControlSystemException {
        ConflictNameException(String message) {
            super(message);
        }
    }

    public static class RemoveActiveBranchException extends VersionControlSystemException {
        RemoveActiveBranchException(String message) {
            super(message);
        }
    }

    public static class ConflictMergeException extends VersionControlSystemException {

        private final List<String> conflicts;

        ConflictMergeException(String message, List<String> conflicts) {
            super(message);
            this.conflicts = Collections.unmodifiableList(conflicts);
        }

        public List<String> getConflicts() {
            return conflicts;
        }
    }

    public static class IllegalBranchToMergeException extends VersionControlSystemException {
        IllegalBranchToMergeException(String message) {
            super(message);
        }
    }
}
