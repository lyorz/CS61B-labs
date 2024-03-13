package deque;

public class LinkedListDeque<T> {

    public class LinkedNode{
        public T item;
        public LinkedNode last;
        public LinkedNode next;

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
        head_sentinel.next.last=node;
        head_sentinel.next=node;
        size++;
    }
    // 在双端队列后添加item
    public void addLast(T item){
        LinkedNode node=new LinkedNode(item,rear_sentinel,rear_sentinel.last);
        rear_sentinel.last.next=node;
        rear_sentinel.last=node;
        size++;
    }
    // 判断双端队列是否为空，空则返回true
    public boolean isEmpty(){
        if(size==0){
            return true;
        }
        return false;
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
        // delete the first LinkedNode
        LinkedNode node=head_sentinel.next;
        head_sentinel.next=node.next;
        T it=node.item;
        node=null;
        size--;
        return it;
    }
    // 删除并返回双端队列后面的项目。如果不存在返回null
    public T removeLast(){
        if(size==0){
            return null;
        }

        LinkedNode node=rear_sentinel.last;
        node.last.next=rear_sentinel;
        rear_sentinel.last=node.last;
        size--;

        T it=node.item;
        node=null;
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

}
