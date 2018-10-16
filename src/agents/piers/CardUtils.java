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

    public static ArrayList<Integer> getPossibleCardValues() {
        ArrayList<Integer>  values = new ArrayList<Integer>();
        for (int i = 1; i <= 5; i++) {
            values.add(i);
        }
        return values;
    }

    public static Colour[] getPossibleCardColours() {
        return Colour.values();
    }

    public static ArrayList<Card> expandCards(Maybe<Colour> ofColour, Maybe<Integer> ofValue) {
        Colour[] colours = ofColour.hasValue()
            ? new Colour[] { ofColour.getValue() }
            : CardUtils.getPossibleCardColours();
        Integer[] values = ofValue.hasValue()
            ? new Integer[] { ofValue.getValue() }
            : CardUtils.getPossibleCardValues().toArray(new Integer[0]);

        ArrayList<Card> cards = new ArrayList<Card>();
        for (Colour colour : colours) {
            for (int value : values) {
                cards.add(new Card(colour, value));
            }
        }

        return cards;
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
        return
            hint.maybeGetActualColour().hasValue()
            && hint.maybeGetActualValue().hasValue()
            && hint.getPossibleCards().contains(c);
    }

    public static Func<Card, Boolean> getColorFilter(Colour colour) {
        return new Func<Card, Boolean>() {
            @Override
            public Boolean apply(Card card) {
                return card.getColour() == colour;
            }
        };
    }

    public static Func<Card, Boolean> getColorsFilter(ArrayList<Colour> colours) {
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

    public static boolean isCardUselessNowAndInTheFuture(State s, CardHint ownView) {
        ArrayList<Card> unplayableCards = Linq.notElementsOf(
            DeckUtils.getHanabiDeck(),
            StateUtils.getFuturePlayableCards(s)
        );
        for (Card unplayableCard : unplayableCards) {
            if (CardUtils.hintMatchesCard(unplayableCard, ownView)) {
                return true;
            }
        }
        return false;
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

    public static float calculateProbabilityOfHintBeingATargetCard(
            ArrayList<Card> cardPool,
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
                    for (Card possibleCard : cardPool) {
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

        ArrayList<Card> targetCardsInPool = Linq.elementsOf(
            cardPool,
            targetCards
        );
        ArrayList<Card> targetCardsInPoolTheHintedCardMayBe = Linq.elementsOf(
            targetCardsInPool,
            hint.getPossibleCards()
        );
        ArrayList<Card> cardsInPoolTheHintedCardMayBe = Linq.elementsOf(
            cardPool,
            hint.getPossibleCards()
        );

        try {
            return
                (float)targetCardsInPoolTheHintedCardMayBe.size()
                /
                (float)cardsInPoolTheHintedCardMayBe.size();
        } catch (ArithmeticException ex) {
            System.out.println("This should never have occured");
            return (float)0.0;
        }
    }

    public static HintUtilityCalculation calculateUtilityOfHintInformationForPlayer(
            State s,
            int playerIndex,
            Either<Colour, Integer> hint,
            float weightingForPointingAtMoreCards,
            float weightingForValueOverColour,
            float weightingForColourOverValue,
            float weightingForHigherValues,
            float weightingForRevealingPlayableCards,
            float weightingForRevealingAUselessCard,
            float weightingForPointingAtLessDistantFuturePlayableCards
    ) {
        Maybe<Colour> colourHint = hint.getLeft();
        Maybe<Integer> valueHint = hint.getRight();

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
        CardHint[] playersViewOfCards = StateUtils.getHintsForPlayer(s, playerIndex);
        ArrayList<Integer> pointedAtCardIndexes = new ArrayList<Integer>();

        for (CardHint knownHint : playersViewOfCards) {
            int cardIndex = knownHint.getCardIndex();
            Card card = hand[cardIndex];

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
                continue;
            }

            /* Record that we would point at a card. */
            pointedAtCardIndexes.add(cardIndex);
        }

        /* A hint that reveals nothing has no utility, and would be illegal! */
        if (pointedAtCardIndexes.size() == 0) {
            new HintUtilityCalculation(
                playerIndex,
                (float)(-Integer.MAX_VALUE),
                valueHint.hasValue()
                    ? ActionType.HINT_VALUE
                    : ActionType.HINT_COLOUR,
                colourHint,
                valueHint,
                pointedAtCardIndexes
            );
        }

        /* All the parameters to the utility function should be between
         * 0.0 - 1.0 so we can easily normalize the result of the utility
         * function.
         */
        float playersHandSize = (float)StateUtils.getNumberOfCardsInPlayersHand(s, playerIndex);
        float paramPercentageOfHandPointedTo =
            (float)pointedAtCardIndexes.size() / playersHandSize;
        float paramPercentageOfCardsInHandThatWerePointedAtAndAreNowKnownToBePlayable =
            (float)(
                Linq.filter(
                    pointedAtCardIndexes,
                    new Func<Integer, Boolean>() {
                        @Override
                        public Boolean apply(Integer cardIndex) {
                            /* We test to see, if the current player were
                             * given the hint if that card would be playable.
                             */
                            return CardUtils.isCardSafeToPlayFromOwnView(
                                s,
                                valueHint.hasValue()
                                    ? CardHint.is(playersViewOfCards[cardIndex], valueHint.getValue())
                                    : CardHint.is(playersViewOfCards[cardIndex], colourHint.getValue())
                            );
                        }
                    }
                ).size()
            )
            /
            playersHandSize;
        float paramPercentageOfCardsInHandThatWerePointedAtAndAreNowKownToBeUseless =
                (float)(
                    Linq.filter(
                        pointedAtCardIndexes,
                        new Func<Integer, Boolean>() {
                            @Override
                            public Boolean apply(Integer cardIndex) {
                                CardHint newOwnViewOfCard = valueHint.hasValue()
                                    ? CardHint.is(playersViewOfCards[cardIndex], valueHint.getValue())
                                    : CardHint.is(playersViewOfCards[cardIndex], colourHint.getValue());
                                return CardUtils.isCardUselessNowAndInTheFuture(s, newOwnViewOfCard);
                            }
                        }
                    ).size()
                )
                /
                playersHandSize;
        float paramAverageValueOfPointedAtCards =
            (float)(
                Linq.avg(
                    pointedAtCardIndexes,
                    new Func<Integer, Integer>() {
                        @Override
                        public Integer apply(Integer cardIndex) {
                            return hand[cardIndex].getValue();
                        }
                    }
                ).getValue()
            )
            /
            (float)(
                Linq.max(
                    CardUtils.getPossibleCardValues(),
                    new Identity<Integer>()
                ).getValue()
            );
        Maybe<Float> averageDistanceToPlayableValueOfPointedAtCards = Linq.avg(
            /* After having filtered out all the null possible integers map them into plain integers. */
            Linq.map(
                /* Filter out all the null differences, the heuristic here is we don't care
                 * about the proportion of the hand pointed at, rather only those
                 * cards we pointed at that could be played in the future.
                 */
                Linq.filter(
                    /* turn each pointed at card into a possible integer difference between its value
                     * and the next playable firework's value of the same colour.
                     */
                    Linq.map(
                        pointedAtCardIndexes,
                        new Func<Integer, Maybe<Integer>>() {
                            @Override
                            public Maybe<Integer> apply(Integer cardIndex) {
                                Card card = hand[cardIndex];
                                Maybe<Card> firework = StateUtils.getTopFireworksCardForColour(
                                    s,
                                    card.getColour()
                                );
                                if (firework.hasValue()
                                        && card.getValue() > firework.getValue().getValue()
                                ) {
                                    return new Maybe<Integer>(
                                        card.getValue() - firework.getValue().getValue()
                                    );
                                }
                                return new Maybe<Integer>(null);
                            }
                        }
                    ),
                    new Func<Maybe<Integer>, Boolean>() {
                        @Override
                        public Boolean apply(Maybe<Integer> positiveDifference) {
                            if (!positiveDifference.hasValue()) {
                                return false;
                            }
                            return true;
                        }
                    }
                ),
                new Func<Maybe<Integer>, Integer>() {
                    @Override
                    public Integer apply(Maybe<Integer> positiveDifference) {
                        return positiveDifference.getValue();
                    }
                }
            ),
            new Identity<Integer>()
        );
        float paramAverageDistanceToPlayableValueOfPointedAtCards =
            averageDistanceToPlayableValueOfPointedAtCards.hasValue()
                ? (float)1.0 / averageDistanceToPlayableValueOfPointedAtCards.getValue()
                : (float)0.0;
        float paramIsValueHint = valueHint.hasValue() ? (float)1.0 : (float)0.0;
        float paramIsColourHint = colourHint.hasValue() ? (float)1.0 : (float)0.0;
        float utility =
            (
                (weightingForPointingAtMoreCards * paramPercentageOfHandPointedTo) +
                (weightingForRevealingPlayableCards * paramPercentageOfCardsInHandThatWerePointedAtAndAreNowKnownToBePlayable) +
                (weightingForRevealingAUselessCard * paramPercentageOfCardsInHandThatWerePointedAtAndAreNowKownToBeUseless) +
                (weightingForHigherValues * paramAverageValueOfPointedAtCards) +
                (weightingForColourOverValue * paramIsColourHint) +
                (weightingForValueOverColour * paramIsValueHint) +
                (weightingForPointingAtLessDistantFuturePlayableCards * paramAverageDistanceToPlayableValueOfPointedAtCards)
            )
            /
            (
                weightingForPointingAtMoreCards +
                weightingForRevealingPlayableCards +
                weightingForRevealingAUselessCard +
                weightingForHigherValues +
                weightingForColourOverValue +
                weightingForValueOverColour +
                weightingForPointingAtLessDistantFuturePlayableCards
            );

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

    public static String getColourInitial(Colour colour) {
        return colour.name().substring(0, 1);
    }

    public static String getColourInitial(Card c) {
        return CardUtils.getColourInitial(c.getColour());
    }

    public static String getShortPreview(Card c) {
        return CardUtils.getColourInitial(c) + ((Integer)c.getValue()).toString();
    }

}