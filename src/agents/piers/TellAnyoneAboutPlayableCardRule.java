package agents.piers;

import java.util.ArrayList;

import hanabAI.Action;
import hanabAI.Card;
import hanabAI.IllegalActionException;
import hanabAI.State;

public class TellAnyoneAboutPlayableCardRule implements IRule {

    private int _playerIndex;
    private float _weightingForPointingAtMoreCards;
    private float _weightingForValueOverColour;
    private float _weightingForColourOverValue;
    private float _weightingForHigherValues;
    private float _weightingForRevealingPlayableCard;
    private float _weightingForRevealingAUselessCard;
    private float _weightingForPointingAtLessDistantFuturePlayableCards;

    public TellAnyoneAboutPlayableCardRule(
            int playerIndex,
            float weightingForPointingAtMoreCards,
            float weightingForValueOverColour,
            float weightingForColourOverValue,
            float weightingForHigherValues,
            float weightingForRevealingPlayableCard,
            float weightingForRevealingAUselessCard,
            float weightingForPointingAtLessDistantFuturePlayableCards
    ) {
        this._playerIndex = playerIndex;
        this._weightingForPointingAtMoreCards = weightingForPointingAtMoreCards;
        this._weightingForValueOverColour = weightingForValueOverColour;
        this._weightingForColourOverValue = weightingForColourOverValue;
        this._weightingForHigherValues = weightingForHigherValues;
        this._weightingForRevealingPlayableCard = weightingForRevealingPlayableCard;
        this._weightingForRevealingAUselessCard = weightingForRevealingAUselessCard;
        this._weightingForPointingAtLessDistantFuturePlayableCards = weightingForPointingAtLessDistantFuturePlayableCards;
    }

    @Override
	public Action play(State s) {
        if (!StateUtils.isHintActionAllowed(s)) {
            return null;
        }

        HintUtilityCalculation bestHintCalculation = null;
        ArrayList<Card> targetCards = StateUtils.getPlayableFireworksCards(s);

		for (int otherPlayerIndex : StateUtils.getPlayersOtherThan(s, this._playerIndex)) {
            Maybe<HintUtilityCalculation> bestHintForPlayer = HintUtils.determineBestHintToGive(
                s,
                otherPlayerIndex,
                new Func<CardHint, Boolean>() {
                    @Override
                    public Boolean apply(CardHint viewOfCardAfterHint) {
                        return CardUtils.doesDeckContainCard(targetCards, viewOfCardAfterHint);
                    }
                },
                this._weightingForPointingAtMoreCards,
                this._weightingForValueOverColour,
                this._weightingForColourOverValue,
                this._weightingForHigherValues,
                this._weightingForRevealingPlayableCard,
                this._weightingForRevealingAUselessCard,
                this._weightingForPointingAtLessDistantFuturePlayableCards
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

        if (bestHintCalculation != null) {
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