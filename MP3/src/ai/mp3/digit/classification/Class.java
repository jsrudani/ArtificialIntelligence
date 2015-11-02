package ai.mp3.digit.classification;

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
public class Class {

    private final int classValue;
    private final Feature feature;
    private long numberOfSamples;
    private float classProbability;

    Class(int classValue) {
        this.classValue = classValue;
        this.feature = new Feature(true);
    }

    public Feature getFeature() {
        return feature;
    }

    public long getNumberOfSamplesForClass() {
        return numberOfSamples;
    }

    public float getClassProbability() {
        return classProbability;
    }

    public void incrementSampleCount() {
        this.numberOfSamples += 1;
    }

    public void setClassProbability(float classProbability) {
        this.classProbability += classProbability;
    }

    public int getClassValue() {
        return classValue;
    }
}
