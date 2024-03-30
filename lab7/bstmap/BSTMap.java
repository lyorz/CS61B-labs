package bstmap;

import edu.princeton.cs.algs4.BST;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Stack;

public class BSTMap<K extends Comparable<K>, V extends Comparable<V>> implements Map61B<K, V>{
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
        // 右子树最小节点
        public BSTNode findMin() {
            BSTNode n = right;
            while (n != null && n.left != null) {
                n = n.left;
            }
            return n;
        }
        public BSTNode findMax() {
            BSTNode n = left;
            while (n != null && n.right != null) {
                n = n.right;
            }
            return n;
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
            else if (key.compareTo(n.k) > 0) {
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
            else {
                if (value.compareTo(n.v) > 0) {
                    if (n.right == null) {
                        n.right = newNode;
                        return;
                    }
                    if (value.compareTo(n.right.v) > 0) {
                        n = n.right;
                    }
                    else {
                        newNode.right = n.right;
                        n.right = newNode;
                        return;
                    }
                }
                else {
                    if (n.left == null) {
                        n.left = newNode;
                        return;
                    }
                    if (value.compareTo(n.left.v) < 0) {
                        n = n.left;
                    }
                    else {
                        newNode.left = n.left;
                        n.left = newNode;
                        return;
                    }
                }
            }
        }
        node = newNode;
    }

    @Override
    public Set<K> keySet() {
        Set<K> s = new HashSet<>();
        BSTNode n = node;
        Stack<BSTNode> stk = new Stack<>();

        while (n != null || !stk.empty()) {
            while (n != null) {
                stk.add(n);
                n = n.left;
            }
            BSTNode topNode = stk.pop();
            s.add(topNode.k);
            n = topNode.right;
        }

        return s;
    }

    private BSTNode findParent(BSTNode child) {
        BSTNode parent = node;
        BSTNode c = node;
        while (c != null && c.k.compareTo(child.k) != 0 && c.v.compareTo(child.v) != 0) {
            parent = c;
            if (c.k.compareTo(child.k) > 0) {
                c = c.left;
            }
            else if (c.k.compareTo(child.k) < 0) {
                c = c.right;
            }
            else {
                if (c.v.compareTo(child.v) > 0) {
                    c = c.left;
                }
                else {
                    c = c.right;
                }
            }

        }
        return parent;
    }

    @Override
    public V remove(K key) {
        // 寻找待删除节点
        BSTNode deleteNode = node;
        BSTNode parent = node;
        while (deleteNode != null && key.compareTo(deleteNode.k) != 0) {
            parent = deleteNode;
            if (key.compareTo(deleteNode.k) > 0) {
                deleteNode = deleteNode.right;
            }
            else {
                deleteNode = deleteNode.left;
            }
        }
        if (deleteNode == null) {
            return null;
        }

        size--;
        V returnItem = deleteNode.v;
        BSTNode leftMax = deleteNode.findMax();
        BSTNode rightMin = deleteNode.findMin();

        // case1: deleteNode为叶子节点
        if (leftMax == null && rightMin == null) {
            // special case: 根节点为待删除节点
            if (key.compareTo(node.k) == 0) {
                node = null;
                return returnItem;
            }
            if (deleteNode.k.compareTo(parent.k) > 0) {
                parent.right = null;
            }
            else {
                parent.left = null;
            }
        }
        // case2: deleteNode有两棵子树
        else if (leftMax != null && rightMin != null) {
            // special case: 根节点为删除节点
            if (key.compareTo(node.k) == 0) {
                node = rightMin;
                rightMin.left = deleteNode.left;
                return returnItem;
            }
            // step0 找到rightMin或者leftMax的父节点
            BSTNode p = findParent(rightMin);
            // step1 右子树最小节点的右孩子（它一定没有左孩子）替代rightMin的位置
            // or    左子树最大节点的左孩子（它一定没有右孩子）替代leftMax的位置
            p.left = rightMin.right; // rightMin一定是p的左孩子
            // step2 rightMin/leftMax替代deleteNode的位置。
            if (deleteNode.k.compareTo(parent.k) > 0) {
                parent.right = rightMin;
            }
            else {
                parent.left = rightMin;
            }
            rightMin.left = deleteNode.left;
            rightMin.right = deleteNode.right;
            deleteNode.left = null;
            deleteNode.right = null;

        }
        // case3: deleteNode有一棵子树
        else {
            // special case: 根节点为待删除节点
            if (key.compareTo(node.k) == 0) {
                if (node.left != null) {
                    node = node.left;
                }
                else {
                    node = node.right;
                }
                return returnItem;
            }

            BSTNode newChild;
            if (leftMax != null) {
                newChild = deleteNode.left;
            }
            else {
                newChild = deleteNode.right;
            }
            if (deleteNode.k.compareTo(parent.k) > 0) {
                parent.right = newChild;
            }
            else {
                parent.left = newChild;
            }
        }
        return returnItem;
    }

    @Override
    public V remove(K key, V value) {
        // 寻找待删除节点
        BSTNode deleteNode = node;
        BSTNode parent = node;
        while (deleteNode != null && key.compareTo(deleteNode.k) != 0 && value.compareTo(deleteNode.v) != 0) {
            parent = deleteNode;
            if (key.compareTo(deleteNode.k) > 0) {
                deleteNode = deleteNode.right;
            }
            else if (key.compareTo(deleteNode.k) < 0){
                deleteNode = deleteNode.left;
            }
            else {
                if (value.compareTo(deleteNode.v) > 0) {
                    deleteNode = deleteNode.right;
                }
                else {
                    deleteNode = deleteNode.left;
                }
            }
        }
        if (deleteNode == null) {
            return null;
        }

        size--;
        V returnItem = deleteNode.v;
        BSTNode leftMax = deleteNode.findMax();
        BSTNode rightMin = deleteNode.findMin();

        // case1: deleteNode为叶子节点
        if (leftMax == null && rightMin == null) {
            // special case: 根节点为待删除节点
            if (key.compareTo(node.k) == 0) {
                node = null;
                return returnItem;
            }
            if (deleteNode.k.compareTo(parent.k) > 0) {
                parent.right = null;
            }
            else {
                parent.left = null;
            }
        }
        // case2: deleteNode有两棵子树
        else if (leftMax != null && rightMin != null) {
            // special case: 根节点为删除节点
            if (key.compareTo(node.k) == 0) {
                node = rightMin;
                rightMin.left = deleteNode.left;
                return returnItem;
            }
            // step0 找到rightMin或者leftMax的父节点
            BSTNode p = findParent(rightMin);
            // step1 右子树最小节点的右孩子（它一定没有左孩子）替代rightMin的位置
            // or    左子树最大节点的左孩子（它一定没有右孩子）替代leftMax的位置
            p.left = rightMin.right; // rightMin一定是p的左孩子
            // step2 rightMin/leftMax替代deleteNode的位置。
            if (deleteNode.k.compareTo(parent.k) > 0) {
                parent.right = rightMin;
            }
            else {
                parent.left = rightMin;
            }
            rightMin.left = deleteNode.left;
            rightMin.right = deleteNode.right;
            deleteNode.left = null;
            deleteNode.right = null;

        }
        // case3: deleteNode有一棵子树
        else {
            // special case: 根节点为待删除节点
            if (key.compareTo(node.k) == 0) {
                if (node.left != null) {
                    node = node.left;
                }
                else {
                    node = node.right;
                }
                return returnItem;
            }

            BSTNode newChild;
            if (leftMax != null) {
                newChild = deleteNode.left;
            }
            else {
                newChild = deleteNode.right;
            }
            if (deleteNode.k.compareTo(parent.k) > 0) {
                parent.right = newChild;
            }
            else {
                parent.left = newChild;
            }
        }
        return returnItem;
    }

    // step1: 定义迭代器类
    private class keySetIterator implements Iterator<K> {
        private int wizPos;
        private Set<K> s;
        public keySetIterator(){
            wizPos = 0;
            s = keySet();
        }
        public boolean hasNext() {
            return wizPos < size();
        }
        public K next() {
            Iterator it = s.iterator();
            K returnKey = null;
            int i = 0;
            while (i < wizPos) {
                i+=1;
                returnKey = (K) it.next();
            }
            wizPos++;
            return returnKey;
        }
    }
    // step2: 提供迭代器方法
    public Iterator<K> iterator() {
        return new keySetIterator();
    }

    public void printInOrder() {
        Set<K> ks = keySet();
        Iterator<K> it = iterator();

        for(K k : ks) {
            System.out.print(k + " ");
        }
        System.out.println();
    }
}
