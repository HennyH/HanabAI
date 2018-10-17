package agents.piers.evolution;

import java.util.ArrayList;
import java.util.Arrays;

import agents.piers.RandomUtils;

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

    public static ArrayList<Float> getRandomWeights() {
        ArrayList<Float> weights = new ArrayList<Float>();
        /* Add a 0-1 weight for confidence. */
        weights.add(RandomUtils.weight());
        for (int i = 1; i <= getParameterCount() - 1; i++) {
            weights.add(
                /* Make negative weights rarer */
                RandomUtils.choose(-1, 1, 1, 1) * RandomUtils.weight()
            );
        }
        return weights;
    }

    public ArrayList<Float> asArray() {
        return new ArrayList<Float>(
            Arrays.asList(
                new Float[]
                {
                    this.utilityThreshold,
                    this.weightingForPointingAtMoreCards,
                    this.weightingForValueOverColour,
                    this.weightingForColourOverValue,
                    this.weightingForHigherValues,
                    this.weightingForRevealingPlayableCard,
                    this.weightingForRevealingAUselessCard,
                    this.weightingForPointingAtLessDistantFuturePlayableCards
                }
            )
        );
    }
}
