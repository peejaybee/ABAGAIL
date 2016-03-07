package opt.test;

import opt.*;
import opt.example.*;
import opt.ga.*;
import shared.*;
import func.nn.backprop.*;

import java.util.*;
import java.io.*;
import java.text.*;

/**
 * Implementation of randomized hill climbing, simulated annealing, and genetic algorithm to
 * find optimal weights to a neural network that is classifying abalone as having either fewer 
 * or more than 15 rings. 
 *
 * @author Hannah Lau
 * @version 1.0
 */
public class AbaloneTest {

    private static int NUMBER_OF_TRAINING_INSTANCES = 500;   //small set
//    private static int NUMBER_OF_TRAINING_INSTANCES = 48842;   //full set
//    private static int NUMBER_OF_TRAINING_INSTANCES = 34110;
    private static int NUMBER_OF_TESTING_INSTANCES = 14732;
    private static int NUMBER_OF_ATTRIBUTES = 13;
//    private static String TRAINING_INSTANCE_FILENAME = "src/opt/test/adult_small.csv";
    private static String TRAINING_INSTANCE_FILENAME = "src/opt/test/Adult-normalized-small.csv";
//    private static String TRAINING_INSTANCE_FILENAME = "src/opt/test/Adult-normalized.csv";
    private static String TESTING_INSTANCE_FILENAME = "src/opt/test/adult_testing.csv";

    private static Instance[] trainingInstances = initializeInstances(NUMBER_OF_TRAINING_INSTANCES, TRAINING_INSTANCE_FILENAME);
//    private static Instance[] testingInstances = initializeInstances(NUMBER_OF_TESTING_INSTANCES, TESTING_INSTANCE_FILENAME);

    private static int inputLayer = NUMBER_OF_ATTRIBUTES, hiddenLayer = 10, outputLayer = 1, trainingIterations = 1000;
    private static BackPropagationNetworkFactory factory = new BackPropagationNetworkFactory();
    
    private static ErrorMeasure measure = new SumOfSquaresError();

    private static DataSet set = new DataSet(trainingInstances);

    private static BackPropagationNetwork networks[] = new BackPropagationNetwork[3];
    private static NeuralNetworkOptimizationProblem[] nnop = new NeuralNetworkOptimizationProblem[3];

    private static OptimizationAlgorithm[] oa = new OptimizationAlgorithm[3];
    private static String[] oaNames = {"RHC", "SA", "GA"};
    private static String results = "";

    private static DecimalFormat df = new DecimalFormat("0.000");

    public static void main(String[] args) {
        for(int i = 0; i < oa.length; i++) {
            networks[i] = factory.createClassificationNetwork(
                new int[] {inputLayer, hiddenLayer, outputLayer}, new func.nn.activation.LogisticSigmoid());
            nnop[i] = new NeuralNetworkOptimizationProblem(set, networks[i], measure);
        }

        oa[0] = new RandomizedHillClimbing(nnop[0]);
        oa[1] = new SimulatedAnnealing(1E11, .95, nnop[1]);
        oa[2] = new StandardGeneticAlgorithm(200, 100, 10, nnop[2]);

        for(int i = 0; i < oa.length; i++) {
            double start = System.nanoTime(), end, trainingTime, testingTime, correct = 0, incorrect = 0;
            train(oa[i], networks[i], oaNames[i]); //trainer.train();
            end = System.nanoTime();
            trainingTime = end - start;
            trainingTime /= Math.pow(10,9);

            Instance optimalInstance = oa[i].getOptimal();
            networks[i].setWeights(optimalInstance.getData());

            EvaluationResult er = evaluateNetwork(networks[i], trainingInstances);

            correct = er.confusionMatrix[0][0] + er.confusionMatrix[1][1];
            incorrect = er.confusionMatrix[0][1] + er.confusionMatrix[1][0];
            testingTime = er.testingTime;
            testingTime /= Math.pow(10,9);

            results +=  "\nResults for " + oaNames[i] + ": \nCorrectly classified " + correct + " instances." +
                        "\nIncorrectly classified " + incorrect + " instances.\nPercent correctly classified: "
                        + df.format(correct/(correct+incorrect)*100) + "%\nTraining time: " + df.format(trainingTime)
                        + " seconds\nTesting time: " + df.format(testingTime) + " seconds\n";

            results += "Confusion matrix\n" +
                    "\t0\t1\n"+
                    "0\t" + er.confusionMatrix[0][0] + "\t" + er.confusionMatrix[0][1] + "\t <---predicted 0\n" +
                    "1\t" + er.confusionMatrix[1][0] + "\t" + er.confusionMatrix[1][1] + "\t <---predicted 1\n";
        }

        System.out.println(results);
    }

    private static void train(OptimizationAlgorithm oa, BackPropagationNetwork network, String oaName) {
        System.out.println("\nError results for " + oaName + "\n---------------------------");

        for(int i = 0; i < trainingIterations; i++) {
            oa.train();

            double error = 0;
            for(int j = 0; j < trainingInstances.length; j++) {
                network.setInputValues(trainingInstances[j].getData());
                network.run();

                Instance output = trainingInstances[j].getLabel(), example = new Instance(network.getOutputValues());
                example.setLabel(new Instance(Double.parseDouble(network.getOutputValues().toString())));
                error += measure.value(output, example);
            }

            System.out.println(df.format(error));
        }
    }

    private static Instance[] initializeInstances(int numberOfInstances, String instanceFilename) {

        double[][][] attributes = new double[numberOfInstances][][];

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(instanceFilename)));

            for(int i = 0; i < attributes.length; i++) {
                Scanner scan = new Scanner(br.readLine());
                scan.useDelimiter(",");

                attributes[i] = new double[2][];
                attributes[i][0] = new double[NUMBER_OF_ATTRIBUTES];
                attributes[i][1] = new double[1];

                for(int j = 0; j < NUMBER_OF_ATTRIBUTES; j++)
                    attributes[i][0][j] = Double.parseDouble(scan.next());

                attributes[i][1][0] = Double.parseDouble(scan.next());
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        }

        Instance[] instances = new Instance[attributes.length];

        for(int i = 0; i < instances.length; i++) {
            instances[i] = new Instance(attributes[i][0]);
            // label needs no translation
            instances[i].setLabel(new Instance(attributes[i][1][0]));
        }

        return instances;
    }

    private static EvaluationResult evaluateNetwork(BackPropagationNetwork network, Instance[] instances){
        EvaluationResult returnValue = new EvaluationResult();

        double start = System.nanoTime();
        double predicted;
        double actual;

        for(int j = 0; j < instances.length; j++) {
            network.setInputValues(instances[j].getData());
            network.run();

            actual = Double.parseDouble(instances[j].getLabel().toString());
            predicted = Double.parseDouble(network.getOutputValues().toString());
            returnValue.confusionMatrix[(int) predicted][(int) actual] += 1;
        }
        double end = System.nanoTime();
        double testingTime = end - start;
        testingTime /= Math.pow(10,9);

        returnValue.testingTime = testingTime;



        return returnValue;

    }

    private static class EvaluationResult{
        public double[][] confusionMatrix = {{0,0},{0,0}};
        public double testingTime = 0;

    }

}
