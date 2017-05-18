package ru.mit.spbau.lobanov.xunit.testing;


import org.junit.Test;
import static org.junit.Assert.*;
import static ru.mit.spbau.lobanov.xunit.testing.Examples.*;
import static ru.mit.spbau.lobanov.xunit.testing.Examples.LoggingExample.Type;

public class TesterTest {
    @Test
    public void ignoredTests() throws Exception {
        Tester tester = new Tester(IgnoredExample.class);
        tester.runTests();
        assertEquals(1, tester.getWarningMessages().size());
        assertEquals(0, tester.getSuccessMessages().size());
        assertEquals(0, tester.getErrorMessages().size());
    }

    @Test
    public void successTests() throws Exception {
        Tester tester = new Tester(SuccessExample.class);
        tester.runTests();
        assertEquals(0, tester.getWarningMessages().size());
        assertEquals(2, tester.getSuccessMessages().size());
        assertEquals(0, tester.getErrorMessages().size());
    }

    @Test
    public void unexpectedErrorTest() throws Exception {
        Tester tester = new Tester(UnexpectedExceptions.class);
        tester.runTests();
        assertEquals(0, tester.getWarningMessages().size());
        assertEquals(0, tester.getSuccessMessages().size());
        assertEquals(2, tester.getErrorMessages().size());
    }


    @Test
    public void multiExpectedExceptionsTests() throws Exception {
        Tester tester = new Tester(MultiExpectedExceptions.class);
        tester.runTests();
        assertEquals(0, tester.getWarningMessages().size());
        assertEquals(2, tester.getSuccessMessages().size());
        assertEquals(0, tester.getErrorMessages().size());
    }


    @Test
    public void expectSuperclassTests() throws Exception {
        Tester tester = new Tester(ExpectSuperclassException.class);
        tester.runTests();
        assertEquals(0, tester.getWarningMessages().size());
        assertEquals(1, tester.getSuccessMessages().size());
        assertEquals(0, tester.getErrorMessages().size());
    }

    @Test
    public void sequenceTest() throws TestingException {
        Tester tester = new Tester(LoggingExample.class);
        tester.runTests();
        Type[] realSequence = LoggingExample.logs.toArray(new Type[LoggingExample.logs.size()]);
        assertArrayEquals(LoggingExample.correctSequence, realSequence);
    }
}