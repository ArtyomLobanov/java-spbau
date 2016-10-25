package ru.spbau.lobanov.functional;

public interface Predicate<X> extends Function1<X, Boolean> {
    Predicate ALWAYS_TRUE = x -> true;
    Predicate ALWAYS_FALSE = x -> false;

    public default Predicate<X> or(Predicate<? super X> another) {
        return x -> Predicate.this.apply(x) || another.apply(x);
    }

    public default Predicate<X> and(Predicate<? super X> another) {
        return x -> Predicate.this.apply(x) && another.apply(x);
    }

    public default Predicate<X> not() {
        return x -> !Predicate.this.apply(x);
    }
}
