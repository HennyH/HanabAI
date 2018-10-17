package agents.piers.evolution.logging;

public class GenerationSummaryYAML {
    public int generation;
    public PopulationStatisticsYAML statistics;
    public FitnessYAML[] rankings;
    public String[] population;
    public GenomeYAML[] children;

    public GenerationSummaryYAML(
            int generation,
            PopulationStatisticsYAML statistics,
            FitnessYAML[] rankings,
            String[] population,
            GenomeYAML[] children
    ) {
        this.generation = generation;
        this.statistics = statistics;
        this.rankings = rankings;
        this.population = population;
        this.children = children;
    }
}
