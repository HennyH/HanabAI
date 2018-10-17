package agents.piers.evolution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

import agents.piers.FallbackRule;
import agents.piers.IRule;
import agents.piers.Maybe;
import agents.piers.Pair;
import agents.piers.RandomUtils;
import agents.piers.RuleSequenceRule;
import hanabAI.Action;
import hanabAI.Agent;
import hanabAI.State;

public class Genome {

    private Maybe<Genome[]> parents;
    private Maybe<Genome> mutationOfGenome;
    private ArrayList<GenomeRule> dna;
    private String name;

    private Genome(ArrayList<GenomeRule> dna) {
        this.parents = new Maybe<>(null);
        this.mutationOfGenome = new Maybe<>(null);
        this.dna = dna;
        this.name = UUID.randomUUID().toString();
    }

    private Genome(Maybe<Genome[]> parents, ArrayList<GenomeRule> dna) {
        this.parents = parents;
        this.mutationOfGenome = new Maybe<>(null);
        this.dna = dna;
        this.name = UUID.randomUUID().toString();
    }

    private Genome(Genome[] parents, ArrayList<GenomeRule> dna) {
        this(new Maybe<>(parents), dna);
    }

    private Genome(Genome[] parents, Genome mutationOfGenome, ArrayList<GenomeRule> dna) {
        this(parents, dna);
        this.mutationOfGenome = new Maybe<>(mutationOfGenome);
    }

    private Genome(Maybe<Genome[]> parents, Genome mutationOfGenome, ArrayList<GenomeRule> dna) {
        this(parents, dna);
        this.mutationOfGenome = new Maybe<>(mutationOfGenome);
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
            return new Genome(new Genome[] { X, Y }, childDna);
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

        return new Genome(new Genome[] { X, Y }, childDna);
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

        return new Genome(
            X.getParents(),
            X,
            mutatedDna
        );
    }

    public String getName() { return this.name; }
    public Maybe<Genome[]> getParents() { return this.parents; }
    public Maybe<Genome> getGenomeThisIsAMutationOf() { return this.mutationOfGenome; }

    @Override
    public String toString() {
        return this.formatDna();
    }

    public String formatDna() {
        StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (int i = 0; i < this.dna.size(); i++) {
            GenomeRule gene = this.dna.get(i);
            builder.append(gene.toString());
            if (i < this.dna.size() - 1) {
                builder.append(">");
            }
        }
        builder.append("}");
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
}