package ru.spbau.lobanov.collections;

import com.sun.istack.internal.NotNull;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Created by Артём on 04.10.2016.
 */
public class MyStack<E> implements Iterable<E> {

    private final Node<E> head;
    private int size;

    public MyStack() {
        head = new Node<>(null);
        size = 0;
    }

    public void push(E value) {
        Node<E> node = new Node<>(value);
        node.next = head.next;
        head.next = node;
        size++;
    }

    public E peek() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        return head.next.value;
    }

    public E pop() {
        if (isEmpty()) {
            throw new NoSuchElementException("Stack is empty");
        }
        Node<E> top = head.next;
        head.next = top.next;
        size--;
        return top.value;
    }

    public boolean isEmpty() {
        return head.next == null;
    }

    public int size() {
        return size;
    }

    @Override
    public Iterator<E> iterator() {
        return new StackIterator<>(head);
    }

    private static class StackIterator<E> implements Iterator<E> {
        Node<E> last;

        StackIterator(@NotNull Node<E> head) {
            this.last = head;
        }

        @Override
        public boolean hasNext() {
            return last.next != null;
        }

        @Override
        public E next() {
            if (!hasNext()) {
                throw new NoSuchElementException();
            }
            last = last.next;
            return last.value;
        }
    }

    private static class Node<E> {
        Node<E> next;
        E value;

        Node(E value) {
            this.value = value;
            next = null;
        }
    }
}
