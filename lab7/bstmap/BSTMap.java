package bstmap;

import edu.princeton.cs.algs4.BST;

import java.util.Iterator;
import java.util.Set;

public class BSTMap<K extends Comparable<K>, V> implements Map61B<K, V>{
    private int size;
    private BSTNode node;
    private class BSTNode {
        private V v;
        private K k;
        private BSTNode left;
        private BSTNode right;
        public BSTNode(K key, V val){
            k = key;
            v = val;
            left = null;
            right = null;
        }
    }
    public BSTMap(){
        size = 0;
        node = null;
    }
    @Override
    public void clear() {
        size = 0;
        node = null;
    }

    /* Returns true if this map contains a mapping for the specified key. */
    @Override
    public boolean containsKey(K key) {
        if (node == null) { return false; }

        BSTNode n = node;
        while(n != null) {
            if (key.compareTo(n.k) == 0) { return true; }
            else if (key.compareTo(n.k) < 0) { n = n.left; }
            else { n = n.right; }
        }

        return false;
    }

    /* Returns the value to which the specified key is mapped, or null if this
     * map contains no mapping for the key.
     */
    @Override
    public V get(K key) {
        if (node == null) { return null; }

        BSTNode n = node;
        while(n != null) {
            if (key.compareTo(n.k) == 0) { return n.v; }
            else if (key.compareTo(n.k) < 0) { n = n.left; }
            else { n = n.right; }
        }

        return null;
    }

    @Override
    public int size() {
        return size;
    }

    /* Associates the specified value with the specified key in this map. */
    @Override
    public void put(K key, V value) {
        BSTNode newNode = new BSTNode(key, value);
        BSTNode n = node;
        size++;
        while (n != null) {
            if (key.compareTo(n.k) < 0) {
                if (n.left == null) {
                    n.left = newNode;
                    return;
                }
                if (key.compareTo(n.left.k) < 0) {
                    n = n.left;
                }
                else {
                    newNode.left = n.left;
                    n.left = newNode;
                    return;
                }
            }
            else {
                if (n.right == null) {
                    n.right = newNode;
                    return;
                }
                if (key.compareTo(n.right.k) > 0) {
                    n = n.right;
                }
                else {
                    newNode.right = n.right;
                    n.right = newNode;
                    return;
                }
            }
        }
        node = newNode;
    }

    public void printInOrder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set keySet() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(K key, V value) {
        throw new UnsupportedOperationException();
    }


    @Override
    public Iterator iterator() {
        throw new UnsupportedOperationException();
    }
}
