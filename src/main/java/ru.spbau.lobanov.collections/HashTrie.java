package ru.spbau.lobanov.collections;

import com.sun.istack.internal.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Created by Артём on 21.09.2016.
 */

public class HashTrie implements Trie, StreamSerializable {

    private Node root = new Node();

    @Override
    public boolean add(@NotNull String element) {
        Stack<Node> stack = getStackForced(element);
        if (stack.peek().isFinal) {
            return false;
        }
        stack.peek().isFinal = true;
        for (Node node : stack) {
            node.prefixCounter++;
        }
        return true;
    }

    @Override
    public boolean contains(@NotNull String element) {
        Stack<Node> stack = getStack(element);
        return stack.size() == element.length() + 1 && stack.peek().isFinal;
    }

    @Override
    public boolean remove(@NotNull String element) {
        Stack<Node> stack = getStack(element);
        if (stack.size() != element.length() + 1 || !stack.peek().isFinal) {
            return false;
        }
        stack.peek().isFinal = false;
        for (Node node : stack) {
            node.prefixCounter--;
        }
        if (stack.peek().prefixCounter != 0) {
            return true;
        }
        while (stack.size() > 1 && stack.peek().prefixCounter == 0) {
            stack.pop();
        }
        stack.peek().removeNext(element.charAt(stack.size() - 1));
        return true;
    }

    @Override
    public int size() {
        return root.prefixCounter;
    }

    @Override
    public int howManyStartsWithPrefix(@NotNull String prefix) {
        Stack<Node> stack = getStack(prefix);
        return stack.size() != prefix.length() + 1? 0 : stack.peek().prefixCounter;
    }

    @Override
    public void serialize(OutputStream out) throws IOException {
        write(root, out);
    }

    @Override
    public void deserialize(InputStream in) throws IOException {
        root = read(in);
    }

    private void write(Node node, OutputStream out) throws IOException {
        out.write(node.prefixCounter);
        out.write(node.isFinal? 1 : 0);
        for (Map.Entry<Character, Node> entry : node.edges.entrySet()) {
            out.write(entry.getKey());
            write(entry.getValue(), out);
        }
        out.write(0);
    }

    private Node read(InputStream in) throws IOException {
        Node node = new Node();
        node.prefixCounter = in.read();
        node.isFinal = (in.read() != 0);
        for (char c = (char) in.read(); c != 0; c = (char) in.read()) {
            node.edges.put(c, read(in));
        }
        return node;
    }

    private Stack<Node> getStack(@NotNull String element) {
        Stack<Node> stack = new Stack<>();
        stack.push(root);
        for (char c : element.toCharArray()) {
            if (!stack.peek().hasNext(c))
                return stack;
            stack.push(stack.peek().next(c));
        }
        return stack;
    }

    private Stack<Node> getStackForced(@NotNull String element) {
        Stack<Node> stack = getStack(element);
        for (int i = stack.size() - 1; i < element.length(); i++) {
            stack.push(stack.peek().next(element.charAt(i)));
        }
        return stack;
    }

    private static class Node {
        final HashMap<Character, Node> edges = new HashMap<>();
        boolean isFinal;
        // the number of words in trie with the same prefix
        int prefixCounter;

        Node(boolean isFinal) {
            this.isFinal = isFinal;
        }

        Node() {
            this(false);
        }

        boolean hasNext(char c) {
            return edges.containsKey(c);
        }

        Node next(char c) {
            Node next = edges.get(c);
            if (next == null) {
                next = new Node();
                edges.put(c, next);
            }
            return next;
        }

        void removeNext(char c) {
            edges.remove(c);
        }
    }
}
