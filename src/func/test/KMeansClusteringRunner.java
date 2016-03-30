package func.test;

import shared.DataSet;
import shared.Instance;
import shared.reader.ArffDataSetReader;
import shared.writer.CSVWriter;

import java.util.List;

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
        String dataName = filename.split("-")[0];
        int k = args.length > 1 ? Integer.parseInt(args[1]) : 2;
        ArffDataSetReader reader = new ArffDataSetReader(filename, true);
        DataSet inputData = reader.read();
        List<String> attrs = reader.mAttributeNames;
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
            int[] clusterMembership = km.clusterMembershipFor(inputData.get(i));
            for (int j = numberOfAttributes; j < numberOfAttributes + k; j++) {
                dataOut[j] = clusterMembership[j - numberOfAttributes];
            }
            outputInstances[i] = new Instance(dataOut);
            outputInstances[i].setLabel(inputInstances[i].getLabel());
//            System.out.println("Input: " + inputInstances[i]);
//            System.out.println("Output: " + outputInstances[i]);
        }
//        DataSet outputDataset = new DataSet(outputInstances);
        for (int i = 0; i < k; i++) {
            attrs.add(attrs.size() - 1, "cluster" + Integer.toString(i));
        }
        CSVWriter writer = new CSVWriter("../Assignment3Output/kmeans-" + dataName + "-k" + k + "-out.csv", attrs.toArray(new String[0]));
        writer.open();
        for (int i = 0; i < outputInstances.length; i++){
            Instance instance = outputInstances[i];
            for (int j = 0; j < instance.size(); j++){
                writer.write(Double.toString(instance.getContinuous(j)));
            }
            writer.write(Double.toString(instance.getLabel().getContinuous(0)));
            writer.nextRecord();
        }
        writer.close();

    }
}
