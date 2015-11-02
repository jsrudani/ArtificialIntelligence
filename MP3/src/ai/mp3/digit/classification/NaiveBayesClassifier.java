package ai.mp3.digit.classification;

import java.io.BufferedReader;
import java.io.FileReader;
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

    public void training(String filename, List<Integer> trainingLabels
            , long totalSample, int height, int width) {

        // Read the image of size (height X Width) from filename
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
                String line = null;
                int lineCount = 1;
                int totalImages = 0;
                int key = trainingLabels.get(totalImages);
                while ((line = br.readLine()) != null) {
                    if (lineCount > ClassifierConstant.TRAINING_DIGIT_HEIGHT) {
                        lineCount = 1;
                        totalImages += 1;
                        key = trainingLabels.get(totalImages);
                    }
                    // Process the pixel information from line
                    processPixelInformation(line, key);
                    lineCount += 1;
                }
            }
        } catch (Exception e) {
            System.out.println("There is error while reading label file " + e.getMessage());
        }
        // Calculate the probability of each feature value
    }

    /**
     * It is used to process line and get pixel information. Pixel is Off (0)
     * if it is ' ' white (background) pixel and On (1) if it is '+' grey or
     * '#' black (foreground) pixel.
     * 
     * @param line
     * @param key
     */
    private void processPixelInformation(String line, int key) {
        // Check if class already exist
        if (!learnedModel.containsKey(key)) {
            learnedModel.put(key, new Class(key));
        }
        for (char ch : line.toCharArray()) {
            int integerValue = Utilities.getIntegerForCharacter(ch);
            learnedModel.get(key).getFeature().getDomainToValueStats().get(integerValue).incrementTotalCount();
        }
    }

    public void test(String filename, Map<Integer,Class> learnedModel) {
        
    }

    public Map<Integer, Class> getlearnedModel() {
        return learnedModel;
    }
}
