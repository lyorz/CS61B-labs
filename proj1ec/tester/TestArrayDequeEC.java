package tester;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Assert;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {
    @Test
    public void RandomTest(){
        StudentArrayDeque<Integer> sad = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> ads = new ArrayDequeSolution<>();

        Assert.assertTrue( sad.isEmpty() );

        int N=1000;

        for(int i = 0; i<N; ++i){
            int option = StdRandom.uniform(0,5);

            if( option == 0 ) {
                sad.addFirst(i);
                ads.addFirst(i);
            }
            else if( option == 1) {
                sad.addLast(i);
                ads.addLast(i);
            }
            else if ( option == 2 ) {
                if ( ads.isEmpty() || sad.isEmpty() ) { continue; }
                int idx = StdRandom.uniform(0, sad.size() );
                Integer expected = ads.get( idx );
                Integer actual = sad.get( idx );
                if ( actual != null ){
                    Assert.assertEquals( "Expect that the " + idx + "th " + "value equals "+ expected + ", but got " + actual + ".\n",
                            expected,
                            actual);
                }

            }
            else if ( option == 3 ) {
                if ( ads.isEmpty() || sad.isEmpty() ) { continue; }
                Integer expected = ads.removeFirst();
                Integer actual = sad.removeFirst();
                if ( actual != null ) {
                    Assert.assertEquals("Expect that the first value equals " + expected + ", but got " + actual + ".\n",
                            expected,
                            actual);
                }
            }
            else {
                if ( ads.isEmpty() || sad.isEmpty() ) { continue; }
                Integer expected = ads.removeLast();
                Integer actual = sad.removeLast();
                if ( actual != null ) {
                    Assert.assertEquals("Expect that the last value equals " + expected + ", but got " + actual + ".\n",
                            expected,
                            actual);
                }
            }
        }

        Integer expected = ads.size();
        Integer actual = sad.size();
        Assert.assertEquals( "Expect that size equals " + expected + ", but got " + actual + ".\n",
                expected,
                actual);
    }

}
