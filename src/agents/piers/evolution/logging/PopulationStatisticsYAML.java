package agents.piers.evolution.logging;

public class PopulationStatisticsYAML {
    public float pSize;
    public float pScoresMin;
    public float pScoresMax;
    public float pScoresMean;
    public float pScoresStdev;
    public float pScoresMeanDelta;
    public float pScoresStdevDelta;

    public PopulationStatisticsYAML(
            float pSize,
            float pScoresMin,
            float pScoresMax,
            float pScoresMean,
            float pScoresStdev,
            float pScoresMeanDelta,
            float pScoresStdevDelta
    ) {
        this.pSize = pSize;
        this.pScoresMin = pScoresMin;
        this.pScoresMax = pScoresMax;
        this.pScoresMean = pScoresMean;
        this.pScoresStdev = pScoresStdev;
        this.pScoresMeanDelta = pScoresMeanDelta;
        this.pScoresStdevDelta = pScoresStdevDelta;
    }
}
