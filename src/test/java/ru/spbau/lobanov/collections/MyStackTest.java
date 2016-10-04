package ru.spbau.lobanov.collections;

import org.junit.Assert;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Created by Артём on 04.10.2016.
 */
public class MyStackTest {
    @org.junit.Test
    public void push() throws Exception {
        MyStack<Integer> stack = new MyStack<>();
        for (int i = 0; i < 100; i++) {
            stack.push(i);
            assertEquals(i + 1, stack.size());
        }
        int x = 100;
        for (int i : stack) {
            x--;
            Assert.assertEquals(x, i);
        }
        assertEquals(0, x);
    }

    @org.junit.Test
    public void peek() throws Exception {
        MyStack<Integer> stack = new MyStack<>();
        for (int i = 0; i < 100; i++) {
            stack.push(i);
            Assert.assertEquals((Integer) i, stack.peek());
        }
        for (int i = 99; i >= 0; i--) {
            Assert.assertEquals((Integer) i, stack.peek());
            stack.pop();
        }
        Assert.assertEquals(0, stack.size());
    }

    @org.junit.Test
    public void pop() throws Exception {
        MyStack<Integer> stack = new MyStack<>();
        for (int i = 0; i < 100; i++) {
            stack.push(i);
        }
        for (int i = 0; i < 10; i++) {
            Assert.assertEquals((Integer) (99 - i), stack.pop());
            Assert.assertEquals(99 - i, stack.size());
        }
        for (int i = 0; i < 10; i++) {
            stack.push(i);
            Assert.assertEquals((Integer) i, stack.pop());
        }
        int cnt = 0;
        while (!stack.isEmpty()) {
            stack.pop();
            cnt++;
        }
        assertEquals(90, cnt);
        assertTrue(stack.isEmpty());
    }

    @org.junit.Test
    public void isEmpty() throws Exception {
        MyStack<Integer> stack = new MyStack<>();
        assertTrue(stack.isEmpty());
        for (int i = 0; i < 5; i++) {
            stack.push(i);
            assertFalse(stack.isEmpty());
        }
        int cnt = 0;
        while (!stack.isEmpty()) {
            stack.pop();
            cnt++;
        }
        assertEquals(5, cnt);
    }

    @org.junit.Test
    public void size() throws Exception {
        MyStack<Integer> stack = new MyStack<>();
        assertEquals(0, stack.size());
        for (int i = 0; i < 10; i++) {
            stack.push(i);
            assertEquals(i + 1, stack.size());
        }
        for (int i = 0; i < 5; i++) {
            stack.pop();
            assertEquals(9 - i, stack.size());
        }
        for (int i = 0; i < 5; i++) {
            stack.push(i);
            assertEquals(6 + i, stack.size());
        }
    }

    @org.junit.Test
    public void iterator() throws Exception {
        MyStack<Integer> stack = new MyStack<>();
        for (int i = 0; i < 10; i++) {
            stack.push(i);
        }
        int cnt = 0;
        for (int i : stack) {
            cnt++;
            assertEquals(10 - cnt, i);
        }
        assertEquals(10, cnt);
    }

}