package agents.piers.evolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;

import agents.piers.DiscardRandomRule;
import agents.piers.Func;
import agents.piers.IRule;
import agents.piers.Identity;
import agents.piers.IfRule;
import agents.piers.Linq;
import agents.piers.Maybe;
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
    public Pair<Integer, Integer> livesRemainingRange;
    public Pair<Integer, Integer> hintsRemainingRange;
    public ArrayList<Float> hintWeightingParamters;

    public GenomeRule(
            GenomeRuleType type,
            Pair<Integer, Integer> livesRemainingRange,
            Pair<Integer, Integer> hintsRemainingRange,
            ArrayList<Float> hintWeightingParamters
    ) {
        this.ruleType = type;
        this.livesRemainingRange = livesRemainingRange;
        this.hintsRemainingRange = hintsRemainingRange;
        this.hintWeightingParamters = hintWeightingParamters;
    }

    public static Pair<Integer, Integer> getRandomLivesRemainingParameter() {
        /* We exclude 1 here because we special case it and it would case
         * a lot of bad genes!
         */
        int lower = RandomUtils.integer(1, 3);
        int upper = Math.min(lower, RandomUtils.integer(1, 3));
        return new Pair<>(lower, upper);
    }

    public static Pair<Integer, Integer> getRandomHintsRemainingParameter() {
        /* Having 0 hints left could be paird with a discard or play rule. */
        int lower = RandomUtils.integer(0, 8);
        int upper = Math.min(lower, RandomUtils.integer(0, 8));
        return new Pair<>(lower, upper);
    }

    public static GenomeRule crossover(GenomeRule X, float xFitness, GenomeRule Y, float yFitness) {
        if (X.ruleType == Y.ruleType) {
            return new GenomeRule(
                X.ruleType,
                RandomUtils.choose(X.livesRemainingRange, Y.livesRemainingRange),
                RandomUtils.choose(X.hintsRemainingRange, Y.hintsRemainingRange),
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
        /* Every 10000 lives suffer a huge mutation that changes the rule type . */
        if (RandomUtils.chance(1.0 / 1000.0)) {
            return new GenomeRule(
                RandomUtils.choose(GenomeRuleType.values()),
                GenomeRule.getRandomLivesRemainingParameter(),
                GenomeRule.getRandomHintsRemainingParameter(),
                X.hintWeightingParamters
            );
        }
        /* Every 5000 lives suffer damage to the hint and lives counters */
        if (RandomUtils.chance(1.0 / 500.0)) {
            return new GenomeRule(
                X.ruleType,
                GenomeRule.getRandomLivesRemainingParameter(),
                GenomeRule.getRandomHintsRemainingParameter(),
                X.hintWeightingParamters
            );
        }
        /* Every 10 lives have our hint weightings modified */
        if (RandomUtils.chance(1.0 / 10.0)) {
            return new GenomeRule(
                X.ruleType,
                X.livesRemainingRange,
                X.hintsRemainingRange,
                Linq.mapi(
                    X.hintWeightingParamters,
                    new Func<Pair<Float, Integer>, Float>() {
                        @Override
                        public Float apply(Pair<Float, Integer> weight) {
                            /* A confidence threshold should never be negative */
                            if (weight.getRight() == 0) {
                                float mutation = RandomUtils.choose(-1, 1, 1) * (float)0.0001;
                                return clamp(weight.getLeft() + mutation, (float)0.0, (float)1.0);
                            } else {
                                float mutation = RandomUtils.choose(-1, 1) * (float)0.002;
                                return clamp(weight.getLeft() + mutation, (float)-1.0, (float)1);
                            }
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

    public static GenomeRule spawnRandom() {
        return new GenomeRule(
            RandomUtils.choose(GenomeRuleType.values()),
            GenomeRule.getRandomLivesRemainingParameter(),
            GenomeRule.getRandomHintsRemainingParameter(),
            GenomeHintWeightingParameters.getRandomWeights()
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

        return IfRule.hintsAndLivesWithinRange(
            X.livesRemainingRange,
            X.hintsRemainingRange,
            consequence
        );
    }

    public static String formatGenomeRule(GenomeRule rule, boolean shortVersion) {
        StringBuilder builder = new StringBuilder();
        GenomeHintWeightingParameters weights = GenomeHintWeightingParameters.asParameterSet(
            rule.hintWeightingParamters
        );
        String weightArrayStr = Arrays.toString(rule.hintWeightingParamters.toArray());
        builder.append(
            String.format(
                shortVersion
                    ? "{R=%s;L=%d-%d;H=%d-%d;W=%s}"
                    : "{R=%s;L=%d-%d;H=%d-%d;W=%s}",
                rule.ruleType.toString(),
                rule.livesRemainingRange.getLeft(),
                rule.livesRemainingRange.getRight(),
                rule.hintsRemainingRange.getLeft(),
                rule.hintsRemainingRange.getRight(),
                weights.utilityThreshold,
                shortVersion
                    ? String.format("%f", Linq.avgF(rule.hintWeightingParamters).getValue())
                    : weightArrayStr.substring(1, weightArrayStr.length() - 1)
            )
        );
        return builder.toString();
    }

    public static GenomeRule parseGene(String gene) {
        gene = gene.replace("{", "").replace("}", "");
        String[] parameters = gene.split(";");
        HashMap<String, String> parameterToArgument = new HashMap<>();
        for (String parameter : parameters) {
            String[] paramArg = parameter.split("=");
            parameterToArgument.put(
                paramArg[0].trim(),
                paramArg[1].trim()
            );
        }

        GenomeRuleType ruleType = GenomeRuleType.fromString(parameterToArgument.get("R"));
        String[] livesRangeStrings = parameterToArgument.get("L").split("-");
        String[] hintsRangeStrings = parameterToArgument.get("H").split("-");
        String[] weightsStrings = parameterToArgument.get("W").split(", ");
        Pair<Integer, Integer> livesRange = new Pair<>(
            Integer.parseInt(livesRangeStrings[0]),
            Integer.parseInt(livesRangeStrings[1])
        );
        Pair<Integer, Integer> hintsRange = new Pair<>(
            Integer.parseInt(hintsRangeStrings[0]),
            Integer.parseInt(hintsRangeStrings[1])
        );
        ArrayList<Float> weights = new ArrayList<>();
        for (String weightString : weightsStrings) {
            weights.add(Float.parseFloat(weightString));
        }

        return new GenomeRule(ruleType, livesRange, hintsRange, weights);
    }

    @Override
    public String toString() {
        return GenomeRule.formatGenomeRule(this, false);
    }

    public String toShortString() {
        return GenomeRule.formatGenomeRule(this, true);
    }
}