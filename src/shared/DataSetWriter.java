package shared;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A class for writing data sets
 * @author Andrew Guillory gtg008g@mail.gatech.edu
 * @version 1.0
 */
public class DataSetWriter {
    /**
     * The dat set
     */
    private DataSet set;
    
    /**
     * The file name
     */
    private String filename;

    /**
     * True to append the results to the end of
     *  the file, false to overwrite.  This is
     *  useful if we need to write some kind of
     *  header to the file before writing the
     *  dataset.
     * 
     */
    private boolean append;

    private String[] labelStrings;

    private String[] headers;

    /**
     * Make a new data set writer
     * @param set the data set to writer
     */
    public DataSetWriter(DataSet set, String filename) {
        this.set = set;
        this.filename = filename;
        this.append = false;
        this.labelStrings = null;
        this.headers = null;
    }

    public DataSetWriter(DataSet set, String filename, String[] headers) {
        this.set = set;
        this.filename = filename;
        this.append = false;
        this.labelStrings = null;
        this.headers = headers;
    }

    /**
     * Make a new data set writer
     * @param set the data set to writer
     */
    public DataSetWriter(DataSet set, String filename, boolean append) {
        this.set = set;
        this.filename = filename;
        this.append = append;
        this.labelStrings = null;
        this.headers = null;
    }

    /**
     * Make a new data set writer
     * @param set the data set to writer
     */
    public DataSetWriter(DataSet set, String filename, boolean append, String[] labelStrings) {
        this.set = set;
        this.filename = filename;
        this.append = append;
        this.labelStrings = labelStrings;
        this.headers = null;
    }

    /**
     * Write the file out
     * @throws IOException when something goes bad
     */
    public void write() throws IOException {
        PrintWriter pw = new PrintWriter(new FileWriter(filename, this.append));
        if (headers != null) {
            for (int i = 0; i < headers.length; i++) {
                pw.print(headers[i] + (i < (headers.length - 1) ? ", " : ""));
            }
        }
        pw.println();
        for (int i = 0; i < set.size(); i++) {
            Instance data = set.get(i);
            boolean label = false;
            while (data != null) {
                if (label && this.labelStrings != null) {
                    for (int j = 0; j < data.size(); j++) {
                        pw.print(this.labelStrings[data.getDiscrete(j)]);
                        if (j + 1 < data.size() || data.getLabel() != null) {
                            pw.print(", ");
                        }
                    }
                } else {
                    for (int j = 0; j < data.size(); j++) {
                        pw.print(data.getContinuous(j));
                        if (j + 1 < data.size() || data.getLabel() != null) {
                            pw.print(", ");
                        }
                    }
                }
                data = data.getLabel();
                label = true;
            }
            pw.println();
        }
        pw.close();
    }
}
