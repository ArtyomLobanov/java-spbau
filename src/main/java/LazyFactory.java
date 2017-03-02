import com.sun.istack.internal.NotNull;
import java.util.function.Supplier;

/**
 * Special class which allows you to create different implementations of Lazy
 */
final class LazyFactory {

    /**
     * Private constructor to forbid to create instances of this class, because it's senseless
     */
    private LazyFactory(){}

    /**
     * This method allows you to create instances of Lazy, which works
     * correctly in case of single-threaded execution
     *
     * @param supplier Supplier which will be wrapped
     * @param <T> Type of result, which supplier returns
     * @return instance of Lazy, which is designed for single-threaded execution
     */
    @NotNull
    static <T> Lazy<T> createSimpleLazy(@NotNull Supplier<T> supplier) {
        return new SimpleLazy<>(supplier);
    }

    /**
     * This method allows you to create instances of Lazy, which works
     * correctly in case of multi-threaded execution.
     * Its guaranteed, that get-method of supplier will be invoked at most once
     *
     * @param supplier Supplier which will be wrapped
     * @param <T> Type of result, which supplier returns
     * @return instance of Lazy, which is safe in case of multi-threaded execution
     */
    @NotNull
    static <T> Lazy<T> createUniqueCreationLazy(@NotNull Supplier<T> supplier) {
        return new UniqueCreationLazy<>(supplier);
    }

    /**
     * This method allows you to create instances of Lazy, which works
     * correctly in case of multi-threaded execution.
     * It isn't guaranteed, that get-method of supplier will be invoked at most once,
     * but still guaranteed, that get-method of returned Lazy will always return the same object
     *
     * @param supplier Supplier which will be wrapped
     * @param <T> Type of result, which supplier returns
     * @return instance of Lazy, which is safe and lock-free in case of multi-threaded execution
     */
    @NotNull
    static <T> Lazy<T> createLockFreeLazy(@NotNull Supplier<T> supplier) {
        return new LockFreeLazy<>(supplier);
    }
}
