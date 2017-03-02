/**
 * Something like supplier which will not calculate
 * result until it is not requested.
 *
 * @param <T> type of result of calculations
 */
public interface Lazy<T> {
    /**
     * Run calculations. It's guaranteed, that
     * this method will always return the same value
     *
     * @return created value
     */
    T get();
}
