package ai.mp3.digit.classification;

/**
 * It is used to represent pair of classes which has highest confusion ratio.
 * 
 * @author rudani2
 *
 */
public class OddsRatioPair {

    private final int correctLabel;
    private final int predictedLabel;
    private double confusionrate;

    OddsRatioPair(int correctLabel, int predictedLabel, double confusionrate) {
        this.correctLabel = correctLabel;
        this.predictedLabel = predictedLabel;
        this.confusionrate = confusionrate;
    }

    @Override
    public String toString() {
        return "OddsRatioPair [correctLabel=" + correctLabel
                + ", predictedLabel=" + predictedLabel + ", confusionrate="
                + confusionrate + "]\n";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(confusionrate);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + correctLabel;
        result = prime * result + predictedLabel;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OddsRatioPair other = (OddsRatioPair) obj;
        if (Double.doubleToLongBits(confusionrate) != Double
                .doubleToLongBits(other.confusionrate))
            return false;
        if (correctLabel != other.correctLabel)
            return false;
        if (predictedLabel != other.predictedLabel)
            return false;
        return true;
    }

    public int getCorrectLabel() {
        return correctLabel;
    }

    public int getPredictedLabel() {
        return predictedLabel;
    }

    public double getConfusionrate() {
        return confusionrate;
    }

}
