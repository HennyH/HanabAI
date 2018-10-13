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

    public static Colour[] getPossibleCardColours() {
        return Colour.values();
    }

    public static ArrayList<Card> expandCards(Maybe<Colour> ofColour, Maybe<Integer> ofValue) {
        Colour[] colours = ofColour.hasValue()
            ? new Colour[] { ofColour.getValue() }
            : CardUtils.getPossibleCardColours();
        int[] values = ofValue.hasValue()
            ? new int[] { ofValue.getValue() }
            : CardUtils.getPossibleCardValues();

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
        return hint.getPossibleCards().contains(c);
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

        ArrayList<Card> possibleCardsHintCouldBe = Linq.elementsOf(
            cardPool,
            hint.getPossibleCards()
        );
        ArrayList<Card> targetCardsHintMatches = Linq.re(
            hint.getPossibleCards(),
            targetCards
        );

        try {
            return (float)targetCardsHintMatches.size() / (float)possibleCardsHintCouldBe.size();
        } catch (ArithmeticException ex) {
            System.out.println("THis should never have occured");
            return (float)0.0;
        }
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
             * Likewise with the value.
             */
            if (
                    (maybeKnownColour.hasValue() && maybeKnownColour.getValue() == card.getColour()) ||
                    (maybeKnownValue.hasValue() && maybeKnownValue.getValue() == card.getValue())
            ) {
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