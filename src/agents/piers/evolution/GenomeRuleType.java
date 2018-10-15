package agents.piers.evolution;

public enum GenomeRuleType {
    PlaySafe,
    PlayProbablySafe,
    TellAnyonePlayable,
    TellAnyoneUseful,
    TellAnyoneUseless,
    OsawaDiscard,
    RandomDiscard;

    @Override
    public String toString() {
        switch (this)  {
            case PlaySafe: return "PS";
            case PlayProbablySafe: return "PPS";
            case TellAnyonePlayable: return "TAP";
            case TellAnyoneUseful: return "TAU";
            case TellAnyoneUseless: return "TAD";
            case OsawaDiscard: return "OD";
            case RandomDiscard: return "RD";
        }
        return "?";
    }
}