package agents.piers.evolution.logging;

public class GenomeYAML {
    public String name;
    public String[] parentsNames;
    public String mutationOf;
    public String dna;

    public GenomeYAML(
            String name,
            String[] parentsNames,
            String mutationOf,
            String dna
    ) {
        this.name = name;
        this.parentsNames = parentsNames;
        this.mutationOf = mutationOf;
        this.dna = dna;
    }

}
