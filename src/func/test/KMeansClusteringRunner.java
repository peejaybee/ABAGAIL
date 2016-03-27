package func.test;

import dist.DiscreteDistribution;
import shared.DataSet;
import shared.Instance;
import shared.reader.ArffDataSetReader;

/**
 * Testing
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class KMeansClusteringRunner {
    /**
     * The test main
     * @param args ignored
     */
    public static void main(String[] args) throws Exception {
        String filename = args[0];
        int k = args.length > 1 ? Integer.parseInt(args[1]) : 2;

        ArffDataSetReader reader = new ArffDataSetReader(filename);
        DataSet inputData = reader.read();
        func.KMeansClusterer km = new func.KMeansClusterer(k);
        km.estimate(inputData);
        Instance[] inputInstances = inputData.getInstances();
        Instance[] outputInstances = new Instance[inputData.size()];
        for (int i = 0; i < inputInstances.length; i++){
            int numberOfAttributes = inputInstances[i].size();
            double[] dataOut = new double[numberOfAttributes + k];
            for (int j = 0; j < numberOfAttributes; j++) {
                dataOut[j] = inputInstances[i].getContinuous(j);
            }
            DiscreteDistribution dist = (DiscreteDistribution) km.distributionFor(inputData.get(i));
            double[] probs = dist.getProbabilities();
            for (int j = numberOfAttributes; j < numberOfAttributes + k; j++) {
                dataOut[j] = probs[j - numberOfAttributes];
            }
            outputInstances[i] = new Instance(dataOut);
            outputInstances[i].setLabel(inputInstances[i].getLabel());
            System.out.println("Input: " + inputInstances[i]);
            System.out.println("Output: " + outputInstances[i]);
        }
        DataSet outputDataset = new DataSet(outputInstances);
    }
}
