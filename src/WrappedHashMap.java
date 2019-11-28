import java.util.HashMap;
import java.util.Map;

/**
 * HashMap backed implementation of MinimalistMap
 * @param <K>
 * @param <V>
 */
public class WrappedHashMap<K,V> implements MinimalistMap<K,V> {

    private Map<K,V> map=new HashMap<>();

    WrappedHashMap(){

    }

    @Override
    public V get(K key) {
        return map.get(key);
    }

    @Override
    public void put(K key, V value) {
        map.put(key,value);
    }

    @Override
    public Iterable<K> keys() {
        return map.keySet();
    }
}
