package shared.test;

import shared.DataSet;
import shared.Instance;
import shared.filt.LinearDiscriminantAnalysis;
import shared.reader.ArffDataSetReader;
import util.linalg.DenseVector;

/**
 * A class for testing
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class LinearDiscriminantAnalysisRunner {
    
    /**
     * The test main
     * @param args ignored
     */
    public static void main(String[] args) throws Exception {
        String filename = args[0];
        ArffDataSetReader reader = new ArffDataSetReader(filename, true);
        DataSet set = reader.read();
        System.out.println("Before LDA");
        System.out.println(set);
        LinearDiscriminantAnalysis filter = new LinearDiscriminantAnalysis(set);
        filter.filter(set);
        System.out.println(filter.getProjection());
        System.out.println("After LDA");
        System.out.println(set);
        filter.reverse(set);
        System.out.println("After reconstructing");
        System.out.println(set);
        
    }

}
