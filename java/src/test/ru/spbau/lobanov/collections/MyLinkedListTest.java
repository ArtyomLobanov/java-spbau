package ru.spbau.lobanov.collections;

import org.junit.Test;


import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;


public class MyLinkedListTest {
    @Test
    public void addFirst() throws Exception {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        for (int i = 255; i >= 0; i--) {
            list.addFirst(i);
        }
        MyLinkedList.ListIterator<Integer> iterator = list.iterator();
        for (int i = 0; i < 256; i++) {
            assertTrue(iterator.hasNext());
            assertEquals((Integer) i, iterator.next());
        }
        assertFalse(iterator.hasNext());
    }

    @Test
    public void find() throws Exception {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        for (int i = 255; i >= 0; i--) {
            list.addFirst(i);
        }
        for (int i = 0; i < 256; i++) {
            assertEquals((Integer) i, list.find(i));
        }
    }

    @Test
    public void remove() throws Exception {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        for (int i = 255; i >= 0; i--) {
            list.addFirst(i);
        }
        for (int i = 0; i < 256; i += 2) {
            list.remove(i);
            assertNull(list.find(i));
        }
        for (int i = 1; i < 256; i += 2) {
            assertEquals((Integer) i, list.find(i));
        }
    }

    @Test
    public void size() throws Exception {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        assertEquals(0, list.size());
        for (int i = 0; i < 256; i++) {
            list.addFirst(i);
            assertEquals(i + 1, list.size());
        }
        for (int i = 0; i < 128; i++) {
            list.remove(i);
            assertEquals(256 - i - 1, list.size());
        }
        list.clear();
        assertEquals(0, list.size());
    }

    @Test
    public void clear() throws Exception {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        for (int i = 0; i < 256; i++) {
            list.addFirst(i);
        }
        list.clear();
        for (int i = 0; i < 256; i++) {
            assertNull(list.find(i));
        }
        assertEquals(0, list.size());
    }

    @Test
    public void iterator() throws Exception {
        MyLinkedList<Integer> list = new MyLinkedList<>();
        for (int i = 255; i >= 0; i--) {
            list.addFirst(i);
        }
        for (int i = 0; i < 256; i += 2) {
            list.remove(i);
        }
        MyLinkedList.ListIterator<Integer> iterator = list.iterator();
        for (int i = 1; i < 256; i += 2) {
            assertTrue(iterator.hasNext());
            assertEquals((Integer) i, iterator.next());
        }
        assertFalse(iterator.hasNext());
    }

}