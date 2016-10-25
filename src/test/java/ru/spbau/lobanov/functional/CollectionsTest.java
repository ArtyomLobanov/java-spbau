package ru.spbau.lobanov.functional;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.*;

import static ru.spbau.lobanov.functional.PredicateTest.checkContents;

public class CollectionsTest {
    private static final Iterable<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    @Test
    public void map() throws Exception {
        Iterable<Integer> result = Collections.map(numbers, x -> x + 1);
        assertTrue(checkContents(result, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));
    }

    @Test
    public void filter() throws Exception {
        Iterable<Integer> result = Collections.filter(numbers, x -> (x * 7 % 10) < 5);
        assertTrue(checkContents(result, 2, 3, 6, 9, 10));
    }

    @Test
    public void takeWhile() throws Exception {
        Iterable<Integer> result = Collections.takeWhile(numbers, x -> x * x < 20);
        assertTrue(checkContents(result, 1, 2, 3, 4));
    }

    @Test
    public void takeUnless() throws Exception {
        Iterable<Integer> result = Collections.takeUnless(numbers, x -> x * x >= 20);
        assertTrue(checkContents(result, 1, 2, 3, 4));
    }

    @Test
    public void foldl() throws Exception {
        Function2<String, Integer, String> f = (x1, x2) -> "(" + x1 + " + " + x2 + ")";
        assertEquals("(((((0 + 1) + 2) + 3) + 4) + 5)", Collections.foldl(f, "0", Arrays.asList(1, 2, 3, 4, 5)));
    }

    @Test
    public void foldr() throws Exception {
        Function2<Integer, String, String> f = (x1, x2) -> "(" + x1 + " + " + x2 + ")";
        assertEquals("(1 + (2 + (3 + (4 + (5 + 0)))))", Collections.foldr(f, "0", Arrays.asList(1, 2, 3, 4, 5)));
    }

}