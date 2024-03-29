package randomizedtest;
import edu.princeton.cs.algs4.StdRandom;
import org.junit.Assert;
import org.junit.Test;


public class randomizedTest {
    @Test
    public void randomizedTest(){
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> BL=new BuggyAList<>();

        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 3);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                BL.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                Assert.assertEquals(L.size(),BL.size());
            } else{
                if(L.size()>0){
                    L.removeLast();
                    BL.removeLast();
                }
            }
        }
    }
}
