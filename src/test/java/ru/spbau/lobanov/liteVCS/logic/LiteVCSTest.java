package ru.spbau.lobanov.liteVCS.logic;

import org.junit.Test;
import ru.spbau.lobanov.liteVCS.primitives.Branch;
import ru.spbau.lobanov.liteVCS.primitives.Commit;
import ru.spbau.lobanov.liteVCS.primitives.ContentDescriptor;
import ru.spbau.lobanov.liteVCS.primitives.Header;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        ContentDescriptor stage = dataManager.getStage();
        String id = stage.getFiles().get("a.txt");
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
    public void reset() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);

        liteVCS.init();

        dataManager.writeFile("a.txt", "data");
        liteVCS.add("a.txt");
        liteVCS.commit("message");

        dataManager.writeFile("a.txt", "data2");
        liteVCS.add("a.txt");
        dataManager.writeFile("b.txt", "data");

        liteVCS.reset();

        assertEquals(1, dataManager.workingCopy.size());
        assertEquals("data".hashCode() + "", dataManager.hash("a.txt"));
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
        assertEquals("data".hashCode() + "", dataManager.hash("a.txt"));
    }

}