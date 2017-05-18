package ru.mit.spbau.lobanov.xunit.testing;

import ru.mit.spbau.lobanov.xunit.annotations.*;

import java.util.ArrayList;
import java.util.List;

import static ru.mit.spbau.lobanov.xunit.testing.Examples.LoggingExample.Type.*;

public class Examples {
    static class IgnoredExample {
        @Test(ignore = "true")
        void test() {}
    }

    static class SuccessExample {
        @Test()
        void test() {}

        @Test(expected = IllegalArgumentException.class)
        void test2() {
            throw new IllegalArgumentException();
        }
    }

    static class UnexpectedExceptions {
        @Test()
        void test() {
            throw new IllegalArgumentException();
        }

        @Test(expected = ArrayIndexOutOfBoundsException.class)
        void test2() {
            throw new IllegalArgumentException();
        }
    }

    static class MultiExpectedExceptions {
        @Test(expected = {RuntimeException.class, ArrayIndexOutOfBoundsException.class})
        void test() {
            throw new RuntimeException();
        }

        @Test(expected = {RuntimeException.class, ArrayIndexOutOfBoundsException.class})
        void test2() {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    static class ExpectSuperclassException {
        @Test(expected = {RuntimeException.class})
        void test2() {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    static class LoggingExample {
        enum Type {BEFORE_CLASS, BEFORE, TEST, AFTER, AFTER_CLASS}
        static final Type[] correctSequence = {BEFORE_CLASS, BEFORE, TEST, AFTER,
                BEFORE, TEST, AFTER, BEFORE, TEST, AFTER, AFTER_CLASS};

        static final ArrayList<Type> logs = new ArrayList<>();

        @BeforeClass
        void beforeClass() {
            logs.add(BEFORE_CLASS);
        }

        @Before
        void before() {
            logs.add(BEFORE);
        }

        @After
        void after() {
            logs.add(AFTER);
        }

        @AfterClass
        void afterClass() {
            logs.add(AFTER_CLASS);
        }

        @Test
        void test1() {
            logs.add(TEST);
        }

        @Test
        void test2() {
            logs.add(TEST);
            throw new IllegalArgumentException();
        }

        @Test(expected = IllegalArgumentException.class)
        void test3() {
            logs.add(TEST);
            throw new IllegalArgumentException();
        }
    }
}
