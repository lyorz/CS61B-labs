package hashmap;

import org.w3c.dom.Node;

import java.util.*;

/**
 *  A hash table-backed Map implementation. Provides amortized constant time
 *  access to elements via get(), remove(), and put() in the best case.
 *
 *  Assumes null keys will never be inserted, and does not resize down upon remove().
 *  @author YOUR NAME HERE
 */
public class MyHashMap<K, V> implements Map61B<K, V> {
    /**
     * Protected helper class to store key/value pairs
     * The protected qualifier allows subclass access
     */
    protected class Node {
        K key;
        V value;

        Node(K k, V v) {
            key = k;
            value = v;
        }
    }

    /* Instance Variables */
    private Collection<Node>[] buckets;
    // You should probably define some more!
    private int size;
    private double MaxLoad;
    private int InitialSize;
    /** Constructors */
    public MyHashMap() {
        size = 0;
        MaxLoad = 0.75;
        InitialSize = 16;
        buckets = createTable(InitialSize);
    }

    public MyHashMap(int initialSize) {
        size = 0;
        InitialSize = initialSize;
        MaxLoad = 0.75;
        buckets = createTable(InitialSize);

    }

    /**
     * MyHashMap constructor that creates a backing array of initialSize.
     * The load factor (# items / # buckets) should always be <= loadFactor
     *
     * @param initialSize initial size of backing array
     * @param maxLoad maximum load factor
     */
    public MyHashMap(int initialSize, double maxLoad) {
        size = 0;
        InitialSize = initialSize;
        MaxLoad = maxLoad;
        buckets = createTable(InitialSize);
    }

    /**
     * Returns a new node to be placed in a hash table bucket
     */
    private Node createNode(K key, V value) {
        return new Node(key, value);
    }

    /**
     * Returns a data structure to be a hash table bucket
     *
     * The only requirements of a hash table bucket are that we can:
     *  1. Insert items (`add` method)
     *  2. Remove items (`remove` method)
     *  3. Iterate through items (`iterator` method)
     *
     * Each of these methods is supported by java.util.Collection,
     * Most data structures in Java inherit from Collection, so we
     * can use almost any data structure as our buckets.
     *
     * Override this method to use different data structures as
     * the underlying bucket type
     *
     * BE SURE TO CALL THIS FACTORY METHOD INSTEAD OF CREATING YOUR
     * OWN BUCKET DATA STRUCTURES WITH THE NEW OPERATOR!
     */
    protected Collection<Node> createBucket() { return new LinkedList<>(); }

    /**
     * Returns a table to back our hash table. As per the comment
     * above, this table can be an array of Collection objects
     *
     * BE SURE TO CALL THIS FACTORY METHOD WHEN CREATING A TABLE SO
     * THAT ALL BUCKET TYPES ARE OF JAVA.UTIL.COLLECTION
     *
     * @param tableSize the size of the table to create
     */
    private Collection<Node>[] createTable(int tableSize) {
        Collection<Node>[] table =  new Collection[tableSize];
        for (int i =0; i<tableSize; ++i) {
            table[i] = createBucket();
        }
        return table;
    }

    // TODO: Implement the methods of the Map61B Interface below
    // Your code won't compile until you do so!

    @Override
    public void clear() {
        buckets = createTable(InitialSize);
        size = 0;
    }

    /** Returns true if this map contains a mapping for the specified key. */
    @Override
    public boolean containsKey(K key) {
        Set<K> s = keySet();
        return s.contains(key);
    }

    /**
     * Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    @Override
    public V get(K key) {
        for (Collection<Node> bucket : buckets) {
            Iterator<Node> it = bucket.iterator();
            if (it.hasNext()) {
                Node n = it.next();
                if (key.equals(n.key)) {
                    return n.value;
                }
            }
        }
        return null;
    }

    /** Returns the number of key-value mappings in this map. */
    @Override
    public int size() {
        return size;
    }

    private void resizeBuckets(int newSize) {
        Collection<Node>[] newBuckets = createTable(newSize);
        int i = 0;
        for (Collection<Node> bucket : buckets) {
            Iterator<Node> it = bucket.iterator();
            while (it.hasNext()) {
                newBuckets[i].add(it.next());
            }
            i += 1;
        }
        buckets = newBuckets;
        InitialSize = newSize;
    }
    /**
     * Associates the specified value with the specified key in this map.
     * If the map previously contained a mapping for the key,
     * the old value is replaced.
     */
    @Override
    public void put(K key, V value) {
        double loadFactor = size/InitialSize;
        if (loadFactor > MaxLoad) {
            resizeBuckets(InitialSize*2);
        }

        Node newNode = createNode(key, value);
        for (Collection<Node> bucket : buckets) {
            if (bucket.isEmpty()) {
                bucket.add(newNode);
                size++;
                break;
            }
            else {
                Iterator<Node> it = bucket.iterator();
                Node n = it.next();
                if (key.equals(n.key)) {
                    n.value = value;
                    break;
                }
            }
        }

    }

    /** Returns a Set view of the keys contained in this map. */
    @Override
    public Set<K> keySet() {
        Set<K> s = new HashSet<>();
        for (Collection<Node> bucket : buckets) {
            Iterator<Node> it = bucket.iterator();
            if (it.hasNext()) {
                Node n = it.next();
                s.add(n.key);
            }
        }
        return s;
    }

    /**
     * Removes the mapping for the specified key from this map if present.
     * Not required for Lab 8. If you don't implement this, throw an
     * UnsupportedOperationException.
     */
    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Iterator<K> iterator() {
        throw new UnsupportedOperationException();
    }
}
