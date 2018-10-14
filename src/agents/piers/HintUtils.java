package agents.piers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import hanabAI.Card;
import hanabAI.Colour;
import hanabAI.State;

public class HintUtils {

    public static Maybe<HintUtilityCalculation> determineBestHintToGive(
                State s,
                int playerRecievingHint,
                Func<CardHint, Boolean> viewOfCardAfterHintFilter,
                float weightingForPointingAtMoreCards,
                float weightingForValueOverColour,
                float weightingForColourOverValue,
                float weightingForHigherValues,
                float weightingForRevealingPlayableCard,
                float weightingForRevealingAUselessCard,
                Maybe<Float> utilityThreshold
    ) {
        ArrayList<CardHint> ownViewOfHand = new ArrayList<CardHint>(
            Arrays.asList(StateUtils.getHintsForPlayer(s, playerRecievingHint))
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
        ArrayList<Either<Colour, Integer>> possibleHintsToGive = new ArrayList<>();
        Card[] outsideViewOfHand = s.getHand(playerRecievingHint);

        for (CardHint ownViewOfCard : ownViewOfHand) {
            Card outsideViewOfCard = outsideViewOfHand[ownViewOfCard.getCardIndex()];
            /* It's possible we're giving a hint during the last round
             * where a player may have less than 5 cards.
             */
            if (outsideViewOfCard == null) {
                continue;
            }

            if (!ownViewOfCard.maybeGetActualColour().hasValue()) {
                Colour hintableColour = outsideViewOfCard.getColour();
                CardHint viewOfCardAfterHint = CardHint.is(ownViewOfCard, hintableColour);
                if (viewOfCardAfterHintFilter.apply(viewOfCardAfterHint)
                        && !possibleColoursToReveal.contains(hintableColour)
                ) {
                    possibleColoursToReveal.add(hintableColour);
                    possibleHintsToGive.add(new Either<Colour, Integer>(hintableColour, null));
                }
            }
            if (!ownViewOfCard.maybeGetActualValue().hasValue()) {
                Integer hintableValue = outsideViewOfCard.getValue();
                CardHint viewOfCardAfterHint = CardHint.is(ownViewOfCard, hintableValue);
                if (viewOfCardAfterHintFilter.apply(viewOfCardAfterHint)
                        && !possibleValuesToReveal.contains(hintableValue)
                ) {
                    possibleValuesToReveal.add(hintableValue);
                    possibleHintsToGive.add(new Either<Colour, Integer>(null, hintableValue));
                }
            }
        }

        /* We now have all the possible hints we could give to the player.
         * Examine each one.
         */
        HintUtilityCalculation bestHintCalculation = null;
        for (Either<Colour, Integer> hint : possibleHintsToGive) {
            HintUtilityCalculation calculation = CardUtils.calculateUtilityOfHintInformationForPlayer(
                s,
                playerRecievingHint,
                hint,
                weightingForPointingAtMoreCards,
                weightingForValueOverColour,
                weightingForColourOverValue,
                weightingForHigherValues,
                weightingForRevealingPlayableCard,
                weightingForRevealingAUselessCard
            );
            if (bestHintCalculation == null) {
                bestHintCalculation = calculation;
            } else if (bestHintCalculation.getUtility() < calculation.getUtility()) {
                bestHintCalculation = calculation;
            }
        }

        return new Maybe<>(bestHintCalculation);
    }

}