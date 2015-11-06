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
    public static final long FEATURE_DOMAIN_VALUE_LIMIT = 2L;
    /**
     * It represent number of sample for digit classification in training data.
     */
    public static final long TRAINING_DIGIT_CLASSIFICATION_COUNT = 5000L;
    /**
     * It represent training digit image height.
     */
    public static final int TRAINING_DIGIT_HEIGHT = 28;
    /**
     * It represent training digit image width.
     */
    public static final int TRAINING_DIGIT_WIDTH = 28;
    /**
     * It represent Laplacian K constant.
     */
    public static final int LAPLACIAN_K_CONSTANT = 1;
    /**
     * It represent number of possible values the feature can take on.
     */
    public static final int V = 2;
    /**
     * It represent number of possible values the feature can take on.
     */
    public static final int DISJOINT_OVERLAP_V = 16;
    /**
     * It represent number of possible values the feature can take on.
     */
    public static final int INCREMENT_ROW = 1;
    /**
     * It represent number of possible values the feature can take on.
     */
    public static final int INCREMENT_COLUMN = 1;

}
