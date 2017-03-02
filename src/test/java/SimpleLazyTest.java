import org.junit.Test;
import org.junit.runners.JUnit4;
import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

public class SimpleLazyTest {
    @Test
    public void simpleTest() {
        TestSupplier<Object> supplier = new TestSupplier<>(Object::new, 0);
        TestTemplates.runSingleThreadTest(LazyFactory.createSimpleLazy(supplier));
        assertEquals(1, supplier.getCounter());
    }

    @Test
    public void nullResultTest() {
        TestSupplier<Object> supplier = new TestSupplier<>(() -> null, 0);
        TestTemplates.runSingleThreadTest(LazyFactory.createSimpleLazy(supplier));
        assertEquals(1, supplier.getCounter());
    }


    @Test
    public void lazyTest() {
        TestSupplier<Object> supplier = new TestSupplier<>(Object::new, 0);
        LazyFactory.createSimpleLazy(supplier);
        assertEquals(0, supplier.getCounter());
    }
}