package agents.piers.evolution.logging;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import agents.piers.Func;
import agents.piers.Linq;
import agents.piers.MathUtils;
import agents.piers.evolution.Genome;

public class EvolutionLogger {

    public static GenerationSummaryYAML createSummaryYAML(
            int generation,
            ArrayList<Float> initialPopulationScores,
            ArrayList<Genome> population,
            HashMap<Genome, ArrayList<Float>> genomeToScores,
            ArrayList<Genome> orderedGenomes,
            ArrayList<Genome> children
    ) {
        float initialPopulationAverageScoreMean = Linq.avgF(initialPopulationScores).getValue();
        float initialPopulationAverageScoreStdev = Linq.avgF(initialPopulationScores).getValue();
        ArrayList<Float> pScores = Linq.chain(genomeToScores.values());
        float pScoresMean = Linq.avgF(pScores).getValue();
        float pScoresStdev = MathUtils.stdevP(pScores);
        return new GenerationSummaryYAML(
            generation,
            new PopulationStatisticsYAML(
                population.size(),
                Linq.min(pScores).getValue(),
                Linq.max(pScores).getValue(),
                pScoresMean,
                pScoresStdev,
                pScoresStdev - initialPopulationAverageScoreMean,
                pScoresStdev - initialPopulationAverageScoreStdev
            ),
            Linq.map(
                genomeToScores.keySet(),
                new Func<Genome, FitnessYAML>() {
                    @Override
                    public FitnessYAML apply(Genome genome) {
                        ArrayList<Float> scores = genomeToScores.get(genome);
                        float scoreMean = Linq.avgF(pScores).getValue();
                        float scoreStdev = MathUtils.stdevP(pScores);
                        return new FitnessYAML(
                            genome.getName(),
                            orderedGenomes.indexOf(genome),
                            Linq.min(scores).getValue(),
                            Linq.max(scores).getValue(),
                            scoreMean,
                            scoreStdev,
                            scoreMean - initialPopulationAverageScoreMean,
                            scoreStdev - initialPopulationAverageScoreStdev
                        );
                    }
                }
            ).toArray(new FitnessYAML[0]),
            Linq.map(
                population,
                new Func<Genome, GenomeYAML>() {
                    @Override
                    public GenomeYAML apply(Genome genome) {
                        String[] parentNames = genome.getParents().hasValue()
                            ? Linq.map(
                                Arrays.asList(genome.getParents().getValue()),
                                new Func<Genome, String>() {
                                    @Override
                                    public String apply(Genome genome) {
                                        return genome.getName();
                                    }
                                }
                            ).toArray(new String[0])
                            : new String[0];
                        return new GenomeYAML(
                            genome.getName(),
                            parentNames,
                            genome.getGenomeThisIsAMutationOf().hasValue()
                                ? genome.getGenomeThisIsAMutationOf().getValue().getName()
                                : null,
                            genome.formatDna()
                        );
                    }
                }
            ).toArray(new GenomeYAML[0]),
            Linq.map(
                children,
                new Func<Genome, GenomeYAML>() {
                    @Override
                    public GenomeYAML apply(Genome genome) {
                        String[] parentNames = genome.getParents().hasValue()
                            ? Linq.map(
                                Arrays.asList(genome.getParents().getValue()),
                                new Func<Genome, String>() {
                                    @Override
                                    public String apply(Genome genome) {
                                        return genome.getName();
                                    }
                                }
                            ).toArray(new String[0])
                            : new String[0];
                        return new GenomeYAML(
                            genome.getName(),
                            parentNames,
                            genome.getGenomeThisIsAMutationOf().hasValue()
                                ? genome.getGenomeThisIsAMutationOf().getValue().getName()
                                : null,
                            genome.formatDna()
                        );
                    }
                }
            ).toArray(new GenomeYAML[0])
        );
    }
}