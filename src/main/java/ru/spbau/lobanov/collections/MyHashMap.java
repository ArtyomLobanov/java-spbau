package ru.spbau.lobanov.collections;

public class MyHashMap {

    private static class Entry<K, V> {
        final K key;
        final int hash;
        V value;

        Entry(K key, V value) {
            this.key = key;
            this.value = value;
            this.hash = key.hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj.getClass() != Entry.class) return false;
            Entry another = (Entry) obj;
            return hash == another.hash && key.equals(another.key);
        }

        @Override
        public int hashCode() {
            return hash;
        }
    }


    public static final double DEFAULT_LOAD_FACTOR = 0.75;
    public static final int DEFAULT_INITIAL_SIZE = 256;

    private int size;
    private MyLinkedList<Entry<String, String>>[] table;
    private final double loadFactor;

    public MyHashMap(int initialSize, double loadFactor) {
        if (Double.isNaN(loadFactor) || loadFactor <= 0 || initialSize < 0)
            throw new IllegalArgumentException();
        this.loadFactor = loadFactor;
        table = new MyLinkedList[initialSize];
        for (int i = 0; i < initialSize; i++) {
            table[i] = new MyLinkedList<>();
        }
    }

    public MyHashMap(int initialSize) {
        this(initialSize, DEFAULT_LOAD_FACTOR);
    }

    public MyHashMap(double loadFactor) {
        this(DEFAULT_INITIAL_SIZE, loadFactor);
    }

    public MyHashMap() {
        this(DEFAULT_INITIAL_SIZE, DEFAULT_LOAD_FACTOR);
    }

    private void rebuild() {
        MyLinkedList<Entry<String, String>>[] oldTable = table;
        table = new MyLinkedList[table.length * 2];
        for (int i = 0; i < table.length; i++) {
            table[i] = new MyLinkedList<>();
        }
        size = 0;
        for (MyLinkedList<Entry<String, String>> list : oldTable) {
            MyLinkedList.ListIterator<Entry<String, String>> iterator = list.iterator();
            while (iterator.hasNext()) {
                addEntry(iterator.next());
            }
        }
    }

    private void validate() {
        if (size >= table.length * loadFactor) {
            rebuild();
        }

    }

    private void addEntry(Entry<String, String> entry) {
        table[getIndex(entry.hash)].addFirst(entry);
        size++;
    }

    private int getIndex(int hash) {
        return Math.abs(hash % table.length);
    }

    public int size() {
        return size;
    }

    public boolean contains(String key) {
        Entry<String, String> template = new Entry<>(key, null);
        return table[getIndex(template.hash)].find(template) != null;
    }

    public String get(String key) {
        Entry<String, String> template = new Entry<>(key, null);
        Entry<String, String> entry = table[getIndex(template.hash)].find(template);
        return entry == null? null : entry.value;
    }

    public String put(String key, String value) {
        Entry<String, String> template = new Entry<>(key, null);
        Entry<String, String> entry = table[getIndex(template.hash)].find(template);
        if (entry == null) {
            table[getIndex(template.hash)].addFirst(template);
            entry = template;
            size++;
            validate();
        }
        String oldValue = entry.value;
        entry.value = value;
        return oldValue;
    }

    public String remove(String key) {
        Entry<String, String> template = new Entry<>(key, null);
        Entry<String, String> entry = table[getIndex(template.hash)].find(template);
        if (entry == null) {
            return null;
        }
        table[getIndex(template.hash)].remove(template);
        size--;
        return entry.value;
    }

    public void clear() {
        for (MyLinkedList<Entry<String, String>> list : table) {
            list.clear();
        }
        size = 0;
    }
}