package ai.mp3.digit.classification;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
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

    NaiveBayesClassifier() {
        learnedModel = new HashMap<Integer,Class>();
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
                    // Check if line read count is greater than image height. If yes then
                    // new image starts else old image
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
            //System.out.println("========== Training Model ==============");
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
/*            // Debug
            System.out.println("Key: " + key);
            System.out.println("Features: \n");
            displayArray(learnedModel.get(key).getFeatures());
            System.out.println("Likelihood: \n");
            displayArray(learnedModel.get(key).getLikelihood());*/
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
            , int height, int width) {
        List<Integer> prediction = new ArrayList<Integer>();
        // Read the image of dimension (height X Width) from filename
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line = null;
                int lineCount = 1;
                int [][] features = new int [height][width];
                while ((line = br.readLine()) != null) {
                    if (lineCount > height) {
                        lineCount = 1;
                        // Calculate MAP for old image
                        calculateMAPProbability(learnedModel, features, prediction);
                        // Reset features for new image
                        features = new int [height][width];
                    }
                    // Process pixel of test image
                    processTestImage(line, (lineCount - 1), features);
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
    private void processTestImage(String line, int row, int [][] features)
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
            , List<Integer> prediction) throws IllegalArgumentException {

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
        } else {
            throw new IllegalArgumentException(" Problem in calculating MAP Maximum a Posterior");
        }
    }

    public Map<Integer, Class> getlearnedModel() {
        return learnedModel;
    }

    // Debug, delete after testing
    private void displayArray(int [][] prob) {
        for (int row = 0; row < prob.length; row++) {
            for (int column = 0; column < prob[0].length; column++) {
                System.out.print(prob[row][column]);
            }
            System.out.println();
        }
    }
    private void displayArray(float [][] prob) {
        for (int row = 0; row < prob.length; row++) {
            for (int column = 0; column < prob[0].length; column++) {
                System.out.print(prob[row][column]);
            }
            System.out.println();
        }
    }
}
