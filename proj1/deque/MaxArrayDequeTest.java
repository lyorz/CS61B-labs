package deque;

import net.sf.saxon.expr.Component;
import org.junit.Assert;
import org.junit.Test;

import java.util.Comparator;

public class MaxArrayDequeTest {
    @Test
    public void MaxItemTest(){
        Comparator<Integer> cInt=new Comparator<Integer>() {
            @Override
            public int compare(Integer o1, Integer o2) {
                return 0;
            }
        };
        Comparator<String> cStr=new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return 0;
            }
        };

        MaxArrayDeque<Integer> mad1=new MaxArrayDeque<>(cInt);
        mad1.addFirst(10);
        mad1.addFirst(15);
        mad1.addFirst(5);
        mad1.addFirst(0);
        mad1.addFirst(16);
        Assert.assertEquals(16,(int)mad1.max());

        MaxArrayDeque<String> mad2=new MaxArrayDeque<>(cStr);
        mad2.addFirst("Hello");
        mad2.addFirst("Java");
        mad2.addFirst("Cpp");
        mad2.addFirst("Python");
        Assert.assertEquals("Python",mad2.max());

    }
}
