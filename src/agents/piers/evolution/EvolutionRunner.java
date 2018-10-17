package agents.piers.evolution;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;

import agents.piers.Identity;
import agents.piers.Linq;
import agents.piers.MathUtils;
import agents.piers.RandomUtils;
import agents.piers.evolution.logging.EvolutionLogger;
import agents.piers.evolution.logging.GenerationSummaryYAML;
import hanabAI.Agent;
import hanabAI.Hanabi;

public class EvolutionRunner {

    public static void dumpGenerationLog(
            String logDirectory,
            int generation,
            ArrayList<Float> initialPopulationsScores,
            ArrayList<Genome> population,
            HashMap<Genome, ArrayList<Float>> genomeToScores,
            ArrayList<Genome> orderedGenomes,
            ArrayList<Genome> children
    ) {
        /* Create a log file for the generation and get a PrintWriter for it
         * so it's easier to print to.
         */
        File logFile = Paths
            .get(logDirectory, String.format("generation-%d.txt", generation))
            .toAbsolutePath()
            .toFile();
        File logFileDirectory = Paths.get(logDirectory).toAbsolutePath().toFile();
        if (!logFileDirectory.exists()) {
            logFileDirectory.mkdirs();
        }
        try {
            FileOutputStream logFileOutputStream = new FileOutputStream(logFile, false);
            OutputStreamWriter logFileStreamWriter = new OutputStreamWriter(logFileOutputStream, "UTF-8");
            BufferedWriter logFileBufferedWriter = new BufferedWriter(logFileStreamWriter);
            GenerationSummaryYAML summary = EvolutionLogger.createSummaryYAML(
                generation,
                initialPopulationsScores,
                population,
                genomeToScores,
                orderedGenomes,
                children
            );
            DumperOptions options = new DumperOptions();
            options.setIndent(4);
            Yaml yaml  = new Yaml(options);
            yaml.dump(summary, logFileBufferedWriter);
            logFileBufferedWriter.flush();
            logFileBufferedWriter.close();
        } catch (Exception ex) {
            System.err.println(ex);
        }
    }

    public static String formatGenerationStatistics(
            int generation,
            ArrayList<Genome> generationPopulation,
            ArrayList<Float> generationIndividualScores,
            ArrayList<Float> generationAverageScores,
            Float originalPopulationAverageScoreMean,
            Float originalPopulationAverageScoreStdev,
            HashMap<Float, ArrayList<Genome>> averageScoreToGenomes
    ) {
        float averageGenerationScoreAverage = Linq.avgF(generationAverageScores).getValue();
        float averageGenerationScoreStdev = MathUtils.stdevP(generationAverageScores);
        float maximumAverageScore = Linq.max(generationAverageScores).getValue();
        float minimumAverageScore = Linq.min(generationAverageScores).getValue();
        return String.format(
            "Generation %-7d: %-7s %-12s %-12s %-10s %-10s %-12s %-12s %-10s %-10s %-12s %-12s%n%n\tStrongest: %s%n\tWeakest:   %s%n",
            generation,
            String.format("n(%d)", generationPopulation.size()),
            /* Statistics around individual game scores across population */
            String.format("min_s(%5.2f)", Linq.min(generationIndividualScores).getValue()),
            String.format("max_s(%5.2f)", Linq.max(generationIndividualScores).getValue()),
            String.format("U_s(%5.2f)", Linq.avgF(generationIndividualScores).getValue()),
            String.format("S_s(%5.2f)", MathUtils.stdevP(generationIndividualScores)),
            /* Statistics around average game scores across population */
            String.format("min_u(%5.2f)", minimumAverageScore),
            String.format("max_u(%5.2f)", maximumAverageScore),
            String.format("U_u(%5.2f)", averageGenerationScoreAverage),
            String.format("S_u(%5.2f)", averageGenerationScoreStdev),
            /* Difference to original population */
            String.format("DU_u(%6.2f)", averageGenerationScoreAverage - originalPopulationAverageScoreMean),
            String.format("DS_u(%6.2f)", averageGenerationScoreStdev - originalPopulationAverageScoreStdev),
            Linq.first(averageScoreToGenomes.get(maximumAverageScore)).getValue().formatDna(),
            Linq.first(averageScoreToGenomes.get(minimumAverageScore)).getValue().formatDna()
        );
    }

    public static ArrayList<Genome> run(
                String logDirectory,
                ArrayList<String> seedDnas,
                int threadCount,
                int initialPopulationSize,
                int maximumPopulationSize,
                float spawnSeedChance,
                float extinctionRate,
                int generations,
                int numberOfPlayers,
                int numberOfSamplesInRound
    ) {
        ArrayList<Genome> population = new ArrayList<Genome>();
        ArrayList<Float> initialPopulationScores = new ArrayList<>();
        ExecutorService pool = Executors.newFixedThreadPool(threadCount);

        for (int generation = 1; generation <= generations; generation++) {

            /* Never let the population drop below the inital amount */
            while (population.size() < initialPopulationSize) {
                if (RandomUtils.chance(spawnSeedChance)) {
                    population.add(Genome.parseDna(RandomUtils.choose(seedDnas)));
                } else {
                    population.add(Genome.spawnRandom());
                }
            }

            /* Evaluate: Measure each agents performance and keep a record. */
            HashMap<Genome, ArrayList<Float>> genomeToScores = new HashMap<>();
            ArrayList<Float> populationScores = new ArrayList<>();

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
                    for (Float score : result.scores) {
                        populationScores.add(score);
                    }
                    genomeToScores.put(result.genome, result.scores);
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
            HashMap<Float, ArrayList<Genome>> averageScoreToGenomes = new HashMap<>();
            HashMap<Genome, Float> genomeToAverageScore = new HashMap<>();
            for (Map.Entry<Genome, ArrayList<Float>> entry : genomeToScores.entrySet()) {
                Genome genome = entry.getKey();
                Float averageScore = Linq.avgF(entry.getValue()).getValue();
                if (!averageScoreToGenomes.containsKey(averageScore)) {
                    averageScoreToGenomes.put(averageScore, new ArrayList<Genome>());
                }
                averageScoreToGenomes.get(averageScore).add(genome);
                genomeToAverageScore.put(genome, averageScore);
            }
            /* Get all the average scores out and sort them */
            Float[] sortedScores = averageScoreToGenomes.keySet().toArray(new Float[0]);
            Arrays.sort(sortedScores, Collections.reverseOrder());
            /* Now that he have the scores in order, and a mapping from scores to
             * genomes we can go over them in order.
             */
            for (Float averageScore : sortedScores) {
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

            /* Reporting: Display progress information */
            if (generation == 1) {
                @SuppressWarnings("unchecked")
                ArrayList<Float> copyOfScores = (ArrayList<Float>)populationScores.clone();
                initialPopulationScores = copyOfScores;
            }

            dumpGenerationLog(
                logDirectory,
                generation,
                initialPopulationScores,
                population,
                genomeToScores,
                orderedSurvivingGenomes,
                newChildren
            );

            /* Add the children into the surving pool */
            for (Genome child : newChildren) {
                orderedSurvivingGenomes.add(child);
            }

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

    private static String defaultSeedDna = "{R=PS;L=1-3;H=0-0;U=0.063310;W=0.06330971, 0.117145516, 0.15623689, 0.10644951, -0.24107927, 0.12780342, 0.4718204, 0.7247719}>{R=PPS;L=1-3;H=0-8;U=0.668297;W=0.6682967, 0.11869569, 0.100679815, 0.12953922, -0.2827804, 0.12144014, 0.48272705, 0.778931}>{R=TAP;L=1-3;H=0-8;U=0.159808;W=0.15980834, 0.12995258, 0.21847118, 0.11413119, -0.10630783, 0.05989501, 0.43849492, 0.5681055}>{R=PS;L=1-3;H=0-8;U=0.018239;W=0.018238984, 0.12550148, 0.08266041, 0.118032835, -0.28588778, 0.08660595, 0.487055, 0.79237664}>{R=TAP;L=3-3;H=5-4;U=0.039606;W=0.03960633, 0.10325708, 0.13658598, 0.13309912, -0.30415744, 0.12176984, 0.4976891, 0.757141}>{R=TAU;L=1-3;H=0-8;U=0.094780;W=0.094779514, 0.17015794, 0.10871784, 0.08933269, -0.24344909, 0.1750237, 0.33825153, 0.6318985}>{R=OD;L=0-3;H=0-8;U=0.024424;W=0.024423653, 0.15138336, 0.14075342, 0.09473344, -0.18523018, 0.14681208, 0.5336484, 0.7439554}>{R=TAU;L=0-3;H=0-8;U=0.803651;W=0.803651, 0.39848068, -0.06410061, -0.28458428, 0.060878616, 0.3048276, 0.35715824, 0.27665156}>{R=OD;L=3-3;H=2-8;U=0.000367;W=3.671875E-4, 0.099656254, 0.10459375, 0.097593755, -0.29634374, 0.09715626, 0.50240624, 0.7982188}>{R=TAD;L=3-1;H=8-0;U=0.815959;W=0.81595933, 0.055366226, -0.32371068, 0.7377975, 0.13841186, 0.35326564, -0.8821597, 0.73575056}>{R=OD;L=3-3;H=2-0;U=0.788791;W=0.78879106, 0.5902765, 0.1043749, -0.37653336, 0.68054354, 0.57003605, -0.36801544, -0.07650026}";

    public static void main(String[] args) {
        HashMap<String, Object> parameters = new HashMap<>();
        parameters.put("seedDnas", defaultSeedDna);
        parameters.put("threadCount", Runtime.getRuntime().availableProcessors());
        parameters.put("initialPopulationSize", 50);
        parameters.put("maximumPopulationSize", 400);
        parameters.put("spawnSeedChance", (float)0.8);
        parameters.put("extinctionRate", (float)0.05);
        parameters.put("generations", 200);
        parameters.put("numberOfPlayers", 4);
        parameters.put("numberOfSamplesInRound", 20);

        if (args.length > 1) {
            for (int i = 1; i < args.length; i++) {
                /* Args are of the form <param>::[f|i|urlenc]::<value> */
                String arg = args[i];
                String[] components = arg.split("::");
                String parameter = components[0];
                String parameterType = components[1];
                String value = components[2];
                if (parameterType == "f") {
                    parameters.put(parameter, Float.parseFloat(value));
                } else if (parameterType == "i") {
                    parameters.put(parameter, Integer.parseInt(value));
                } else if (parameterType == "urlenc") {
                    try {
                        parameters.put(parameter, URLDecoder.decode(value, "UTF8"));
                    } catch (UnsupportedEncodingException ex) {
                        System.err.println(ex);
                    }
                } else {
                    throw new InvalidParameterException(
                        String.format("Parameter type %s not recognised.", parameterType)
                    );
                }
            }
        }

        if (!parameters.containsKey("logDir")) {
            parameters.put(
                "logDir",
                Paths.get("evolution-logs", UUID.randomUUID().toString()).toString()
            );
        }

        EvolutionRunner.run(
            (String)parameters.get("logDir"),
            new ArrayList<String>(Arrays.asList(((String)parameters.get("seedDnas")).split("::"))),
            (int)parameters.get("threadCount"),
            (int)parameters.get("initialPopulationSize"),
            (int)parameters.get("maximumPopulationSize"),
            (float)parameters.get("spawnSeedChance"),
            (float)parameters.get("extinctionRate"),
            (int)parameters.get("generations"),
            (int)parameters.get("numberOfPlayers"),
            (int)parameters.get("numberOfSamplesInRound")
        );
    }
}