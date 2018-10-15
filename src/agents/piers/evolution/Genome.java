package agents.piers.evolution;

import java.util.ArrayList;

import agents.piers.FallbackRule;
import agents.piers.IRule;
import agents.piers.RandomUtils;
import agents.piers.RuleSequenceRule;
import hanabAI.Action;
import hanabAI.Agent;
import hanabAI.State;

public class Genome {

    private ArrayList<GenomeRule> dna;

    private Genome(ArrayList<GenomeRule> dna) {
        this.dna = dna;
    }

    public static Genome crossover(Genome X, float xFitness, Genome Y, int yFitness) {
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

        /* Every 1000 lives drop a random segment of dna */
        if (RandomUtils.chance(1.0 / 1000.0) && mutatedDna.size() > 1) {
            mutatedDna.remove(RandomUtils.choose(mutatedDna));
        }
        /* Every 1000 lives add a random segment of dna */
        if (RandomUtils.chance(1.0 / 1000.0) && mutatedDna.size() <= 20) {
            mutatedDna.add(GenomeRule.spawn());
        }
        /* Every 50 lives swap a segment of dna around */
        if (RandomUtils.chance(0.02) && mutatedDna.size() > 3) {
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

    public static Genome spawn() {
        ArrayList<GenomeRule> dna = new ArrayList<GenomeRule>();
        int length = 5 + RandomUtils.integer(1, 4);
        for (int i = 1; i <= length; i++) {
            dna.add(GenomeRule.spawn());
        }
        return new Genome(dna);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        for (int i = 0; i < this.dna.size(); i++) {
            GenomeRule gene = this.dna.get(i);
            builder.append(gene.toString());
            if (i < this.dna.size() - 1) {
                builder.append("-");
            }
        }
        builder.append("]");
        return builder.toString();
    }

    public static Agent asAgent(Genome X, int playerIndex) {
        ArrayList<IRule> rules = new ArrayList<IRule>();
        rules.add(new FallbackRule(playerIndex));
        IRule policy = new RuleSequenceRule(rules);
        for (GenomeRule gene : X.dna) {
            rules.add(GenomeRule.asRule(gene, playerIndex));
        }
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
}