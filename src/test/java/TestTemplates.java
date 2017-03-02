import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

class TestTemplates {
    static final int TESTS_NUMBER = 10;

    static <T> void runSingleThreadTest(Lazy<T> lazy) {
        Object result = lazy.get();
        for (int i = 1; i < TESTS_NUMBER; i++) {
            assertSame(result, lazy.get());
        }
    }

    static <T> void runMultiThreadTest(Lazy<T> lazy) {
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
