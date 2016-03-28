#!/usr/bin/env bash
shopt -s expand_aliases

DATA_DIR='../Assignment3Data'
alias weka="java -cp weka.jar"

weka weka.filters.unsupervised.attribute.NominalToBinary -i $DATA_DIR/titanic3-trimmed.arff \
| weka weka.filters.unsupervised.attribute.NumericToNominal -R last \
| weka weka.filters.unsupervised.attribute.Normalize -o titanic-binary-normalized.arff

#Start by resampling Adult to 10%
weka weka.filters.unsupervised.instance.Resample -S 1 -Z 10.0 -no-replacement -i $DATA_DIR/adult-trimmed.arff \
| weka weka.filters.unsupervised.attribute.NominalToBinary \
| weka weka.filters.unsupervised.attribute.ReplaceMissingValues \
| weka weka.filters.unsupervised.attribute.NumericToNominal -R last \
| weka weka.filters.unsupervised.attribute.Normalize -o adult-binary-normalized.arff