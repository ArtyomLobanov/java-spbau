package ru.spbau.lobanov.liteVCS.logic;

import org.junit.Test;
import ru.spbau.lobanov.liteVCS.logic.LiteVCS.FileStatus;
import ru.spbau.lobanov.liteVCS.logic.LiteVCS.StageStatus;
import ru.spbau.lobanov.liteVCS.primitives.*;

import java.util.Map;

import static org.junit.Assert.*;
import static ru.spbau.lobanov.liteVCS.logic.LiteVCS.FileStatus.*;
import static ru.spbau.lobanov.liteVCS.logic.LiteVCS.StageStatus.REMOVED;
import static ru.spbau.lobanov.liteVCS.logic.LiteVCS.StageStatus.UPDATED;

public class LiteVCSTest {
    @Test
    public void hello() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);

        liteVCS.init();
        liteVCS.hello("author");
        Header header = dataManager.getHeader();
        assertEquals("author", header.getAuthor());
    }

    @Test
    public void add() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);

        liteVCS.init();
        dataManager.writeFile("a.txt", "data");
        liteVCS.add("a.txt");
        Stage stage = dataManager.getStage();
        String id = stage.getChangedFiles().get("a.txt");
        assertNotNull(id);
        VirtualFile f = (VirtualFile) dataManager.fetchFile(id);
        assertEquals("data", f.getValue());
    }

    @Test
    public void commit() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);

        liteVCS.init();
        dataManager.writeFile("a.txt", "data");
        liteVCS.add("a.txt");
        liteVCS.commit("message");
        assertEquals(2, dataManager.commits.size());
    }

    @Test
    public void logs() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);

        liteVCS.init();
        dataManager.writeFile("a.txt", "data");
        liteVCS.add("a.txt");
        liteVCS.commit("message");
        dataManager.writeFile("b.txt", "data2");
        liteVCS.add("b.txt");
        liteVCS.commit("message2");

        Commit commit1 = liteVCS.history("2").get(1);
        Commit commit2 = liteVCS.history("2").get(0);

        assertEquals("message", commit1.getCommitMessage());
        assertEquals("message2", commit2.getCommitMessage());
    }

    @Test
    public void createBranch() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);

        liteVCS.init();
        dataManager.writeFile("a.txt", "data");
        liteVCS.add("a.txt");
        liteVCS.commit("message");

        liteVCS.createBranch("branch");
        assertEquals(2, dataManager.branches.size());
        Branch master = dataManager.branches.get("master");
        Branch branch = dataManager.branches.get("branch");
        assertEquals(master.getVersionNodeID(), branch.getVersionNodeID());
    }

    @Test
    public void removeBranch() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);

        liteVCS.init();
        dataManager.writeFile("a.txt", "data");
        liteVCS.add("a.txt");
        liteVCS.commit("message");

        liteVCS.createBranch("branch");
        assertEquals(2, dataManager.branches.size());
        liteVCS.removeBranch("branch");
        assertEquals(1, dataManager.branches.size());
    }

    @Test
    public void mergeBranch() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);

        liteVCS.init();

        liteVCS.createBranch("branch");

        dataManager.writeFile("a.txt", "data");
        liteVCS.add("a.txt");
        liteVCS.commit("message");

        liteVCS.switchBranch("branch");
        dataManager.writeFile("b.txt", "data2");
        liteVCS.add("b.txt");
        liteVCS.commit("message2");

        liteVCS.mergeBranch("master", "message3");

        assertEquals(2, dataManager.branches.size());
        String versionNodeID = dataManager.branches.get("branch").getVersionNodeID();
        String commitID = dataManager.versions.get(versionNodeID).getCommitID();
        String descriptorID = dataManager.commits.get(commitID).getContentDescriptorID();
        ContentDescriptor descriptor = dataManager.descriptors.get(descriptorID);
        assertEquals("data".hashCode(), dataManager.fetchFile(descriptor.getFiles().get("a.txt")).hashCode());
        assertEquals("data2".hashCode(), dataManager.fetchFile(descriptor.getFiles().get("b.txt")).hashCode());
    }

    @Test
    public void switchBranch() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);

        liteVCS.init();

        liteVCS.createBranch("branch");
        liteVCS.switchBranch("branch");

        dataManager.writeFile("a.txt", "data");
        liteVCS.add("a.txt");
        liteVCS.commit("message");


        assertEquals(1, dataManager.workingCopy.size());
        liteVCS.switchBranch("master");
        assertEquals(0, dataManager.workingCopy.size());
    }

    @Test
    public void checkout() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);

        liteVCS.init();

        dataManager.writeFile("a.txt", "data");
        liteVCS.add("a.txt");
        liteVCS.commit("message");

        String versionNodeID = dataManager.branches.get("master").getVersionNodeID();
        String commitID = dataManager.versions.get(versionNodeID).getCommitID();
        String descriptorID = dataManager.commits.get(commitID).getContentDescriptorID();

        dataManager.writeFile("a.txt", "data2");
        liteVCS.add("a.txt");
        dataManager.writeFile("b.txt", "data");

        liteVCS.checkout(descriptorID);
        assertEquals(1, dataManager.workingCopy.size());
        assertEquals("data".hashCode() + "", dataManager.hashFile("a.txt"));
    }

    @Test
    public void clean() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);

        liteVCS.init();

        dataManager.writeFile("a.txt", "data");
        liteVCS.add("a.txt");
        liteVCS.commit("message");

        dataManager.writeFile("b.txt", "data2");
        liteVCS.add("b.txt");
        dataManager.writeFile("b.txt", "data3");

        dataManager.writeFile("c.txt", "data3");

        liteVCS.clean();
        assertEquals(2, dataManager.workingCopy.size());
        assertTrue(dataManager.workingCopy.containsKey("a.txt"));
        assertTrue(dataManager.workingCopy.containsKey("b.txt"));
        assertEquals("data3".hashCode(), dataManager.workingCopy.get("b.txt").hashCode());
    }

    @Test
    public void remove() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);

        liteVCS.init();

        dataManager.writeFile("a.txt", "data");
        liteVCS.add("a.txt");
        liteVCS.commit("message");

        liteVCS.remove("a.txt");
        assertFalse(dataManager.workingCopy.containsKey("a.txt"));
        liteVCS.reset("a.txt");
        assertTrue(dataManager.workingCopy.containsKey("a.txt"));

        liteVCS.remove("a.txt");
        liteVCS.commit("1");
        assertFalse(dataManager.workingCopy.containsKey("a.txt"));
        String descriptionID = liteVCS.history("1").get(0).getContentDescriptorID();
        liteVCS.checkout(descriptionID);
        assertFalse(dataManager.workingCopy.containsKey("a.txt"));
    }

    @Test
    public void reset() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);

        liteVCS.init();

        dataManager.writeFile("a.txt", "data");
        liteVCS.add("a.txt");
        liteVCS.commit("message");

        dataManager.writeFile("a.txt", "data2");
        liteVCS.add("a.txt");

        liteVCS.reset("a.txt");
        assertEquals("data".hashCode(), dataManager.workingCopy.get("a.txt").hashCode());
        assertFalse(dataManager.getStage().getChangedFiles().containsKey("a.txt"));
    }

    @Test
    public void workingCopyStatus() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);
        liteVCS.init();

        dataManager.writeFile("a.txt", "data");
        Map<String, FileStatus> result = liteVCS.workingCopyStatus();
        assertEquals(UNKNOWN, result.get("a.txt"));

        liteVCS.add("a.txt");
        result = liteVCS.workingCopyStatus();
        assertEquals(NOT_CHANGED, result.get("a.txt"));

        dataManager.writeFile("a.txt", "data2");
        result = liteVCS.workingCopyStatus();
        assertEquals(CHANGED, result.get("a.txt"));

        dataManager.removeFile("a.txt");
        result = liteVCS.workingCopyStatus();
        assertEquals(DISAPPEARED, result.get("a.txt"));
    }

    @Test
    public void stageStatus() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);
        liteVCS.init();

        dataManager.writeFile("a.txt", "data");
        liteVCS.add("a.txt");
        Map<String, StageStatus> result = liteVCS.stageStatus();

        liteVCS.remove("a.txt");
        result = liteVCS.stageStatus();
        assertEquals(REMOVED, result.get("a.txt"));
    }
}