package ai.mp3.digit.classification;

import java.util.Arrays;

/**
 * It is used to represent class for which we need to classify test data into. It contains
 * following information.
 * <pre>
 * 1. Features
 * 2. # total number of samples for this class
 * 3. probability of class
 * </pre>
 * 
 * @author rudani2
 *
 */
@SuppressWarnings("unused")
public class Class {

    private final int classValue;
    private long numberOfSamples;
    private float prior;
    private int [][] features;
    private float [][] likelihood;
    private final int height;
    private final int width;

    Class(int classValue, int height, int width) {
        this.classValue = classValue;
        this.height = height;
        this.width = width;
        this.features = new int[height][width];
        this.likelihood = new float[height][width];
    }

    public long getNumberOfSamplesForClass() {
        return numberOfSamples;
    }

    public float getPrior() {
        return this.prior;
    }

    public void incrementSampleCount() {
        this.numberOfSamples += 1;
    }

    public void incrementFeatures(int row, int column) {
        this.features[row][column] += 1;
    }

    public void setLikelihoodProbability(int row, int column, float likelihood) {
        this.likelihood[row][column] = likelihood;
    }

    public void setPriorProbability(float prior) {
        this.prior = prior;
    }

    public int getClassValue() {
        return classValue;
    }

    public int[][] getFeatures() {
        return features;
    }

    public float[][] getLikelihood() {
        return likelihood;
    }

    @Override
    public String toString() {
        return "Class [#Samples=" + numberOfSamples + ", prior=" + prior
                + ", features=" + Arrays.asList(features) + "\n, likelihood="
                + Arrays.asList(likelihood) + "]\n";
    }

}
