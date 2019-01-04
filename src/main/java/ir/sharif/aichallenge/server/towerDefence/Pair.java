package ir.sharif.aichallenge.server.towerDefence;

/**
 * Created by Future on 2/6/18.
 */
public class Pair<K, V> {

    private K key;
    private V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }


    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }
}
