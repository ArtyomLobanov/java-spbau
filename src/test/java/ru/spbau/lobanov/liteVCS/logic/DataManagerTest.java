package ru.spbau.lobanov.liteVCS.logic;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.spbau.lobanov.liteVCS.primitives.*;

import java.io.File;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class DataManagerTest {
    private static final String workspace = "test_workspace";

    @Before
    public void initRepository() throws Exception {
        DataManager dataManager = new DataManager(workspace);
        dataManager.initRepository();
        assertNotNull(dataManager.getHeader());
        assertNotNull(dataManager.getStage());
        File dir = Paths.get(workspace, ".liteVCS").toFile();
        File[] files = dir.listFiles();
        assertNotNull(files);
        assertEquals(7, files.length);
    }

    @After
    public void clearWorkspace() throws DataManager.RepositoryNotInitializedException {
        DataManager dataManager = new DataManager(workspace);
        dataManager.clearWorkingCopy();
        dataManager.uninstallRepository();
        File[] files = new File(workspace).listFiles();
        assertNotNull(files);
        assertEquals(0, files.length);
    }

    @Test
    public void addAndFetchVersionNode() throws Exception {
        DataManager dataManager = new DataManager(workspace);
        VersionNode versionNode = new VersionNode("id", 2, new String[0]);
        String versionID = dataManager.addVersionNode(versionNode);
        VersionNode copy = dataManager.fetchVersionNode(versionID);
        assertEquals(versionNode.getCommitID(), copy.getCommitID());
        assertEquals(versionNode.getDeepLevel(), copy.getDeepLevel());
    }

    @Test
    public void addAndFetchContentDescriptor() throws Exception {
        DataManager dataManager = new DataManager(workspace);
        ContentDescriptor descriptor = ContentDescriptor
                .builder()
                .addFile("1", "2")
                .addFile("hello", "world")
                .build();
        String descriptorID = dataManager.addContentDescriptor(descriptor);
        ContentDescriptor copy = dataManager.fetchContentDescriptor(descriptorID);
        assertEquals(descriptor.getFiles(), copy.getFiles());
    }

    @Test
    public void addAndFetchCommit() throws Exception {
        DataManager dataManager = new DataManager(workspace);
        Commit commit = new Commit("id", "mes", 19980108, "a");
        String commitID = dataManager.addCommit(commit);
        Commit commit1 = dataManager.fetchCommit(commitID);
        assertEquals(commit.getContentDescriptorID(), commit1.getContentDescriptorID());
        assertEquals(commit.getAuthor(), commit1.getAuthor());
        assertEquals(commit.getCommitMessage(), commit1.getCommitMessage());
    }

    @Test
    public void fetchFile() throws Exception {

    }

    @Test
    public void addFile() throws Exception {

    }

    @Test
    public void addAndFetchBranch() throws Exception {
        DataManager dataManager = new DataManager(workspace);
        Branch branch = new Branch("id", "mes");
        dataManager.addBranch(branch);
        Branch branch1 = dataManager.fetchBranch(branch.getName());
        assertEquals(branch.getName(), branch1.getName());
        assertEquals(branch.getVersionNodeID(), branch1.getVersionNodeID());
    }

    @Test
    public void hasAndRemoveBranch() throws Exception {
        DataManager dataManager = new DataManager(workspace);
        assertFalse(dataManager.hasBranch("branch"));
        dataManager.addBranch(new Branch("d", "branch"));
        assertTrue(dataManager.hasBranch("branch"));
        dataManager.removeBranch("branch");
        assertFalse(dataManager.hasBranch("branch"));
    }

    @Test
    public void putAndGetHeader() throws Exception {
        DataManager dataManager = new DataManager(workspace);
        Header header = new Header("a", "branch");
        dataManager.putHeader(header);
        Header header1 = dataManager.getHeader();
        assertEquals(header.getAuthor(), header1.getAuthor());
        assertEquals(header.getCurrentBranchName(), header1.getCurrentBranchName());
    }

    @Test
    public void putAndGetStage() throws Exception {
        DataManager dataManager = new DataManager(workspace);
        Stage stage = Stage
                .builder()
                .addFile("1", "2")
                .addFile("hello", "world")
                .build();
        dataManager.putStage(stage);
        Stage stage1 = dataManager.getStage();
        assertEquals(stage.getChangedFiles(), stage1.getChangedFiles());
        assertEquals(stage.getRemovedFiles(), stage1.getRemovedFiles());
    }
//
//    @Test
//    public void getFile() throws Exception {
//        DataManager dataManager = new DataManager(workspace);
//        File file = dataManager.getFile(Paths.get(".liteVCS", "stage.lVCS").toString());
//        assertTrue(file.exists());
//        assertEquals("stage.lVCS", file.getName());
//    }

}