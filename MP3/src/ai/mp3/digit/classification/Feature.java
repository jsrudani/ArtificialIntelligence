package ai.mp3.digit.classification;

import java.util.HashMap;
import java.util.Map;

/**
 * It is used to represent features for each class. Feature include following
 * information.
 * <pre>
 * 
 * 1. Map {Domain -> ValueStatistics}
 *      For e.g.
 *      Domain      |       ValueStats
 *  -----------------------------------------------------
 *      0                     |      Count(0),Prob.(0)
 *
 * </pre>
 * 
 * The idea is to generalize Feature class, currently we only have binary information
 * for each feature (pixel). In future if we have more information for feature, we
 * can modify this class to incorporate new information.
 * 
 * @author rudani2
 *
 */
public class Feature {

    private final Map<Integer,ValueStats> domainToValueStats;

    Feature(boolean binary) {
        domainToValueStats = new HashMap<Integer, ValueStats>();
        populateDomainValueStore(binary);
    }

    private void populateDomainValueStore(boolean binary) {
        if (binary) {
            for (int i = 0;i < ClassifierConstant.FEATURE_DOMAIN_VALUE_LIMIT;i++) {
                domainToValueStats.put(i, new ValueStats(i));
            }
        }
    }

    public Map<Integer, ValueStats> getDomainToValueStats() {
        return domainToValueStats;
    }
}
