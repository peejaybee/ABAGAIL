package func.test;

import shared.DataSet;
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
        ArffDataSetReader reader = new ArffDataSetReader(filename, true);
        DataSet inputData = reader.read();
        func.KMeansClusterer km = new func.KMeansClusterer();
        km.estimate(inputData);
        System.out.println(km);
    }
}
