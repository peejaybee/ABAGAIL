#!/usr/bin/env bash
java -cp weka.jar weka.classifiers.functions.MultilayerPerceptron -L 0.15 -M 0.2 -N 500 -V 0 -S 0 -E 20 -H a -t titanic3-normalized-testing.arff -k