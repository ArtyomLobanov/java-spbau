import java.util.Objects;
import java.util.function.Supplier;

public class UniqueCreationLazy<T> implements Lazy<T> {

    private volatile Supplier<T> supplier;
    private volatile T result;

    UniqueCreationLazy(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * It isn guaranteed, that get-method of supplier will be invoked at most once.
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
