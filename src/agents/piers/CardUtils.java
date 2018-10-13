package agents.piers;

import java.util.ArrayList;
import java.util.Arrays;

import hanabAI.ActionType;
import hanabAI.Card;
import hanabAI.Colour;
import hanabAI.State;

public class CardUtils {

    public static int getHighestValueOfCardType(Card c) {
        ArrayList<Card> sameColouredCards = Linq.filter(
            DeckUtils.getHanabiDeck(),
            CardUtils.getColorFilter(c.getColour())
        );
        int maxValue = -1;
        for (Card sameColouredCard : sameColouredCards) {
            maxValue = Math.max(maxValue, sameColouredCard.getValue());
        }
        return maxValue;
    }

    public static int[] getPossibleCardValues() {
        int[] values = new int[5];
        int j = 0;
        for (int i = 1; i <= 5; i++) {
            values[j++] = i;
        }
        return values;
    }

    public static boolean doesCardHasMaximumValue(Card c) {
        return c.getValue() == 5;
    }

    public static Maybe<Card> getNextHighestCardWithSameColour(Card c) {
        return CardUtils.doesCardHasMaximumValue(c)
            ? new Maybe<Card>(null)
            : new Maybe<Card>(new Card(c.getColour(), c.getValue() + 1));
    }

    public static boolean hintMatchesCard(Card c, CardHint hint) {
        Maybe<Colour> hintColour = hint.maybeGetActualColour();
        Maybe<Integer> hintValue = hint.maybeGetActualValue();
        return hintColour.hasValue()
               && hintValue.hasValue()
               && hintColour.getValue() == c.getColour()
               && hintValue.getValue() == c.getValue();
    }

    public static Func<Card, Boolean> getColorFilter(Colour colour) {
        return new Func<Card, Boolean>() {
            @Override
            public Boolean apply(Card card) {
                return card.getColour() == colour;
            }
        };
    }

    public static Func<Card, Boolean> getColorsFilter(Colour[] colours) {
        return new Func<Card, Boolean>() {
            @Override
            public Boolean apply(Card card) {
                for (Colour colour : colours) {
                    if (card.getColour() == colour) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static Func<Card, Boolean> getValueFilter(int value) {
        return new Func<Card, Boolean>() {
            @Override
            public Boolean apply(Card card) {
                return card.getValue() == value;
            }
        };
    }

    public static Func<Card, Boolean> getValuesFilter(Integer[] values) {
        return new Func<Card, Boolean>() {
            @Override
            public Boolean apply(Card card) {
                for (Integer value : values) {
                    if (card.getValue() == value) {
                        return true;
                    }
                }
                return false;
            }
        };
    }

    public static boolean isCardSafeToPlayFromOwnView(State s, CardHint ownView) {
        ArrayList<Card> safeCards = StateUtils.getPlayableFireworksCards(s);

        /* This is the obvious case where we know both the colour and value. */
        if (CardUtils.doesDeckContainCard(safeCards, ownView)) {
            return true;
        }

        /* Consider the following example where we know the actual value
         * but not the colour:
         *
         * We have a 2 that could be either Red or White. Both the Red and
         * White piles need a 2 as their next card. We can play the card in
         * our hand not knowing specifically which pile it'll end up in.
         */
        Maybe<Integer> actualValue = ownView.maybeGetActualValue();
        if (actualValue.hasValue()) {
            /* Look at the piles whose colour may match that of the card
             * we're holding
             */
            safeCards = Linq.filter(
                safeCards,
                CardUtils.getColorsFilter(ownView.getPossibleColours())
            );
            /* Now make sure that in all such piles our value would be suitable. */
            for (Card safeCard : safeCards) {
                if (actualValue.getValue() != safeCard.getValue()) {
                    return false;
                }
            }

            /* We have checked that in all the piles whose colour we might be,
             * that our value would be okay.
             */
            return true;
        }

        return false;
    }

    public static Func<Card, Boolean> getColorAndValueFilter(
            Maybe<Colour> maybeColour,
            Maybe<Integer> maybeValue
    ) {
        return new Func<Card, Boolean>() {
            @Override
            public Boolean apply(Card card) {
                return
                    (maybeColour.hasValue() ? card.getColour() == maybeColour.getValue() : true) &&
                    (maybeValue.hasValue() ? card.getValue() == maybeValue.getValue() : true);
            }
        };
    }

    public static Boolean doesDeckContainCard(ArrayList<Card> deck, Card card) {
        return Linq.any(
            deck,
            CardUtils.getColorAndValueFilter(
                new Maybe<Colour>(card.getColour()),
                new Maybe<Integer>(card.getValue())
            )
        );
    }

    public static Boolean doesDeckContainCard(ArrayList<Card> deck, CardHint hint) {
        return
            hint.maybeGetActualColour().hasValue()
            && hint.maybeGetActualValue().hasValue()
            && Linq.any(
                deck,
                CardUtils.getColorAndValueFilter(
                    hint.maybeGetActualColour(),
                    hint.maybeGetActualValue()
                )
            );
    }

    public static float calculateProbabilityOfHintBeingAParticularCard(
            ArrayList<Card> cardPool,
            Card targetCard,
            CardHint hint
        )
    {
        if (!CardUtils.doesDeckContainCard(cardPool, targetCard)) {
            return (float)0.0;
        }

        ArrayList<Card> possibleCardsHintCouldBe = CardHint.expandPossibleCards(hint);
        /*


        Maybe<Colour> colourHint = hint.maybeGetActualColour();
        Maybe<Integer> valueHint = hint.maybeGetActualValue();

        /* If we know the colour and value from the hint, and the colour and
         * value match up to the target card we can be ceartin of a match.
         */
        if (colourHint.hasValue()
                && valueHint.hasValue()
                && colourHint.getValue() == targetCard.getColour()
                && valueHint.getValue() == targetCard.getValue()
        ) {
            return (float)1.0;
        }

        /* If we know nothing from the hint, the probability that it is the
         * target card is equal to the probability of drawing it out of the
         * pool at random.
         */
        if (!valueHint.hasValue() && !colourHint.hasValue()) {
            return (float)(1.0 / cardPool.size());
        }

        /* If we know the colour from a hint and it matches the target card,
         * the probabiltiy of the hinted card being the target is equal to
         * the 1 / the number of same coloured cards in the pool.
         */
        if (colourHint.hasValue()
                && !valueHint.hasValue()
                && colourHint.getValue() == targetCard.getColour()
        ) {
            double numberOfSameColouredCardsInPool = (double)Linq.count(
                cardPool,
                CardUtils.getColorFilter(colourHint.getValue())
            );
            return (float)(1.0 / numberOfSameColouredCardsInPool);
        }

        /* Similar to the color-only hinted case, except we examine the number
         * of possible cards that share the same value regardless of colour.
         */
        if (valueHint.hasValue()
                && !colourHint.hasValue()
                && valueHint.getValue() == targetCard.getValue()
        ) {
            double numberOfSameValuedCardsInPool = (double)Linq.count(
                cardPool,
                CardUtils.getValueFilter(valueHint.getValue())
            );
            return (float)(1.0 / numberOfSameValuedCardsInPool);
        }

        return (float)0.0;
    }

    public static float calculateProbabilityOfHintBeingATargetCard(
            ArrayList<Card> possibleCards,
            ArrayList<Card> targetCards,
            CardHint hint
        )
    {
        /* Filter out any target cards that aren't in the possible cards. */
        targetCards = Linq.filter(
            targetCards,
            new Func<Card, Boolean>() {
                @Override
                public Boolean apply(Card targetCard) {
                    for (Card possibleCard : possibleCards) {
                        if (possibleCard.equals(targetCard)) {
                            /* keep */
                            return true;
                        }
                    }
                    /* discard */
                    return false;
                }
            }
        );

        ArrayList<Float> probabilities = new ArrayList<Float>();
        for (Card targetCard : targetCards) {
            probabilities.add(
                CardUtils.calculateProbabilityOfHintBeingAParticularCard(
                    possibleCards,
                    targetCard,
                    hint
                )
            );
        }

        return Linq.sum(probabilities);
    }

    public static HintUtilityCalculation calculateUtilityOfHintInformationForPlayer(
            State s,
            int playerIndex,
            Maybe<Colour> colourHint,
            Maybe<Integer> valueHint,
            float weightingForPointingAtMoreCards,
            float weightingForValueOverColour,
            float weightingForColourOverValue,
            float weightingForHigherValues,
            float weightingForRevealingPlayableCard
    ) {
        if (StateUtils.getCurrentPlayer(s) == playerIndex) {
            throw new IllegalArgumentException(
                "Cannot determine utility of a hint from the current player " +
                "to the current player."
            );
        }
        if (colourHint.hasValue() && valueHint.hasValue()) {
            throw new IllegalArgumentException(
                "Cannot provide both a colour and value hint at the same time."
            );
        }
        if (!colourHint.hasValue() && !valueHint.hasValue()) {
            throw new IllegalArgumentException(
                "Must provide either a colour or value hint."
            );
        }

        Card[] hand = s.getHand(playerIndex);
        CardHint[] playersHints = StateUtils.getHintsForPlayer(s, playerIndex);
        ArrayList<Integer> pointedAtCardIndexes = new ArrayList<Integer>();
        float utility = (float)0.0;

        System.out.println("\t\tEXAMINING HINT colour = " + colourHint.toString() + "\t value = " + valueHint.toString());

        for (CardHint knownHint : playersHints) {
            int cardIndex = knownHint.getCardIndex();
            Card card = hand[cardIndex];
            Maybe<Colour> maybeKnownColour = knownHint.maybeGetActualColour();
            Maybe<Integer> maybeKnownValue = knownHint.maybeGetActualValue();

            //System.out.println("\t\t\t FOR CARD " + card.toString());

            /* These cases capture where you wouldn't end up pointing at a
             * a card because it either doesn't exist (null in hand) or isn't
             * relavent to the given hint.
             */
            if (
                /* When you have less than 5 cards, nulls represent a lack of
                 * card in your hand.
                 */
                card == null ||
                /* If the hinted color or value does not match a card, the hint
                 * provides no utility for that card.
                 */
                (
                    (colourHint.hasValue() && colourHint.getValue() != card.getColour()) ||
                    (valueHint.hasValue() && valueHint.getValue() != card.getValue())
                )
            ) {
                utility += 0;
                continue;
            }

            /* If we already know the colour (and are correct in our assumption)
             * then telling us the colour again doesn't provide any new information.
             */
            if (maybeKnownColour.hasValue() && maybeKnownColour.getValue() == card.getColour()) {
                utility += 0;
                pointedAtCardIndexes.add(cardIndex);
                continue;
            }

            /* Now that we have established the hint at least concerns the card
             * we calculate the degree to which it does.
             *
             * All 'factors' here should be normalized and range between
             * 0.0 - 1.0
             */
            float isValueHint = valueHint.hasValue() ? (float)1.0 : (float)0.0;
            float isColourHint = colourHint.hasValue() ? (float)1.0 : (float)0.0;
            /* We normalize this by dividing by the highest possible value for
             * a samle coloured card. 1/5 = 0.2 but 5/5 = 1.0 the max.
             */
            float highnessOfValue =
                card.getValue() / (float)CardUtils.getHighestValueOfCardType(card);
            float playablenessOfCard = CardUtils.isCardSafeToPlayFromOwnView(s, knownHint)
                ? (float)1.0
                : (float)0.0;

            utility +=
                (weightingForValueOverColour * isValueHint) +
                (weightingForColourOverValue * isColourHint) +
                (weightingForHigherValues * highnessOfValue) +
                (weightingForRevealingPlayableCard * playablenessOfCard);
            pointedAtCardIndexes.add(cardIndex);
        }

        /* This will be between 0.0 - 1.0. */
        float percentageOfHandPointedTo =
            (float)pointedAtCardIndexes.size() / (float)StateUtils.getNumberOfCardsInPlayersHand(s, playerIndex);
        utility +=
            (weightingForPointingAtMoreCards * percentageOfHandPointedTo);

        System.out.println("\t\tDETERMINED UTILITY OF HINT = " + ((Float)utility).toString());
        return new HintUtilityCalculation(
            playerIndex,
            utility,
            valueHint.hasValue()
                ? ActionType.HINT_VALUE
                : ActionType.HINT_COLOUR,
            colourHint,
            valueHint,
            pointedAtCardIndexes
        );
    }
}