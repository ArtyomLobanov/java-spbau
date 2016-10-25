package ru.spbau.lobanov.functional;

public interface Function1<X, Y> {
    public Y apply(X x);

    public default <Z> Function1<X, Z> compose(Function1<? super Y, ? extends Z> g) {
        return x -> g.apply(Function1.this.apply(x));
    }
}
