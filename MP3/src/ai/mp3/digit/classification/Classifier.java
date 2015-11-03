package ai.mp3.digit.classification;

import java.util.List;

/**
 * It is used to perform digit classification using Naive bayes model.
 * 
 * @author rudani2
 *
 */
public class Classifier {

    public static void main(String[] args) {
        // Validate if file name is passed as an argument
        if (args.length != 4) {
            printUsage();
        }
        // Get the Training data
        String trainingimages = args[0];
        // Get the Training labels
        String traininglabels = args[1];
        // Get the Test data
        String testimages = args[2];
        // Get the Test labels
        String testlabels = args[3];

        // Debug statement
        System.out.println("Training data file name: " + trainingimages);
        System.out.println("Training label file name: " + traininglabels);
        System.out.println("Test data file name: " + testimages);
        System.out.println("Test label file name: " + testlabels);

        // Get the training and test labels
        List<Integer> trainingLabels = Utilities.readLabelFile(traininglabels);
        List<Integer> testLables = Utilities.readLabelFile(testlabels);

        NaiveBayesClassifier nbClassifier = new NaiveBayesClassifier();
        // Train the model
        nbClassifier.training(trainingimages, trainingLabels
                , ClassifierConstant.TRAINING_DIGIT_CLASSIFICATION_COUNT
                , ClassifierConstant.TRAINING_DIGIT_HEIGHT
                , ClassifierConstant.TRAINING_DIGIT_WIDTH);
        // Test the model with learned model
        List<Integer> prediction = nbClassifier.test(testimages, nbClassifier.getlearnedModel()
                            , ClassifierConstant.TRAINING_DIGIT_HEIGHT
                            , ClassifierConstant.TRAINING_DIGIT_WIDTH);

        // Metric collection
        System.out.println("============= Prediction ==============");
        System.out.println(testLables);
        System.out.println(prediction);
    }

    /**
     * It is used to print the usage information of about how to run the program.
     */
    private static void printUsage() {
        String usage = "Please enter the following argument. \n"
                + "1. File containing Training data \n"
                + "2. File containing Training labels \n"
                + "3. File containing Test Data \n"
                + "4. File containing Test labels \n";
        System.out.println(usage);
    }
}
