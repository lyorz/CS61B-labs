package deque;  
import java.util.Iterator;  
public class ArrayDeque<T> implements Deque<T>, Iterable<T> {
    private Object[] items;  
    private int start;        //start指向队列第一个元素
    private int end;          //end指向队列最后一个元素

    private int capacity;  
    private int size;  

    // 构造函数
    public ArrayDeque ( ) {
        items = new Object[8];  
        capacity = 8;  
        size = 0;  
        start = 0;  
        end = 0;  
    }
    // 数组扩容/缩容
    private void resize (  int newCapacity ) {
        Object[] newItems = new Object[newCapacity];  

        // case1: 0:null,1:null,2:start,...5:end,6:null,7:null
        if  (  start <= end ) {
            for  (  int i = 0;  i<size;  ++ i ) {
                newItems[i] = items[i + start];  
            }
        }
        // case2: 0:x,1:y,...3:end,...,6:start,7:z
        else {
            for  (  int i = 0;  i<capacity - start;  ++i ) {
                newItems[i] = items[i + start];  
            }

            for  (  int i = 0;  i <= end;   ++i ) {
                newItems[capacity - start + i] = items[i];  
            }
        }
        start = 0;  
        end = size - 1;  
        capacity = newCapacity;  
        items = newItems;  
    }
    // 队首添加item
    public void addFirst (  T item ) {
        // 队列为空时直接添加
        if  (  size == 0 ) {
            items[start] = item;  
            size++ ;  
            return;  
        }
        // 数组已满 扩容
        if  (  size == capacity ) {
            resize (  capacity * 2 );  
        }

        // 情况一: 0:start...4:end...7:null
        if  (  start == 0 ) {
            start = capacity - 1;  //0:x...4:end...7:start
        }
        // 情况二：0:null,1:null,...3:start,4:end,....7:null
        // 或者：  0:x,1:y,2:end,....5:null,6:start,7:z
        else  {
            start -- ;  //0:x,1:y,2:end,....5:start,6:m,7:z
        }

        items[start] = item;  
        size ++ ;  
    }
    // 队尾添加item
    public void addLast (  T item ) {
        // 队列为空时直接添加
        if  (  size == 0 ) {
            items[end] = item;  
            size ++ ;  
            return;  
        }

        // 数组已满 扩容
        if  (  size == capacity ) {
            resize (  capacity * 2 );  
        }

        // case1 0:null,1:null,...3:start,4:x,....7:end
        if  (  end == capacity - 1 ) {
            end = 0;  
        }
        // case2 0:null,1:null,...3:start,4:x,5:end,...7:null
        // or    0:x,1:end,...5:start,6:y,7:z
        else  {
            end ++ ;  
        }

        items[end] = item;  
        size ++ ;  
    }

    // 返回队列元素数
    public int size ( ) { return size;  }
    // 从前向后打印元素 以空格分隔
    // 打印完成后打印一个新行
    public void printDeque ( ) {
        for  (  int i = 0;  i<size;  ++ i ) {
            System.out.print (  get (  i ) + " " );  
        }
        System.out.println ( );  
    }
    // 删除并返回队首元素 不存在返回null
    public T removeFirst ( ) {
        if  (  isEmpty ( ) ) {
            return null;  
        }

        // 如果占用率低于25 数组缩容
        if  (  capacity>= 16 && size <  (   capacity / 4  ) ) {
            resize (  capacity / 4 );  
        }

        size -- ;  
        T firstItem =   (  T ) items[start];  

        // case3: 0:start end,1:null....7:null 不做处理
        if  (  start == end ) {
            return firstItem;  
        }

        // case1: 0:x,1:y,2:end,...7:start
        if  (  start == capacity - 1 ) {
            start = 0;  // 0:start (  x ),1:y,2:z,....7:null
        }
        // case2: 0:start,1:x,2:end,...7:null
        // or:    0:x,1:end,...6:start,7:y
        else  {
            start ++ ;  // 0:null,1:start (  x ),2:end,...7:null
                    // 0:x,1:end,...6:null,7:start (  y )
        }


        return firstItem;  
    }
    // 删除并返回队尾元素 不存在返回null
    public T removeLast ( ) {
        if  (  isEmpty ( ) ) {
            return null;  
        }

        // 如果占用率低于25 数组缩容
        if  (  capacity>= 16 && size< (  capacity/4 ) ) {
            resize (  capacity/4 );  
        }

        size -- ;  
        T firstItem =   (  T ) items[end];  

        // case3: 0:start end,1:null....7:null 不做处理
        if  (  start == end ) {
            return firstItem;  
        }

        // case1: 0:end,....6:start,7:x
        if  (  end == 0 ) {
            end = capacity - 1;  
        }
        // case2: 0:start,1:x,2:end,...7:null
        // or:    0:x,1:end,...6:start,7:y
        else  {
            end -- ;  // 0:start,1:end (  x ),2:null,...7:null
                    // 0:x,1:z,2:end...6:start,7:y
        }

        return firstItem;  
    }
    // 获取给定索引处元素 不存在返回null
    public T get (  int index ) {
        if  (  isEmpty ( ) ) {
            return null;  
        }

        // 记录索引元素在items中真正的索引
        int realIndex;  
        // case1: 0:start,1:x,2:y,....5:end,...7:null
        // or:    0:null,1:null,2:start,....5:end,...7:null
        if  (  start <= end ) {
            realIndex = index + start;  
        }
        // case2: 0:x,2:end,....5:start,6:
        else  {
            if  (  index + start < capacity ) {
                realIndex = index + start;  
            }
            else  {
                realIndex = index -  (  capacity - start );  
            }
        }
        // 判断realIndex是否合法
        if  (  indexIsValid (  realIndex ) ) {
            return  (  T ) items[realIndex];  
        }
        return null;  
    }
    // 补充方法：判断索引是否合法
    private boolean indexIsValid (  int index ) {
        if  (  size == 0 ) return false;  
        // case1: 0:start,1:x,2:y,....5:end,...7:null
        // or:    0:null,1:null,2:start,....5:end,...7:null
        if  (  start <= end ) {
            if  (  index >= start && index <= end ) {
                return true;  
            }
        }
        // case2: 0:x,2:end,....5:start,6:
        else  {
            if  (   (  index>= start && index<capacity ) ||  (  index >= 0 && index <= end ) ) {
                return true;  
            }
        }
        return false;  
    }

    // 迭代器实现：
    // step1：定义迭代器类
    private class ArrayDequeIterator implements Iterator<T>{
        private int wizPos;  
        public ArrayDequeIterator ( ) {
            wizPos = start;  
        }
        public boolean hasNext ( ) {
            if  (  indexIsValid (  wizPos ) ) {
                return true;  
            }
            return false;  
        }

        public T next ( ) {
            T returnItem =  (  T ) items[wizPos];  
            // case1: 0:start,1:x,2:y,....5:end,...7:null
            // or:    0:null,1:null,2:start,....5:end,...7:null
            if  (  start <= end ) {
                wizPos ++ ;  
            }
            // case2: 0:x,2:end,....5:start,6:
            else  {
                if  (  wizPos == capacity - 1 ) {
                    wizPos = 0;  
                }
                else{
                    wizPos ++ ;  
                }
            }
            return returnItem;  
        }
    }
    // step2: 实现iterator方法
    public Iterator<T> iterator ( ) {
        return new ArrayDequeIterator ( );  
    }

    // 判断对象是否是某一个类的实例
    @Override
    public boolean equals (  Object o ) {
        if  (  o == null ) {return false;  }
        if  (  ! (  o instanceof Deque ) ) {return false;  }
        if  (  o == this ) {return true;  }

        Deque<T> others =  (  Deque<T> ) o;  
        if  (  others.size ( ) != size ) {return false;  }

        for  (  int i = 0;   i<size;   ++ i ) {
            if  (  !others.get (  i ).equals (  get (  i ) ) ) {
                return false;  
            }
        }
        return true;  
    }
}
