package agents.piers.evolution;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;

import agents.piers.Identity;
import agents.piers.Linq;
import agents.piers.MathUtils;
import agents.piers.RandomUtils;
import hanabAI.Agent;
import hanabAI.Hanabi;

public class EvolutionRunner {

    public static void logDetailedGenerationSummary(
            PrintWriter log,
            int generation,
            Float topScore,
            HashMap<Float, ArrayList<Genome>> scoreToGenomes,
            ArrayList<Genome> newChildren,
            ArrayList<Genome> population,
            ArrayList<Float> populationAverageScores
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append(
            String.format(
                "Generation %-7d: %-7s %-10s %-8s %-8s",
                generation,
                String.format("n(%d)", population.size()),
                String.format("max(%5.2f)", topScore),
                String.format("μ(%5.2f)", Linq.avgF(populationAverageScores, new Identity<Float>()).getValue()),
                String.format("σ(%5.2f)", MathUtils.stdevP(populationAverageScores))
            )
        );
        builder.append("-----------------\n");
        for (Genome genome : scoreToGenomes.get(topScore)) {
            builder.append("\t " + genome.toString() + "\n");
        }
        builder.append("\n\tChildren:\n\t--------\n");
        for (Genome genome : newChildren) {
            builder.append("\t>" + genome.toString() + "\n");
        }
        log.println(builder.toString());
    }

    public static void printShortGenerationSummary(
            PrintWriter log,
            int generation,
            Float topScore,
            HashMap<Float, ArrayList<Genome>> scoreToGenomes,
            ArrayList<Genome> newChildren,
            ArrayList<Genome> population,
            ArrayList<Float> populationAverageScores
    ) {
        System.out.println(
            String.format(
                "Generation %-7d: %-7s %-10s %-8s %-8s",
                generation,
                String.format("n(%d)", population.size()),
                String.format("max(%5.2f)", topScore),
                String.format("μ(%5.2f)", Linq.avgF(populationAverageScores, new Identity<Float>()).getValue()),
                String.format("σ(%5.2f)", MathUtils.stdevP(populationAverageScores))
            )
        );
    }



    public static ArrayList<Genome> run(
                PrintWriter log,
                int initialPopulationSize,
                int maximumPopulationSize,
                float extinctionRate,
                int generations,
                int numberOfPlayers,
                int roundSamples
    ) {
        ArrayList<Genome> population = new ArrayList<Genome>();

        for (int generation = 1; generation <= generations; generation++) {

            /* Never let the population drop below the inital amount */
            while (population.size() < initialPopulationSize) {
                population.add(Genome.spawn());
            }

            /* Evaluate: Measure each agents performance and keep a record. */
            HashMap<Float, ArrayList<Genome>> scoreToGenomes = new HashMap<>();
            HashMap<Genome, Float> genomeToScore = new HashMap<>();
            ArrayList<Float> populationAverageScores = new ArrayList<Float>();
            for (Genome genome : population) {
                int totalScore = 0;
                for (int round = 1; round <= roundSamples; round++) {
                    Agent[] agents = new Agent[numberOfPlayers];
                    for (int playerIndex = 0; playerIndex < numberOfPlayers; playerIndex++) {
                        agents[playerIndex] = Genome.asAgent(genome, playerIndex);
                    }
                    Hanabi game = new Hanabi(agents);
                    totalScore += game.play();
                }
                float averageScore = (float)((double)totalScore / (double)roundSamples);
                populationAverageScores.add(averageScore);
                genomeToScore.put(genome, averageScore);
                if (!scoreToGenomes.containsKey(averageScore)) {
                    scoreToGenomes.put(averageScore, new ArrayList<Genome>());
                }
                scoreToGenomes.get(averageScore).add(genome);
            }

            /* Selection: Let the environment kill some percentage of the worst
             *            performing agents.
             */
            int currentPopulationSize = population.size();
            int numberOfGenomesToKeep = currentPopulationSize - (int)(currentPopulationSize * extinctionRate);
            ArrayList<Genome> orderedSurvivingGenomes = new ArrayList<>();
            Float[] scores = scoreToGenomes.keySet().toArray(new Float[0]);
            Arrays.sort(scores, Collections.reverseOrder());
            for (Float score : scores) {
                for (Genome genome : scoreToGenomes.get(score)) {
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
                                    genomeToScore.get(alphaGenome),
                                    betaGenome,
                                    genomeToScore.get(betaGenome)
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
                            genomeToScore.get(X),
                            Y,
                            genomeToScore.get(Y)
                        )
                    )
                );
            }
            /* Add the children into the surving pool */
            for (Genome child : newChildren) {
                orderedSurvivingGenomes.add(child);
            }

            population = orderedSurvivingGenomes;

            /* Culling: If the population size goes over the maximum perform a cull */
            if (population.size() > maximumPopulationSize) {
                /* Take the number to be 5% below the maximum */
                int numberToCull = (int)((population.size() - maximumPopulationSize) + (0.05 * population.size()));
                for (int i = 1; i <= numberToCull; i++) {
                    population.remove(RandomUtils.choose(population));
                }
            }

            logDetailedGenerationSummary(log, generation, scores[0], scoreToGenomes, newChildren, population, populationAverageScores);
            printShortGenerationSummary(log, generation, scores[0], scoreToGenomes, newChildren, population, populationAverageScores);
            log.flush();
        }

        return population;
    }
}