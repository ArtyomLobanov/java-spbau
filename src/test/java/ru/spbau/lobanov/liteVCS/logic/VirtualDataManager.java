package ru.spbau.lobanov.liteVCS.logic;

import org.jetbrains.annotations.NotNull;
import ru.spbau.lobanov.liteVCS.primitives.*;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class VirtualDataManager extends DataManager {

    private static final String ROOT_VERSION_NODE_ID = "root";

    boolean isInitialized;
    HashMap<String, Branch> branches = new HashMap<>();
    HashMap<String, Commit> commits = new HashMap<>();
    HashMap<String, ContentDescriptor> descriptors = new HashMap<>();
    HashMap<String, VersionNode> versions = new HashMap<>();
    HashMap<String, VirtualFile> files = new HashMap<>();
    Header header;
    ContentDescriptor stage;
    HashMap<String, File> workingCopy = new HashMap<>();


    VirtualDataManager() {
        super("");
    }

    private static String createRandomKey(Set<String> used) {
        String key;
        do {
            key = "khfd" + Math.random() + "efd";
        } while (used.contains(key));
        return key;
    }

    public static String hash(File file) {
        return "fl" + file.hashCode();
    }

    void initRepository() throws RecreatingRepositoryException {
        if (isInitialized) {
            throw new RecreatingRepositoryException("");
        }
        isInitialized = true;
        try {
            String initialDescriptorID = addContentDescriptor(ContentDescriptor.EMPTY);
            String initialCommitID = addCommit(new Commit(initialDescriptorID, "Initial commit",
                    System.currentTimeMillis(), "lVCS"));
            VersionNode start = Algorithms.createRootNode(ROOT_VERSION_NODE_ID, initialCommitID);
            versions.put(ROOT_VERSION_NODE_ID, start);
            Branch master = new Branch(ROOT_VERSION_NODE_ID, "master");
            addBranch(master);
            Header header = new Header("Unknown", master.getName());
            putHeader(header);
            putStage(ContentDescriptor.EMPTY);
        } catch (RepositoryNotInitializedException e) {
            throw new Error("");
        }
    }

    @NotNull
    VersionNode fetchVersionNode(@NotNull String id) throws LostFileException, BrokenFileException {
        return versions.get(id);
    }

    @NotNull
    String addVersionNode(@NotNull VersionNode versionNode) throws RepositoryNotInitializedException {
        String id = createRandomKey(versions.keySet());
        versions.put(id, versionNode);
        return id;
    }

    @NotNull
    ContentDescriptor fetchContentDescriptor(@NotNull String id) throws LostFileException, BrokenFileException {
        return descriptors.get(id);
    }

    @NotNull
    String addContentDescriptor(@NotNull ContentDescriptor contentDescriptor) throws RepositoryNotInitializedException {
        String id = createRandomKey(descriptors.keySet());
        descriptors.put(id, contentDescriptor);
        return id;
    }

    @NotNull
    Commit fetchCommit(@NotNull String id) throws LostFileException, BrokenFileException {
        return commits.get(id);
    }

    @NotNull
    String addCommit(@NotNull Commit commit) throws RepositoryNotInitializedException {
        String id = createRandomKey(commits.keySet());
        commits.put(id, commit);
        return id;
    }

    @NotNull
    File fetchFile(@NotNull String id) throws LostFileException {
        return files.get(id);
    }

    @NotNull
    String addFile(@NotNull File file) throws RepositoryNotInitializedException {
        String id = "fl" + file.hashCode();
        files.put(id, (VirtualFile) file);
        return id;
    }

    void addBranch(@NotNull Branch branch) throws RepositoryNotInitializedException {
        branches.put(branch.getName(), branch);
    }

    @NotNull
    Branch fetchBranch(@NotNull String name) throws LostFileException, BrokenFileException {
        return branches.get(name);
    }

    boolean hasBranch(@NotNull String name) {
        return branches.containsKey(name);
    }

    void removeBranch(@NotNull String name) throws LostFileException {
        branches.remove(name);
    }
    @NotNull
    Header getHeader() throws LostFileException, BrokenFileException {
        return header;
    }

    void putHeader(@NotNull Header header) throws RepositoryNotInitializedException {
        this.header = header;
    }

    @NotNull
    ContentDescriptor getStage() throws LostFileException, BrokenFileException {
        return stage;
    }

    void putStage(@NotNull ContentDescriptor stage) throws RepositoryNotInitializedException {
        this.stage = stage;
    }

    void loadFiles(@NotNull String descriptorID) throws BrokenFileException, LostFileException, IOException {
        ContentDescriptor descriptor = fetchContentDescriptor(descriptorID);
        for (Map.Entry<String, String> pair : descriptor.getFiles().entrySet()) {
            workingCopy.put(pair.getKey(), fetchFile(pair.getValue()));
        }
    }

    void clearWorkingCopy() {
        workingCopy.clear();
    }

    /**
     * Method allow to clear work space be removing all files including ROOT_DIRECTORY
     *
     * @throws RepositoryNotInitializedException if there was no repository
     */
    void uninstallRepository() throws RepositoryNotInitializedException {
        workingCopy.clear();
        branches.clear();
        files.clear();
        versions.clear();
        commits.clear();
        descriptors.clear();
        header = null;
        stage = null;
        isInitialized = false;
    }

    @NotNull
    File getFile(@NotNull String filename) {
        return workingCopy.get(filename);
    }

    void writeFile(String filename, String value) {
        VirtualFile f = new VirtualFile(value);
        workingCopy.put(filename, f);
    }

    String hash(String filename) {
        return "" + workingCopy.get(filename).hashCode();
    }
}
