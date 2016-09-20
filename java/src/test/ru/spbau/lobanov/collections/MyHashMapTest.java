package ru.spbau.lobanov.collections;

import org.junit.Test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class MyHashMapTest {
    @Test
    public void size() throws Exception {
        MyHashMap map = new MyHashMap();
        for (int i = 0; i < 512; i++) {
            map.put("key" + i, "value" + i);
        }
        assertEquals(512, map.size());
        for (int i = 0; i < 512; i += 2) {
            map.remove("key" + i);
        }
        assertEquals(256, map.size());
        for (int i = 1; i < 512; i += 2) {
            map.put("key" + i, "value" + i);
        }
        assertEquals(256, map.size());
        map.clear();
        assertEquals(0, map.size());
    }

    @Test
    public void contains() throws Exception {
        MyHashMap map = new MyHashMap();
        for (int i = 0; i < 512; i++) {
            map.put("key" + i, "value" + i);
            assertTrue(map.contains("key" + i));
        }

        for (int i = 0; i < 512; i += 2) {
            map.remove("key" + i);
            assertFalse(map.contains("key" + i));
        }
        for (int i = 1; i < 512; i += 2) {
            assertTrue(map.contains("key" + i));
        }
        map.clear();
        for (int i = 0; i < 512; i++) {
            assertFalse(map.contains("key" + i));
        }
    }

    @Test
    public void get() throws Exception {
        MyHashMap map = new MyHashMap();
        for (int i = 0; i < 512; i++) {
            map.put("key" + i, "value" + i);
        }
        for (int i = 0; i < 512; i ++) {
            assertEquals("value" + i, map.get("key" + i));
        }
        for (int i = 1; i < 512; i += 2) {
            map.put("key" + i, "newValue" + i);
        }
        for (int i = 0; i < 512; i += 2) {
            assertEquals((i % 2 == 1? "newV" : "v") + "alue" + i, map.get("key" + i));
        }
        map.clear();
        for (int i = 0; i < 512; i ++) {
            assertSame(null, map.get("key" + i));
        }
    }

    @Test
    public void put() throws Exception {
        MyHashMap map = new MyHashMap();
        for (int i = 0; i < 512; i++) {
            map.put("key" + (i % 30), "value" + i);
            assertEquals("value" + i, map.get("key" + (i % 30)));
        }
    }

    @Test
    public void remove() throws Exception {
        MyHashMap map = new MyHashMap();
        for (int i = 0; i < 512; i++) {
            map.put("key" + i, "value" + i);
        }
        for (int i = 0; i < 512; i += 2) {
            map.remove("key" + i);
        }
        for (int i = 0; i < 512; i ++) {
            assertEquals(i % 2 != 0, map.contains("key" + i));
        }
    }

    @Test
    public void clear() throws Exception {
        MyHashMap map = new MyHashMap();
        for (int i = 0; i < 512; i++) {
            map.put("key" + i, "value" + i);
        }
        map.clear();
        assertEquals(0, map.size());
        for (int i = 0; i < 512; i ++) {
            assertSame(null, map.get("key" + i));
        }
    }

}