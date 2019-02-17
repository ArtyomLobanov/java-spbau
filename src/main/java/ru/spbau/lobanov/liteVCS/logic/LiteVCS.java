package ru.spbau.lobanov.liteVCS.logic;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.lobanov.liteVCS.logic.DataManager.*;
import ru.spbau.lobanov.liteVCS.primitives.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.Logger;

import static ru.spbau.lobanov.liteVCS.logic.LiteVCS.FileStatus.*;
import static ru.spbau.lobanov.liteVCS.logic.LiteVCS.StageStatus.REMOVED;
import static ru.spbau.lobanov.liteVCS.logic.LiteVCS.StageStatus.UPDATED;

/**
 * Special class which provides all the main
 * functionality of library by static methods
 */
public class LiteVCS {

    private static final Logger logger = Logger.getLogger(LiteVCS.class.getName());

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
     * @throws LostFileException                 if file contained one of interesting object was corrupted
     * @throws BrokenFileException               if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException if repository was not initialized
     */
    public void hello(@NotNull String author) throws BrokenFileException,
            LostFileException, RepositoryNotInitializedException {
        checkStatus();
        String branchName = dataManager.getHeader().getCurrentBranchName();
        dataManager.putHeader(new Header(author, branchName));
        logger.fine("Author's name set (" + author + ")");
    }

    /**
     * Wrapper for init-method of DataManager
     *
     * @throws RecreatingRepositoryException if repository was already created
     */
    public void init() throws RecreatingRepositoryException, IOException {
        dataManager.initRepository();
        logger.fine("Repository successfully created");
    }

    /**
     * Method which add file to stage (list of files to commit).
     *
     * @param fileName relative path to target file
     * @throws LostFileException                 if file contained one of interesting object was corrupted
     * @throws BrokenFileException               if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException if repository was not initialized
     */
    public void add(@NotNull String fileName) throws RepositoryNotInitializedException,
            LostFileException, BrokenFileException, NonexistentFileAdditionException {
        checkStatus();
        Stage stage = dataManager.getStage();
        String fileID = dataManager.addFile(fileName);
        Stage updatedStage = stage.change()
                .addFile(fileName, fileID)
                .build();
        dataManager.putStage(updatedStage);
        logger.fine("File " + fileName + " was added to stage area");
    }

    /**
     * Method which mark file as removed at stage and remove that file from working directory
     *
     * @param fileName relative path to target file
     * @throws RepositoryNotInitializedException if repository was not initialized
     */
    public void remove(@NotNull String fileName) throws RepositoryNotInitializedException,
            LostFileException, BrokenFileException, NonexistentFileDeletionException {
        checkStatus();
        Header header = dataManager.getHeader();
        Branch currentBranch = dataManager.fetchBranch(header.getCurrentBranchName());
        VersionNode currentVersion = dataManager.fetchVersionNode(currentBranch.getVersionNodeID());
        Commit lastCommit = dataManager.fetchCommit(currentVersion.getCommitID());
        ContentDescriptor descriptor = dataManager.fetchContentDescriptor(lastCommit.getContentDescriptorID());
        Stage stage = dataManager.getStage();
        if (descriptor.getFiles().containsKey(fileName) || stage.getChangedFiles().containsKey(fileName)) {
            Stage updatedStage = stage.change()
                    .removeFile(fileName)
                    .build();
            dataManager.putStage(updatedStage);
            logger.fine("File " + fileName + " will be finally removed from repository in next commit");
        }
        dataManager.removeFile(fileName);
    }

    /**
     * Method which get list of changed from stage,
     * create Commit and add it to head of current branch
     *
     * @param message text which explain changes which was made in this commit
     * @throws LostFileException                 if file contained one of interesting object was corrupted
     * @throws BrokenFileException               if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException if repository was not initialized
     */
    public void commit(@NotNull String message) throws BrokenFileException,
            LostFileException, RepositoryNotInitializedException {
        checkStatus();
        Header header = dataManager.getHeader();
        Branch currentBranch = dataManager.fetchBranch(header.getCurrentBranchName());
        VersionNode currentVersion = dataManager.fetchVersionNode(currentBranch.getVersionNodeID());
        Commit lastCommit = dataManager.fetchCommit(currentVersion.getCommitID());
        ContentDescriptor currentDescriptor = dataManager.fetchContentDescriptor(lastCommit.getContentDescriptorID());
        Stage stage = dataManager.getStage();

        ContentDescriptor updatedDescriptor = ContentDescriptor.builder()
                .addAll(currentDescriptor)
                .addAll(stage)
                .build();
        String descriptorID = dataManager.addContentDescriptor(updatedDescriptor);
        Commit newCommit = new Commit(descriptorID, message, System.currentTimeMillis(), header.getAuthor());
        String commitID = dataManager.addCommit(newCommit);
        VersionNode newVersion = Algorithms.createVersionNode(commitID, currentBranch.getVersionNodeID(), dataManager);
        String versionID = dataManager.addVersionNode(newVersion);
        Branch updatedBranch = new Branch(versionID, currentBranch.getName());
        dataManager.addBranch(updatedBranch);
        dataManager.putStage(Stage.EMPTY);
        logger.fine("New commit created (descriptor id = " + descriptorID + ")");
    }

    /**
     * Method which return list of node's parents sorted by increasing distance
     *
     * @param lengthLimit limit size of returned List
     * @throws LostFileException                 if file contained one of interesting object was corrupted
     * @throws BrokenFileException               if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException is repository wasn't found
     */
    @NotNull
    public List<Commit> history(@NotNull String lengthLimit) throws BrokenFileException, LostFileException,
            RepositoryNotInitializedException {
        checkStatus();
        int limit;
        try {
            limit = Integer.parseInt(lengthLimit);
        } catch (NumberFormatException e) {
            logger.fine("Failed to show history, because length limit has wrong format: " + lengthLimit);
            throw new IllegalArgumentException("Cant parse length limit", e);
        }
        Header header = dataManager.getHeader();
        Branch currentBranch = dataManager.fetchBranch(header.getCurrentBranchName());
        List<VersionNode> versions = Algorithms.getAllParents(currentBranch.getVersionNodeID(), limit, dataManager);
        List<Commit> commits = new ArrayList<>();
        for (VersionNode versionNode : versions) {
            commits.add(dataManager.fetchCommit(versionNode.getCommitID()));
        }
        logger.fine("History of commit was shown (" + commits.size() + " commits)");
        return commits;
    }

    /**
     * Method create new Branch which current version
     * equal to version of active branch
     *
     * @param branchName name of new branch
     * @throws LostFileException                 if file contained one of interesting object was corrupted
     * @throws BrokenFileException               if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException if repository was not initialized
     * @throws ConflictNameException             if branch with equal name is already exist
     */
    public void createBranch(@NotNull String branchName) throws BrokenFileException, LostFileException,
            ConflictNameException, RepositoryNotInitializedException {
        checkStatus();
        Header header = dataManager.getHeader();
        Branch currentBranch = dataManager.fetchBranch(header.getCurrentBranchName());
        if (dataManager.hasBranch(branchName)) {
            logger.warning("Failed to create new branch because of name collision");
            throw new ConflictNameException("Branch with the same name is already exist");
        }
        Branch newBranch = new Branch(currentBranch.getVersionNodeID(), branchName);
        dataManager.addBranch(newBranch);
        logger.fine("New branch successfully created (name = " + branchName + ")");
    }

    /**
     * Method removes record about given branch
     *
     * @param branchName name of branch to remove
     * @throws LostFileException                 if file contained one of interesting object was corrupted
     * @throws BrokenFileException               if file contained one of interesting object was not found
     * @throws RemoveActiveBranchException       if user try to remove active(current) branch
     * @throws UnknownBranchException            if branch with the same name wasn't found
     * @throws RepositoryNotInitializedException is repository wasn't found
     */
    public void removeBranch(@NotNull String branchName) throws BrokenFileException, LostFileException,
            RemoveActiveBranchException, UnknownBranchException, RepositoryNotInitializedException {
        checkStatus();
        Header header = dataManager.getHeader();
        if (header.getCurrentBranchName().equals(branchName)) {
            logger.warning("Failed to remove branch because target branch was active");
            throw new RemoveActiveBranchException("Cant remove active branch");
        }
        if (!dataManager.hasBranch(branchName)) {
            logger.warning("Failed to remove branch because branch with name=" + branchName + " wasn't found");
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
     * @throws LostFileException                 if file contained one of interesting object was corrupted
     * @throws BrokenFileException               if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException if repository was not initialized
     * @throws IllegalBranchToMergeException     if it's logically impossible to merge thar branches
     * @throws UnknownBranchException            if branch with the same name wasn't found
     * @throws UncommittedChangesException       if stage not empty
     * @throws ConflictMergeException            if unresolvable conflict was found
     */
    public void mergeBranch(@NotNull String branchName, @NotNull String message)
            throws BrokenFileException, LostFileException, IllegalBranchToMergeException, UnknownBranchException,
            UncommittedChangesException, ConflictMergeException, RepositoryNotInitializedException {
        checkStatus();
        Header header = dataManager.getHeader();
        if (header.getCurrentBranchName().equals(branchName)) {
            logger.warning("Failed to merge branches. Cant merge branch with itself");
            throw new IllegalBranchToMergeException("Cant merge branch with itself");
        }
        if (!dataManager.hasBranch(branchName)) {
            logger.warning("Failed to merge branches because one of them (" + branchName + " wasn't found");
            throw new UnknownBranchException("Branch to merge doesn't exist");
        }
        if (!dataManager.getStage().isEmpty()) {
            logger.warning("Failed to merge branches. Stage wasn't empty");
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
            logger.warning("Failed to merge branches because of difficult conflicts");
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
        logger.warning("Branch " + branchName + " was successfully merged into " + header.getCurrentBranchName());
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
     * @param versionID id of VersionNode
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

    @NotNull
    private ContentDescriptor getActualDescriptor() throws BrokenFileException, LostFileException {
        Header header = dataManager.getHeader();
        Branch currentBranch = dataManager.fetchBranch(header.getCurrentBranchName());
        VersionNode currentVersion = dataManager.fetchVersionNode(currentBranch.getVersionNodeID());
        Commit lastCommit = dataManager.fetchCommit(currentVersion.getCommitID());
        return dataManager.fetchContentDescriptor(lastCommit.getContentDescriptorID());
    }

    /**
     * @param branchName name of interesting branch
     * @throws LostFileException                 if file contained one of interesting object was corrupted
     * @throws BrokenFileException               if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException if repository was not initialized
     * @throws SwitchOnCurrentBranchException    if you try to switch on your current branch
     * @throws UncommittedChangesException       if stage isn't empty before change branch
     * @throws UnknownBranchException            if such branch wasn't found
     * @throws IOException                       in case of some IO problems
     */
    public void switchBranch(@NotNull String branchName)
            throws BrokenFileException, LostFileException, SwitchOnCurrentBranchException,
            UncommittedChangesException, UnknownBranchException, IOException, RepositoryNotInitializedException {
        checkStatus();
        Header header = dataManager.getHeader();
        if (header.getCurrentBranchName().equals(branchName)) {
            logger.warning("Failed to switch branches. Target branch is already active");
            throw new SwitchOnCurrentBranchException("This branch is already chosen");
        }
        if (!dataManager.getStage().isEmpty()) {
            logger.warning("Failed to switch branches. Stage area isn't empty");
            throw new UncommittedChangesException("Commit changes before switch branch");
        }
        if (!dataManager.hasBranch(branchName)) {
            logger.warning("Failed to switch branches. Branch wasn't found (" + branchName + ")");
            throw new UnknownBranchException("Branch wasn't found");
        }
        Branch branch = dataManager.fetchBranch(branchName);
        VersionNode versionNode = dataManager.fetchVersionNode(branch.getVersionNodeID());
        Commit lastCommit = dataManager.fetchCommit(versionNode.getCommitID());
        checkout(lastCommit.getContentDescriptorID());
        Header updatedHeader = new Header(header.getAuthor(), branchName);
        dataManager.putHeader(updatedHeader);
        logger.fine(branchName + " is active branch now");
    }

    /**
     * Restore saved copy from current Branch
     *
     * @throws LostFileException                 if file contained one of interesting object was corrupted
     * @throws BrokenFileException               if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException if repository was not initialized
     * @throws IOException                       in case of some IO problems
     * @throws UnobservedFileException           in case if filename wasn't observed before
     */
    public void reset(String filename) throws BrokenFileException, LostFileException, IOException,
            RepositoryNotInitializedException, UnobservedFileException {
        checkStatus();
        ContentDescriptor descriptor = getActualDescriptor();
        if (!descriptor.getFiles().containsKey(filename)) {
            logger.warning("Try to reset " + filename + ", but it wasn't observed");
            throw new UnobservedFileException("File " + filename + " have no saved versions");
        }
        logger.fine("Load saved version of file " + filename);
        dataManager.loadFile(descriptor.getFiles().get(filename), filename);
        Stage stage = dataManager.getStage();
        if (stage.getChangedFiles().containsKey(filename) || stage.getRemovedFiles().contains(filename)) {
            Stage updatedStage = stage.change()
                    .reset(filename)
                    .build();
            dataManager.putStage(updatedStage);
            logger.fine("Information about " + filename + " was removed from stage");
        }
    }

    /**
     * Restore saved files from given ContentDescriptor
     *
     * @param descriptorID id of interesting description
     * @throws LostFileException                 if file contained one of interesting object was corrupted
     * @throws BrokenFileException               if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException is repository wasn't found
     * @throws IOException                       in case of some IO problems
     */
    public void checkout(@NotNull String descriptorID) throws IOException, BrokenFileException,
            LostFileException, RepositoryNotInitializedException {
        checkStatus();
        dataManager.clearWorkingCopy();
        ContentDescriptor contentDescriptor = dataManager.fetchContentDescriptor(descriptorID);
        for (Map.Entry<String, String> file : contentDescriptor.getFiles().entrySet()) {
            dataManager.loadFile(file.getValue(), file.getKey());
        }
        logger.fine("Checkout to " + descriptorID + " was successfully finished");
    }

    /**
     * This method remove all untracked files from working directory
     *
     * @throws LostFileException                 if file contained one of interesting object was corrupted
     * @throws BrokenFileException               if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException is repository wasn't found
     * @throws IOException                       in case of some IO problems
     * @throws NonexistentFileDeletionException  in case if some error occurred  during cleaning working copy
     */
    public void clean() throws IOException, BrokenFileException, LostFileException, NonexistentFileDeletionException,
            RepositoryNotInitializedException {
        checkStatus();
        List<String> paths = dataManager.workingCopyFiles();
        Stage stage = dataManager.getStage();
        ContentDescriptor headVersion = getActualDescriptor();
        for (String path : paths) {
            if (!stage.getChangedFiles().containsKey(path) && !headVersion.getFiles().containsKey(path)) {
                dataManager.removeFile(path);
            }
        }
        logger.fine("Working directory was successfully cleaned");
    }

    /**
     * Remove repository, but doesn't touch files in working copy
     *
     * @throws RepositoryNotInitializedException if there is nothing to delete
     */
    public void uninstall() throws RepositoryNotInitializedException {
        checkStatus();
        dataManager.uninstallRepository();
    }

    /**
     * Return information about files in the stage area
     *
     * @return Map from File name to Status for every file, added to stage
     * @throws LostFileException                 if file contained one of interesting object was corrupted
     * @throws BrokenFileException               if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException is repository wasn't found
     */
    public Map<String, StageStatus> stageStatus() throws BrokenFileException, LostFileException,
            RepositoryNotInitializedException {
        checkStatus();
        Stage stage = dataManager.getStage();
        HashMap<String, StageStatus> result = new HashMap<>();
        for (String s : stage.getChangedFiles().keySet()) {
            result.put(s, UPDATED);
        }
        for (String s : stage.getRemovedFiles()) {
            result.put(s, REMOVED);
        }
        return result;
    }


    /**
     * Return information about files in the working folder
     *
     * @return Map from File name to Status for every file belonging to folder or known in last commit
     * @throws LostFileException                 if file contained one of interesting object was corrupted
     * @throws BrokenFileException               if file contained one of interesting object was not found
     * @throws RepositoryNotInitializedException is repository wasn't found
     */
    public Map<String, FileStatus> workingCopyStatus() throws BrokenFileException, LostFileException, IOException,
            RepositoryNotInitializedException {
        checkStatus();
        List<String> paths = dataManager.workingCopyFiles();
        Stage stage = dataManager.getStage();
        ContentDescriptor headVersion = getActualDescriptor();
        HashMap<String, FileStatus> result = new HashMap<>();

        ContentDescriptor stagedDescriptor = ContentDescriptor.builder()
                .addAll(headVersion)
                .addAll(stage)
                .build();


        Map<String, String> files = stagedDescriptor.getFiles();
        for (String path : paths) {
            if (!files.containsKey(path)) {
                result.put(path, UNKNOWN);
                continue;
            }
            String hash = dataManager.hashFile(path);
            if (hash.equals(files.get(path))) {
                result.put(path, NOT_CHANGED);
            } else {
                result.put(path, CHANGED);
            }
        }
        for (String path : files.keySet()) {
            if (!result.containsKey(path)) {
                result.put(path, DISAPPEARED);
            }
        }
        return result;
    }

    /**
     * Return name of current branch
     *
     * @return name of current branch
     * @throws RepositoryNotInitializedException is repository wasn't found
     * @throws LostFileException                 if file contained one of interesting object was corrupted
     * @throws BrokenFileException               if file contained one of interesting object was not found
     */
    public String getActiveBranchName() throws RepositoryNotInitializedException,
            BrokenFileException, LostFileException {
        checkStatus();
        return dataManager.getHeader().getCurrentBranchName();
    }

    /**
     * Throws exception if dataManager claimed, that repository wasn't initialized.
     *
     * @throws RepositoryNotInitializedException if repository was not initialized
     */
    private void checkStatus() throws RepositoryNotInitializedException {
        if (!dataManager.isInitialized()) {
            throw new RepositoryNotInitializedException("Repository wasn't initialized or was broken");
        }
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

    public static class UnobservedFileException extends VersionControlSystemException {
        public UnobservedFileException(String message) {
            super(message);
        }
    }

    public enum StageStatus {UPDATED, REMOVED}

    public enum FileStatus {CHANGED, DISAPPEARED, UNKNOWN, NOT_CHANGED}
}
