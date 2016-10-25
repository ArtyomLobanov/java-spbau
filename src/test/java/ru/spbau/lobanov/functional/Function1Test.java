package ru.spbau.lobanov.functional;

import org.junit.Test;


import static org.junit.Assert.*;

public class Function1Test {
    @Test
    public void composeTest() throws Exception {
        Function1<Integer, Double> sqrt = Math::sqrt;
        Function1<Object, String> printer = Object::toString;
        Function1<String, Double> parser = Double::parseDouble;

        assertEquals(16, sqrt.compose(printer).compose(parser).apply(256), 0.00001);
        assertEquals(Math.sqrt(5), sqrt.compose(printer).compose(parser).apply(5), 0.00001);
    }

    @Test
    public void recursionTest() throws Exception {
        Function1<Double, Double> sqrt = Math::sqrt;
        Function1<Double, Double> fourthRoot = sqrt.compose(sqrt);
        Function1<Double, Double> sixteenthRoot = fourthRoot.compose(fourthRoot);

        assertEquals(239566, Math.pow(fourthRoot.apply(239566.), 4), 0.00001);
        assertEquals(566239, Math.pow(sixteenthRoot.apply(566239.), 16), 0.00001);
    }
}