import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;

import java.util.Objects;
import java.util.function.Supplier;

public class UniqueCreationLazy<T> implements Lazy<T> {

    private volatile Supplier<T> supplier;
    private volatile T result;

    UniqueCreationLazy(@NotNull Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * It isn guaranteed, that get-method of supplier will be invoked at most once.
     * Also this method will always return the same object
     *
     * @return object, created by wrapped supplier
     */
    @Nullable
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
