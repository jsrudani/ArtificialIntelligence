package ai.mp3.digit.classification;

/**
 * It is used to represent value statistic information. It can be directly used
 * for test data classification.
 * 
 * <pre>
 * 1. Count of value
 * 2. Probability of value
 * </pre>
 * 
 * @author rudani2
 *
 */
public class ValueStats {

    private final int featureValue;
    private long totalCount;
    private float probablility;

    ValueStats(int featureValue) {
        this.featureValue = featureValue;
        totalCount = 0L;
        probablility = 0.0f;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public float getProbablility() {
        return probablility;
    }

    public void setProbablility(float probablility) {
        this.probablility = probablility;
    }

    public void incrementTotalCount() {
        this.totalCount += 1;
    }

    public int getFeatureValue() {
        return featureValue;
    }

    @Override
    public String toString() {
        return "Value[Count="
                + totalCount + ",likelihood=" + probablility + "]";
    }
}
