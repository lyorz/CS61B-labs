package deque;

import java.util.Iterator;

public class LinkedListDeque<T> implements Deque<T>, Iterable<T> {

    private class LinkedNode{
        private T item;
        private LinkedNode last;
        private LinkedNode next;

        public LinkedNode(T i,LinkedNode n,LinkedNode l){
            item=i;
            next=n;
            last=l;
        }
    }
    private LinkedNode head_sentinel;
    private LinkedNode rear_sentinel;
    private int size;

    public LinkedListDeque(){
        rear_sentinel=new LinkedNode(null,null,null);
        head_sentinel=new LinkedNode(null,rear_sentinel,null);
        rear_sentinel.last=head_sentinel;
        size=0;
    }

    // 在双端队列前添加item
    public void addFirst(T item)
    {
        LinkedNode node=new LinkedNode(item,head_sentinel.next,head_sentinel);
        LinkedNode srcFirstNode=head_sentinel.next;

        srcFirstNode.last=node;
        head_sentinel.next=node;

        node.next=srcFirstNode;
        node.last=head_sentinel;

        size++;
    }
    // 在双端队列后添加item
    public void addLast(T item){
        LinkedNode node=new LinkedNode(item,rear_sentinel,rear_sentinel.last);
        LinkedNode srcLastNode=rear_sentinel.last;

        srcLastNode.next=node;
        rear_sentinel.last=node;

        node.last=srcLastNode;
        node.next=rear_sentinel;

        size++;
    }
    // 返回双端队列中项目数
    public int size(){
        return size;
    }

    // 从第一个到最后一个打印双端队列中的项目，以空格分割
    // 打印完后打印一个新行
    public void printDeque(){
        LinkedNode node=head_sentinel.next;
        for(int i=0;i<size;++i){
            System.out.print(node.item+" ");
            node=node.next;
        }
        System.out.println();
    }
    // 删除并返回双端队列前面的元素，如果不存在返回null
    public T removeFirst(){
        // head_sentinel.next=rear_sentinel return null
        if(size==0){
            return null;
        }
        T it=head_sentinel.next.item;
        // delete the first LinkedNode
        LinkedNode node=head_sentinel.next;
        head_sentinel.next=node.next;
        node.next.last=head_sentinel;

        node.last=null;
        node.next=null;
        size-=1;
        return it;
    }
    // 删除并返回双端队列后面的项目。如果不存在返回null
    public T removeLast(){
        if(size==0){
            return null;
        }

        T it=rear_sentinel.last.item;

        LinkedNode node=rear_sentinel.last;
        node.last.next=rear_sentinel;
        rear_sentinel.last=node.last;
        size--;
        node.last=null;
        node.next=null;

        return it;
    }
    // 迭代获取给定索引处的项目，0表示队首第一个元素（非sentinel）
    public T get(int index){
        // 检查非法索引情况
        if(index>=size || index<0){
            return null;
        }

        LinkedNode node=head_sentinel.next;
        for(int i=0;i<index;++i){
            node=node.next;
        }

        return node.item;
    }
    // 递归获取给定索引处的项目
    public T getRecursive(int index){
        // 检查非法索引情况
        if(index>=size || index<0){
            return null;
        }
        if(index==0){
            return head_sentinel.next.item;
        }
        LinkedListDeque<T> L=this.copy();
        L.removeFirst();
        return L.getRecursive(index-1);
    }

    // 拷贝构造一份和this所指对象相同的LinkedListDeque
    private LinkedListDeque copy(){
        LinkedListDeque L=new LinkedListDeque();
        LinkedNode node=head_sentinel.next;
        for(int i=0;i<size;++i){
            L.addLast(node.item);
            node=node.next;
        }
        return L;
    }

    private class LinkedListDequeIterator implements Iterator<T>{
        int wizPos;
        public LinkedListDequeIterator(){
            wizPos=0;
        }
        public boolean hasNext(){
            if(wizPos>=size){
                return false;
            }
            return true;
        }
        public T next(){
            T returnItem=get(wizPos);
            wizPos++;
            return returnItem;
        }
    }
    public Iterator<T> iterator(){
        return new LinkedListDequeIterator();
    }

    public boolean equals(Object o) {
        if (o == null) {
            return false;
        }
        if ( ! (o instanceof Deque ) ) {
            return false;
        }
        if ( o == this ) {
            return true;
        }
        Deque<T> others=(Deque<T>) o;
        if(others.size()!=size){
            return false;
        }

        for(int i=0;i<size;++i){
            if(!others.get(i).equals(get(i))){
                return false;
            }
        }

        return true;
    }

}
