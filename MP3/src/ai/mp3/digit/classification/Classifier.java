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
        // Record the number of classes
        NaiveBayesClassifier.setTotalClasses(nbClassifier.getlearnedModel().keySet().size());
        // Test the model with learned model
        List<Integer> prediction = nbClassifier.test(testimages, nbClassifier.getlearnedModel()
                            , ClassifierConstant.TRAINING_DIGIT_HEIGHT
                            , ClassifierConstant.TRAINING_DIGIT_WIDTH
                            , testLables);

        // Metric collection
        System.out.println("============= Prediction ==============");
        System.out.println(testLables);
        System.out.println(prediction);

        System.out.println("============= Classification rate and Max/Min posterior probability =============");
        int totalTestImages = testLables.size();
        int correctPredictedTestImages = 0;
        for (Integer key : nbClassifier.getPredictedModel().keySet()) {
            System.out.print(key + " ---> ");
            int totalCount = nbClassifier.getPredictedModel().get(key).getTestImageInstance();
            int correctClassificationCount = nbClassifier.getPredictedModel().get(key).getPredictedLabel()[key];
            double classificationRate = Math.ceil( ((float) correctClassificationCount/ (float)totalCount) * 100 );
            System.out.println(classificationRate + " ---> "
            + Math.ceil(nbClassifier.getPredictedModel().get(key).getMaxPosteriorProbability()) + " ---> "
            + Math.ceil(nbClassifier.getPredictedModel().get(key).getMinPosteriorProbability()) );
            correctPredictedTestImages += correctClassificationCount;
        }
        System.out.println("============= Overall Accuracy =============");
        System.out.println( Math.ceil( ((float)correctPredictedTestImages / (float)totalTestImages) * 100) );
        System.out.println("============= Confusion Matrix =============");
        for (int i = 0; i < NaiveBayesClassifier.getTotalClasses(); i++) {
            System.out.print("    " + i + "|");
        }
        System.out.println();
        for (Integer key : nbClassifier.getPredictedModel().keySet()) {
            int totalCount = nbClassifier.getPredictedModel().get(key).getTestImageInstance();
            System.out.print(key);
            for (int i = 0; i < NaiveBayesClassifier.getTotalClasses(); i++) {
                int correctClassificationCount = nbClassifier.getPredictedModel().get(key).getPredictedLabel()[i];
                double classificationRate = Math.ceil( ((float) correctClassificationCount/ (float)totalCount) * 100 );
                System.out.printf("  " + classificationRate + "|");
            }
            System.out.println();
        }
        System.out.println("============= Highest/Lowest MAP for each class =============");
        for (Integer key : nbClassifier.getPredictedModel().keySet()) {
            System.out.println("Class: " + key);
            System.out.println("Highest Posterior Probabilities : " + nbClassifier.getPredictedModel().get(key).getMaxPosteriorProbability());
            displayArray(nbClassifier.getPredictedModel().get(key).getMaxPosteriorProbabilityFeature());
            System.out.println("Lowest Posterior Probabilities: " + nbClassifier.getPredictedModel().get(key).getMinPosteriorProbability());
            displayArray(nbClassifier.getPredictedModel().get(key).getMinPosteriorProbabilityFeature());
        }
    }

    private static void displayArray(int [][] feature) {
        for (int row = 0; row < feature.length; row++) {
            for (int column = 0; column < feature[0].length; column++) {
                if (feature[row][column] > 0) {
                    System.out.print('#');
                } else {
                    System.out.print(' ');
                }
            }
            System.out.println();
        }
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
