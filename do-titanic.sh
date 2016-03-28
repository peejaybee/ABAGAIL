#!/usr/bin/env bash
LEARNING_RATE=.15
MOMENTUM=0.2
TRAINING_TIME=250
TRAINING_FILE=titanic-binary-normalized.arff
#TRAINING_FILE=titanic-training.arff

java -cp weka.jar weka.classifiers.functions.MultilayerPerceptron -k -L $LEARNING_RATE -M $MOMENTUM -N $TRAINING_TIME -x 10 -V 0 -S 0 -E 20 -H a -t $TRAINING_FILE
