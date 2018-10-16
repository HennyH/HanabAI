package agents.piers.evolution;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import agents.piers.Identity;
import agents.piers.Linq;
import agents.piers.MathUtils;
import agents.piers.RandomUtils;
import hanabAI.Agent;
import hanabAI.Hanabi;

public class EvolutionRunner {

    public static void logDetailedGenerationSummary(
            Writer log,
            int generation,
            ArrayList<Genome> generationPopulation,
            ArrayList<Float> generationIndividualScores,
            ArrayList<Float> generationAverageScores,
            Float originalPopulationAverageScoreMean,
            Float originalPopulationAverageScoreStdev,
            HashMap<Float, ArrayList<Genome>> averageScoreToGenomes,
            ArrayList<Genome> children
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append(
            EvolutionRunner.formatGenerationStatistics(
                generation,
                generationPopulation,
                generationIndividualScores,
                generationAverageScores,
                originalPopulationAverageScoreMean,
                originalPopulationAverageScoreStdev
            )
        );
        builder.append("\n-----------------\n");
        float topAverageScore = Linq.max(generationAverageScores).getValue();
        for (Genome genome : averageScoreToGenomes.get(topAverageScore)) {
            builder.append("\tW: " + genome.getName() + "\n");
        }
        builder.append("\n\tChildren:\n\t--------\n");
        for (Genome genome : children) {
            builder.append("\tC: " + genome.toString() + "\n");
        }
        try {
            log.write(builder.toString());
            log.flush();
        } catch (IOException ex) {
            System.err.println(ex);
        }
    }

    public static String formatGenerationStatistics(
            int generation,
            ArrayList<Genome> generationPopulation,
            ArrayList<Float> generationIndividualScores,
            ArrayList<Float> generationAverageScores,
            Float originalPopulationAverageScoreMean,
            Float originalPopulationAverageScoreStdev
    ) {
        float averageGenerationScoreAverage = Linq.avgF(generationAverageScores).getValue();
        float averageGenerationScoreStdev = MathUtils.stdevP(generationAverageScores);
        return String.format(
            "Generation %-7d: %-7s %-12s %-12s %-10s %-10s %-12s %-12s %-10s %-10s %-12s %-12s",
            generation,
            String.format("n(%d)", generationPopulation.size()),
            /* Statistics around individual game scores across population */
            String.format("min_s(%5.2f)", Linq.min(generationIndividualScores).getValue()),
            String.format("max_s(%5.2f)", Linq.max(generationIndividualScores).getValue()),
            String.format("U_s(%5.2f)", Linq.avgF(generationIndividualScores).getValue()),
            String.format("S_s(%5.2f)", MathUtils.stdevP(generationIndividualScores)),
            /* Statistics around average game scores across population */
            String.format("min_u(%5.2f)", Linq.min(generationAverageScores).getValue()),
            String.format("max_u(%5.2f)", Linq.max(generationAverageScores).getValue()),
            String.format("U_u(%5.2f)", averageGenerationScoreAverage),
            String.format("S_u(%5.2f)", averageGenerationScoreStdev),
            /* Difference to original population */
            String.format("DU_u(%6.2f)", averageGenerationScoreAverage - originalPopulationAverageScoreMean),
            String.format("DS_u(%6.2f)", averageGenerationScoreStdev - originalPopulationAverageScoreStdev)
        );
    }



    public static ArrayList<Genome> run(
                Writer log,
                int threadCount,
                int initialPopulationSize,
                int maximumPopulationSize,
                float extinctionRate,
                int generations,
                int numberOfPlayers,
                int numberOfSamplesInRound
    ) {
        ArrayList<Genome> population = new ArrayList<Genome>();
        Float originalPopulationAverageScoresMean = null;
        Float originalPopulationAverageScoresStdev = null;
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        for (int generation = 1; generation <= generations; generation++) {

            /* Never let the population drop below the inital amount */
            while (population.size() < initialPopulationSize) {
                population.add(Genome.spawn());
            }

            /* Evaluate: Measure each agents performance and keep a record. */
            HashMap<Float, ArrayList<Genome>> averageScoreToGenomes = new HashMap<>();
            HashMap<Genome, Float> genomeToAverageScore = new HashMap<>();
            ArrayList<Float> populationAverageScores = new ArrayList<Float>();
            ArrayList<Float> populationIndividualScores = new ArrayList<Float>();

            /* Create all the simulation tasks */
            ArrayList<Callable<SimulationCallable.Result>> simulations = new ArrayList<>();
            for (Genome genome : population) {
                simulations.add(
                    new SimulationCallable(
                        genome,
                        numberOfPlayers,
                        numberOfSamplesInRound
                    )
                );
            }

            /* Wait for all the simulation tasks to finish */
            List<Future<SimulationCallable.Result>> futures = new ArrayList<>();
            try {
                futures = pool.invokeAll(simulations);
            } catch (InterruptedException ex) {
                System.err.print(ex);
                break;
            }

            /* Process the results of the simulations */
            for (Future<SimulationCallable.Result> future : futures) {
                try {
                    SimulationCallable.Result result = future.get();
                    for (Integer score : result.scores) {
                        populationIndividualScores.add((float)score);
                    }
                    float averageScore = Linq.avg(result.scores).getValue();
                    populationAverageScores.add(averageScore);
                    /* Preserve a mapping so we can go from scores to genomes
                     * and back from genomes to scores.
                     */
                    genomeToAverageScore.put(result.genome, averageScore);
                    if (!averageScoreToGenomes.containsKey(averageScore)) {
                        averageScoreToGenomes.put(averageScore, new ArrayList<Genome>());
                    }
                    averageScoreToGenomes.get(averageScore).add(result.genome);
                } catch (InterruptedException | ExecutionException ex) {
                    System.err.print(ex);
                }
            }

            /* Selection: Let the environment kill some percentage of the worst
             *            performing agents.
             */
            int currentPopulationSize = population.size();
            int numberOfGenomesToKeep = currentPopulationSize - (int)(currentPopulationSize * extinctionRate);
            ArrayList<Genome> orderedSurvivingGenomes = new ArrayList<>();
            Float[] averageScores = averageScoreToGenomes.keySet().toArray(new Float[0]);
            Arrays.sort(averageScores, Collections.reverseOrder());
            for (Float averageScore : averageScores) {
                for (Genome genome : averageScoreToGenomes.get(averageScore)) {
                    if (orderedSurvivingGenomes.size() == numberOfGenomesToKeep) {
                        break;
                    }
                    orderedSurvivingGenomes.add(genome);
                }
                if (orderedSurvivingGenomes.size() == numberOfGenomesToKeep) {
                    break;
                }
            }

            /* Reproduction: Have the survivours reproduce with preference
             *               given to the alphas of the pack.
             */
            ArrayList<Genome> newChildren = new ArrayList<Genome>();
            int survivingPopulationSize = orderedSurvivingGenomes.size();
            /* Let the top 20% of genomes be the alphas */
            Stack<Genome> reproductionQueue = new Stack<>();
            for (Genome survivingGenome : orderedSurvivingGenomes) {
                reproductionQueue.add(survivingGenome);
            }
            int numberOfAlphas = (int)(0.2 * survivingPopulationSize);
            Stack<Genome> alphaGenomes = new Stack<>();
            Stack<Genome> betaGenomes = new Stack<>();
            for (int i = 1; i <= numberOfAlphas; i++) {
                if (!reproductionQueue.empty()) {
                    alphaGenomes.add(reproductionQueue.pop());
                }
            }
            while (!reproductionQueue.empty()) {
                betaGenomes.add(reproductionQueue.pop());
            }
            /* Let the alphas reproduce with 3 betas each */
            while (!alphaGenomes.empty()) {
                Genome alphaGenome = alphaGenomes.pop();
                for (int i = 1; i <= 3; i++) {
                    if (!betaGenomes.empty()) {
                        Genome betaGenome = betaGenomes.pop();
                        newChildren.add(
                            Genome.mutate(
                                Genome.crossover(
                                    alphaGenome,
                                    genomeToAverageScore.get(alphaGenome),
                                    betaGenome,
                                    genomeToAverageScore.get(betaGenome)
                                )
                            )
                        );
                    }
                }
            }
            /* Let the remaining betas reproduce with each other */
            while (!betaGenomes.empty() && betaGenomes.size() >= 2) {
                Genome X = betaGenomes.pop();
                Genome Y = betaGenomes.pop();
                newChildren.add(
                    Genome.mutate(
                        Genome.crossover(
                            X,
                            genomeToAverageScore.get(X),
                            Y,
                            genomeToAverageScore.get(Y)
                        )
                    )
                );
            }
            /* Add the children into the surving pool */
            for (Genome child : newChildren) {
                orderedSurvivingGenomes.add(child);
            }

            /* Reporting: Display progress information */
            if (originalPopulationAverageScoresMean == null
                    && originalPopulationAverageScoresStdev == null
            ) {
                originalPopulationAverageScoresMean = Linq.avgF(populationAverageScores, new Identity<Float>()).getValue();
                originalPopulationAverageScoresStdev = MathUtils.stdevP(populationAverageScores);
            }

            logDetailedGenerationSummary(
                log,
                generation,
                population,
                populationIndividualScores,
                populationAverageScores,
                originalPopulationAverageScoresMean,
                originalPopulationAverageScoresStdev,
                averageScoreToGenomes,
                newChildren
            );
            System.out.println(
                formatGenerationStatistics(
                    generation,
                    population,
                    populationIndividualScores,
                    populationAverageScores,
                    originalPopulationAverageScoresMean,
                    originalPopulationAverageScoresStdev
                )
            );

            /* The surviving genomes and their children become the new population */
            population = orderedSurvivingGenomes;

            /* Culling: If the population size goes over the maximum perform a cull */
            if (population.size() > maximumPopulationSize) {
                /* Take the number to be 5% below the maximum */
                int numberToCull = (int)((population.size() - maximumPopulationSize) + (0.05 * population.size()));
                for (int i = 1; i <= numberToCull; i++) {
                    population.remove(RandomUtils.choose(population));
                }
            }
        }

        return population;
    }
}