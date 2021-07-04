package ru.mit.spbau.lobanov;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * GUI class, which show table of buttons
 */
public class GamePanel extends JPanel {
    private final JButton[][] buttons;
    private final List<GamePanelListener> listeners;
    private final int n;
    private final int m;
    private boolean isLocked;

    GamePanel(int n, int m) {
        listeners = new ArrayList<>();
        this.n = n;
        this.m = m;
        setLayout(new GridLayout(n, m));
        buttons = new JButton[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                buttons[i][j] = new JButton();
                final int index = i * m + j;
                buttons[i][j].addActionListener(e -> buttonClicked(index));
                add(buttons[i][j]);
            }
        }
    }

    /**
     * Set test on button, which have number index
     * @param index number of button
     * @param text text to be shown
     */
    public void setText(int index, String text) {
        buttons[index / m][index % m].setText(text);
    }

    /**
     * Set status of button, which have number index
     * @param index number of button
     * @param isEnabled status
     */
    public void setEnabled(int index, boolean isEnabled) {
        buttons[index / m][index % m].setEnabled(isEnabled);
    }

    /**
     * If GamePanel is locked, listeners will not achieve
     * information about clicks
     *
     * @param isLocked gamePanel status
     */
    public void setLocked(boolean isLocked) {
        this.isLocked = isLocked;
    }

    private void buttonClicked(int index) {
        if (!isLocked) {
            listeners.forEach(listener -> listener.cellClicked(index, this));
        }

    }

    public void addGamePanelListener(GamePanelListener listener) {
        listeners.add(listener);
    }

    interface GamePanelListener {
        void cellClicked(int index, GamePanel source);
    }
}
