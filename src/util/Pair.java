package util;

/**
 * Generic interface for a key-value pair.
 *
 * @param <K> type of the key
 * @param <V> type of the value
 */
public interface Pair<K, V> {
    public K getKey();
    public V getValue();
}
