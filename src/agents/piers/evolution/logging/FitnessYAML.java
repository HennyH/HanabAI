package agents.piers.evolution.logging;

public class FitnessYAML {
    public String name;
    public int ranking;
    public float[] scores;
    public float scoreMin;
    public float scoreMax;
    public float scoreMean;
    public float scoreStdev;
    public float scoreMeanDelta;
    public float scoreStdevDelta;

    public FitnessYAML(
            String name,
            int ranking,
            float scoreMin,
            float scoreMax,
            float scoreMean,
            float scoreStdev,
            float scoreMeanDelta,
            float scoreStdevDelta
    ) {
        this.name = name;
        this.ranking = ranking;
        this.scoreMin = scoreMin;
        this.scoreMax = scoreMax;
        this.scoreMean = scoreMean;
        this.scoreStdev = scoreStdev;
        this.scoreMeanDelta = scoreMeanDelta;
        this.scoreStdevDelta = scoreStdevDelta;
    }
}
