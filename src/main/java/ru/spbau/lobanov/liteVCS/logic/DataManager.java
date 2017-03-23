package ru.spbau.lobanov.liteVCS.logic;

import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import org.jetbrains.annotations.NotNull;
import ru.spbau.lobanov.liteVCS.primitives.*;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.Random;

/**
 * This class is adapter between logic part of LiteVCS
 * and data storage
 */
class DataManager {

    private static final String ROOT_DIRECTORY_NAME = ".liteVCS";
    private static final String PATH_TO_VERSIONS_FILES = ROOT_DIRECTORY_NAME + "\\versions";
    private static final String PATH_TO_CONTENT_DESCRIPTORS_FILES = ROOT_DIRECTORY_NAME + "\\descriptors";
    private static final String PATH_TO_COMMITS_FILES = ROOT_DIRECTORY_NAME + "\\commits";
    private static final String PATH_TO_SAVED_FILES = ROOT_DIRECTORY_NAME + "\\files";
    private static final String PATH_TO_BRANCHES = ROOT_DIRECTORY_NAME + "\\branches";
    private static final String ROOT_VERSION_NODE_ID = "root";
    private static final String PATH_TO_STAGE = ROOT_DIRECTORY_NAME + "\\stage.lVCS";
    private static final String PATH_TO_HEADER = ROOT_DIRECTORY_NAME + "\\header.lVCS";

    private final String workingDirectory;

    DataManager(@NotNull String workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    /**
     * Method which init repository: create folders and initial files
     *
     * @throws RecreatingRepositoryException if repository was already created
     */
    void initRepository() throws RecreatingRepositoryException {
        File rootDirectory = Paths.get(workingDirectory, DataManager.ROOT_DIRECTORY_NAME).toFile();
        if (rootDirectory.exists()) {
            throw new RecreatingRepositoryException("Repository was already created here:" + workingDirectory);
        }
        boolean success = Paths.get(workingDirectory, DataManager.PATH_TO_COMMITS_FILES).toFile().mkdirs() &&
                Paths.get(workingDirectory, DataManager.PATH_TO_CONTENT_DESCRIPTORS_FILES).toFile().mkdirs() &&
                Paths.get(workingDirectory, DataManager.PATH_TO_SAVED_FILES).toFile().mkdirs() &&
                Paths.get(workingDirectory, DataManager.PATH_TO_VERSIONS_FILES).toFile().mkdirs() &&
                Paths.get(workingDirectory, DataManager.PATH_TO_BRANCHES).toFile().mkdirs();
        if (!success) {
            throw new Error("Unexpected error during directories creating");
        }
        try {
            String initialDescriptorID = addContentDescriptor(ContentDescriptor.EMPTY);
            String initialCommitID = addCommit(new Commit(initialDescriptorID, "Initial commit",
                    System.currentTimeMillis(), "lVCS"));
            VersionNode start = Algorithms.createRootNode(DataManager.ROOT_VERSION_NODE_ID, initialCommitID);
            writeObject(Paths.get(workingDirectory, PATH_TO_VERSIONS_FILES, ROOT_VERSION_NODE_ID), start);
            Branch master = new Branch(DataManager.ROOT_VERSION_NODE_ID, "master");
            addBranch(master);
            Header header = new Header("Unknown", master.getName());
            putHeader(header);
            putStage(ContentDescriptor.EMPTY);
        } catch (RepositoryNotInitializedException e) {
            throw new Error("Unexpected error during repository initialization");
        }
    }

    /**
     * Method allow to find and load VersionNode by id
     *
     * @param id identifier of VersionNode
     * @return loaded VersionNode
     * @throws LostFileException   if file contained one of interesting object was corrupted
     * @throws BrokenFileException if file contained one of interesting object was not found
     */
    @NotNull
    VersionNode getVersionNode(@NotNull String id) throws LostFileException, BrokenFileException {
        return readObject(Paths.get(workingDirectory, PATH_TO_VERSIONS_FILES, id), VersionNode.class);
    }

    /**
     * Method allow to save VersionNode in file system
     *
     * @param versionNode object to save
     * @return generated id, by which you can get that object late
     * @throws RepositoryNotInitializedException if file creating failed
     */
    @NotNull
    String addVersionNode(@NotNull VersionNode versionNode) throws RepositoryNotInitializedException {
        String id = createUniqueID(PATH_TO_VERSIONS_FILES);
        writeObject(Paths.get(workingDirectory, PATH_TO_VERSIONS_FILES, id), versionNode);
        return id;
    }

    /**
     * Method allow to find and load ContentDescriptor by id
     *
     * @param id identifier of ContentDescriptor
     * @return loaded ContentDescriptor
     * @throws LostFileException   if file contained one of interesting object was corrupted
     * @throws BrokenFileException if file contained one of interesting object was not found
     */
    @NotNull
    ContentDescriptor getContentDescriptor(@NotNull String id) throws LostFileException, BrokenFileException {
        return readObject(Paths.get(workingDirectory, PATH_TO_CONTENT_DESCRIPTORS_FILES, id), ContentDescriptor.class);
    }

    /**
     * Method allow to save ContentDescriptor in file system
     *
     * @param contentDescriptor object to save
     * @return generated id, by which you can get that object late
     * @throws RepositoryNotInitializedException if file creating failed
     */
    @NotNull
    String addContentDescriptor(@NotNull ContentDescriptor contentDescriptor) throws RepositoryNotInitializedException {
        String id = createUniqueID(PATH_TO_CONTENT_DESCRIPTORS_FILES);
        writeObject(Paths.get(workingDirectory, PATH_TO_CONTENT_DESCRIPTORS_FILES, id), contentDescriptor);
        return id;
    }

    /**
     * Method allow to find and load Commit by id
     *
     * @param id identifier of Commit
     * @return loaded Commit
     * @throws LostFileException   if file contained one of interesting object was corrupted
     * @throws BrokenFileException if file contained one of interesting object was not found
     */
    @NotNull
    Commit getCommit(@NotNull String id) throws LostFileException, BrokenFileException {
        return readObject(Paths.get(workingDirectory, PATH_TO_COMMITS_FILES, id), Commit.class);
    }

    /**
     * Method allow to save Commit in file system
     *
     * @param commit object to save
     * @return generated id, by which you can get that object late
     * @throws RepositoryNotInitializedException if file creating failed
     */
    @NotNull
    String addCommit(@NotNull Commit commit) throws RepositoryNotInitializedException {
        String id = createUniqueID(PATH_TO_COMMITS_FILES);
        writeObject(Paths.get(workingDirectory, PATH_TO_COMMITS_FILES, id), commit);
        return id;
    }

    /**
     * Method allow to find copy of files by id
     *
     * @param id identifier of interesting file
     * @return link to saved copy
     * @throws LostFileException if file contained one of interesting object was corrupted
     */
    @NotNull
    File getFile(@NotNull String id) throws LostFileException {
        File file = Paths.get(workingDirectory, PATH_TO_SAVED_FILES, id).toFile();
        if (!file.exists()) {
            throw new LostFileException("File wasn't found:" + file.getName(), null, file);
        }
        return file;
    }

    /**
     * Method allow to save copy of file in file system
     *
     * @param file object to save
     * @return generated id, by which you can get that object late
     * @throws RepositoryNotInitializedException if file creating failed
     */
    @NotNull
    String addFile(@NotNull File file) throws RepositoryNotInitializedException {
        String hash;
        try {
            hash = Files.hash(file, Hashing.sha256()).toString() + ".sc";
        } catch (IOException e) {
            throw new Error("Unknown exception during hash creating");
        }
        File savedCopy = Paths.get(workingDirectory, PATH_TO_SAVED_FILES, hash).toFile();
        try {
            if (savedCopy.createNewFile()) {
                Files.copy(file, savedCopy);
            }
        } catch (IOException e) {
            throw new RepositoryNotInitializedException("Directory wasn't found:" + PATH_TO_SAVED_FILES, e);
        }
        return hash;
    }

    /**
     * Method allow to save branch in file system
     * This method doesn't return id because branches are identified by name
     *
     * @param branch object to save
     * @throws RepositoryNotInitializedException if file creating failed
     */
    @NotNull
    void addBranch(@NotNull Branch branch) throws RepositoryNotInitializedException {
        writeObject(Paths.get(workingDirectory, PATH_TO_BRANCHES, branch.getName()), branch);
    }

    /**
     * Method allow to find and load Branch by name
     *
     * @param name name of interesting Branch
     * @return loaded Branch
     * @throws LostFileException   if file contained one of interesting object was corrupted
     * @throws BrokenFileException if file contained one of interesting object was not found
     */
    @NotNull
    Branch getBranch(@NotNull String name) throws LostFileException, BrokenFileException {
        return readObject(Paths.get(workingDirectory, PATH_TO_BRANCHES, name), Branch.class);
    }

    /**
     * Method allow to check if Branch with such name exist
     *
     * @param name name of interesting Branch
     * @return true if such branch exist, false otherwise
     */
    boolean hasBranch(@NotNull String name) {
        return Paths.get(workingDirectory, PATH_TO_BRANCHES, name).toFile().exists();
    }

    /**
     * Method allow to check if Branch with such name exist
     *
     * @param name name of interesting Branch
     * @throws LostFileException if such branch wasn't found
     */
    void removeBranch(@NotNull String name) throws LostFileException {
        File file = Paths.get(workingDirectory, PATH_TO_BRANCHES, name).toFile();
        if (!file.delete()) {
            throw new LostFileException("File wasn't found", null, file);
        }
    }

    /**
     * Method allow to load Header of repository
     *
     * @return loaded Header
     * @throws LostFileException   if file contained one of interesting object was corrupted
     * @throws BrokenFileException if file contained one of interesting object was not found
     */
    @NotNull
    Header getHeader() throws LostFileException, BrokenFileException {
        return readObject(Paths.get(workingDirectory, PATH_TO_HEADER), Header.class);
    }

    /**
     * Method allow to save Header of repository
     *
     * @param header object to save
     * @throws RepositoryNotInitializedException if file saving failed
     */
    void putHeader(@NotNull Header header) throws RepositoryNotInitializedException {
        writeObject(Paths.get(workingDirectory, PATH_TO_HEADER), header);
    }

    /**
     * Method allow to load Stage descriptor
     *
     * @return stage ContentDescriptor
     * @throws LostFileException   if file contained one of interesting object was corrupted
     * @throws BrokenFileException if file contained one of interesting object was not found
     */
    @NotNull
    ContentDescriptor getStage() throws LostFileException, BrokenFileException {
        return readObject(Paths.get(workingDirectory, PATH_TO_STAGE), ContentDescriptor.class);
    }

    /**
     * Method allow to save Stage descriptor
     *
     * @param stage object to save
     * @throws RepositoryNotInitializedException if file saving failed
     */
    void putStage(@NotNull ContentDescriptor stage) throws RepositoryNotInitializedException {
        writeObject(Paths.get(workingDirectory, PATH_TO_STAGE), stage);
    }

    /**
     * Method allow to create all files and folder in which
     * they are contained, specified by ContentDescriptor
     *
     * @param descriptorID identifier of ContentDescriptor
     * @throws LostFileException   if file contained one of interesting object was corrupted
     * @throws BrokenFileException if file contained one of interesting object was not found
     * @throws IOException         if file creating failed because of File System
     */
    void loadFiles(@NotNull String descriptorID) throws BrokenFileException, LostFileException, IOException {
        ContentDescriptor descriptor = getContentDescriptor(descriptorID);
        for (Entry<String, String> pair : descriptor.getFiles().entrySet()) {
            File savedCopy = getFile(pair.getValue());
            File targetFile = Paths.get(workingDirectory, pair.getKey()).toFile();
            Files.createParentDirs(targetFile);
            Files.touch(targetFile);
            Files.copy(savedCopy, targetFile);
        }
    }

    /**
     * Method allow to clear work space be removing all files except ROOT_DIRECTORY
     */
    void clearWorkingCopy() {
        Path rootDirectory = Paths.get(workingDirectory, ROOT_DIRECTORY_NAME);
        File[] files = Paths.get(workingDirectory).toFile().listFiles();
        if (files == null) {
            throw new Error("Cant clear Directory:");
        }
        for (File file : files) {
            if (!file.toPath().startsWith(rootDirectory)) {
                if (file.isDirectory()) {
                    clearDirectory(file);
                }
                if (!file.delete()) {
                    throw new Error("Cant remove file: " + file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * Method allow to clear work space be removing all files including ROOT_DIRECTORY
     *
     * @throws RepositoryNotInitializedException if there was no repository
     */
    void uninstallRepository() throws RepositoryNotInitializedException {
        File rootDirectory = Paths.get(workingDirectory, ROOT_DIRECTORY_NAME).toFile();
        if (!rootDirectory.exists()) {
            throw new RepositoryNotInitializedException("Repository wasn't found");
        }
        clearDirectory(rootDirectory);
        if (!rootDirectory.delete()) {
            throw new Error();
        }

    }

    /**
     * Method remove all files from directory
     *
     * @param directory directory to remove
     */
    private void clearDirectory(@NotNull File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    clearDirectory(file);
                }
                if (!file.delete()) {
                    throw new Error("Cant remove file");
                }
            }
        }
    }

    /**
     * Method allow you to create such names,
     * that files with that names doesn't exist in interesting directory
     *
     * @param directory tracked folder
     * @return such name, that there is no file with that name in that folder
     */
    @NotNull
    private String createUniqueID(@NotNull String directory) {
        Random random = new Random();
        String id;
        do {
            id = "" + random.nextLong();
        } while (Paths.get(workingDirectory, directory, id).toFile().exists());
        return id;
    }

    /**
     * Method allow you to load every Serializable object
     * from specified files
     *
     * @param path file, included important information
     * @return loaded object
     */
    @NotNull
    private static <T> T readObject(@NotNull Path path, @NotNull Class<T> expectedType) throws BrokenFileException,
            LostFileException {
        Object o;
        try (FileInputStream fileInputStream = new FileInputStream(path.toFile());
             ObjectInputStream inputStream = new ObjectInputStream(fileInputStream)) {
            o = inputStream.readObject();
        } catch (ClassNotFoundException | InvalidClassException | OptionalDataException e) {
            throw new BrokenFileException("Cant load data from file: " + path.toString(), e, path.toFile());
        } catch (FileNotFoundException e) {
            throw new LostFileException("Found reference to non-existent file: " + path.toString(), e, path.toFile());
        } catch (IOException e) {
            throw new Error("Unknown error occurred while reading the file: " + path.toString(), e);
        }
        if (!expectedType.isInstance(o)) {
            throw new BrokenFileException("Unexpected data was found in file: " + path.toString(), path.toFile());
        }
        return expectedType.cast(o);
    }

    /**
     * Method allow you to save every Serializable object
     * in specified files
     *
     * @param path   path to target file
     * @param object object to save
     */
    private static void writeObject(@NotNull Path path, @NotNull Object object)
            throws RepositoryNotInitializedException {
        try (FileOutputStream fileOutputStream = new FileOutputStream(path.toFile());
             ObjectOutputStream outputStream = new ObjectOutputStream(fileOutputStream)) {
            outputStream.writeObject(object);
        } catch (FileNotFoundException e) {
            throw new RepositoryNotInitializedException("Repository wasn't initialized or was corrupted");
        } catch (IOException e) {
            throw new Error("Unknown error occurred while writing the file: " + path.toString(), e);
        }
    }

    public static class LostFileException extends VersionControlSystemException {
        private final File expectedFile;

        LostFileException(String message, Throwable cause, File expectedFile) {
            super(message, cause);
            this.expectedFile = expectedFile;
        }

        public File getExpectedFile() {
            return expectedFile;
        }
    }

    public static class BrokenFileException extends VersionControlSystemException {
        private final File brokenFile;

        BrokenFileException(String message, Throwable cause, File brokenFile) {
            super(message, cause);
            this.brokenFile = brokenFile;
        }

        BrokenFileException(String message, File brokenFile) {
            super(message);
            this.brokenFile = brokenFile;
        }

        public File getBrokenFile() {
            return brokenFile;
        }
    }


    public static class RepositoryNotInitializedException extends VersionControlSystemException {
        RepositoryNotInitializedException(String message) {
            super(message);
        }

        RepositoryNotInitializedException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class RecreatingRepositoryException extends VersionControlSystemException {
        RecreatingRepositoryException(String message) {
            super(message);
        }
    }
}
