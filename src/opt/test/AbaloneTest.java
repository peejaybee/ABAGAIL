package opt.test;

import func.nn.backprop.BackPropagationNetwork;
import func.nn.backprop.BackPropagationNetworkFactory;
import opt.OptimizationAlgorithm;
import opt.RandomizedHillClimbing;
import opt.SimulatedAnnealing;
import opt.example.NeuralNetworkOptimizationProblem;
import opt.ga.StandardGeneticAlgorithm;
import shared.DataSet;
import shared.ErrorMeasure;
import shared.Instance;
import shared.SumOfSquaresError;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.text.DecimalFormat;
import java.util.Scanner;

/**
 * Implementation of randomized hill climbing, simulated annealing, and genetic algorithm to
 * find optimal weights to a neural network that is classifying passengers on the RMS Titanic
 * as having survived or perished
 *
 * @author Hannah Lau
 * @author Phillip Birmingham
 * @version 1.1
 */
public class AbaloneTest {

    private static int NUMBER_OF_TRAINING_INSTANCES = 917;
    private static int NUMBER_OF_TESTING_INSTANCES = 392;
    private static int NUMBER_OF_ATTRIBUTES = 9;
    private static String TRAINING_INSTANCE_FILENAME = "data/titanic3-normalized-training.csv";
    private static String TESTING_INSTANCE_FILENAME = "data/titanic3-normalized-testing.csv";

    private static Instance[] trainingInstances = initializeInstances(NUMBER_OF_TRAINING_INSTANCES, TRAINING_INSTANCE_FILENAME);
    private static Instance[] testingInstances = initializeInstances(NUMBER_OF_TESTING_INSTANCES, TESTING_INSTANCE_FILENAME);

    private static int inputLayer = NUMBER_OF_ATTRIBUTES, hiddenLayer = 5, outputLayer = 1, trainingIterations = 1000;
    private static BackPropagationNetworkFactory factory = new BackPropagationNetworkFactory();

    private static ErrorMeasure measure = new SumOfSquaresError();

    private static DataSet set = new DataSet(trainingInstances);

    private static BackPropagationNetwork networks[] = new BackPropagationNetwork[3];
    private static NeuralNetworkOptimizationProblem[] nnop = new NeuralNetworkOptimizationProblem[3];

    private static OptimizationAlgorithm[] oa = new OptimizationAlgorithm[3];
    private static String[] oaNames = {"RHC", "SA", "GA"};
    private static String trainingResults = "";
    private static String testingResults = "";

    private static DecimalFormat df = new DecimalFormat("0.000");
    private static boolean shortFormat = true;

    public static void main(String[] args) {

        hiddenLayer = Integer.parseInt(args[0]);
        trainingIterations = Integer.parseInt(args[1]);
        shortFormat = Boolean.parseBoolean(args[2]);


        for (int i = 0; i < oa.length; i++) {
            networks[i] = factory.createClassificationNetwork(
                    new int[]{inputLayer, hiddenLayer, outputLayer}, new func.nn.activation.LogisticSigmoid());
            nnop[i] = new NeuralNetworkOptimizationProblem(set, networks[i], measure);
        }

        oa[0] = new RandomizedHillClimbing(nnop[0]);
        oa[1] = new SimulatedAnnealing(1E11, .95, nnop[1]);
        oa[2] = new StandardGeneticAlgorithm(200, 100, 10, nnop[2]);

        double trainingKappa[] = new double[oa.length];
        double testingKappa[] = new double[oa.length];
        double trainingClockTime[] = new double[oa.length];
        double testingClockTime[] = new double[oa.length];
        double testPercentRight[] = new double[oa.length];
        double trainPercentRight[] = new double[oa.length];


        System.out.printf("Hidden nodes: %d training iterations: %d\n", hiddenLayer, trainingIterations);
        for (int i = 0; i < oa.length; i++) {

            System.out.println("Training " + oaNames[i]);

            double start = System.nanoTime(), end, trainingTime, testingTime, correct = 0, incorrect = 0;
            train(oa[i], networks[i], oaNames[i]); //trainer.train();
            end = System.nanoTime();
            trainingTime = end - start;
            trainingTime /= Math.pow(10, 9);

            Instance optimalInstance = oa[i].getOptimal();
            networks[i].setWeights(optimalInstance.getData());

            EvaluationResult erTrain = evaluateNetwork(networks[i], trainingInstances);
            EvaluationResult erTest = evaluateNetwork(networks[i], testingInstances);


            if (!shortFormat) {
                correct = erTrain.confusionMatrix[0][0] + erTrain.confusionMatrix[1][1];
                incorrect = erTrain.confusionMatrix[0][1] + erTrain.confusionMatrix[1][0];
                testingTime = erTrain.testingTime;

                trainingResults += "\nTraining set results for " + oaNames[i] + ": \nCorrectly classified " + correct + " instances." +
                        "\nIncorrectly classified " + incorrect + " instances.\nPercent correctly classified: "
                        + df.format(correct / (correct + incorrect) * 100) + "%\nTraining time: " + df.format(trainingTime)
                        + " seconds\nTesting time: " + df.format(testingTime) + " seconds\n";

                trainingResults += "Confusion matrix\n" +
                        "\t0\t1\n" +
                        "0\t" + erTrain.confusionMatrix[0][0] + "\t" + erTrain.confusionMatrix[0][1] + "\t <---predicted 0\n" +
                        "1\t" + erTrain.confusionMatrix[1][0] + "\t" + erTrain.confusionMatrix[1][1] + "\t <---predicted 1\n";

                trainingResults += "Kappa: " + erTrain.Kappa() + "\n";

                correct = erTest.confusionMatrix[0][0] + erTest.confusionMatrix[1][1];
                incorrect = erTest.confusionMatrix[0][1] + erTest.confusionMatrix[1][0];
                testingTime = erTest.testingTime;

                testingResults += "\nTesting set results for " + oaNames[i] + ": \nCorrectly classified " + correct + " instances." +
                        "\nIncorrectly classified " + incorrect + " instances.\nPercent correctly classified: "
                        + df.format(correct / (correct + incorrect) * 100) + "%\nTraining time: " + df.format(trainingTime)
                        + " seconds\nTesting time: " + df.format(testingTime) + " seconds\n";

                testingResults += "Confusion matrix\n" +
                        "\t0\t1\n" +
                        "0\t" + erTest.confusionMatrix[0][0] + "\t" + erTest.confusionMatrix[0][1] + "\t <---predicted 0\n" +
                        "1\t" + erTest.confusionMatrix[1][0] + "\t" + erTest.confusionMatrix[1][1] + "\t <---predicted 1\n";

                testingResults += "Kappa: " + erTest.Kappa() + "\n";
            } else {
                trainingKappa[i] = erTrain.Kappa();
                testingKappa[i] = erTest.Kappa();
                trainingClockTime[i] = trainingTime;
                testingClockTime[i] = erTest.testingTime;
                testPercentRight[i] = erTest.numCorrect() / (erTest.numCorrect() + erTest.numWrong());
                trainPercentRight[i] = erTrain.numCorrect() / (erTrain.numCorrect() + erTrain.numWrong());
            }
        }
        if (!shortFormat) {
            System.out.println(trainingResults + testingResults);
        } else {
            System.out.println("algo, hidden, iter, Ktr, Kte, Ttr, Tte, %tr, %te");
            for (int i = 0; i < oa.length; i++) {
                String result = oaNames[i] + "," + hiddenLayer + "," + trainingIterations + "," +
                        trainingKappa[i] + "," + testingKappa[i] + "," +
                        trainingClockTime[i] + "," + testingClockTime[i] + "," +
                        trainPercentRight[i] + "," + testPercentRight[i];
                System.out.println(result);
            }
        }
    }

    private static void train(OptimizationAlgorithm oa, BackPropagationNetwork network, String oaName) {
//        System.out.println("\nError results for " + oaName + "\n---------------------------");

        for (int i = 0; i < trainingIterations; i++) {
            oa.train();

            double error = 0;
            for (int j = 0; j < trainingInstances.length; j++) {
                network.setInputValues(trainingInstances[j].getData());
                network.run();

                Instance output = trainingInstances[j].getLabel(), example = new Instance(network.getOutputValues());
                example.setLabel(new Instance(Double.parseDouble(network.getOutputValues().toString())));
                error += measure.value(output, example);
            }

//            System.out.println(df.format(error));
        }
    }

    private static Instance[] initializeInstances(int numberOfInstances, String instanceFilename) {

        double[][][] attributes = new double[numberOfInstances][][];

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(instanceFilename)));

            for (int i = 0; i < attributes.length; i++) {
                Scanner scan = new Scanner(br.readLine());
                scan.useDelimiter(",");

                attributes[i] = new double[2][];
                attributes[i][0] = new double[NUMBER_OF_ATTRIBUTES];
                attributes[i][1] = new double[1];

                for (int j = 0; j < NUMBER_OF_ATTRIBUTES; j++)
                    attributes[i][0][j] = Double.parseDouble(scan.next());

                attributes[i][1][0] = Double.parseDouble(scan.next());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Instance[] instances = new Instance[attributes.length];

        for (int i = 0; i < instances.length; i++) {
            instances[i] = new Instance(attributes[i][0]);
            // label needs no translation
            instances[i].setLabel(new Instance(attributes[i][1][0]));
        }

        return instances;
    }

    private static EvaluationResult evaluateNetwork(BackPropagationNetwork network, Instance[] instances) {
        EvaluationResult returnValue = new EvaluationResult();

        double start = System.nanoTime();
        double predicted;
        double actual;

        double actualSum = 0;
        double predictedSum = 0;

        for (int j = 0; j < instances.length; j++) {
            network.setInputValues(instances[j].getData());
            network.run();

            actual = Double.parseDouble(instances[j].getLabel().toString());
            predicted = Double.parseDouble(network.getOutputValues().toString());
            int iPredict = predicted < .5 ? 0 : 1;
            int iActual = actual < .5 ? 0 : 1;
            returnValue.confusionMatrix[iPredict][iActual] += 1;
            actualSum += actual;
            predictedSum += predicted;
        }
        double end = System.nanoTime();
        double testingTime = end - start;
        testingTime /= Math.pow(10, 9);

        returnValue.testingTime = testingTime;
        returnValue.avgActual = actualSum / instances.length;
        returnValue.avgPredicted = predictedSum / instances.length;


        return returnValue;

    }

    private static class EvaluationResult {
        public double[][] confusionMatrix = {{0, 0}, {0, 0}};
        public double testingTime = 0;
        public double avgPredicted = 0;
        public double avgActual = 0;

        public double numCorrect() {
            return confusionMatrix[0][0] + confusionMatrix[1][1];
        }

        public double numWrong() {
            return confusionMatrix[1][0] + confusionMatrix[0][1];
        }

        public double Kappa() {

            double aApA = confusionMatrix[0][0];
            double aApB = confusionMatrix[1][0];
            double aBpA = confusionMatrix[0][1];
            double aBpB = confusionMatrix[1][1];

            double total = aApA + aApB + aBpA + aBpB;
            double fracpA = (aApA + aBpA) / total;
            double fracpB = (aApB + aBpB) / total;
            double fracaA = (aApA + aApB) / total;
            double fracaB = (aBpA + aBpB) / total;

            double probRandomAA = fracpA * fracaA;
            double probRandomBB = fracpB * fracaB;
            double probRandomAgreement = probRandomAA + probRandomBB;

            double fracCorrect = (aApA + aBpB) / total;

            double kappa = (fracCorrect - probRandomAgreement) / (1.0 - probRandomAgreement);

            return kappa;

        }
    }
}
