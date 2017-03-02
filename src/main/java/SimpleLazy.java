import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Its implementation of Lazy, which is safe
 * in case of multi-threaded execution
 * Also this implementation ensure, that get-method
 * of wrapped supplier will be invoked at most once
 *
 * @param <T> Type of result, which wrapped supplier returns
 */
public class SimpleLazy<T> implements Lazy<T> {

    private Supplier<T> supplier;
    private T result;

    SimpleLazy(@NotNull Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * It is guaranteed, that get-method of supplier will
     * be invoked at most once and this method will always
     * return the same object in case of single-thread execution
     *
     * @return object, created by wrapped supplier
     */
    @Nullable
    @Override
    public T get() {
        if (!Objects.isNull(supplier)) {
            result = supplier.get();
            supplier = null;
        }
        return result;
    }
}
