package timingtest;
import edu.princeton.cs.algs4.Stopwatch;
import org.checkerframework.checker.units.qual.A;

/**
 * Created by hug.
 */
public class TimeSLList {
    private static void printTimingTable(AList<Integer> Ns, AList<Double> times, AList<Integer> opCounts) {
        System.out.printf("%12s %12s %12s %12s\n", "N", "time (s)", "# ops", "microsec/op");
        System.out.printf("------------------------------------------------------------\n");
        for (int i = 0; i < Ns.size(); i += 1) {
            int N = Ns.get(i);
            double time = times.get(i);
            int opCount = opCounts.get(i);
            double timePerOp = time / opCount * 1e6;
            System.out.printf("%12d %12.2f %12d %12.2f\n", N, time, opCount, timePerOp);
        }
    }

    public static void main(String[] args) {
        timeGetLast();
    }

    public static void timeGetLast() {
        // TODO: YOUR CODE HERE
        AList<Integer> Ns=new AList<>();
        AList<Integer> ops=new AList<>();
        for(int i=0;i<=7;++i){
            Ns.addLast((int)Math.pow(2,i)*1000);
            ops.addLast(10000);
        }

        AList<Double> times=new AList<>();


        for(int i=0;i<=7;++i){
            int N = Ns.get(i);
            int M=ops.get(i);
            // create a sllist
            SLList<Integer> test = new SLList<>();
            // add N items to the sllist
            for (int n = 0; n < N; ++n) {
                test.addLast(n);
            }
            // start the timer
            Stopwatch sw=new Stopwatch();
            // perform getLast operations on the sllist
            for(int m =0;m<M;++m){
                test.getLast();

            }
            times.addLast(sw.elapsedTime());
        }

        printTimingTable(Ns,times,ops);


    }

}
