package opt.test.FourPeaks;

import dist.DiscreteDependencyTree;
import dist.DiscreteUniformDistribution;
import dist.Distribution;
import opt.EvaluationFunction;
import opt.example.FourPeaksEvaluationFunction;
import opt.prob.GenericProbabilisticOptimizationProblem;
import opt.prob.ProbabilisticOptimizationProblem;
import shared.FixedIterationTrainer;
import util.RunningStats;

import java.util.Arrays;

/**
 * Copied from ContinuousPeaksTest
 *
 * @version 1.0
 */
public class MIMIC {
    /**
     * The n value
     */
    private static final int N = 200;
    /**
     * The t value
     */
    private static final int T = N / 5;
    public static final int LOOPS = 100;

    public static void main(String[] args) {

        int iterLow = Integer.parseInt(args[0]);
        int samples = Integer.parseInt(args[1]);
        int tokeep = Integer.parseInt(args[2]);

        int[] ranges = new int[N];
        Arrays.fill(ranges, 2);
        EvaluationFunction ef = new FourPeaksEvaluationFunction(T);
        Distribution odd = new DiscreteUniformDistribution(ranges);
        Distribution df = new DiscreteDependencyTree(.1, ranges);
        ProbabilisticOptimizationProblem pop = new GenericProbabilisticOptimizationProblem(ef, odd, df);

        double start = System.nanoTime();
        RunningStats rs = new RunningStats();
        for (int i = 0; i < LOOPS; i++) {
            opt.prob.MIMIC mimic = new opt.prob.MIMIC(samples, tokeep, pop);
            FixedIterationTrainer fit = new FixedIterationTrainer(mimic, iterLow);
            fit.train();
            rs.push(ef.value(mimic.getOptimal()));
        }
        double end = System.nanoTime();
        double trainingTime = end - start;
        trainingTime /= (Math.pow(10, 9) * LOOPS);
        System.out.println("algo, iterations, parameters, mean, standard_deviation, average time");
        System.out.println("MIMIC," + iterLow  + ",Samples: " + samples + " tokeep: " + tokeep + "," + rs.mean() + "," + rs.standardDeviation() + "," + trainingTime);
    }
}
