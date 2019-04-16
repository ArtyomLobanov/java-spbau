package ru.spbau.lobanov.functional;

public interface Function2<X1, X2, Y> {
    Y apply(X1 x1, X2 x2);

    default <Z> Function2<X1, X2, Z> compose(Function1<? super Y, ? extends Z> g) {
        return (x1, x2) -> g.apply(apply(x1, x2));
    }

    default Function1<X2, Y> bind1(X1 x) {
        return x2 -> apply(x, x2);
    }

    default Function1<X1, Y> bind2(X2 x) {
        return x1 -> apply(x1, x);
    }

    default Function1<X1, Function1<X2, Y>> curry() {
        return x1 -> x2 -> apply(x1, x2);
    }

    default Function2<X2, X1, Y> flip() {
        return (x2, x1) -> apply(x1, x2);
    }
}
