package shared.test;

import shared.DataSet;
import shared.Instance;
import shared.filt.IndependentComponentAnalysis;
import shared.filt.RandomizedProjectionFilter;
import shared.reader.ArffDataSetReader;
import util.linalg.Matrix;
import util.linalg.RectangularMatrix;

/**
 * A class for testing
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class RandomizedProjectionRunner {
    
    /**
     * The test main
     * @param args ignored
     */
    public static void main(String[] args) throws Exception {
        String filename = args[0];
        ArffDataSetReader reader = new ArffDataSetReader(filename, true);
        DataSet set = reader.read();
        int attributeCount = set.get(0).size();
//        IndependentComponentAnalysis filter = new IndependentComponentAnalysis(set, 1);
        RandomizedProjectionFilter filter = new RandomizedProjectionFilter(attributeCount / 2, attributeCount);
        filter.filter(set);
        System.out.println("After ICA");
        System.out.println(set);
          
    }

}