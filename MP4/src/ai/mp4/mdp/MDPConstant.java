package ai.mp4.mdp;

/**
 * It includes all the constants used during MDP.
 * 
 * @author rudani2
 *
 */
public class MDPConstant {

    // MDP
    public final static float GAMMA = 0.99f;
    public final static float INTENDED_OUTCOME_PROBABILITY = 0.8f;
    public final static float INTENDED_DIRECTION_PROBABILITY = 0.1f;
    public final static float EPSILON = 0.001f;

    // Reinforcement Learning
    public final static int MAXIMUM_NEXT_STATE = 4;
    public final static int Ne = 100;
    public final static int NUMBER_OF_TRIAL = 1000;

    // Model based Q Learning
    public final static float INTENDED_SPECIFIED_OUTCOME_PROBABILITY = 0.9f;
    public final static float INTENDED_SPECIFIED_DIRECTION_PROBABILITY = 0.05f;

    public final static float INTENDED_OUTCOME_PROBABILITY_CARRY_PIZZA = 0.8f;
    public final static float INTENDED_DIRECTION_PROBABILITY_CARRY_PIZZA = 0.05f;
    public final static float INTENDED_SAMESTATE_PROBABILITY_CARRY_PIZZA = 0.1f;

    public final static float INTENDED_OUTCOME_PROBABILITY_HAVE_PIZZA = 0.6f;
    public final static float INTENDED_DIRECTION_PROBABILITY_HAVE_PIZZA = 0.05f;
    public final static float INTENDED_SAMESTATE_PROBABILITY_HAVE_PIZZA = 0.3f; 

}
