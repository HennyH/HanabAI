package agents.piers.evolution.logging;

public class GenerationSummaryYAML {
    public int generation;
    public PopulationStatisticsYAML statistics;
    public FitnessYAML[] rankings;
    public GenomeYAML[] population;
    public GenomeYAML[] children;

    public GenerationSummaryYAML(
            int generation,
            PopulationStatisticsYAML statistics,
            FitnessYAML[] rankings,
            GenomeYAML[] population,
            GenomeYAML[] children
    ) {
        this.generation = generation;
        this.statistics = statistics;
        this.rankings = rankings;
        this.population = population;
        this.children = children;
    }
}
