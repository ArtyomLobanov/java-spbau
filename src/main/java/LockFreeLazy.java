import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.function.Supplier;

/**
 * Its implementation of Lazy, which is safe and
 * lock-free in case of multi-threaded execution
 *
 * @param <T> Type of result, which wrapped supplier returns
 */
public class LockFreeLazy<T> implements Lazy<T> {

    private static final Object NOT_CALCULATED = new Object();

    private static final AtomicReferenceFieldUpdater<LockFreeLazy, Object> RESULT_UPDATER =
            AtomicReferenceFieldUpdater.newUpdater(LockFreeLazy.class, Object.class, "result");

    private volatile Supplier<T> supplier;
    private volatile T result;

    LockFreeLazy(@NotNull Supplier<T> supplier) {
        this.supplier = supplier;
        // After compilation T will be replaced by Object,
        // so there is no problem to cast Object to T
        //noinspection unchecked
        result = (T) NOT_CALCULATED;
    }

    /**
     * It isn't guaranteed, that get-method of supplier will be invoked at most once,
     * but it's guaranteed, that this method will always return the same object
     *
     * @return object, created by wrapped supplier
     */
    @Nullable
    @Override
    public T get() {
        Supplier<T> currentSupplier = supplier;
        if (result != NOT_CALCULATED || Objects.isNull(currentSupplier)) {
            return result;
        }
        // supplier was cached to avoid NPE in next line
        T myResult = currentSupplier.get();
        RESULT_UPDATER.compareAndSet(this, NOT_CALCULATED, myResult);
        supplier = null;
        return result;
    }
}
