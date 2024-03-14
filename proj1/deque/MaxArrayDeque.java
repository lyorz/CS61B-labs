package deque;

import java.util.Comparator;

public class MaxArrayDeque<T> extends ArrayDeque<T>{

    private final Comparator<T> comparator;
    public MaxArrayDeque(Comparator<T> c){
        super();
        comparator=c;
    }
    // 返回双端队列中最大元素
    public T max(){
        return max(comparator);
    }
    public T max(Comparator<T> c){
        if(isEmpty()){
            return null;
        }
        T maxItem=get(0);
        for(int i=0;i<size();++i){
            T currItem=get(i);
            if(c.compare(currItem,maxItem)>0){
                maxItem=currItem;
            }
        }
        return maxItem;
    }

}
