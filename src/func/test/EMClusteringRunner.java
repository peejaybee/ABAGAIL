package func.test;

import dist.DiscreteDistribution;
import dist.Distribution;
import dist.MultivariateGaussian;
import func.EMClusterer;
import shared.DataSet;
import shared.Instance;
import shared.reader.ArffDataSetReader;
import shared.writer.CSVWriter;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * Testing
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class EMClusteringRunner {
    /**
     * The test main
     * @param args ignored
     */
    public static void main(String[] args) throws Exception {
        String filename = args[0];
        int k = args.length > 1 ? Integer.parseInt(args[1]) : 2;
        double tolerance = args.length > 2 ? Double.parseDouble(args[2]) : 1E-6;
        int maxIterations = args.length > 3 ? Integer.parseInt(args[3]) : 1000;

        String dataName = new File(filename).getName().split("\\.")[0];
        ArffDataSetReader reader = new ArffDataSetReader(filename, true);
        DataSet inputData = reader.read();
        List<String> attrs = reader.mAttributeNames;

        EMClusterer em = new EMClusterer(k, tolerance, maxIterations);
        em.estimate(inputData);
//        System.out.println("Iterations: " + em.getIterations());
        Instance[] inputInstances = inputData.getInstances();
        Instance[] outputInstances = new Instance[inputData.size()];
        int numberOfAttributes = inputInstances[0].size();
        for (int i = 0; i < inputInstances.length; i++){
            double[] dataOut = new double[numberOfAttributes + k + 1];
            for (int j = 0; j < numberOfAttributes; j++) {
                dataOut[j] = inputInstances[i].getContinuous(j);
            }
            DiscreteDistribution dist = (DiscreteDistribution) em.distributionFor(inputInstances[i]);
            double[] probs = dist.getProbabilities();
            for (int j = numberOfAttributes; j < numberOfAttributes + k; j++) {
                dataOut[j] = probs[j - numberOfAttributes];
            }
            dataOut[numberOfAttributes + k] = em.value(inputInstances[i]).getContinuous();
            outputInstances[i] = new Instance(dataOut);
            outputInstances[i].setLabel(inputInstances[i].getLabel());
//            System.out.println("Input: " + inputInstances[i]);
//            System.out.println("Output: " + outputInstances[i]);
        }
//        DataSet outputDataset = new DataSet(outputInstances);
        for (int i = 0; i < k; i++) {
            attrs.add(attrs.size() - 1, "cluster" + Integer.toString(i));
        }
        attrs.add(attrs.size() - 1, "clusterNum");

        CSVWriter writer = new CSVWriter("../Assignment3Output/EM-" + dataName + "-k" + k + "-out.csv", attrs.toArray(new String[0]));
        writer.open();
        for (int i = 0; i < outputInstances.length; i++){
            Instance instance = outputInstances[i];
            for (int j = 0; j < instance.size(); j++){
                double instanceVal = instance.getContinuous(j);
                writer.write(Double.toString(instanceVal));
            }
            writer.write(Double.toString(instance.getLabel().getContinuous(0)));
            writer.nextRecord();
        }
        writer.close();

        double sumSqErr = 0;
        Distribution[] dists = em.getMixture().getComponents();
        for(Instance instance : inputInstances){
            int clusterNum = (int) em.value(instance).getContinuous();
            for (int j = 0; j < numberOfAttributes; j++){
                double instanceVal = instance.getContinuous(j);
                sumSqErr += Math.pow((instanceVal - ((MultivariateGaussian) dists[clusterNum]).getMean().get(j)), 2);
            }
        }
        System.out.println(dataName.split("-")[0] + ",EM, " + k + "," + sumSqErr);
    }
}
