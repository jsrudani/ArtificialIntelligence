package ai.mp3.digit.classification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * It is used to implement Naive Bayes Classifier to classify digits and face.
 * 
 * @author rudani2
 *
 */
public class NaiveBayesClassifier {

    private Map<Integer,Class> learnedModel;
    private Map<Integer,Class> trainedModelWithGroupFeature;
    private Map<Integer, Metric> predictedModel;
    private static int totalClasses = 0;

    NaiveBayesClassifier() {
        learnedModel = new HashMap<Integer,Class>();
        predictedModel = new HashMap<Integer, Metric>();
        trainedModelWithGroupFeature = new HashMap<Integer,Class>();
    }

    /**
     * It is used to train the model by calculating prior and likelihood probability for each feature
     * value.
     * 
     * @param filename
     * @param trainingLabels
     * @param totalSample
     * @param height
     * @param width
     */
    public void training(String filename, List<Integer> trainingLabels
            , long totalSample, int height, int width) {

        // Read the image of dimension (height X Width) from filename
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line = null;
                int lineCount = 1;
                int totalImages = 0;
                int key = trainingLabels.get(totalImages);
                if (!learnedModel.containsKey(key)) {
                    learnedModel.put(key, new Class(key,height,width));
                }
                // Increment sample count for current class
                learnedModel.get(key).incrementSampleCount();
                while ((line = br.readLine()) != null) {
                    // Check if line read count is greater than image height. If yes then new image starts else old image
                    if (lineCount > height) {
                        lineCount = 1;
                        totalImages += 1;
                        key = trainingLabels.get(totalImages);
                        // Check if class already exist
                        if (!learnedModel.containsKey(key)) {
                            learnedModel.put(key, new Class(key,height,width));
                        }
                        // Increment sample count for current class
                        learnedModel.get(key).incrementSampleCount();
                    }
                    // Process the pixel information from line
                    processPixelInformation(line, key, (lineCount - 1));
                    lineCount += 1;
                }
            }
            // Calculate probability
            calculatePriorNLikelihoodProbability(learnedModel, totalSample);

        } catch (Exception e) {
            System.out.println("There is error while preparing Training model "
                                + e.getMessage());
        }
    }

    /**
     * It is used to process line and get pixel information. Pixel is Off (0)
     * if it is ' ' white (background) pixel and On (1) if it is '+' grey or
     * '#' black (foreground) pixel.
     * 
     * @param line
     * @param key
     */
    private void processPixelInformation(String line, int key, int row)
            throws IllegalArgumentException {
        int column = 0;
        // Process pixels in given image
        for (char pixel : line.toCharArray()) {
            int integerValue = Utilities.getIntegerForPixel(pixel);
            // Check for valid character
            if (integerValue < 0) {
                throw new IllegalArgumentException(" Pixel value in training data is not valid ");
            }
            // Increment the count of pixel value if 1
            if (integerValue == 1) {
                learnedModel.get(key).incrementFeatures(row, column);
            }
            // Increment column number
            column += 1;
        }
    }

    /**
     * It is used to calculate prior and likelihood probability for every class.
     * 
     * @param learnedModel
     * @param totalSample
     */
    private void calculatePriorNLikelihoodProbability(Map<Integer,Class> learnedModel
            , long totalSample) {
        // For each class calculate the prior and likelihood probability for each feature (pixel) value
        for (Integer key : learnedModel.keySet()) {
            // Calculate prior probability
            long samples = learnedModel.get(key).getNumberOfSamplesForClass();
            float prior = (float)samples/(float)totalSample;
            learnedModel.get(key).setPriorProbability(prior);
            // Calculate likelihood probability
            int [][] featureValue = learnedModel.get(key).getFeatures();
            for (int row = 0; row < featureValue.length; row++) {
                for (int column = 0; column < featureValue[0].length; column++) {
                    int count_1 = featureValue[row][column];
                    float likelihood = (float)(count_1 + ClassifierConstant.LAPLACIAN_K_CONSTANT)
                            /(float)(samples + (ClassifierConstant.LAPLACIAN_K_CONSTANT * ClassifierConstant.V) );
                    learnedModel.get(key).setLikelihoodProbability(row, column, likelihood);
                }
            }
        }
    }

    /**
     * It is used to classify the digit from test image.
     * 
     * @param filename
     * @param learnedModel
     * @param height
     * @param width
     * @return predicted values
     */
    public List<Integer> test(String filename, Map<Integer,Class> learnedModel
            , int height, int width, List<Integer> testLables) {
        List<Integer> prediction = new ArrayList<Integer>();
        // Read the image of dimension (height X Width) from filename
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line = null;
                int lineCount = 1;
                int [][] features = new int [height][width];
                int currentTestClass = 0;
                while ((line = br.readLine()) != null) {
                    if (lineCount > height) {
                        lineCount = 1;
                        // Calculate MAP for old image
                        calculateMAPProbability(learnedModel, features, prediction
                                , testLables, currentTestClass);
                        // Reset features for new image
                        features = new int [height][width];
                        // Get the next test class for new test image
                        currentTestClass += 1;
                    }
                    // Process pixel of test image
                    processImageFromFile(line, (lineCount - 1), features);
                    lineCount += 1;
                }
            }
        } catch (Exception e) {
            System.out.println("There is error while preparing Testing model "
                                + e.getMessage());
        }
        return prediction;
    }

    /**
     * It is used to process line and get pixel information. Pixel is Off (0)
     * if it is ' ' white (background) pixel and On (1) if it is '+' grey or
     * '#' black (foreground) pixel.
     * 
     * @param line
     * @param key
     */
    private void processImageFromFile(String line, int row, int [][] features)
            throws IllegalArgumentException {
        int column = 0;
        // Process pixels in given image
        for (char pixel : line.toCharArray()) {
            int integerValue = Utilities.getIntegerForPixel(pixel);
            // Check for valid character
            if (integerValue < 0) {
                throw new IllegalArgumentException(" Pixel value in test data is not valid ");
            }
            // Increment the count of pixel value either (0) or (1)
            features[row][column] = integerValue;
            // Increment column number
            column += 1;
        }
    }

    /**
     * It is used to calculate maximum posterior probability. If pixel value is 0 in test image then
     * we select (1 - likelihood[r][c]). Since we have store probability of pixel being set (1) in class
     * likelihood.
     * 
     * @param learnedModel
     * @param features
     * @param prediction
     */
    private void calculateMAPProbability (Map<Integer,Class> learnedModel, int [][] features
            , List<Integer> prediction, List<Integer> testLables, int currentTestLabel) throws IllegalArgumentException {

        float maximumPosterior = -Float.MAX_VALUE;
        Class maximumLikelihoodClass = null;
        // For each class calculate likelihood and select maximum likelihood
        for (Integer key : learnedModel.keySet()) {
            // Calculate prior probability based on pixel from test image
            float likelihood = 0.0f;
            likelihood += Math.log(learnedModel.get(key).getPrior());
            float [][] classLikelihood = learnedModel.get(key).getLikelihood();
            // Calculate likelihood probability based on pixel from test image
            for (int row = 0; row < features.length; row++) {
                for (int column = 0; column < features[0].length; column++) {
                    if (features[row][column] == 0) {
                        likelihood += Math.log( (1 - classLikelihood[row][column]) );
                    } else if (features[row][column] == 1) {
                        likelihood += Math.log( classLikelihood[row][column] );
                    }
                }
            }
            // Compare with maximum
            if (likelihood > maximumPosterior) {
                maximumLikelihoodClass = learnedModel.get(key);
                maximumPosterior = likelihood;
            }
        }
        // Set the prediction based on maximum likelihood
        if (maximumLikelihoodClass != null) {
            prediction.add(maximumLikelihoodClass.getClassValue());
            int testLabel = testLables.get(currentTestLabel);
            if (!predictedModel.containsKey(testLabel)) {
                predictedModel.put(testLabel, new Metric(testLabel, totalClasses));
            }
            // Increment test image instances
            predictedModel.get(testLabel).incrementTestImageInstance();
            // Set the predicted value
            predictedModel.get(testLabel).incrementPredictedLabel(maximumLikelihoodClass.getClassValue());
            // Set the maximum and minimum posterior probability
            predictedModel.get(testLabel).compareAndSetMaxMinPosteriorProbability(features
                    , maximumPosterior);
        } else {
            throw new IllegalArgumentException(" Problem in calculating MAP Maximum a Posterior");
        }
    }

    /**
     * It is used to train the model using naive bayesian.
     * It uses group of pixel as feature value. Group is identified by feature height and
     * width.
     * 
     * @param filename
     * @param trainingLabels
     * @param totalSample
     * @param height
     * @param width
     * @param FEATURE_HEIGHT
     * @param FEATURE_WIDTH
     * @param isFeatureOverlap
     */
    public void training(String filename, List<Integer> trainingLabels
            , long totalSample, final int height, final int width
            , final int FEATURE_HEIGHT, final int FEATURE_WIDTH
            , final boolean isFeatureOverlap) {

        // Get the number of values per features
        final int NUMBER_OF_VALUE_PER_FEATURE = (int) Math.pow(2, (FEATURE_HEIGHT * FEATURE_WIDTH));
        // Set the increment factor for row and column
        int incrementRow = (isFeatureOverlap ? ClassifierConstant.INCREMENT_ROW : FEATURE_HEIGHT);
        int incrementColumn = (isFeatureOverlap ? ClassifierConstant.INCREMENT_COLUMN : FEATURE_WIDTH);

        // Read the training file with image of dimension (height * width)
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line = null;
                int lineCount = 1;
                int totalImages = 0;
                int key = trainingLabels.get(totalImages);
                if (!trainedModelWithGroupFeature.containsKey(key)) {
                    trainedModelWithGroupFeature.put(key, new Class(key));
                }
                // Increment sample count for current class
                trainedModelWithGroupFeature.get(key).incrementSampleCount();
                // Get the binary representation of training image
                int [][] trainingimg = new int [height][width];
                while ((line = br.readLine()) != null) {
                    // Check if line read count is greater than image height. If yes then new image starts else old image
                    if (lineCount > height) {
                        lineCount = 1;
                        // Process the previous image stored in training image feature matrix
                        evaluateDisjointNOverlapFeature(trainingimg
                                , trainedModelWithGroupFeature
                                , key
                                , height, width, FEATURE_HEIGHT, FEATURE_WIDTH
                                , incrementRow, incrementColumn
                                , NUMBER_OF_VALUE_PER_FEATURE);
                        // Perform activity for new image
                        totalImages += 1;
                        key = trainingLabels.get(totalImages);
                        // Check if class already exist
                        if (!trainedModelWithGroupFeature.containsKey(key)) {
                            trainedModelWithGroupFeature.put(key, new Class(key));
                        }
                        // Increment sample count for current class
                        trainedModelWithGroupFeature.get(key).incrementSampleCount();
                        // Reset the binary representation of training image for new image
                        trainingimg = new int [height][width];
                    }
                    // Process the pixel information from line
                    processImageFromFile(line, (lineCount - 1), trainingimg);
                    lineCount += 1;
                }
            }
            // Calculate probability
            calculateDisjointOverlapFeatureProbability(trainedModelWithGroupFeature, totalSample);
        } catch (Exception e) {
            System.out.println("There is error while preparing Training model for feature overlapping"
                                + e.getMessage());
        }
    }

    /**
     * It is used to calculate prior and likelihood probability for every class.
     */
    private void evaluateDisjointNOverlapFeature(int [][] trainingimg
            , Map<Integer,Class> trainedModelWithGroupFeature
            , int key
            , final int height
            , final int width
            , final int FEATURE_HEIGHT
            , final int FEATURE_WIDTH
            , int incrementRow
            , int incrementColumn
            , final int NUMBER_OF_VALUE_PER_FEATURE) {

        // Hold which feature it is
        int featureId = 0;
        for (int row = 0; row <= (height - FEATURE_HEIGHT); row += incrementRow) {
            for (int column = 0; column <= (width - FEATURE_WIDTH); column += incrementColumn) {
                // Get the element
                int element = 0;
                // Set the exponent to convert into decimal format
                int exponent = ((FEATURE_HEIGHT * FEATURE_WIDTH) - 1);
                for (int indexX = 0; indexX < FEATURE_HEIGHT; indexX++) {
                    for (int indexY = 0; indexY < FEATURE_WIDTH; indexY++) {
                        // Set the element based on decimal representation
                        element += ( Math.pow(2, exponent) * trainingimg[(row + indexX)][(column + indexY)] );
                        // Decrement the exponent
                        exponent -= 1;
                    }
                }
                // Increment the feature value count
                if (!trainedModelWithGroupFeature.get(key).getClassToFeatureValueMap().containsKey(featureId)) {
                    int [] featureValue = new int [NUMBER_OF_VALUE_PER_FEATURE];
                    float [] featureValueLikelihood = new float [NUMBER_OF_VALUE_PER_FEATURE];
                    trainedModelWithGroupFeature.get(key).getClassToFeatureValueMap().put(featureId, featureValue);
                    trainedModelWithGroupFeature.get(key).getClassToFeatureLikelikhoodMap().put(featureId, featureValueLikelihood);
                }
                trainedModelWithGroupFeature.get(key).getClassToFeatureValueMap().get(featureId)[element] += 1;
                // Increment the feature Id for new feature
                featureId += 1;
            } // End of column
        } // End of row
    }

    private void calculateDisjointOverlapFeatureProbability (Map<Integer,Class> trainedModelWithGroupFeature
            , long totalSample) {
        // For each class calculate the prior and likelihood probability for each feature (pixel) value
        for (Integer key : trainedModelWithGroupFeature.keySet()) {
            // Calculate prior probability
            long samples = trainedModelWithGroupFeature.get(key).getNumberOfSamplesForClass();
            float prior = (float)samples/(float)totalSample;
            trainedModelWithGroupFeature.get(key).setPriorProbability(prior);
            // Calculate likelihood probability
            for (Integer feature : trainedModelWithGroupFeature.get(key).getClassToFeatureValueMap().keySet()) {
                int [] featureValue = trainedModelWithGroupFeature.get(key).getClassToFeatureValueMap().get(feature);
                for (int index = 0; index < featureValue.length; index++) {
                    int feature_value_count = featureValue[index];
                    float likelihood = (float)(feature_value_count + ClassifierConstant.LAPLACIAN_K_CONSTANT)
                                /(float)(samples + (ClassifierConstant.LAPLACIAN_K_CONSTANT * ClassifierConstant.DISJOINT_OVERLAP_V) );
                    trainedModelWithGroupFeature.get(key).getClassToFeatureLikelikhoodMap().get(feature)[index] = likelihood;
                }
            }
        }
    }

    public List<Integer> test(String filename
            , Map<Integer,Class> learnedModel
            , int height
            , int width
            , final int FEATURE_HEIGHT
            , final int FEATURE_WIDTH
            , final boolean isFeatureOverlap
            , List<Integer> testLables) {

        List<Integer> prediction = new ArrayList<Integer>();
        // Set the increment factor for row and column
        int incrementRow = (isFeatureOverlap ? ClassifierConstant.INCREMENT_ROW : FEATURE_HEIGHT);
        int incrementColumn = (isFeatureOverlap ? ClassifierConstant.INCREMENT_COLUMN : FEATURE_WIDTH);

        // Read the image of dimension (height X Width) from filename
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line = null;
                int lineCount = 1;
                int [][] features = new int [height][width];
                while ((line = br.readLine()) != null) {
                    if (lineCount > height) {
                        lineCount = 1;
                        // Prepare feature based on disjoint or overlap feature characteristics
                        List<Integer> feature = prepareDisjointOverlapFeature(features,height,width,FEATURE_HEIGHT,FEATURE_WIDTH,incrementRow,incrementColumn);
                        // Calculate MAP for old image
                        calculateMAPProbability(learnedModel, feature, prediction);
                        // Reset features for new image
                        features = new int [height][width];
                    }
                    // Process pixel of test image
                    processImageFromFile(line, (lineCount - 1), features);
                    lineCount += 1;
                }
            }
        } catch (Exception e) {
            System.out.println("There is error while Testing model with Disjoint/Overlap feature"
                                + e.getMessage());
        }
        return prediction;
    }

    private List<Integer> prepareDisjointOverlapFeature (int [][] testImage
            , final int height
            , final int width
            , final int FEATURE_HEIGHT
            , final int FEATURE_WIDTH
            , int incrementRow
            , int incrementColumn) {
        List<Integer> features = new ArrayList<Integer>();
        for (int row = 0; row <= (height - FEATURE_HEIGHT); row += incrementRow) {
            for (int column = 0; column <= (width - FEATURE_WIDTH); column += incrementColumn) {
                // Get the element
                int element = 0;
                // Set the exponent to convert into decimal format
                int exponent = ((FEATURE_HEIGHT * FEATURE_WIDTH) - 1);
                for (int indexX = 0; indexX < FEATURE_HEIGHT; indexX++) {
                    for (int indexY = 0; indexY < FEATURE_WIDTH; indexY++) {
                        // Set the element based on decimal representation
                        element += ( Math.pow(2, exponent) * testImage[(row + indexX)][(column + indexY)] );
                        // Decrement the exponent
                        exponent -= 1;
                    }
                }
                // Store the feature extracted based on disjoint/overlap property
                features.add(element);
            } // End of column
        } // End of row
        return features;
    }

    private void calculateMAPProbability(Map<Integer,Class> trainedModelWithGroupFeature
            , List<Integer> feature
            , List<Integer> prediction) {
        float maximumPosterior = -Float.MAX_VALUE;
        Class maximumLikelihoodClass = null;
        // For each class calculate likelihood and select maximum likelihood
        for (Integer key : trainedModelWithGroupFeature.keySet()) {
            // Calculate prior probability based on pixel from test image
            float likelihood = 0.0f;
            likelihood += Math.log(trainedModelWithGroupFeature.get(key).getPrior());
            int index = 0;
            for (int keyIndex = 0; keyIndex < trainedModelWithGroupFeature.get(key).getClassToFeatureLikelikhoodMap().keySet().size(); keyIndex++) {
                // Get the value represented by feature group
                int featureValue = feature.get(index);
                // Get the probability of feature value
                likelihood += Math.log(trainedModelWithGroupFeature.get(key).getClassToFeatureLikelikhoodMap().get(keyIndex)[featureValue]);
                // Increment index
                index += 1;
            }
            // Compare with maximum
            if (likelihood > maximumPosterior) {
                maximumLikelihoodClass = trainedModelWithGroupFeature.get(key);
                maximumPosterior = likelihood;
            }
        }
        // Set the prediction based on maximum posterior probability
        if (maximumLikelihoodClass != null) {
            prediction.add(maximumLikelihoodClass.getClassValue());
        }
    }

    public Map<Integer, Class> getlearnedModel() {
        return learnedModel;
    }

    public static int getTotalClasses() {
        return totalClasses;
    }

    public static void setTotalClasses(int totalClasses) {
        NaiveBayesClassifier.totalClasses = totalClasses;
    }

    public Map<Integer, Metric> getPredictedModel() {
        return predictedModel;
    }

    public Map<Integer, Class> getTrainedModelWithGroupFeature() {
        return trainedModelWithGroupFeature;
    }
}
