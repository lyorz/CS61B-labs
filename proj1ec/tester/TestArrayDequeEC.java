package tester;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Assert;
import org.junit.Test;
import student.StudentArrayDeque;

public class TestArrayDequeEC {
    private String getOperationsList(ArrayDequeSolution<String> a){
        String s= "";
        while(!a.isEmpty()){
            s=s+a.removeFirst();
        }
        return s;
    }
    @Test
    public void RandomTest(){
        StudentArrayDeque<Integer> sad = new StudentArrayDeque<>();
        ArrayDequeSolution<Integer> ads = new ArrayDequeSolution<>();
        ArrayDequeSolution<String> operations = new ArrayDequeSolution<>();

        Assert.assertTrue( sad.isEmpty() );

        int N=1000;

        for(int i = 0; i<N; ++i){
            int option = StdRandom.uniform(0,5);

            if( option == 0 ) {
                sad.addFirst(i);
                ads.addFirst(i);
                operations.addLast("addFirst(" + i + ")\n");
            }
            else if( option == 1) {
                sad.addLast(i);
                ads.addLast(i);
                operations.addLast("addLast(" + i + ")\n");
            }
            else if ( option == 2 ) {
                if ( ads.isEmpty() || sad.isEmpty() ) { continue; }
                int idx = StdRandom.uniform(0, sad.size() );
                Integer expected = ads.get( idx );
                Integer actual = sad.get( idx );
                operations.addLast("get(" + idx + ")\n");
                if( actual != null ) {
                    Assert.assertEquals(getOperationsList(operations),
                            expected,
                            actual);
                }
            }
            else if ( option == 3 ) {
                if ( ads.isEmpty() || sad.isEmpty() ) { continue; }
                Integer expected = ads.removeFirst();
                Integer actual = sad.removeFirst();
                operations.addLast("removeFirst()\n");
                if( actual != null ) {
                    Assert.assertEquals(getOperationsList(operations),
                            expected,
                            actual);
                }
            }
            else {
                if ( ads.isEmpty() || sad.isEmpty() ) { continue; }
                Integer expected = ads.removeLast();
                Integer actual = sad.removeLast();
                operations.addLast("removeLast()\n");
                if( actual != null ) {
                    Assert.assertEquals(getOperationsList(operations),
                            expected,
                            actual);
                }
            }
        }

        Integer expected = ads.size();
        Integer actual = sad.size();
        Assert.assertEquals( getOperationsList(operations),
                expected,
                actual);
    }

}
