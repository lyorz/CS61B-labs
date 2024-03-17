package deque;

import org.checkerframework.checker.units.qual.A;
import org.junit.Assert;
import org.junit.Test;

import javax.swing.text.html.HTMLDocument;

import java.util.Iterator;

import static org.junit.Assert.*;


/** Performs some basic ArrayDeque tests. */
public class ArrayDequeTest  {

    @Test
    /** Adds a few things to the list, checking isEmpty() and size() are correct,
     * finally printing the results.
     *
     * && is the "and" operation. */
    public void addIsEmptySizeTest() {

        ArrayDeque<String> lld1 = new ArrayDeque<String>();

        assertTrue("A newly initialized LLDeque should be empty", lld1.isEmpty());
        lld1.addFirst("front");

        // The && operator is the same as "and" in Python.
        // It's a binary operator that returns true if both arguments true, and false otherwise.
        assertEquals(1, lld1.size());
        assertFalse("lld1 should now contain 1 item", lld1.isEmpty());

        lld1.addLast("middle");
        assertEquals(2, lld1.size());

        lld1.addLast("back");
        assertEquals(3, lld1.size());

        System.out.println("Printing out deque: ");
        lld1.printDeque();

    }

    @Test
    /** Adds an item, then removes an item, and ensures that dll is empty afterwards. */
    public void addRemoveTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        // should be empty
        assertTrue("lld1 should be empty upon initialization", lld1.isEmpty());

        lld1.addFirst(10);
        // should not be empty
        assertFalse("lld1 should contain 1 item", lld1.isEmpty());

        lld1.removeFirst();
        // should be empty
        assertTrue("lld1 should be empty after removal", lld1.isEmpty());
    }

    @Test
    /* Tests removing from an empty deque */
    public void removeEmptyTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<>();
        lld1.addFirst(3);

        lld1.removeLast();
        lld1.removeFirst();
        lld1.removeLast();
        lld1.removeFirst();

        int size = lld1.size();
        String errorMsg = "  Bad size returned when removing from empty deque.\n";
        errorMsg += "  student size() returned " + size + "\n";
        errorMsg += "  actual size() returned 0\n";

        assertEquals(errorMsg, 0, size);

    }

    @Test
    /* Check if you can create ArrayDeques with different parameterized types*/
    public void multipleParamTest() {

        ArrayDeque<String>  lld1 = new ArrayDeque<String>();
        ArrayDeque<Double>  lld2 = new ArrayDeque<Double>();
        ArrayDeque<Boolean> lld3 = new ArrayDeque<Boolean>();

        lld1.addFirst("string");
        lld2.addFirst(3.14159);
        lld3.addFirst(true);

        String s = lld1.removeFirst();
        double d = lld2.removeFirst();
        boolean b = lld3.removeFirst();

    }

    @Test
    /* check if null is return when removing from an empty ArrayDeque. */
    public void emptyNullReturnTest() {

        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();

        boolean passed1 = false;
        boolean passed2 = false;
        assertEquals("Should return null when removeFirst is called on an empty Deque,", null, lld1.removeFirst());
        assertEquals("Should return null when removeLast is called on an empty Deque,", null, lld1.removeLast());

    }

    @Test
    /* Add large number of elements to deque; check if order is correct. */
    public void bigLLDequeTest() {
        // 100w is a too big number for my computer.
        int testNum=50000;
        ArrayDeque<Integer> lld1 = new ArrayDeque<Integer>();
        for (int i = 0; i < testNum; i++) {
            lld1.addLast(i);
        }

        for (double i = 0; i < testNum/2; i++) {
            assertEquals("Should have the same value", i, (double) lld1.removeFirst(), 0.0);
        }

        for (double i = testNum-1; i > testNum/2; i--) {
            assertEquals("Should have the same value", i, (double) lld1.removeLast(), 0.0);
        }

    }

    @Test
    /* Check two types of get() */
    public void getTest(){
        ArrayDeque<Integer> lld1=new ArrayDeque<Integer>();
        for(int i=0;i<5;++i){
            lld1.addFirst(i);
        }
        lld1.printDeque();
        Assert.assertEquals(0,(int)lld1.get(4));
        Assert.assertEquals(5,5);
    }

    @Test
    public void fillUpEmptyFillUpAgain(){
        ArrayDeque<Integer> lld1=new ArrayDeque<>();
        for(int i=0;i<4;++i){
            lld1.addFirst(i);
        }
        for(int i=4;i<8;++i){
            lld1.addLast(i);
        }

        for(int i=0;i<8;++i){
            lld1.removeLast();
        }
        lld1.printDeque();

        for(int i=0;i<4;++i){
            lld1.addFirst(i);
        }
        for(int i=4;i<8;++i){
            lld1.addLast(i);
        }
        lld1.printDeque();
    }
    @Test
    public void EqualsTest(){
        ArrayDeque<Integer> l=new ArrayDeque<>();
        LinkedListDeque<Integer> l2=new LinkedListDeque<>();
        ArrayDeque<Integer> l3=new ArrayDeque<>();


        for(int i=0;i<5;++i){
            l.addLast(i);
            l3.addFirst(i);
        }
        ArrayDeque<Integer> l4=l;

        Assert.assertEquals(l.equals(l),true);
        Assert.assertEquals(l.equals(l3),false);
        Assert.assertEquals(l.equals(l),true);
        Assert.assertEquals(l.equals(l2),false);
    }

    @Test
    public void IteratorTest(){
        ArrayDeque<Integer> ad=new ArrayDeque<>();
        for(int i=0;i<1000;++i){
            ad.addLast(i);
        }

        int index=0;
        Iterator<Integer> it=ad.iterator();
        while(it.hasNext()){
            Assert.assertEquals(ad.get(index),it.next());
            index+=1;
        }

    }
}

