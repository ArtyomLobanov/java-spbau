import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LockFreeLazyTest {

    @Test
    public void simpleTest() {
        TestSupplier<Object> supplier = new TestSupplier<>(Object::new, 0);
        TestTemplates.runSingleThreadTest(LazyFactory.createLockFreeLazy(supplier));
        assertTrue(supplier.getCounter() <= TestTemplates.TESTS_NUMBER);
    }

    @Test
    public void nullResultTest() {
        TestSupplier<Object> supplier = new TestSupplier<>(() -> null, 0);
        TestTemplates.runSingleThreadTest(LazyFactory.createLockFreeLazy(supplier));
        assertTrue(supplier.getCounter() <= TestTemplates.TESTS_NUMBER);
    }

    @Test
    public void lazyTest() {
        TestSupplier<Object> supplier = new TestSupplier<>(Object::new, 0);
        LazyFactory.createLockFreeLazy(supplier);
        assertEquals(0, supplier.getCounter());
    }

    @Test
    public void multiThreadMinorTimeWaitingTest() throws Exception {
        TestSupplier<Object> supplier = new TestSupplier<>(Object::new, 0);
        TestTemplates.runMultiThreadTest(LazyFactory.createLockFreeLazy(supplier));
        assertTrue(supplier.getCounter() <= TestTemplates.TESTS_NUMBER);
    }

    @Test
    public void multiThreadMajorTimeWaitingTest() throws Exception {
        TestSupplier<Object> supplier = new TestSupplier<>(Object::new, 250);
        TestTemplates.runMultiThreadTest(LazyFactory.createLockFreeLazy(supplier));
        assertTrue(supplier.getCounter() <= TestTemplates.TESTS_NUMBER);
    }

    @Test
    public void multiThreadNullResultTest() throws Exception {
        TestSupplier<Object> supplier = new TestSupplier<>(() -> null, 250);
        TestTemplates.runMultiThreadTest(LazyFactory.createLockFreeLazy(supplier));
        assertTrue(supplier.getCounter() <= TestTemplates.TESTS_NUMBER);
    }
}