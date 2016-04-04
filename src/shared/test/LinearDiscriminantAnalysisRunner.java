package shared.test;

import shared.DataSet;
import shared.DataSetWriter;
import shared.Instance;
import shared.filt.LinearDiscriminantAnalysis;
import shared.reader.ArffDataSetReader;
import util.linalg.DenseVector;

import java.io.File;

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
        int numberOfComponents = args.length > 1 ? Integer.parseInt(args[1]) : 2;

        String dataName = new File(filename).getName().split("\\.")[0];
        ArffDataSetReader reader = new ArffDataSetReader(filename, true);
        DataSet set = reader.read();
        System.out.println("Before LDA");
        System.out.println(set);
        LinearDiscriminantAnalysis filter = new LinearDiscriminantAnalysis(set, numberOfComponents);
        filter.filter(set);
        System.out.println(filter.getProjection());
        System.out.println("After LDA");
        System.out.println(set);
        filter.reverse(set);
        System.out.println("After reconstructing");
        System.out.println(set);
        Instance firstInstance = set.get(0);
        int attrCount = firstInstance.size() + firstInstance.getLabel().size();
        String[] attribNames = new String[attrCount];
        for(int i = 0; i < firstInstance.size(); i++){
            attribNames[i] = "attrib" + i;
        }
        for (int i = 0; i < firstInstance.getLabel().size(); i++){
            attribNames[i + firstInstance.size()] = "label" + Double.toString(firstInstance.getLabel().getContinuous());
        }
        DataSetWriter dsw = new DataSetWriter(set, "../Assignment3Output/LDA-" + dataName + "-" + numberOfComponents + ".csv", attribNames);
        dsw.write();
    }

}
