package ru.spbau.lobanov.functional;

import org.junit.Test;

import static org.junit.Assert.*;

public class Function2Test {

    @Test
    public void compose() throws Exception {
        Function2<Integer, Integer, Integer> pown = (x, n) -> {
            int res = 1;
            for (int i = 0; i < n; i++) res *= x;
            return res;
        };
        Function1<Object, String> writer = Object::toString;
        assertEquals("256", pown.compose(writer).apply(2, 8));
    }

    @Test
    public void bind1() throws Exception {
        Function2<Integer, Integer, Double> pow = Math::pow;
        Function1<Integer, Double> power2 = pow.bind1(2);
        assertEquals(256, power2.apply(8), 0.00001);
    }

    @Test
    public void bind2() throws Exception {
        Function2<Integer, Double, Double> pow = Math::pow;
        Function1<Integer, Double> sqrt = pow.bind2(0.5);
        assertEquals(16, sqrt.apply(256), 0.00001);
    }

    @Test
    public void curry() throws Exception {
        Function2<Integer, Double, Double> pow = Math::pow;
        assertEquals(16, pow.curry().apply(256).apply(0.5), 0.00001);
    }

    @Test
    public void flip() throws Exception {
        Function2<Integer, Double, Double> pow1 = Math::pow;
        Function2<Double, Integer, Double> pow2 = pow1.flip();
        assertEquals(pow1.apply(239566, 10.), pow2.apply(10., 239566), 0);
    }

}
