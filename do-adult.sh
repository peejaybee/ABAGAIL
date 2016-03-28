#!/usr/bin/env bash
LEARNING_RATE=.15
MOMENTUM=0.2
TRAINING_TIME=250
#TRAINING_FILE=adult-binary-normalized.arff
TRAINING_FILE=adult-training.arff

java -cp weka.jar weka.classifiers.functions.MultilayerPerceptron -L $LEARNING_RATE -M $MOMENTUM -N $TRAINING_TIME  -V 0 -S 0 -E 20 -H a -k -t $TRAINING_FILE
