package util;

/**
 * Created by pbirmingham on 3/9/16.
 * Adapted from C++ implementation of Welford's method
 * published by John D. Cook http://www.johndcook.com/blog/standard_deviation/
 */

public class RunningStats {

    private int n;
    private double oldM;
    private double newM;
    private double oldS;
    private double newS;

    public RunningStats() {
        n = 0;
    }

    public void clear() {
        n = 0;
    }

    public void push(double x) {
        n++;

        // See Knuth TAOCP vol 2, 3rd edition, page 232
        if (n == 1) {
            oldM = newM = x;
            oldS = 0.0;
        } else {
            newM = oldM + (x - oldM) / n;
            newS = oldS + (x - oldM) * (x - newM);

            // set up for next iteration
            oldM = newM;
            oldS = newS;
        }
    }

    public int numDataValues() {
        return n;
    }

    public double mean() {
        return (n > 0) ? newM : 0.0;
    }

    public double variance() {
        return ((n > 1) ? newS / (n - 1) : 0.0);
    }

    public double standardDeviation() {
        return Math.sqrt(variance());
    }
}
