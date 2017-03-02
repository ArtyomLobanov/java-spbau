import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

/**
 * Special wrapper for Supplier, which count
 * the number of calls of get-method
 *
 * @param <T> type of result
 */
public class TestSupplier<T> implements Supplier<T> {

    private final Supplier<T> realSupplier;
    private final long waitingTime;
    private final AtomicInteger counter;

    /**
     * @param realSupplier Supplier to be wrapped
     * @param waitingTime delay to simulate long calculations
     */
    TestSupplier(Supplier<T> realSupplier, long waitingTime) {
        this.realSupplier = realSupplier;
        this.waitingTime = waitingTime;
        counter = new AtomicInteger();
    }

    /**
     * This method simulate long calculation and then
     * invoke get-method of wrapped supplier
     *
     * @return value, returned by wrapped supplier
     */
    @Override
    public T get() {
        counter.incrementAndGet();
        long startTime = System.currentTimeMillis();
        long cache;
        while ((cache = System.currentTimeMillis() - startTime) < waitingTime) {
            try {
                Thread.sleep(waitingTime - cache);
            } catch (InterruptedException ignored) {}
        }
        return realSupplier.get();
    }

    /**
     * This method allows you to check the number
     * of calls of get-method
     *
     * @return current value of counter
     */
    int getCounter() {
        return counter.get();
    }
}
