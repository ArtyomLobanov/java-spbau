package ru.spbau.lobanov.functional;

public interface Function1<X, Y> {
    Y apply(X x);

    default <Z> Function1<X, Z> compose(Function1<? super Y, ? extends Z> g) {
        return x -> g.apply(apply(x));
    }
}
