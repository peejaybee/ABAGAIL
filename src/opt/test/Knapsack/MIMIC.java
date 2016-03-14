package opt.test.Knapsack;

import dist.DiscreteDependencyTree;
import dist.DiscreteUniformDistribution;
import dist.Distribution;
import opt.*;
import opt.example.KnapsackEvaluationFunction;
import opt.ga.*;
import opt.prob.GenericProbabilisticOptimizationProblem;
import opt.prob.ProbabilisticOptimizationProblem;
import shared.FixedIterationTrainer;
import util.RunningStats;

import java.util.Arrays;
import java.util.Random;

/**
 * A test of the knap sack problem
 *
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class MIMIC {
    /**
     * Random number generator
     */
    private static final Random random = new Random(1);
    /**
     * The number of items
     */
    private static final int NUM_ITEMS = 40;
    /**
     * The number of copies each
     */
    private static final int COPIES_EACH = 4;
    /**
     * The maximum weight for a single element
     */
    private static final double MAX_WEIGHT = 50;
    /**
     * The maximum volume for a single element
     */
    private static final double MAX_VOLUME = 50;
    /**
     * The volume of the knapsack
     */
    private static final double KNAPSACK_VOLUME =
            MAX_VOLUME * NUM_ITEMS * COPIES_EACH * .4;

    public static final int LOOPS = 10;
    /**
     * The test main
     *
     * @param args ignored
     */
    public static void main(String[] args) {

        int iterLow = Integer.parseInt(args[0]);
        int samples = Integer.parseInt(args[1]);
        int tokeep = Integer.parseInt(args[2]);

        int[] copies = new int[NUM_ITEMS];
        Arrays.fill(copies, COPIES_EACH);
        double[] weights = new double[NUM_ITEMS];
        double[] volumes = new double[NUM_ITEMS];
        for (int i = 0; i < NUM_ITEMS; i++) {
            weights[i] = random.nextDouble() * MAX_WEIGHT;
            volumes[i] = random.nextDouble() * MAX_VOLUME;
        }
        int[] ranges = new int[NUM_ITEMS];
        Arrays.fill(ranges, COPIES_EACH + 1);
        EvaluationFunction ef = new KnapsackEvaluationFunction(weights, volumes, KNAPSACK_VOLUME, copies);
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
        System.out.println("MIMIC," + iterLow + ",Samples: " + samples + " tokeep: " + tokeep + "," + rs.mean() + "," + rs.standardDeviation() + "," + trainingTime);
    }
}
