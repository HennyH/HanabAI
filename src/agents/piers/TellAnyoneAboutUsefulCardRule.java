package agents.piers;

import hanabAI.Action;
import hanabAI.IllegalActionException;
import hanabAI.State;

public class TellAnyoneAboutUsefulCardRule implements IRule {

    private int _playerIndex;
    private float _utilityThreshold;
    private float _weightingForPointingAtMoreCards;
    private float _weightingForValueOverColour;
    private float _weightingForColourOverValue;
    private float _weightingForHigherValues;
    private float _weightingForRevealingPlayableCard;
    private float _weightingForRevealingAUselessCard;

    public TellAnyoneAboutUsefulCardRule(
            int playerIndex,
            float utilityThreshold,
            float weightingForPointingAtMoreCards,
            float weightingForValueOverColour,
            float weightingForColourOverValue,
            float weightingForHigherValues,
            float weightingForRevealingPlayableCard,
            float weightingForRevealingAUselessCard
    ) {
        this._playerIndex = playerIndex;
        this._utilityThreshold = utilityThreshold;
        this._weightingForPointingAtMoreCards = weightingForPointingAtMoreCards;
        this._weightingForValueOverColour = weightingForValueOverColour;
        this._weightingForColourOverValue = weightingForColourOverValue;
        this._weightingForHigherValues = weightingForHigherValues;
        this._weightingForRevealingPlayableCard = weightingForRevealingPlayableCard;
        this._weightingForRevealingAUselessCard = weightingForRevealingAUselessCard;
    }

    @Override
	public Action play(State s) {
        if (!StateUtils.isHintActionAllowed(s)) {
            return null;
        }

        HintUtilityCalculation bestHintCalculation = null;

        for (int otherPlayerIndex : StateUtils.getPlayersOtherThan(s, this._playerIndex)) {
            Maybe<HintUtilityCalculation> bestHintForPlayer = HintUtils.determineBestHintToGive(
                s,
                otherPlayerIndex,
                new Func<CardHint, Boolean>() {
                    @Override
                    public Boolean apply(CardHint viewOfCardAfterHint) {
                        return true;
                    }
                },
                this._weightingForPointingAtMoreCards,
                this._weightingForValueOverColour,
                this._weightingForColourOverValue,
                this._weightingForHigherValues,
                this._weightingForRevealingPlayableCard,
                this._weightingForRevealingAUselessCard
            );

            if (!bestHintForPlayer.hasValue()) {
                continue;
            }

            if (bestHintCalculation == null) {
                bestHintCalculation = bestHintForPlayer.getValue();
            } else if (bestHintForPlayer.hasValue()
                    && bestHintForPlayer.getValue().getUtility() > bestHintCalculation.getUtility()
            ) {
                bestHintCalculation = bestHintForPlayer.getValue();
            }
        }

        if (bestHintCalculation != null
                && bestHintCalculation.getUtility() >= this._utilityThreshold
        ) {
            try {
                return HintUtilityCalculation.convertToAction(s, this._playerIndex, bestHintCalculation);
            } catch (IllegalActionException ex) {
                System.out.println(ex.getStackTrace());
                return null;
            }
        }

        return null;
	}
}