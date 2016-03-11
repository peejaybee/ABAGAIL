package opt.test.CountOnes;

import dist.DiscreteDependencyTree;
import dist.DiscreteUniformDistribution;
import dist.Distribution;
import opt.*;
import opt.example.CountOnesEvaluationFunction;
import opt.ga.*;
import opt.prob.GenericProbabilisticOptimizationProblem;
import opt.prob.MIMIC;
import opt.prob.ProbabilisticOptimizationProblem;
import shared.FixedIterationTrainer;
import util.RunningStats;

import java.util.Arrays;

/**
 * 
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class SA {
    /** The n value */
    private static final int N = 80;
    public static final int LOOPS = 100;

    public static void main(String[] args) {

        int iterLow = Integer.parseInt(args[0]);
        double temp = Double.parseDouble(args[1]);
        double cool = Double.parseDouble(args[2]);

        int[] ranges = new int[N];
        Arrays.fill(ranges, 2);
        EvaluationFunction ef = new CountOnesEvaluationFunction();
        Distribution odd = new DiscreteUniformDistribution(ranges);
        NeighborFunction nf = new DiscreteChangeOneNeighbor(ranges);
        HillClimbingProblem hcp = new GenericHillClimbingProblem(ef, odd, nf);

        RunningStats rs = new RunningStats();
        double start = System.nanoTime();
        for (int i = 0; i < LOOPS; i++) {
            SimulatedAnnealing sa = new SimulatedAnnealing(temp, cool, hcp);
            FixedIterationTrainer fit = new FixedIterationTrainer(sa, iterLow);
            fit.train();
            rs.push(ef.value(sa.getOptimal()));
        }
        double end = System.nanoTime();
        double trainingTime = end - start;
        trainingTime /= (Math.pow(10, 9) * LOOPS);
        System.out.println("algo, iterations, parameters, mean, standard_deviation, average time");
        System.out.println("SA," + iterLow + ",temp " + temp + "cool " + cool + "," + rs.mean() + "," + rs.standardDeviation() + "," + trainingTime);
        
    }
}