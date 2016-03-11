package opt.test.Knapsack;

import dist.DiscreteDependencyTree;
import dist.DiscreteUniformDistribution;
import dist.Distribution;
import opt.*;
import opt.example.KnapsackEvaluationFunction;
import opt.ga.*;
import opt.prob.GenericProbabilisticOptimizationProblem;
import opt.prob.MIMIC;
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
public class GA {
    /**
     * Random number generator
     */
    private static final Random random = new Random();
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

    public static final int LOOPS = 100;
    /**
     * The test main
     *
     * @param args ignored
     */
    public static void main(String[] args) {

        int iterLow = Integer.parseInt(args[0]);

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
        MutationFunction mf = new DiscreteChangeOneMutation(ranges);
        CrossoverFunction cf = new UniformCrossOver();
        GeneticAlgorithmProblem gap = new GenericGeneticAlgorithmProblem(ef, odd, mf, cf);


        double start = System.nanoTime();
        RunningStats rs = new RunningStats();
        for (int i = 0; i < LOOPS; i++) {
            StandardGeneticAlgorithm ga = new StandardGeneticAlgorithm(200, 150, 25, gap);
            FixedIterationTrainer fit = new FixedIterationTrainer(ga, iterLow);
            fit.train();
            rs.push(ef.value(ga.getOptimal()));
        }

        double end = System.nanoTime();
        double trainingTime = end - start;
        trainingTime /= (Math.pow(10, 9) * LOOPS);
        System.out.println("algo, iterations, parameters, mean, standard_deviation, average time");
        System.out.println("GA," + iterLow + ",," + rs.mean() + "," + rs.standardDeviation() + "," + trainingTime);

    }

}
