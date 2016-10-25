package ru.spbau.lobanov.functional;

import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;

import static org.junit.Assert.*;

public class PredicateTest {
    private static final Iterable<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15);

    @Test
    public void or() throws Exception {
        Predicate<Integer> isMultipleOfFive = x -> x % 5 == 0;
        // for Integer hashCode return intValue
        Predicate<Object> isMultipleOfThree = x -> x.hashCode() % 3 == 0;
        Predicate<Integer> predicate = isMultipleOfFive.or(isMultipleOfThree);
        Iterable<Integer> result = Collections.filter(numbers, predicate);
        assertTrue(checkContents(result, 3, 5, 6, 9, 10, 12, 15));
    }

    @Test
    public void and() throws Exception {
        Predicate<Integer> isEven = x -> (x & 1) == 0;
        // for Integer hashCode return intValue
        Predicate<Object> isMultipleOfThree = x -> x.hashCode() % 3 == 0;
        Predicate<Integer> isMultipleOfSix = isEven.and(isMultipleOfThree);
        Iterable<Integer> result = Collections.filter(numbers, isMultipleOfSix);
        assertTrue(checkContents(result, 6, 12));
    }

    @Test
    public void not() throws Exception {
        Predicate<Integer> isEven = x -> (x & 1) == 0;
        Predicate<Integer> isOdd = isEven.not();
        assertTrue(checkContents(Collections.filter(numbers, isOdd), 1, 3, 5, 7, 9, 11, 13, 15));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void constantsTest() {
        assertTrue(checkContents(Collections.filter(numbers, Predicate.ALWAYS_TRUE), 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15));
        assertTrue(checkContents(Collections.filter(numbers, Predicate.ALWAYS_FALSE)));
    }

    @SafeVarargs
    static <E> boolean checkContents(Iterable<E> collection, E... values) {
        Iterator iterator = collection.iterator();
        for (E value : values) {
            if (!iterator.hasNext() || !value.equals(iterator.next())) {
                return false;
            }
        }
        return !iterator.hasNext();
    }

}