package ru.spbau.lobanov.liteVCS.logic;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
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

    private LiteVCS() {
    }


    public static void init(@NotNull String path) throws RecreatingRepositoryException,
            RepositoryNotInitializedException {
        new DataManager(path).initRepository();
    }

    /**
     * Method which add file to stage (list of files to commit).
     *
     * @param path path to working directory
     * @throws RecreatingRepositoryException if repository was already created
     */
    public static void add(@NotNull String path, @NotNull String fileName)
            throws VersionControlSystemException {
        File file = Paths.get(path, fileName).toFile();
        DataManager dataManager = new DataManager(path);
        ContentDescriptor stage = dataManager.getStage();
        ContentDescriptor updatedStage;
        if (!file.isFile()) {
            throw new Error("Possible to add files only");
        }
        String fileID = dataManager.addFile(file);
        String relatedPath = Paths.get(path).relativize(file.toPath()).toString();
        updatedStage = ContentDescriptor.builder()
                .addAllFiles(stage)
                .addFile(relatedPath, fileID)
                .build();
        dataManager.putStage(updatedStage);
    }

    /**
     * Method which get list of changed from stage,
     * create Commit and add it to head of current branch
     *
     * @param path    path to working directory
     * @param message text which explain changes which was made in this commit
     * @throws LostFileException   if file contained one of interesting object was corrupted
     * @throws BrokenFileException if file contained one of interesting object was not found
     */
    public static void commit(@NotNull String path, @NotNull String message) throws BrokenFileException,
            LostFileException, RepositoryNotInitializedException {
        DataManager dataManager = new DataManager(path);
        Header header = dataManager.getHeader();
        Branch currentBranch = dataManager.getBranch(header.getCurrentBranchName());
        VersionNode currentVersion = dataManager.getVersionNode(currentBranch.getVersionNodeID());
        Commit lastCommit = dataManager.getCommit(currentVersion.getCommitID());
        ContentDescriptor currentDescriptor = dataManager.getContentDescriptor(lastCommit.getContentDescriptorID());
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
     * @param path        path to working directory
     * @param lengthLimit limit size of returned List
     * @throws LostFileException   if file contained one of interesting object was corrupted
     * @throws BrokenFileException if file contained one of interesting object was not found
     */
    @NotNull
    public static List<Commit> log(@NotNull String path, @NotNull String lengthLimit)
            throws BrokenFileException, LostFileException {
        int limit = Integer.MAX_VALUE;
        if (lengthLimit != null) {
            try {
                limit = Integer.parseInt(lengthLimit);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cant parse length limit", e);
            }
        }
        DataManager dataManager = new DataManager(path);
        Header header = dataManager.getHeader();
        Branch currentBranch = dataManager.getBranch(header.getCurrentBranchName());
        List<VersionNode> versions = Algorithms.getAllParents(currentBranch.getVersionNodeID(), limit, dataManager);
        List<Commit> commits = new ArrayList<>();
        for (VersionNode versionNode : versions) {
            commits.add(dataManager.getCommit(versionNode.getCommitID()));
        }
        return commits;
    }

    /**
     * Method create new Branch which current version
     * equal to version of active branch
     *
     * @param path       path to working directory
     * @param branchName name of new branch
     * @throws LostFileException     if file contained one of interesting object was corrupted
     * @throws BrokenFileException   if file contained one of interesting object was not found
     * @throws ConflictNameException if branch with equal name is already exist
     */
    public static void createBranch(@NotNull String path, @NotNull String branchName)
            throws BrokenFileException, LostFileException, ConflictNameException, RepositoryNotInitializedException {
        DataManager dataManager = new DataManager(path);
        Header header = dataManager.getHeader();
        Branch currentBranch = dataManager.getBranch(header.getCurrentBranchName());
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
     * @param path       path to working directory
     * @throws LostFileException           if file contained one of interesting object was corrupted
     * @throws BrokenFileException         if file contained one of interesting object was not found
     * @throws RemoveActiveBranchException if user try to remove active(current) branch
     * @throws UnknownBranchException      if branch with the same name wasn't found
     */
    public static void removeBranch(@NotNull String path, @NotNull String branchName)
            throws BrokenFileException, LostFileException,
            RemoveActiveBranchException, UnknownBranchException {
        DataManager dataManager = new DataManager(path);
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
     * @param path       path to working directory
     * @throws LostFileException             if file contained one of interesting object was corrupted
     * @throws BrokenFileException           if file contained one of interesting object was not found
     * @throws IllegalBranchToMergeException if it's logically impossible to merge thar branches
     * @throws UnknownBranchException        if branch with the same name wasn't found
     * @throws UncommittedChangesException   if stage not empty
     * @throws ConflictMergeException        if unresolvable conflict was found
     */
    public static void mergeBranch(@NotNull String path, @NotNull String branchName, @NotNull String message)
            throws BrokenFileException, LostFileException, IllegalBranchToMergeException, UnknownBranchException,
            UncommittedChangesException, ConflictMergeException, RepositoryNotInitializedException {
        DataManager dataManager = new DataManager(path);
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
        String activeVersionID = dataManager.getBranch(header.getCurrentBranchName()).getVersionNodeID();
        String sideVersionID = dataManager.getBranch(branchName).getVersionNodeID();
        String lcaVersionID = Algorithms.findLowestCommonAncestor(activeVersionID, sideVersionID, dataManager);
        ContentDescriptor activeContent = toContentDescriptor(activeVersionID, dataManager);
        ContentDescriptor sideContent = toContentDescriptor(sideVersionID, dataManager);
        ContentDescriptor lcaContent = toContentDescriptor(lcaVersionID, dataManager);
        List<String> conflicts = checkConflicts(activeContent, sideContent, lcaContent);
        if (!conflicts.isEmpty()) {
            throw new ConflictMergeException("Conflicts was found", conflicts);
        }
        ContentDescriptor mergedDescriptor = mergeContent(activeContent, sideContent, lcaContent);
        String descriptorID = dataManager.addContentDescriptor(mergedDescriptor);
        String commitMessage = Objects.nonNull(message) ? message : "Merge branch " + branchName;
        Commit commit = new Commit(descriptorID, commitMessage, System.currentTimeMillis(), header.getAuthor());
        String commitID = dataManager.addCommit(commit);
        VersionNode versionNode = Algorithms.createVersionNode(commitID, activeVersionID, dataManager);
        String versionNodeID = dataManager.addVersionNode(versionNode);
        Branch updatedBranch = new Branch(versionNodeID, header.getCurrentBranchName());
        dataManager.addBranch(updatedBranch);
        dataManager.addBranch(new Branch(descriptorID, header.getCurrentBranchName()));
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
     * @param dataManager dataManager, which provides access to the files
     * @return loaded ContentDescriptor
     * @throws LostFileException   if file contained one of interesting object was corrupted
     * @throws BrokenFileException if file contained one of interesting object was not found
     */
    @NotNull
    private static ContentDescriptor toContentDescriptor(@NotNull String versionID, @NotNull DataManager dataManager)
            throws BrokenFileException, LostFileException {
        VersionNode versionNode = dataManager.getVersionNode(versionID);
        Commit commit = dataManager.getCommit(versionNode.getCommitID());
        return dataManager.getContentDescriptor(commit.getContentDescriptorID());
    }

    /**
     * @param path       path to working directory
     * @param branchName name of interesting branch
     * @throws LostFileException              if file contained one of interesting object was corrupted
     * @throws BrokenFileException            if file contained one of interesting object was not found
     * @throws SwitchOnCurrentBranchException if you try to switch on your current branch
     * @throws UncommittedChangesException    if stage isn't empty before change branch
     * @throws UnknownBranchException         if such branch wasn't found
     * @throws IOException                    in case of some IO problems
     */
    public static void switchBranch(@NotNull String path, @NotNull String branchName)
            throws BrokenFileException, LostFileException, SwitchOnCurrentBranchException,
            UncommittedChangesException, UnknownBranchException, IOException, RepositoryNotInitializedException {
        DataManager dataManager = new DataManager(path);
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
        Branch branch = dataManager.getBranch(branchName);
        VersionNode versionNode = dataManager.getVersionNode(branch.getVersionNodeID());
        Commit lastCommit = dataManager.getCommit(versionNode.getCommitID());
        checkout(path, lastCommit.getContentDescriptorID());
        Header updatedHeader = new Header(header.getAuthor(), branchName);
        dataManager.putHeader(updatedHeader);
    }

    /**
     * Restore saved copies from current Branch
     *
     * @param path path to working directory
     * @throws LostFileException   if file contained one of interesting object was corrupted
     * @throws BrokenFileException if file contained one of interesting object was not found
     * @throws IOException         in case of some IO problems
     */
    public static void reset(@NotNull String path) throws BrokenFileException, LostFileException, IOException,
            RepositoryNotInitializedException {
        DataManager dataManager = new DataManager(path);
        dataManager.putStage(ContentDescriptor.EMPTY);
        Header header = dataManager.getHeader();
        Branch branch = dataManager.getBranch(header.getCurrentBranchName());
        VersionNode versionNode = dataManager.getVersionNode(branch.getVersionNodeID());
        Commit lastCommit = dataManager.getCommit(versionNode.getCommitID());
        checkout(path, lastCommit.getContentDescriptorID());
    }

    /**
     * Restore saved file from given ContentDescriptor
     *
     * @param path         path to working directory
     * @param descriptorID id of interesting description
     * @throws LostFileException   if file contained one of interesting object was corrupted
     * @throws BrokenFileException if file contained one of interesting object was not found
     * @throws IOException         in case of some IO problems
     */
    public static void checkout(@NotNull String path, @NotNull String descriptorID)
            throws IOException, BrokenFileException, LostFileException {
        DataManager dataManager = new DataManager(path);
        dataManager.clearWorkingCopy();
        dataManager.loadFiles(descriptorID);
    }

    /**
     * This method remove all file from working directory
     *
     * @param path path to working directory
     */
    public static void clear(@NotNull String path) {
        new DataManager(path).clearWorkingCopy();
    }

    /**
     * @param path path to working directory
     * @throws RepositoryNotInitializedException if there is nothing to delete
     */
    public static void uninstall(@NotNull String path) throws RepositoryNotInitializedException {
        new DataManager(path).uninstallRepository();
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
