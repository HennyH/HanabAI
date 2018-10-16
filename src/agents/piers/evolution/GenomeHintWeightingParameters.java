package agents.piers.evolution;

import java.util.ArrayList;
import java.util.Arrays;

public class GenomeHintWeightingParameters {
    public float utilityThreshold;
    public float weightingForPointingAtMoreCards;
    public float weightingForValueOverColour;
    public float weightingForColourOverValue;
    public float weightingForHigherValues;
    public float weightingForRevealingPlayableCard;
    public float weightingForRevealingAUselessCard;
    public float weightingForPointingAtLessDistantFuturePlayableCards;

    public GenomeHintWeightingParameters(
        float utilityThreshold,
        float weightingForPointingAtMoreCards,
        float weightingForValueOverColour,
        float weightingForColourOverValue,
        float weightingForHigherValues,
        float weightingForRevealingPlayableCard,
        float weightingForRevealingAUselessCard,
        float weightingForPointingAtLessDistantFuturePlayableCards
    ) {
        this.utilityThreshold = utilityThreshold;
        this.weightingForPointingAtMoreCards = weightingForPointingAtMoreCards;
        this.weightingForValueOverColour = weightingForValueOverColour;
        this.weightingForColourOverValue = weightingForColourOverValue;
        this.weightingForHigherValues = weightingForHigherValues;
        this.weightingForRevealingPlayableCard = weightingForRevealingPlayableCard;
        this.weightingForRevealingAUselessCard = weightingForRevealingAUselessCard;
        this.weightingForPointingAtLessDistantFuturePlayableCards = weightingForPointingAtLessDistantFuturePlayableCards;
    }

    public static int getParameterCount() {
        return 8;
    }

    public static GenomeHintWeightingParameters asParameterSet(ArrayList<Float> weights) {
        if (weights.size() != getParameterCount()) {
            throw new IllegalStateException("Invalid parameter set produced:" + Arrays.toString(weights.toArray()));
        }
        return new GenomeHintWeightingParameters(
            weights.get(0),
            weights.get(1),
            weights.get(2),
            weights.get(3),
            weights.get(4),
            weights.get(5),
            weights.get(6),
            weights.get(7)
        );
    }
}
