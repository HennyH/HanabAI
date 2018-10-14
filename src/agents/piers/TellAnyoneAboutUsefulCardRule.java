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

    public TellAnyoneAboutUsefulCardRule(
            int playerIndex,
            float usefullnessThreshold,
            float weightingForPointingAtMoreCards,
            float weightingForValueOverColour,
            float weightingForColourOverValue,
            float weightingForHigherValues,
            float weightingForRevealingPlayableCard
    ) {
        this._playerIndex = playerIndex;
        this._usefullnessThreshold = usefullnessThreshold;
        this._weightingForPointingAtMoreCards = weightingForPointingAtMoreCards;
        this._weightingForValueOverColour = weightingForValueOverColour;
        this._weightingForColourOverValue = weightingForColourOverValue;
        this._weightingForHigherValues = weightingForHigherValues;
        this._weightingForRevealingPlayableCard = weightingForRevealingPlayableCard;
    }

    private static Func<CardHint, Boolean> getFullyResolvedHintFilter() {
        return new Func<CardHint, Boolean>() {
            @Override
            public Boolean apply(CardHint hint) {
                return hint.maybeGetActualColour().hasValue() && hint.maybeGetActualValue().hasValue();
            }
        };
    }

    @Override
	public Action play(State s) {
        /* for each player figure out their view of their hand */
            /* remove cards they already know for ceartin */
                /* remove cards which even if they knew wouldn't be immedietly playable */
                    /* pick lowest value card to give hint for */
                        /* choose hint at random */

        HintUtilityCalculation bestHintCalculation = null;

        System.out.println("PLAYER " + ((Integer)this._playerIndex).toString()  + " IS LOOKING FOR HINT TO GIVE");

        for (int otherPlayerIndex : StateUtils.getPlayersOtherThan(s, this._playerIndex)) {

            System.out.println("\t LOOKING AT PLAYER " + ((Integer)otherPlayerIndex).toString());

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
                    TellAnyoneAboutUsefulCardRule.getFullyResolvedHintFilter()
                )
            );
            /* Remove cards that even if the player were to know about, they
             * would not be able to play the card safely.
             */
            // ownViewOfHand = Linq.filter(
            //     ownViewOfHand,
            //     TellAnyoneAboutUsefulCardRule.getCardOfFullyResolvedHintUnplayableFilter(s, otherPlayerIndex)
            // );

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

            System.out.println("\n\t OWN VIEW:");
            System.out.println("\t " + Arrays.toString(ownViewOfHand.toArray()));
            System.out.println("\t OUTSIDE VIEW:");
            System.out.println("\t " + Arrays.toString(outsideViewOfHand));
            System.out.println("\t COLOUR HINTS:");
            System.out.println("\t " + Arrays.toString(possibleColoursToReveal.toArray()));
            System.out.println("\t VALUE HINTS:");
            System.out.println("\t " + Arrays.toString(possibleValuesToReveal.toArray()));
            System.out.println("\t --------------\t");

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
                    this._weightingForRevealingPlayableCard
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
                    this._weightingForRevealingPlayableCard
                );

                if (bestHintCalculation == null) {
                    bestHintCalculation = calculation;
                } else if (bestHintCalculation.getUtility() < calculation.getUtility()) {
                    bestHintCalculation = calculation;
                }
            }
        }

        if (bestHintCalculation != null && bestHintCalculation.getUtility() >= this._usefullnessThreshold) {
            System.out.println("\t GIVING HINT TO PLAYER " + ((Integer)bestHintCalculation.getPlayerRecievingHintIndex()).toString());
            System.out.println("\t colour = " + bestHintCalculation.getHintedColour().toString() + "\t value = " + bestHintCalculation.getHintedValue().toString());
            System.out.println("\t " + (Arrays.toString(bestHintCalculation.getCardPointedAtArray())));
            System.out.println("\t -------------------");

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