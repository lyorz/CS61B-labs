package deque; 

import edu.princeton.cs.algs4.StdRandom; 
import org.junit.Assert; 
import org.junit.Test; 

public class RandomDequeTest {
    @Test
    public void randomizedTest ( ){
        Deque<Integer> L  =  new ArrayDeque<> ( ); 
        Deque<Integer> BL = new LinkedListDeque<> ( ); 

        int N  =  5000; 
        for  ( int i  =  0;  i < N;  i +=  1) {
            int operationNumber  =  StdRandom.uniform ( 0, 4); 
            if  ( operationNumber  ==  0) {
                // addLast
                int randVal  =  StdRandom.uniform ( 0, 100); 
                L.addLast ( randVal); 
                BL.addLast ( randVal); 
            } else if  ( operationNumber  ==  1) {
                // get
                if ( L.size ( ) == 0){
                    continue; 
                }
                int idx  =  StdRandom.uniform ( 0,L.size ( )); 
                Assert.assertEquals ( L.get ( idx),BL.get ( idx)); 
            } else if  ( operationNumber  ==  2 ){
                // addFirst
                int randVal  =  StdRandom.uniform ( 0, 100); 
                L.addFirst ( randVal); 
                BL.addFirst ( randVal); 
            }
            else{
                if ( L.size ( )>0){
                    L.removeLast ( ); 
                    BL.removeLast ( ); 
                }
            }
        }
    }
}
