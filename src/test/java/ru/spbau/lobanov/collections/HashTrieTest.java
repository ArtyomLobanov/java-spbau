package ru.spbau.lobanov.collections;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by Артём on 04.10.2016.
 */
public class HashTrieTest {
    @Test
    public void add() throws Exception {
        HashTrie trie = new HashTrie();
        trie.add("hello");
        trie.add("hell");
        trie.add("world");
        trie.add("");

        assertTrue(trie.contains("hello"));
        assertTrue(trie.contains("hell"));
        assertTrue(trie.contains("world"));
        assertTrue(trie.contains(""));

        assertFalse(trie.contains("hel"));
        assertFalse(trie.contains("he"));
        assertFalse(trie.contains("h"));
        assertFalse(trie.contains("hellot"));
        assertFalse(trie.contains("ell"));

        assertEquals(4, trie.size());
    }

    @Test
    public void contains() throws Exception {
        HashTrie trie = new HashTrie();
        trie.add("hello");
        trie.add("hell");
        trie.add("world");
        trie.add("");

        assertTrue(trie.contains("hello"));
        assertTrue(trie.contains("hell"));
        assertTrue(trie.contains("world"));
        assertTrue(trie.contains(""));

        trie.remove("hell");

        assertTrue(trie.contains("hello"));
        assertFalse(trie.contains("hell"));
        assertFalse(trie.contains("he"));
        assertFalse(trie.contains("h"));
        assertFalse(trie.contains("hellot"));
        assertFalse(trie.contains("ell"));

        assertEquals(3, trie.size());
    }

    @Test
    public void remove() throws Exception {
        HashTrie trie = new HashTrie();
        trie.add("hello");
        trie.add("hell");
        trie.add("hellt");
        trie.add("hellop");
        trie.add("he");
        trie.add("wi");

        trie.remove("hel");
        assertEquals(6, trie.size());

        trie.remove("hell");
        assertEquals(5, trie.size());
        assertFalse(trie.contains("hell"));
        assertTrue(trie.contains("hellop"));
        assertTrue(trie.contains("he"));

        trie.remove("hello");
        trie.remove("hellt");
        trie.remove("hellop");
        trie.remove("he");
        trie.remove("wi");

        assertEquals(0, trie.size());
    }

    @Test
    public void size() throws Exception {
        HashTrie trie = new HashTrie();

        assertEquals(0, trie.size());

        String[] list = {"hell", "Hello", "hello", "ewr", "erfd", "ewt", "Ewt"};
        for (int i = 0; i < list.length; i++) {
            trie.add(list[i]);
            assertEquals(i + 1, trie.size());
        }
        for (String s : list) {
            trie.add(s);
            assertEquals(list.length, trie.size());
        }

        trie.remove(list[0]);
        assertEquals(list.length - 1, trie.size());
        trie.remove(list[0]);
        assertEquals(list.length - 1, trie.size());

        for (int i = 1; i < list.length; i++) {
            trie.remove(list[i]);
            assertEquals(list.length - i - 1, trie.size());
        }

        assertEquals(0, trie.size());
    }

    @Test
    public void howManyStartsWithPrefix() throws Exception {
        HashTrie trie = new HashTrie();
        String s = "Sorry, i am later";
        for (int i = 0; i <= s.length(); i++) {
            trie.add(s.substring(0, i));
        }
        for (int i = 0; i <= s.length(); i++) {
            assertEquals(s.length() - i + 1, trie.howManyStartsWithPrefix(s.substring(0, i)));
        }
        String s2 = "Sorry, i am not later";
        for (int i = 0; i <= s2.length(); i++) {
            trie.add(s2.substring(0, i));
        }
        assertEquals(15, trie.howManyStartsWithPrefix("Sorry, i am "));
        assertEquals(16, trie.howManyStartsWithPrefix("Sorry, i am"));
        assertEquals(9, trie.howManyStartsWithPrefix("Sorry, i am n"));
        assertEquals(0, trie.howManyStartsWithPrefix("sorry, i am n"));
    }
}