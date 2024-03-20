package flik;


import org.junit.Assert;
import org.junit.Test;


public class FlikTest {
    @Test
    public void stevesTest(){
        for(int i=0,j=0;i<500;++i,++j){
            Assert.assertTrue(Flik.isSameNumber(i,j));
        }
    }
    @Test
    public void testSpecificVal(){
        Integer a = 128;
        Integer b = 128;
        Assert.assertEquals(a,b);
    }
}
