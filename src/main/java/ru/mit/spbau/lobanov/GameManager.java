package ru.mit.spbau.lobanov;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class which control game process
 */
public class GameManager {
    private static final int PAIR_FOUND = -1;
    private static final int NOTHING = -2;
    private final int[] field;
    private final GamePanel gamePanel;
    private final List<GameOverListener> listeners;
    private int pairCounter;
    private int firstChosen;
    private long startTime;

    GameManager(int n, int m, GamePanel gamePanel) {
        field = generateField(n, m);
        this.gamePanel = gamePanel;
        this.listeners = new ArrayList<>();
        gamePanel.setLocked(true);
        gamePanel.addGamePanelListener(this::processClick);
        firstChosen = NOTHING;
        pairCounter = n * m / 2;
    }

    /**
     * Run game
     */
    public void startGame() {
        gamePanel.setLocked(false);
        startTime = System.currentTimeMillis();
    }

    public void addGameOverListener(GameOverListener listener) {
        listeners.add(listener);
    }

    private int[] generateField(int n, int m) {
        Random random = new Random();
        int[] array = new int[n * m];
        for (int i = 0; i < n * m; i++) {
            array[i] = i % 2;
        }
        for (int i = 0; i < n * m; i++) {
            int first = random.nextInt(n * m);
            int second = random.nextInt(n * m);
            int tmp = array[first];
            array[first] = array[second];
            array[second] = tmp;
        }
        return array;
    }

    private void processClick(int index, GamePanel source) {
        if (pairCounter == 0) {
            return;
        }
        if (field[index] == PAIR_FOUND) {
            return;
        }
        if (index == firstChosen) {
            return;
        }
        if (firstChosen == NOTHING) {
            firstChosen = index;
            gamePanel.setText(index, "" + field[index]);
            gamePanel.setEnabled(firstChosen, false);
        } else {
            gamePanel.setText(index, "" + field[index]);
            gamePanel.setEnabled(index, false);
            if (field[firstChosen] == field[index]) {
                field[firstChosen] = PAIR_FOUND;
                field[index] = PAIR_FOUND;
                pairCounter--;
                if (pairCounter == 0) {
                    gameOver();
                }
            } else {
                gamePanel.setLocked(true);
                int first = firstChosen;
                invokeLater(() -> {
                    gamePanel.setLocked(false);
                    gamePanel.setText(first, "");
                    gamePanel.setEnabled(first, true);
                    gamePanel.setText(index, "");
                    gamePanel.setEnabled(index, true);
                });
            }
            firstChosen = NOTHING;
        }
    }

    private void invokeLater(Task task) {
        Timer timer = new Timer(1000, e -> task.doTask());
        timer.setRepeats(false);
        timer.start();
    }

    private void gameOver() {
        gamePanel.setLocked(true);
        long totalTime = System.currentTimeMillis() - startTime;
        listeners.forEach(listener -> listener.gameIsOver(totalTime, field.length / 2));
    }

    interface Task {
        void doTask();
    }

    interface GameOverListener {
        void gameIsOver(long time, int pairs);
    }
}
