package ai.mp3.digit.classification;

/**
 * It is used to hold information which is used to build confusion metrics and classification
 * rate.
 * 
 * @author rudani2
 *
 */
public class Metric {

    private final int testLabel;
    private int [] predictedLabel;
    private int [][] maxPosteriorProbabilityFeature;
    private int [][] minPosteriorProbabilityFeature;
    private int testImageInstance;
    private float maxPosteriorProbability;
    private float minPosteriorProbability;

    Metric(int testLabel, int totalClasses) {
        this.testLabel = testLabel;
        this.predictedLabel = new int [totalClasses];
        this.maxPosteriorProbability = -Float.MAX_VALUE;
        this.minPosteriorProbability = Float.MAX_VALUE;
        this.testImageInstance = 0;
    }

    public void incrementPredictedLabel(int predictedLabel) {
        this.predictedLabel[predictedLabel] += 1;
    }

    public void incrementTestImageInstance() {
        this.testImageInstance += 1;
    }

    public void compareAndSetMaxMinPosteriorProbability(int [][] features,
            float predictedProbability) {
        if (predictedProbability > maxPosteriorProbability) {
            this.maxPosteriorProbability = predictedProbability;
            this.maxPosteriorProbabilityFeature = features;
        } else if (predictedProbability < minPosteriorProbability) {
            this.minPosteriorProbability = predictedProbability;
            this.minPosteriorProbabilityFeature = features;
        }
    }

    public int[] getPredictedLabel() {
        return predictedLabel;
    }

    public void setPredictedLabel(int[] predictedLabel) {
        this.predictedLabel = predictedLabel;
    }

    public int getTestImageInstance() {
        return testImageInstance;
    }

    public void setTestImageInstance(int testImageInstance) {
        this.testImageInstance = testImageInstance;
    }

    public float getMaxPosteriorProbability() {
        return maxPosteriorProbability;
    }

    public void setMaxPosteriorProbability(float maxPosteriorProbability) {
        this.maxPosteriorProbability = maxPosteriorProbability;
    }

    public float getMinPosteriorProbability() {
        return minPosteriorProbability;
    }

    public void setMinPosteriorProbability(float minPosteriorProbability) {
        this.minPosteriorProbability = minPosteriorProbability;
    }

    public int getTestLabel() {
        return testLabel;
    }

    public int[][] getMaxPosteriorProbabilityFeature() {
        return maxPosteriorProbabilityFeature;
    }

    public int[][] getMinPosteriorProbabilityFeature() {
        return minPosteriorProbabilityFeature;
    }

}
