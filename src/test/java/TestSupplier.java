import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class TestSupplier<T> implements Supplier<T> {

    private final Supplier<T> realSupplier;
    private final long waitingTime;
    private final AtomicInteger counter;

    TestSupplier(Supplier<T> realSupplier, long waitingTime) {
        this.realSupplier = realSupplier;
        this.waitingTime = waitingTime;
        counter = new AtomicInteger();
    }

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

    int getCounter() {
        return counter.get();
    }
}
