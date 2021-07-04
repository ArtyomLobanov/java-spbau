package ru.mit.spbau.lobanov;

import javax.swing.*;
import java.awt.*;

/**
 * Main frame of Game
 */
public class GameFrame extends JFrame {

    private GamePanel panel;
    private static final int[] LEVELS = {4, 6, 8, 10};
    private static final String[] LEVELS_NAMES = {"EASY", "NORMAL", "HARD", "VERY HARD"};

    GameFrame() {
        setTitle("Find pair game");
        setSize(400, 300);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        JMenuBar menuBar = new JMenuBar();
        JMenu gameMenu = new JMenu("Game");
        JMenuItem start = new JMenuItem("Start new game");
        start.addActionListener(e -> startGame());
        JMenuItem exit = new JMenuItem("Exit");
        exit.addActionListener(e -> System.exit(0));
        gameMenu.add(start);
        gameMenu.add(exit);
        menuBar.add(gameMenu);
        setJMenuBar(menuBar);
    }

    private void startGame() {
        if (panel != null) {
            remove(panel);
        }
        int response = JOptionPane.showOptionDialog(
                this,"Choose level",
                "Start new Game", JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE, null,
                LEVELS_NAMES,"NORMAL");
        if (response == -1) {
            return;
        }
        int size = LEVELS[response];
        panel = new GamePanel(size, size);
        add(panel, BorderLayout.CENTER);
        GameManager gameManager = new GameManager(size, size, panel);
        gameManager.addGameOverListener(this::showGameOverMessage);
        gameManager.startGame();
        setSize(new Dimension(90 * size, 90 * size + 20));
    }

    private void showGameOverMessage(long totalTime, int pairs) {
        JOptionPane.showMessageDialog(this,
                "You matched " + pairs + " pairs in " + (totalTime / 1000) + " seconds!",
                "Game over!", JOptionPane.INFORMATION_MESSAGE);
    }
}
