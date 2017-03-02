import java.util.Objects;
import java.util.function.Supplier;

/**
 * Its implementation of Lazy, which is safe
 * in case of multi-threaded execution.
 * Also this implementation promises to run
 * calculations at most once.
 *
 * @param <T> Type of result, which wrapped supplier returns
 */
public class UniqueCreationLazy<T> implements Lazy<T> {

    private volatile Supplier<T> supplier;
    private volatile T result;

    UniqueCreationLazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * It isn guaranteed, that get-method of supplier
     * will be invoked at most once.
     * Also this method will always return the same object
     *
     * @return object, created by wrapped supplier
     */
    @Override
    public T get() {
        if (Objects.isNull(supplier)) {
            return result;
        }
        synchronized (this) {
            if (!Objects.isNull(supplier)) {
                result = supplier.get();
                supplier = null;
            }
        }
        return result;
    }
}
