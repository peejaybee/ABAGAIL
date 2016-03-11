/**
 * Created by Yeshwant Dattatreya. Adopted from TitanicTest.java by Hannah Lau. Bug fix from Andrew Whitaker incorporated.
 */
package opt.test;

import opt.*;
import opt.example.*;
import opt.ga.*;
import shared.*;
import func.nn.backprop.*;

//Java imports
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.io.*;
import java.text.*;
import java.lang.Runnable;
import java.lang.Thread;

class Analyze implements Runnable {
    private Thread t;
    private String threadName;

    private String training_data_file;
    private String output_dir;
    private Instance[] training_instances;
    private Instance[] test_instances;
    private DataSet training_set;
    private String comments;

    private int trainingIterations;

    //ANN specifications
    private int inputLayer;
    private int outputLayer;
    private int hiddenLayer;

    private ErrorMeasure measure = new SumOfSquaresError();
    private DecimalFormat df = new DecimalFormat("0.000");

    private double[] convert_to_double_arr(String[] str_array) {
        double[] double_arr = new double[str_array.length];
        for (int i=0; i < str_array.length; i++) {
            double_arr[i] = Double.parseDouble(str_array[i]);
        }
        return double_arr;
    }

    private Instance[] initializeInstances(String data_file) {

        Instance[] instances = null;

        try {
            ArrayList<String []> instance_list = new ArrayList();
            String line;
            BufferedReader br = new BufferedReader(new FileReader(new File(data_file)));
            while ((line = br.readLine()) != null) {
                instance_list.add(line.split(","));
            }

            instances = new Instance[instance_list.size()];

            for(int i = 0; i < instances.length; i++) {
                double[] attributes = convert_to_double_arr(instance_list.get(i));
                instances[i] = new Instance(Arrays.copyOfRange(attributes, 0, attributes.length - 1)); // Create an instance with the attributes
                instances[i].setLabel(new Instance(attributes[attributes.length - 1])); // Set the label for each instance
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return instances;
    }

    private double calculate_accuracy(Instance[] instances, Instance optimalInstance) {
        int correct = 0, incorrect = 0;
        BackPropagationNetwork network = new BackPropagationNetworkFactory().createClassificationNetwork(new int[] {inputLayer, hiddenLayer, outputLayer});
        network.setWeights(optimalInstance.getData());
        for(int j = 0; j < instances.length; j++) {
            network.setInputValues(instances[j].getData());
            network.run();

            double actual = Double.parseDouble(instances[j].getLabel().toString());
            double predicted = Double.parseDouble(network.getOutputValues().toString());

            System.out.println("Predicted: " + predicted + " Actual: " + actual + "\n" );

            double trash = Math.abs(predicted - actual) < 0.5 ? correct++ : incorrect++;
        }
        return correct*100.0/(correct+incorrect);
    }

    public void write_output_to_file(String output_dir, String file_path, String result, Boolean final_result) {
        //This function will have to be modified depending on the format of your file name. Else the splits won't work.
        try {
            if (final_result) {
                String full_path = output_dir + "/" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "/" + "final_result.csv";
                String[] params = file_path.split("_");
                String line = "";
                switch (params.length) {
                    case 9:
                        line = params[0] + ",none," + params[6] + "," + params[8].split("\\.")[0];
                        break;
                    case 10:
                        line = params[0] + "," + params[3] + "," + params[7] + "," + params[9].split("\\.")[0];
                        break;
                    case 11:
                        line = params[0] + "," + params[3] + "_" + params[4] + "," + params[8] + "," + params[10].split("\\.")[0];
                        break;
                }
                PrintWriter pwtr = new PrintWriter(new BufferedWriter(new FileWriter(full_path, true)));
                synchronized (pwtr) {
                    pwtr.println(line + result);
                    pwtr.close();
                }
            }
            else {
                String full_path = output_dir + "/" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "/" + file_path;
                Path p = Paths.get(full_path);
                Files.createDirectories(p.getParent());
                Files.write(p, result.getBytes());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Analyze(
            String optimization_algorithm,
            String data_folder_path,
            String training_data_file,
            String test_data_file,
            String comments,
            int trainingIterations,
            String output_dir
    ) {
        threadName = optimization_algorithm;
        this.training_data_file  = training_data_file;
        this.comments = comments;
        this.trainingIterations = trainingIterations;
        this.output_dir = output_dir;

        //Prepare instances and dataset
        training_instances = initializeInstances(data_folder_path + training_data_file);
        training_set = new DataSet(training_instances);
        test_instances = initializeInstances(data_folder_path + test_data_file);

        //ANN specifications
        inputLayer = training_instances[0].size() - 1;
        outputLayer = 1;
        hiddenLayer = (int)(inputLayer + outputLayer)/2;
    }

    public void run() {
        String results = "";
        OptimizationAlgorithm oa = null;
        BackPropagationNetwork network = new BackPropagationNetworkFactory().createClassificationNetwork(new int[] {inputLayer, hiddenLayer, outputLayer});
        NeuralNetworkOptimizationProblem nnop = new NeuralNetworkOptimizationProblem(training_set, network, measure);
        try {
            switch (threadName) {
                case "RHC":
                    oa = new RandomizedHillClimbing(nnop);
                    break;
                case "SA":
                    oa = new SimulatedAnnealing(1E11, .95, nnop);
                    break;
                case "GA":
                    oa = new StandardGeneticAlgorithm(200, 100, 10, nnop);
                    break;
            }

            double start = System.nanoTime(), end, trainingTime;
            for(int k = 0; k < trainingIterations; k++) {
                double error = 1/oa.train();
                results += df.format(error) + "\n";
            }
            end = System.nanoTime();
            trainingTime = (end - start)/Math.pow(10,9);

            Instance optimalInstance = oa.getOptimal();
            String training_accuracy = df.format(calculate_accuracy(training_instances, optimalInstance));
            String test_accuracy = df.format(calculate_accuracy(test_instances, optimalInstance));
            String training_time = df.format(trainingTime);

            results +=
                    "\nTraining Accuracy: " + training_accuracy + "%\n"
                            + "Testing Accuracy: " + test_accuracy + "%\n"
                            + "Training time: " + training_time + " seconds";
            String file_path = threadName + "_" + training_data_file.split("\\.")[0] + "_" + comments + "_iter_" + trainingIterations + ".csv";
            write_output_to_file(output_dir, file_path, results, false);
            write_output_to_file(output_dir, file_path, "," + training_accuracy + "," + test_accuracy + "," + training_time, true);
            System.out.println("\n" + file_path);
            System.out.println(results);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start () {
        if (t == null)
        {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
}

 public class NNTrain{
    public static void main(String[] args) {
        String output_dir = "results";
        String[] algorithms = {
                "RHC",
                "SA",
                "GA"
        };
        String data_folder_path = "data/";
        String training_data_files[] = {
                "titanic3-normalized.csv"
        };
        String test_data_files[] = {
                "titanic3-normalized.csv"
        };

        int num_runs = 5; //This starts a new run at a new random point or random population
        int training_iterations[] = {5, 10, 15, 20};

        for (int i = 0; i < algorithms.length; i++) {
            for (int j = 0; j < training_data_files.length; j++) {
                for (int k = 0; k < num_runs; k++) {
                    for (int l = 0; l < training_iterations.length; l++) {
                        new Analyze(
                                algorithms[i],
                                data_folder_path,
                                training_data_files[j],
                                test_data_files[j],
                                "run_" + (k + 1),
                                training_iterations[l],
                                output_dir
                        ).start();
                    }
                }
            }
        }
    }
}