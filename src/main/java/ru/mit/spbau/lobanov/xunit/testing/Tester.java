package ru.mit.spbau.lobanov.xunit.testing;

import ru.mit.spbau.lobanov.xunit.annotations.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Tester {

    private final Class<?> testPack;
    private final List<Method> beforeClassMethods;
    private final List<Method> afterClassMethods;
    private final List<Method> beforeMethods;
    private final List<Method> afterMethods;
    private final List<Method> testMethods;
    private List<SuccessMessage> successMessages;
    private List<WarningMessage> warningMessages;
    private List<ErrorMessage> errorMessages;
    private List<Message> protocol;


    public Tester(String testPackName) throws TestingException {
        Class<?> testPack = Object.class;
        try {
            testPack = Class.forName(testPackName);
        } catch (ClassNotFoundException e) {
            throw new TestingException("Bad ClassLoader or wrong path: Class with tests wasn't found", e);
        }
        beforeClassMethods = filterMethods(testPack.getDeclaredMethods(), BeforeClass.class);
        afterClassMethods = filterMethods(testPack.getDeclaredMethods(), AfterClass.class);
        beforeMethods = filterMethods(testPack.getDeclaredMethods(), Before.class);
        afterMethods = filterMethods(testPack.getDeclaredMethods(), After.class);
        testMethods = filterMethods(testPack.getDeclaredMethods(), Test.class);
        this.testPack = testPack;
        successMessages = Collections.emptyList();
        warningMessages = Collections.emptyList();
        errorMessages = Collections.emptyList();
        protocol = Collections.emptyList();
    }

    private List<Method> filterMethods(Method[] methods, Class<? extends Annotation> target) {
        return Arrays.stream(methods)
                .filter(m -> m.isAnnotationPresent(target))
                .collect(Collectors.toList());
    }

    private void reportError(String text, Throwable error, Method test) {
        ErrorMessage message = new ErrorMessage(text, error, test);
        errorMessages.add(message);
        protocol.add(message);
    }

    private void reportWarning(String text, Method test) {
        WarningMessage message = new WarningMessage(text, test);
        warningMessages.add(message);
        protocol.add(message);
    }

    private void reportSuccess(String text, long time, Method test) {
        SuccessMessage message = new SuccessMessage(text, time, test);
        successMessages.add(message);
        protocol.add(message);
    }

    private void invokeAll(Object instance, List<Method> methods) throws Throwable {
        try {
            for (Method method : methods) {
                method.invoke(instance);
            }
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    public void runTests() throws TestingException {
        successMessages = new ArrayList<>();
        warningMessages = new ArrayList<>();
        errorMessages = new ArrayList<>();
        protocol = new ArrayList<>();
        final Object instance;
        try {
            instance = testPack.newInstance();
        } catch (InstantiationException e) {
            String exceptionName = e.getCause().getClass().getName();
            throw new TestingException("Exception occurred while instance was constructing: " + exceptionName);
        } catch (IllegalAccessException e) {
            throw new TestingException("Tests pack must have public constructor without parameters");
        }
        try {
            invokeAll(instance, beforeClassMethods);
        } catch (Throwable e) {
            String message = "Can't run test, because error occurred, while @BeforeClass methods were running: " +
                    e.getClass().getName();
            reportError(message, e, null);
            return;
        }
        testMethods.forEach(test -> runTest(instance, test));
        try {
            invokeAll(instance, afterClassMethods);
        } catch (Throwable e) {
            String message = "Error occurred, while @AfterClass methods were running: " +
                    e.getClass().getName();
            reportWarning(message, null);
        }

    }

    private void runTest(Object instance, Method method) {
        Test annotation = method.getAnnotation(Test.class);
        if (!annotation.ignore().isEmpty()) {
            reportWarning("Test is ignored, reason:" + annotation.ignore(), method);
            return;
        }
        try {
            invokeAll(instance, beforeMethods);
        } catch (Throwable e) {
            reportError("Cant start test, error occurred while @Before methods were invoking", e, method);
            return;
        }
        long start = System.currentTimeMillis();
        try {
            method.invoke(instance);
            if (annotation.expected().length == 0) {
                reportSuccess("Test passed", System.currentTimeMillis() - start, method);
            } else {
                reportError("Exceptions expected, but didn't occurred", null, method);
            }
        } catch (IllegalAccessException e) {
            reportError("Cant run test, because method marked as private", e, method);
        } catch (IllegalArgumentException e) {
            reportError("Method, annotated as @Test can't have arguments", e, method);
        } catch (InvocationTargetException e) {
            Class<? extends Throwable> internalException = e.getCause().getClass();
            boolean expected = Arrays.stream(annotation.expected())
                    .anyMatch(internalException::isAssignableFrom);
            if (expected) {
                reportSuccess("Test passed", System.currentTimeMillis() - start, method);
            } else {
                reportError("Unexpected exception: " + internalException.getName(), e.getCause(), method);
            }
        } finally {
            try {
                invokeAll(instance, afterMethods);
            } catch (Throwable e) {
                String errorName = e.getClass().getName();
                reportWarning("Error occurred while @After methods were invoking: " + errorName, method);
            }
        }
    }

    public List<SuccessMessage> getSuccessMessages() {
        return Collections.unmodifiableList(successMessages);
    }

    public List<WarningMessage> getWarningMessages() {
        return Collections.unmodifiableList(warningMessages);
    }

    public List<ErrorMessage> getErrorMessages() {
        return Collections.unmodifiableList(errorMessages);
    }

    public List<Message> getProtocol() {
        return Collections.unmodifiableList(protocol);
    }
}
