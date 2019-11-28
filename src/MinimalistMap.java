/**
 * Interface for the 3 map methods that are used in computation of similarity in Lab3.
 * @param <K> Key
 * @param <V> Value
 */
public interface MinimalistMap<K,V> {
    V get(K key);
    void put(K key,V value);
    Iterable<K> keys();
}
