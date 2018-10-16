package agents.piers.evolution;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import agents.piers.RandomUtils;
import hanabAI.Agent;
import hanabAI.Hanabi;

public class EvolutionRunner {

    public static void logGenerationSummary(
            PrintWriter log,
            int generation,
            int topScore,
            HashMap<Integer, ArrayList<Genome>> scoreToGenomes,
            ArrayList<Genome> newChildren
    ) {
        StringBuilder builder = new StringBuilder();
        builder.append(String.format("Generation %d, Top Score %d: %n", generation, topScore));
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

    public static ArrayList<Genome> run(PrintWriter log, int populationSize, int cullSize, int generations, int numberOfPlayers, int roundSamples) {
        ArrayList<Genome> population = new ArrayList<Genome>();
        for (int i = 1; i <= populationSize; i++) {
            population.add(Genome.spawn());
        }

        for (int generation = 1; generation <= generations; generation++) {
            System.err.println(String.format("Generation %d", generation));
            /* Run the simulations and rank the agents */
            HashMap<Integer, ArrayList<Genome>> scoreToGenomes = new HashMap<>();
            HashMap<Genome, Integer> genomeToScore = new HashMap<>();
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
                int averageScore = (int)Math.floor((double)totalScore / (double)roundSamples);
                genomeToScore.put(genome, averageScore);
                if (!scoreToGenomes.containsKey(averageScore)) {
                    scoreToGenomes.put(averageScore, new ArrayList<Genome>());
                }
                scoreToGenomes.get(averageScore).add(genome);
            }

            /* Cull the worst performers */
            int genomesSurvived = 0;
            int genomesToKeep = populationSize - cullSize;
            ArrayList<Genome> survivingAgents = new ArrayList<>();
            Integer[] scores = scoreToGenomes.keySet().toArray(new Integer[0]);
            Arrays.sort(scores);
            for (Integer score : scores) {
                for (Genome genome : scoreToGenomes.get(score)) {
                    if (genomesSurvived == genomesToKeep) {
                        break;
                    }
                    genomesSurvived++;
                    survivingAgents.add(genome);
                }
                if (genomesSurvived == genomesToKeep) {
                    break;
                }
            }

            /* Choose pairs randomly to produce offspring to make the population stable. */
            ArrayList<Genome> newChildren = new ArrayList<Genome>();
            for (int i = 1; i <= cullSize; i++) {
                ArrayList<Genome> parents = RandomUtils.sample(survivingAgents, 2);
                Genome X = parents.get(0);
                Genome Y = parents.get(1);
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
                survivingAgents.add(child);
            }

            logGenerationSummary(log, generation, scores[0], scoreToGenomes, newChildren);
            log.flush();
            population = survivingAgents;
        }

        return population;
    }
}