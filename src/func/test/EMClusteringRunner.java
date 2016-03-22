package func.test;

import func.EMClusterer;
import shared.DataSet;
import shared.reader.ArffDataSetReader;

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
        ArffDataSetReader reader = new ArffDataSetReader(filename, true);
        DataSet inputData = reader.read();
        EMClusterer em = new EMClusterer();
        em.estimate(inputData);
        System.out.println(em);
    }
}
