package ru.spbau.lobanov.gui;

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

public class FileProvider extends JPanel {
    private static final String[] COLUMNS = {"File", "Type"};
    private final Model model;
    private final JTable table;
    private final List<FileClickListener> listeners;
    private FileDescriptor[] currentFiles;

    public FileProvider(FileDescriptor[] files) {
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

    public void update(FileDescriptor[] files) {
        currentFiles = files;
        model.notifyDataSetChanged();
    }

    public void addFileClickListener(FileClickListener listener) {
        listeners.add(listener);
    }

    @Override
    public Dimension getPreferredSize() {
        return table.getPreferredSize();
    }

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

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }

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
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            throw new UnsupportedOperationException("Cant change data this way");
        }

        @Override
        public void addTableModelListener(TableModelListener l) {
            listeners.add(l);
        }

        @Override
        public void removeTableModelListener(TableModelListener l) {
            listeners.remove(l);
        }
    }

    interface FileClickListener {
        void fileClicked(FileDescriptor fileDescriptor, FileProvider provider);
    }
}
