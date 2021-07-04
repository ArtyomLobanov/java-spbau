import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

/**
 * This class contains parts of code, which used in every test
 */
class TestTemplates {
    static final int TESTS_NUMBER = 10;

    /**
     * This method sequentially invokes get-method of tested
     * lazy TESTS_NUMBER times and compare results
     *
     * @param lazy instance to be tested
     */
    static void runSingleThreadTest(Lazy<?> lazy) {
        Object result = lazy.get();
        for (int i = 1; i < TESTS_NUMBER; i++) {
            assertSame(result, lazy.get());
        }
    }

    /**
     * This method invokes in parallel get-method of tested
     * lazy TESTS_NUMBER times and compare results
     *
     * @param lazy instance to be tested
     */
    static void runMultiThreadTest(Lazy<?> lazy) {
        CountDownLatch latch = new CountDownLatch(TESTS_NUMBER);
        Object[] results = new Object[TESTS_NUMBER];
        for (int i = 0; i < TESTS_NUMBER; i++) {
            int index = i;
            new Thread(() -> {
                results[index] = lazy.get();
                latch.countDown();
            }).start();
        }
        while(latch.getCount() != 0) {
            try {
                latch.await();
            } catch (InterruptedException ignored) {}
        }
        for (int i = 1; i < 10; i++) {
            assertSame(results[0], results[i]);
        }
    }
}
