package ru.spbau.lobanov.collections;

public class MyLinkedList<E> { // implements Iterable<E>, is Iterable a part of std lib?
    private static class Node<E> {

        E value;
        Node<E> next;
        Node(E value) {
            this.value = value;
        }

    }

    public static class ListIterator<E> { // implements Iterator<E>, is Iterator a part of std lib?
        private Node<E> next;

        private ListIterator(Node<E> next) {
            this.next = next;
        }

        public boolean hasNext() {
            return next != null;
        }

        public E next() {
            E value = next.value;
            next = next.next;
            return value;
        }
    }

    private Node<E> head;

    private int size;
    public MyLinkedList() {}

    public void addFirst(E object) {
        Node<E> tmp = head;
        head = new Node<>(object);
        head.next = tmp;
        size++;
    }

    private boolean equals(E first, E second) {
        if (first == null)
            return second == null;
        return first.equals(second);
    }

    public E find(E value) {
        Node<E> current = head;
        while (current != null) {
            if (equals(value, current.value))
                return current.value;
            current = current.next;
        }
        return null;
    }

    public boolean remove(E value) {
        Node<E> previous = null;
        Node<E> current = head;
        while (current != null) {
            if (equals(value, current.value)) {
                if (previous == null) {
                    head = current.next;
                } else {
                    previous.next = current.next;
                }
                size--;
                return true;
            }
            previous = current;
            current = current.next;
        }
        return false;
    }

    public int size() {
        return size;
    }

    public void clear() {
        head = null;
        size = 0;
    }

    public ListIterator<E> iterator() {
        return new ListIterator<>(head);
    }

}
