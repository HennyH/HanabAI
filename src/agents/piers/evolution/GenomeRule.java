package agents.piers.evolution;

import java.util.ArrayList;
import java.util.Arrays;

import agents.piers.DiscardRandomRule;
import agents.piers.Func;
import agents.piers.IRule;
import agents.piers.Identity;
import agents.piers.IfRule;
import agents.piers.Linq;
import agents.piers.OsawaDiscardRule;
import agents.piers.RandomUtils;
import agents.piers.TellAnyoneAboutPlayableCardRule;
import agents.piers.TellAnyoneAboutUsefulCardRule;
import agents.piers.TellAnyoneAboutUselessCardRule;
import agents.piers.Pair;
import agents.piers.PlayProbablySafeCardRule;
import agents.piers.PlaySafeCardRule;

public class GenomeRule {
    public GenomeRuleType ruleType;
    public int livesRemainingParameter;
    public int hintsRemainingParameter;
    public ArrayList<Float> hintWeightingParamters;

    public GenomeRule(
            GenomeRuleType type,
            int livesRemainingParameter,
            int hintsRemainingParameter,
            ArrayList<Float> hintWeightingParamters
    ) {
        this.ruleType = type;
        this.livesRemainingParameter = livesRemainingParameter;
        this.hintsRemainingParameter = hintsRemainingParameter;
        this.hintWeightingParamters = hintWeightingParamters;
    }

    public static int getRandomLivesRemainingParameter() {
        /* We exclude 1 here because we special case it and it would case
         * a lot of bad genes!
         */
        return RandomUtils.integer(2, 3);
    }

    public static int getRandomHintsRemainingParameter() {
        /* Having 0 hints left could be paird with a discard or play rule. */
        return RandomUtils.integer(0, 8);
    }

    public static GenomeRule crossover(GenomeRule X, float xFitness, GenomeRule Y, int yFitness) {
        if (X.ruleType == Y.ruleType) {
            return new GenomeRule(
                X.ruleType,
                RandomUtils.choose(X.livesRemainingParameter, Y.livesRemainingParameter),
                RandomUtils.choose(X.hintsRemainingParameter, Y.hintsRemainingParameter),
                Linq.zipShortestMap(
                    X.hintWeightingParamters,
                    Y.hintWeightingParamters,
                    new Func<Pair<Float, Float>, Float>() {
                        @Override
                        public Float apply(Pair<Float, Float> xy) {
                            return (xy.getLeft() + xy.getRight()) / 2;
                        }
                    }
                )
            );
        }

        float fitnessDifference = Math.max(Math.abs(xFitness - yFitness), (float)0.0001);
        GenomeRule fittestRule = xFitness > yFitness ? X : Y;
        if (RandomUtils.chance(fitnessDifference)
                || RandomUtils.chance(0.4)
        ) {
            return fittestRule;
        }

        return RandomUtils.choose(new GenomeRule[] { X, Y });
    }

    public static GenomeRule mutate(GenomeRule X) {
        /* Every 2000 lives suffer a huge mutation that changes the rule type . */
        if (RandomUtils.chance(0.005)) {
            return new GenomeRule(
                RandomUtils.choose(GenomeRuleType.values()),
                GenomeRule.getRandomLivesRemainingParameter(),
                GenomeRule.getRandomHintsRemainingParameter(),
                X.hintWeightingParamters
            );
        }
        /* Every 100 lives suffer damage to the hint and lives counters */
        if (RandomUtils.chance(0.01)) {
            return new GenomeRule(
                X.ruleType,
                GenomeRule.getRandomLivesRemainingParameter(),
                GenomeRule.getRandomHintsRemainingParameter(),
                X.hintWeightingParamters
            );
        }
        /* Every 10 lives have our hint weightings modified */
        if (RandomUtils.chance(0.1)) {
            return new GenomeRule(
                X.ruleType,
                X.livesRemainingParameter,
                X.hintsRemainingParameter,
                Linq.map(
                    X.hintWeightingParamters,
                    new Func<Float, Float>() {
                        @Override
                        public Float apply(Float weight) {
                            float mutation = RandomUtils.choose(-1, 1) * (float)0.02;
                            return clamp(weight + mutation, (float)-1.0, (float)1);
                        }
                    }
                )
            );
        }
        return X;
    }

    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    public static GenomeRule spawn() {
        return new GenomeRule(
            RandomUtils.choose(GenomeRuleType.values()),
            GenomeRule.getRandomLivesRemainingParameter(),
            GenomeRule.getRandomHintsRemainingParameter(),
            RandomUtils.randomWeights(GenomeHintWeightingParameters.getParameterCount())
        );
    }

    public static IRule asRule(GenomeRule X, int playerIndex) {
        IRule consequence = null;
        GenomeHintWeightingParameters weights = GenomeHintWeightingParameters.asParameterSet(
            X.hintWeightingParamters
        );
        if (X.ruleType == GenomeRuleType.PlaySafe) {
            consequence = new PlaySafeCardRule(playerIndex);
        } else if (X.ruleType == GenomeRuleType.PlayProbablySafe) {
            consequence = new PlayProbablySafeCardRule(
                playerIndex,
                weights.utilityThreshold
            );
        } else if (X.ruleType == GenomeRuleType.TellAnyonePlayable) {
            consequence = new TellAnyoneAboutPlayableCardRule(
                playerIndex,
                weights.weightingForPointingAtMoreCards,
                weights.weightingForValueOverColour,
                weights.weightingForColourOverValue,
                weights.weightingForHigherValues,
                weights.weightingForRevealingPlayableCard,
                weights.weightingForRevealingAUselessCard,
                weights.weightingForPointingAtLessDistantFuturePlayableCards
            );
        } else if (X.ruleType == GenomeRuleType.TellAnyoneUseful) {
            consequence = new TellAnyoneAboutUsefulCardRule(
                playerIndex,
                weights.utilityThreshold,
                weights.weightingForPointingAtMoreCards,
                weights.weightingForValueOverColour,
                weights.weightingForColourOverValue,
                weights.weightingForHigherValues,
                weights.weightingForRevealingPlayableCard,
                weights.weightingForRevealingAUselessCard,
                weights.weightingForPointingAtLessDistantFuturePlayableCards
            );
        } else if (X.ruleType == GenomeRuleType.TellAnyoneUseless) {
            consequence = new TellAnyoneAboutUselessCardRule(
                playerIndex,
                weights.weightingForPointingAtMoreCards,
                weights.weightingForValueOverColour,
                weights.weightingForColourOverValue,
                weights.weightingForHigherValues,
                weights.weightingForRevealingPlayableCard,
                weights.weightingForRevealingAUselessCard,
                weights.weightingForPointingAtLessDistantFuturePlayableCards
            );
        } else if (X.ruleType == GenomeRuleType.OsawaDiscard) {
            consequence = new OsawaDiscardRule(playerIndex);
        } else if (X.ruleType == GenomeRuleType.RandomDiscard) {
            consequence = new DiscardRandomRule(playerIndex);
        }

        return IfRule.atLeastXHintsAndYLivesLeft(
            X.hintsRemainingParameter,
            X.livesRemainingParameter,
            consequence
        );
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        GenomeHintWeightingParameters weights = GenomeHintWeightingParameters.asParameterSet(
            this.hintWeightingParamters
        );
        builder.append(this.ruleType.toString());
        if (this.ruleType == GenomeRuleType.PlayProbablySafe) {
            builder.append("(");
            builder.append(weights.utilityThreshold);
            builder.append(")");
        } else if (this.ruleType == GenomeRuleType.TellAnyonePlayable
                || this.ruleType == GenomeRuleType.TellAnyoneUseless
        ) {
            builder.append("(");
            builder.append(Arrays.toString(this.hintWeightingParamters.toArray()));
            builder.append(")");
        } else if (this.ruleType == GenomeRuleType.TellAnyoneUseful) {
            builder.append("(");
            builder.append(Arrays.toString(this.hintWeightingParamters.toArray()));
            builder.append(")");
        }
        return builder.toString();
    }
}