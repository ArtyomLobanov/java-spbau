package ru.spbau.lobanov.gui;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.spbau.lobanov.client.FileDescriptor;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;


/**
 * GUI class, which show table of files
 */
public class FileProvider extends JPanel {
    private static final String[] COLUMNS = {"File", "Type"};
    private final Model model;
    private final JTable table;
    private final List<FileClickListener> listeners;
    private FileDescriptor[] currentFiles;

    public FileProvider(@NotNull FileDescriptor[] files) {
        listeners = new ArrayList<>();
        setLayout(new BorderLayout());
        currentFiles = files;
        model = new Model();
        table = new JTable(model);
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() >= 2) {
                    FileDescriptor clickedFile = currentFiles[table.rowAtPoint(e.getPoint())];
                    listeners.forEach(listener -> listener.fileClicked(clickedFile, FileProvider.this));
                }
            }
        });
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(table.getTableHeader(), BorderLayout.NORTH);
    }

    /**
     * Changes set of files
     *
     * @param files new set of files
     */
    public void update(@NotNull FileDescriptor[] files) {
        currentFiles = files;
        model.notifyDataSetChanged();
    }

    /**
     * Add new listener
     *
     * @param listener listener, which will be called if any files will be clicked
     */
    public void addFileClickListener(@NotNull FileClickListener listener) {
        listeners.add(listener);
    }

    /**
     * Class which wrap array of FileDescriptors
     */
    private class Model implements TableModel {
        private final List<TableModelListener> listeners = new ArrayList<>();

        void notifyDataSetChanged() {
            listeners.forEach(listener -> listener.tableChanged(new TableModelEvent(this)));
        }

        @Override
        public int getRowCount() {
            return currentFiles.length;
        }

        @Override
        public int getColumnCount() {
            return COLUMNS.length;
        }

        @Override
        public String getColumnName(int columnIndex) {
            return COLUMNS[columnIndex];
        }

        @NotNull
        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

        @NotNull
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return currentFiles[rowIndex].getName();
                case 1:
                    return currentFiles[rowIndex].isFolder() ? "Folder" : "File";
                default:
                    throw new IndexOutOfBoundsException("Wrong column index");
            }
        }

        @Override
        public void setValueAt(@Nullable Object aValue, int rowIndex, int columnIndex) {
            throw new UnsupportedOperationException("Cant change data this way");
        }

        @Override
        public void addTableModelListener(@NotNull TableModelListener l) {
            listeners.add(l);
        }

        @Override
        public void removeTableModelListener(@NotNull TableModelListener l) {
            listeners.remove(l);
        }
    }

    /**
     * Listener, which will be called if some file will be clicked
     */
    interface FileClickListener {
        void fileClicked(@NotNull FileDescriptor fileDescriptor, @NotNull FileProvider provider);
    }
}
