package ai.mp3.digit.classification;

/**
 * This class is used to represent constant information required by different
 * classifier.
 * 
 * @author rudani2
 *
 */
public class ClassifierConstant {

    /**
     * It represent upper limit for Feature domain value.
     */
    public static long FEATURE_DOMAIN_VALUE_LIMIT = 2L;
    /**
     * It represent number of sample for digit classification in training data.
     */
    public static long TRAINING_DIGIT_CLASSIFICATION_COUNT = 5000L;
    /**
     * It represent training digit image height.
     */
    public static int TRAINING_DIGIT_HEIGHT = 28;
    /**
     * It represent training digit image width.
     */
    public static int TRAINING_DIGIT_WIDTH = 28;

}
