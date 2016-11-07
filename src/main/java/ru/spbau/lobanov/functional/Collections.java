package ru.spbau.lobanov.functional;

import java.util.LinkedList;

public final class Collections {
    public static <X, Y> LinkedList<Y> map(Iterable<X> stream, Function1<? super X, Y> f) {
        LinkedList<Y> res = new LinkedList<>();
        for (X x : stream) {
            res.addLast(f.apply(x));
        }
        return res;
    }

    public static <X> LinkedList<X> filter(Iterable<X> stream, Predicate<? super X> predicate) {
        LinkedList<X> res = new LinkedList<>();
        for (X x : stream) {
            if (predicate.apply(x)) {
                res.addLast(x);
            }
        }
        return res;
    }

    public static <X> LinkedList<X> takeWhile(Iterable<X> stream, Predicate<? super X> predicate) {
        LinkedList<X> res = new LinkedList<>();
        for (X x : stream) {
            if (!predicate.apply(x)) return res;
            res.addLast(x);
        }
        return res;
    }

    public static <X> LinkedList<X> takeUnless(Iterable<X> stream, Predicate<? super X> predicate) {
        return takeWhile(stream, predicate.not());
    }

    public static <X, Y> Y foldl(Function2<? super Y, ? super X, Y> f, Y initialValue, Iterable<X> stream) {
        Y result = initialValue;
        for (X x : stream) {
            result = f.apply(result, x);
        }
        return result;
    }

    public static <X, Y> Y foldr(Function2<? super X, ? super Y, Y> f, Y initialValue, Iterable<X> stream) {
        LinkedList<X> list = new LinkedList<>();
        stream.forEach(list::addFirst);
        return foldl(f.flip(), initialValue, list);
    }
}
