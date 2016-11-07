package ru.spbau.lobanov.functional;

public interface Predicate<X> extends Function1<X, Boolean> {
    Predicate<Object> ALWAYS_TRUE = x -> true;
    Predicate<Object> ALWAYS_FALSE = x -> false;

    default Predicate<X> or(Predicate<? super X> another) {
        return x -> apply(x) || another.apply(x);
    }

    default Predicate<X> and(Predicate<? super X> another) {
        return x -> apply(x) && another.apply(x);
    }

    default Predicate<X> not() {
        return x -> !apply(x);
    }
}
