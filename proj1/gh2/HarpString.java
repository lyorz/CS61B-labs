package gh2;

// TODO: uncomment the following import once you're ready to start this portion
import deque.Deque;
// TODO: maybe more imports
import deque.LinkedListDeque;

//Note: This file will not compile until you complete the Deque implementations
public class HarpString {
    /** Constants. Do not change. In case you're curious, the keyword final
     * means the values cannot be changed at runtime. We'll discuss this and
     * other topics in lecture on Friday. */
    private static final int SR = 44100;      // Sampling Rate
    private static final double DECAY = .996; // energy decay factor

    /* Buffer for storing sound data. */
    private Deque<Double> buffer;

    /* Create a guitar string of the given frequency.  */
    public HarpString(double frequency) {
        int capacity=(int)Math.round(SR/frequency);
        buffer= new LinkedListDeque<>();
        for(int i=0;i<capacity;++i){
            buffer.addFirst(0.0);
        }
    }

    private boolean repeated(double d){
        for(int i=buffer.size()-1;i>=0;--i){
            if(buffer.get(i)==d){
                return true;
            }
            else if(buffer.get(i)==0.0){
                break;
            }
        }
        return false;
    }
    /* Pluck the guitar string by replacing the buffer with white noise. */
    public void pluck() {
        //       Make sure that your random numbers are different from each
        //       other. This does not mean that you need to check that the numbers
        //       are different from each other. It means you should repeatedly call
        //       Math.random() - 0.5 to generate new random numbers for each array index.

        int BufferSize=buffer.size();
        for(int i=0;i< BufferSize;++i){
            double r=Math.random()-0.5;
            while(repeated(r)){
                r=Math.random()-0.5;
            }

            buffer.removeLast();
            buffer.addFirst(r);
        }
    }

    /* Advance the simulation one time step by performing one iteration of
     * the Karplus-Strong algorithm.
     */
    public void tic() {
        double front=buffer.removeFirst();
        double next=buffer.get(0);
        double newDouble=-(front+next)/2*DECAY;
        buffer.addLast(newDouble);
    }

    /* Return the double at the front of the buffer. */
    public double sample() {
        // TODO: Return the correct thing.
        return buffer.get(0);
    }
}