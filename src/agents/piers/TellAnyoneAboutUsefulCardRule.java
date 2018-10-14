package agents.piers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import hanabAI.Action;
import hanabAI.Card;
import hanabAI.Colour;
import hanabAI.IllegalActionException;
import hanabAI.State;

public class TellAnyoneAboutUsefulCardRule implements IRule {

    private int _playerIndex;
    private float _usefullnessThreshold;
    private float _weightingForPointingAtMoreCards;
    private float _weightingForValueOverColour;
    private float _weightingForColourOverValue;
    private float _weightingForHigherValues;
    private float _weightingForRevealingPlayableCard;
    private float _weightingForRevealingAUselessCard;

    public TellAnyoneAboutUsefulCardRule(
            int playerIndex,
            float usefullnessThreshold,
            float weightingForPointingAtMoreCards,
            float weightingForValueOverColour,
            float weightingForColourOverValue,
            float weightingForHigherValues,
            float weightingForRevealingPlayableCard,
            float weightingForRevealingAUselessCard
    ) {
        this._playerIndex = playerIndex;
        this._usefullnessThreshold = usefullnessThreshold;
        this._weightingForPointingAtMoreCards = weightingForPointingAtMoreCards;
        this._weightingForValueOverColour = weightingForValueOverColour;
        this._weightingForColourOverValue = weightingForColourOverValue;
        this._weightingForHigherValues = weightingForHigherValues;
        this._weightingForRevealingPlayableCard = weightingForRevealingPlayableCard;
        this._weightingForRevealingAUselessCard = weightingForRevealingAUselessCard;
    }

    @Override
	public Action play(State s) {
        HintUtilityCalculation bestHintCalculation = null;

        for (int otherPlayerIndex : StateUtils.getPlayersOtherThan(s, this._playerIndex)) {

            ArrayList<CardHint> ownViewOfHand = new ArrayList<CardHint>(
                Arrays.asList(StateUtils.getHintsForPlayer(s, otherPlayerIndex))
            );
            /* Remove cards which the player already has full information
             * about. There is not point providing a hint if it doesn't provide
             * information.
             */
            ownViewOfHand = Linq.filter(
                ownViewOfHand,
                Func.invert(
                    CardHint.getFullyResolvedHintFilter()
                )
            );

            /* Figure out what possible hints we could give. */
            HashSet<Colour> possibleColoursToReveal = new HashSet<Colour>();
            HashSet<Integer> possibleValuesToReveal = new HashSet<Integer>();
            Card[] outsideViewOfHand = s.getHand(otherPlayerIndex);

            for (CardHint ownViewOfCard : ownViewOfHand) {
                Card outsideViewOfCard = outsideViewOfHand[ownViewOfCard.getCardIndex()];
                /* It's possible we're giving a hint during the last round
                 * where a player may have less than 5 cards.
                 */
                if (outsideViewOfCard == null) {
                    continue;
                }

                /* If the player doesn't know the colour of their card add it
                 * as a potential colour hint.
                 */
                if (!ownViewOfCard.maybeGetActualColour().hasValue()) {
                    possibleColoursToReveal.add(
                        outsideViewOfCard.getColour()
                    );
                }
                if (!ownViewOfCard.maybeGetActualValue().hasValue()) {
                    possibleValuesToReveal.add(
                        outsideViewOfCard.getValue()
                    );
                }
            }

            /* We now have all the possible hints we could give to the player.
             * Examine each one.
             */
            for (Colour hintColour : possibleColoursToReveal) {
                HintUtilityCalculation calculation = CardUtils.calculateUtilityOfHintInformationForPlayer(
                    s,
                    otherPlayerIndex,
                    new Maybe<Colour>(hintColour),
                    new Maybe<Integer>(null),
                    this._weightingForPointingAtMoreCards,
                    this._weightingForValueOverColour,
                    this._weightingForColourOverValue,
                    this._weightingForHigherValues,
                    this._weightingForRevealingPlayableCard,
                    this._weightingForRevealingAUselessCard
                );

                if (bestHintCalculation == null) {
                    bestHintCalculation = calculation;
                } else if (bestHintCalculation.getUtility() < calculation.getUtility()) {
                    bestHintCalculation = calculation;
                }
            }
            for (Integer hintValue : possibleValuesToReveal) {
                HintUtilityCalculation calculation = CardUtils.calculateUtilityOfHintInformationForPlayer(
                    s,
                    otherPlayerIndex,
                    new Maybe<Colour>(null),
                    new Maybe<Integer>(hintValue),
                    this._weightingForPointingAtMoreCards,
                    this._weightingForValueOverColour,
                    this._weightingForColourOverValue,
                    this._weightingForHigherValues,
                    this._weightingForRevealingPlayableCard,
                    this._weightingForRevealingAUselessCard
                );

                if (bestHintCalculation == null) {
                    bestHintCalculation = calculation;
                } else if (bestHintCalculation.getUtility() < calculation.getUtility()) {
                    bestHintCalculation = calculation;
                }
            }
        }

        if (bestHintCalculation != null && bestHintCalculation.getUtility() >= this._usefullnessThreshold) {
            try {
                if (bestHintCalculation.getHintedColour().hasValue()) {
                    return new Action(
                        this._playerIndex,
                        s.getName(this._playerIndex),
                        bestHintCalculation.getHintActionType(),
                        bestHintCalculation.getPlayerRecievingHintIndex(),
                        bestHintCalculation.getCardPointedAtArray(),
                        bestHintCalculation.getHintedColour().getValue()
                    );
                } else {
                    return new Action(
                        this._playerIndex,
                        s.getName(this._playerIndex),
                        bestHintCalculation.getHintActionType(),
                        bestHintCalculation.getPlayerRecievingHintIndex(),
                        bestHintCalculation.getCardPointedAtArray(),
                        bestHintCalculation.getHintedValue().getValue()
                    );
                }
            } catch (IllegalActionException ex) {
                System.out.println(ex.getStackTrace());
            }
        }

        return null;
	}
}