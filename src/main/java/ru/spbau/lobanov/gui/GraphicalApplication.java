package ru.spbau.lobanov.gui;


import com.sun.istack.internal.Nullable;
import org.jetbrains.annotations.NotNull;
import ru.spbau.lobanov.client.Client;
import ru.spbau.lobanov.client.FileDescriptor;
import ru.spbau.lobanov.gui.FileProvider.FileClickListener;
import ru.spbau.lobanov.gui.ServerSettingBar.ServerSettingsListener;

import javax.swing.*;
import java.awt.*;
import java.nio.file.Path;
import java.nio.file.Paths;

import static javax.swing.JFileChooser.APPROVE_OPTION;

public class GraphicalApplication extends JFrame {

    private static final FileDescriptor BACK = new FileDescriptor("..", "", true);
    private static final Path EMPTY_PATH = Paths.get("");
    private final FileProvider fileProvider;
    private Path serverPath = EMPTY_PATH;
    private Client client;

    GraphicalApplication() {
        client = new Client(System.out);

        fileProvider = new FileProvider(new FileDescriptor[0]);
        fileProvider.addFileClickListener(providerListener);
        add(fileProvider, BorderLayout.CENTER);

        ServerSettingBar settingBar = new ServerSettingBar();
        settingBar.addServerSettingsListener(settingBarListener);
        add(settingBar, BorderLayout.NORTH);

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(600, 300));
    }

    /**
     * Reload files and update FileProvider
     */
    private void refresh() {
        new Thread(() -> {
            FileDescriptor[] requestedFiles = null;
            try {
                String path = serverPath.toString();
                requestedFiles = client.listFiles(path.isEmpty() ? "." : path);
            } catch (Client.ClientException e) {
                showErrorMessage(e.getMessage());
            }
            FileDescriptor[] result = prepareResult(requestedFiles);
            SwingUtilities.invokeLater(() -> fileProvider.update(result));
        }).start();
    }

    /**
     * Process array of FileDescriptors returned by Client.
     * Add BACK-folder, if it's necessary
     *
     * @param descriptors array returned by Client
     * @return array of FileDescriptors which is ready to be given to FileProvider
     */
    @NotNull
    private FileDescriptor[] prepareResult(@Nullable FileDescriptor[] descriptors) {
        if (descriptors == null) {
            return new FileDescriptor[0];
        }
        if (!serverPath.toString().isEmpty()) {
            FileDescriptor[] buffer = new FileDescriptor[descriptors.length + 1];
            buffer[0] = BACK;
            System.arraycopy(descriptors, 0, buffer, 1, descriptors.length);
            descriptors = buffer;
        }
        return descriptors;
    }

    /**
     * Show user Dialog with given message
     *
     * @param message message to be shown
     */
    private void showErrorMessage(@NotNull String message) {
        SwingUtilities.invokeLater(() ->
                JOptionPane.showMessageDialog(this, message, "Error", JOptionPane.ERROR_MESSAGE));
    }

    @SuppressWarnings("FieldCanBeLocal")
    private final ServerSettingsListener settingBarListener = (host, port) -> {
        client.setServer(host, port);
        refresh();
    };

    @SuppressWarnings("FieldCanBeLocal")
    private final FileClickListener providerListener = (descriptor, provider) -> {
        if (descriptor == BACK) {
            serverPath = serverPath.getParent() == null ? EMPTY_PATH : serverPath.getParent();
            refresh();
        } else if (descriptor.isFolder()) {
            serverPath = Paths.get(serverPath.toString(), descriptor.getName());
            refresh();
        } else {
            JFileChooser fileChooser = new JFileChooser();
            if (fileChooser.showSaveDialog(this) == APPROVE_OPTION) {
                new Thread(() -> {
                    try {
                        String path = fileChooser.getSelectedFile().getPath();
                        String serverPath = Paths.get(descriptor.getPath(), descriptor.getName()).toString();
                        client.getFile(serverPath, path);
                    } catch (Client.ClientException e) {
                        showErrorMessage(e.getMessage());
                    }
                }).start();
            }
        }
    };

    public static void main(String[] args) {
        JFrame f = new GraphicalApplication();
        f.pack();
        f.setVisible(true);
    }
}
