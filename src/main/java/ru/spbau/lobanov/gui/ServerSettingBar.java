package ru.spbau.lobanov.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

/**
 * GUI class which show user setting of server like port number etc
 */
public class ServerSettingBar extends JPanel {
    private static final String hostRegexp = ".*";

    private final List<ServerSettingsListener> listeners = new ArrayList<>();
    private final JTextField hostInput = new JTextField();
    private final JTextField portInput = new JTextField();
    private final JButton updateButton = new JButton("Apply");
    private String actualHost;
    private String actualPort;

    ServerSettingBar() {
        setLayout(new GridLayout(1, 5, 20, 0));
        add(new JLabel("Enter host:", SwingConstants.RIGHT));
        add(hostInput);
        add(new JLabel("Enter port:", SwingConstants.RIGHT));
        add(portInput);
        add(updateButton);
        hostInput.addActionListener(hostInputListener);
        portInput.addActionListener(portInputListener);
        updateButton.addActionListener(updateButtonListener);
        updateButton.setEnabled(false);
    }

    /**
     * Add listener
     *
     * @param serverSettingsListener which will be called if settings new applied
     */
    public void addServerSettingsListener(ServerSettingsListener serverSettingsListener) {
        listeners.add(serverSettingsListener);
    }

    /**
     * Parse text to get port number
     *
     * @param text text to be parsed
     * @return port number if it's valid or -1 otherwise
     */
    private int parsePort(String text) {
        try {
            int port = Integer.parseInt(text);
            return (0 <= port && port <= 65535) ? port : -1;
        } catch (NumberFormatException exception) {
            return -1;
        }
    }

    /**
     * Check is updateButton should be enabled
     */
    private void checkSettingStatus() {
        boolean isReady = !hostInput.getText().isEmpty();
        isReady &= parsePort(portInput.getText()) != -1;
        isReady &= !hostInput.getText().equals(actualHost) || !portInput.getText().equals(actualPort);
        updateButton.setEnabled(isReady);
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final ActionListener hostInputListener = e -> {
        String text = hostInput.getText();
        portInput.setBackground(text.isEmpty() ? Color.RED : Color.WHITE);
        checkSettingStatus();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final ActionListener portInputListener = e -> {
        String text = portInput.getText();
        portInput.setBackground(parsePort(text) != -1 ? Color.WHITE : Color.RED);
        checkSettingStatus();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final ActionListener updateButtonListener = e -> {
        String host = hostInput.getText();
        int port = parsePort(portInput.getText());
        if (host.isEmpty() || port == -1) {
            JOptionPane.showMessageDialog(getParent(), "Wrong settings!", "Error", JOptionPane.ERROR_MESSAGE);
        }
        listeners.forEach(listener -> listener.settingsUpdated(host, port));
        actualHost = host;
        actualPort = Integer.toString(port);
        checkSettingStatus();
    };

    /**
     * Listener which will be called if settings new applied
     */
    public interface ServerSettingsListener {
        void settingsUpdated(String newHost, int port);
    }
}
