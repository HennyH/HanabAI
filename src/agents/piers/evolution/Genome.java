package agents.piers.evolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import agents.piers.FallbackRule;
import agents.piers.IRule;
import agents.piers.Pair;
import agents.piers.RandomUtils;
import agents.piers.RuleSequenceRule;
import hanabAI.Action;
import hanabAI.Agent;
import hanabAI.State;

public class Genome {

    private ArrayList<GenomeRule> dna;
    private String name;

    private Genome(ArrayList<GenomeRule> dna) {
        this.dna = dna;
        this.name = UUID.randomUUID().toString();
    }

    public static Genome crossover(Genome X, float xFitness, Genome Y, float yFitness) {
        ArrayList<GenomeRule> childDna = new ArrayList<GenomeRule>();
        Genome strongerGenome = xFitness >= yFitness ? X : Y;
        Genome weakerGenome = xFitness >= yFitness ? Y : X;

        /* For the length of genes they have in common take the one
         * from the stronger genome with higher liklihood.
         */
        int sharedGenomeLength = Math.min(X.dna.size(), Y.dna.size());
        for (int i = 0; i < sharedGenomeLength; i++) {
            childDna.add(
                GenomeRule.crossover(
                    X.dna.get(i),
                    xFitness,
                    Y.dna.get(i),
                    yFitness
                )
            );
        }

        /* If they were of equal length we will have already considerd
         * the genetic material of both parents.
         */
        if (X.dna.size() == Y.dna.size()) {
            return new Genome(childDna);
        }

        Genome longerGenome = X.dna.size() > Y.dna.size() ? X : Y;
        int longerGenomeLength = longerGenome.dna.size();
        for (int i = sharedGenomeLength; i < longerGenomeLength; i++) {
            if (strongerGenome == longerGenome
                    && RandomUtils.chance(0.9)
            ) {
                childDna.add(longerGenome.dna.get(i));
            } else if (weakerGenome == longerGenome
                    && RandomUtils.chance(0.4)
            ) {
                childDna.add(longerGenome.dna.get(i));
            }
        }

        return new Genome(childDna);
    }

    public static Genome mutate(Genome X) {
        @SuppressWarnings("unchecked")
        ArrayList<GenomeRule> mutatedDna = (ArrayList<GenomeRule>)X.dna.clone();

        /* Every 10000 lives drop a random segment of dna */
        if (RandomUtils.chance(1.0 / 100.0) && mutatedDna.size() > 1) {
            mutatedDna.remove(RandomUtils.choose(mutatedDna));
        }
        /* Every 10000 lives add a random segment of dna */
        if (RandomUtils.chance(1.0 / 100.0) && mutatedDna.size() <= 20) {
            mutatedDna.add(GenomeRule.spawnRandom());
        }
        /* Every 5000 lives swap a segment of dna around */
        if (RandomUtils.chance(1.0 / 50.0) && mutatedDna.size() > 3) {
            GenomeRule a = RandomUtils.choose(mutatedDna);
            GenomeRule b = RandomUtils.choose(mutatedDna);
            int aIndex = mutatedDna.indexOf(a);
            int bIndex = mutatedDna.indexOf(b);
            mutatedDna.set(aIndex, b);
            mutatedDna.set(bIndex, a);
        }

        for (int i = 0; i < mutatedDna.size(); i++) {
            mutatedDna.set(i, GenomeRule.mutate(mutatedDna.get(i)));
        }

        return new Genome(mutatedDna);
    }

    public String getName() { return this.name; }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.name);
        builder.append("::");
        for (int i = 0; i < this.dna.size(); i++) {
            GenomeRule gene = this.dna.get(i);
            builder.append(gene.toString());
            if (i < this.dna.size() - 1) {
                builder.append(">");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    public String formatShortDna() {
        StringBuilder builder = new StringBuilder();
        builder.append("(");
        for (int i = 0; i < this.dna.size(); i++) {
            GenomeRule gene = this.dna.get(i);
            builder.append(gene.toShortString());
            if (i < this.dna.size() - 1) {
                builder.append(">");
            }
        }
        builder.append(")");
        return builder.toString();
    }

    public static Agent asAgent(Genome X, int playerIndex) {
        ArrayList<IRule> rules = new ArrayList<IRule>();
        for (GenomeRule gene : X.dna) {
            rules.add(GenomeRule.asRule(gene, playerIndex));
        }
        rules.add(new FallbackRule(playerIndex));
        IRule policy = new RuleSequenceRule(rules);
        return new Agent() {
            @Override
            public String toString() {
                return X.toString();
            }

            @Override
            public Action doAction(State s) {
				return policy.play(s);
			}
        };
    }

    public static Genome parseDna(String dnaString) {
        String[] genes = dnaString.split(">");
        ArrayList<GenomeRule> dna = new ArrayList<>();
        for (String gene : genes) {
            dna.add(GenomeRule.parseGene(gene));
        }
        return new Genome(dna);
    }

    public static Genome spawnRandom() {
        ArrayList<GenomeRule> dna = new ArrayList<GenomeRule>();
        int length = 5 + RandomUtils.integer(1, 4);
        for (int i = 1; i <= length; i++) {
            dna.add(GenomeRule.spawnRandom());
        }
        return new Genome(dna);
    }

    public static Genome spawnModel() {
        ArrayList<GenomeRule> dna = new ArrayList<GenomeRule>();
        ArrayList<Float> playSemiSafeWeights = new ArrayList<Float>(
            Arrays.asList(
                new Float[]
                {
                    (float)0.7,
                    (float)0.1,
                    (float)0.1,
                    (float)0.1,
                    (float)-0.3,
                    (float)0.1,
                    (float)0.5,
                    (float)0.8
                }
            )
        );
        ArrayList<Float> weights = new ArrayList<Float>(
            Arrays.asList(
                new Float[]
                {
                    (float)0.0,
                    (float)0.1,
                    (float)0.1,
                    (float)0.1,
                    (float)-0.3,
                    (float)0.1,
                    (float)0.5,
                    (float)0.8
                }
            )
        );
        dna.add(
            new GenomeRule(
                GenomeRuleType.PlaySafe,
                new Pair<Integer, Integer>(1,3),
                new Pair<Integer, Integer>(0,8),
                new GenomeHintWeightingParameters(
                    (float)0.0047613387,
                    (float)0.10607247,
                    (float)0.106292374,
                    (float)0.10083674,
                    (float)-0.29725158,
                    (float)0.1036114,
                    (float)0.5042223,
                    (float)0.7958471
                ).asArray()
            )
        );
        dna.add(
            new GenomeRule(
                GenomeRuleType.PlayProbablySafe,
                new Pair<Integer, Integer>(1,3),
                new Pair<Integer, Integer>(0,8),
                new GenomeHintWeightingParameters(
                    (float)0.67648125,
                    (float)0.100917436,
                    (float)0.10119392,
                    (float)0.10176204,
                    (float)-0.29964948,
                    (float)0.09964585,
                    (float)0.49983552,
                    (float)0.8013597
                ).asArray()
            )
        );
        dna.add(
            new GenomeRule(
                GenomeRuleType.TellAnyonePlayable,
                new Pair<Integer, Integer>(1,3),
                new Pair<Integer, Integer>(0,8),
                new GenomeHintWeightingParameters(
                    (float)0.13620576,
                    (float)0.14117011,
                    (float)0.19230494,
                    (float)0.045892805,
                    (float)-0.13685624,
                    (float)0.045723233,
                    (float)0.38740453,
                    (float)0.6898493
                ).asArray()
            )
        );
        dna.add(
            new GenomeRule(
                GenomeRuleType.PlaySafe,
                new Pair<Integer, Integer>(1,3),
                new Pair<Integer, Integer>(0,8),
                weights
            )
        );
        dna.add(
            new GenomeRule(
                GenomeRuleType.TellAnyonePlayable,
                new Pair<Integer, Integer>(1,3),
                new Pair<Integer, Integer>(0,8),
                new GenomeHintWeightingParameters(
                    (float)0.017988663,
                    (float)0.12786958,
                    (float)0.102385625,
                    (float)0.11974608,
                    (float)-0.29432404,
                    (float)0.099960625,
                    (float)0.502476,
                    (float)0.7809707
                ).asArray()
            )
        );
        dna.add(
            new GenomeRule(
                GenomeRuleType.TellAnyoneUseful,
                new Pair<Integer, Integer>(1,3),
                new Pair<Integer, Integer>(0,8),
                new GenomeHintWeightingParameters(
                    (float)0.078329414,
                    (float)0.15415362,
                    (float)0.1028495,
                    (float)0.10937385,
                    (float)-0.25686795,
                    (float)0.1600922,
                    (float)0.38822067,
                    (float)0.68456745
                ).asArray()
            )
        );
        dna.add(
            new GenomeRule(
                GenomeRuleType.OsawaDiscard,
                new Pair<Integer, Integer>(0,3),
                new Pair<Integer, Integer>(0,8),
                weights
            )
        );
        dna.add(
            new GenomeRule(
                GenomeRuleType.TellAnyoneUseless,
                new Pair<Integer, Integer>(0,3),
                new Pair<Integer, Integer>(0,8),
                new GenomeHintWeightingParameters(
                    (float)0.8158468,
                    (float)0.057116225,
                    (float)-0.32946068,
                    (float)0.7360475,
                    (float)0.14416188,
                    (float)0.35551566,
                    (float)-0.88390964,
                    (float)0.73350054
                ).asArray()
            )
        );
        dna.add(
            new GenomeRule(
                GenomeRuleType.OsawaDiscard,
                new Pair<Integer, Integer>(3,3),
                new Pair<Integer, Integer>(2,8),
                weights
            )
        );
        dna.add(
            new GenomeRule(
                GenomeRuleType.TellAnyoneUseful,
                new Pair<Integer, Integer>(0,3),
                new Pair<Integer, Integer>(0,8),
                new GenomeHintWeightingParameters(
                    (float)0.8094126,
                    (float)0.40024704,
                    (float)-0.06535219,
                    (float)-0.28192252,
                    (float)0.059569538,
                    (float)0.30669165,
                    (float)0.35209474,
                    (float)0.26821125
                ).asArray()
            )
        );

        Genome modelGenome = new Genome(dna);
        for (int i = 1; i <= 30; i++) {
            modelGenome = Genome.mutate(modelGenome);
        }
        return modelGenome;
    }
}