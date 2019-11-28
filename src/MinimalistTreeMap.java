/**
 * Interface for maps that are tree-backed, in order to be able to access size and height.
 * @param <K>
 * @param <V>
 */
public interface MinimalistTreeMap<K,V> extends MinimalistMap<K,V> {
    int size();
    int height();
}
