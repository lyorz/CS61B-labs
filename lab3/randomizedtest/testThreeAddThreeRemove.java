package randomizedtest;
import org.junit.Assert;
import org.junit.Test;
public class testThreeAddThreeRemove {
    @Test
    public void testThreeAddThreeRemove(){
        AListNoResizing<Integer> correct=new AListNoResizing<>();
        BuggyAList<Integer> buggy=new BuggyAList<>();

        correct.addLast(5);
        correct.addLast(10);
        correct.addLast(15);

        buggy.addLast(5);
        buggy.addLast(10);
        buggy.addLast(15);

        Assert.assertEquals(correct.size(),buggy.size());

        Assert.assertEquals(correct.removeLast(),buggy.removeLast());
        Assert.assertEquals(correct.removeLast(),buggy.removeLast());
        Assert.assertEquals(correct.removeLast(),buggy.removeLast());
    }
}
