package ru.spbau.lobanov.liteVCS.logic;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LiteVCSGeneralTests {

    @Test
    public void addBranchSwitchChangeSwitchMerge() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);
        liteVCS.init();
        dataManager.writeFile("a.txt", "versiona1");
        String initialState = dataManager.hash("a.txt");
        liteVCS.add("a.txt");
        liteVCS.commit("commit #1");
        liteVCS.createBranch("br");
        liteVCS.switchBranch("br");
        dataManager.writeFile("a.txt", "versiona2");
        String lastState = dataManager.hash("a.txt");
        liteVCS.add("a.txt");
        liteVCS.commit("commit2");
        liteVCS.switchBranch("master");
        assertEquals(initialState, dataManager.hash("a.txt"));
        liteVCS.mergeBranch("br", "mesage");
        liteVCS.reset("a.txt");
        assertEquals(lastState, dataManager.hash("a.txt"));
    }

    @Test
    public void addBranchChangeSwitchChangeSwitchMerge() throws Exception {
        VirtualDataManager dataManager = new VirtualDataManager();
        LiteVCS liteVCS = new LiteVCS(dataManager);
        liteVCS.init();

        dataManager.writeFile("a.txt", "versiona1");

        dataManager.writeFile("b.txt", "versionb1");
        String initialStateB = dataManager.hash("b.txt");

        liteVCS.add("a.txt");
        liteVCS.add("b.txt");
        liteVCS.commit("commit #1");
        liteVCS.createBranch("br");


        dataManager.writeFile("a.txt", "versiona2");
        String resultStateA = dataManager.hash("a.txt");
        liteVCS.add("a.txt");
        liteVCS.commit("commit #2");

        liteVCS.switchBranch("br");

        dataManager.writeFile("b.txt", "versionb2");
        String resultStateB = dataManager.hash("b.txt");
        liteVCS.add("b.txt");
        liteVCS.commit("commit3");

        liteVCS.switchBranch("master");
        assertEquals(resultStateA, dataManager.hash("a.txt"));
        assertEquals(initialStateB, dataManager.hash("b.txt"));

        liteVCS.mergeBranch("br", "mesage");
        liteVCS.reset("a.txt");
        liteVCS.reset("b.txt");


        assertEquals(resultStateA, dataManager.hash("a.txt"));
        assertEquals(resultStateB, dataManager.hash("b.txt"));
    }
}