package agents.piers;

import java.util.ArrayList;

import hanabAI.Card;
import hanabAI.Colour;

public class CardUtils {

    public static boolean doesCardHasMaximumValue(Card c) {
        return c.getValue() == 5;
    }

    public static Card getNextCardInFireworksSequence(Card c) {
        return new Card(c.getColour(), c.getValue() + 1);
    }

    public static boolean hintMatchesCard(Card c, CardHint hint) {
        Maybe<Colour> hintColour = hint.getColourHint();
        Maybe<Integer> hintValue = hint.getValueHint();
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

    public static Func<Card, Boolean> getValueFilter(int value) {
        return new Func<Card, Boolean>() {
            @Override
            public Boolean apply(Card card) {
                return card.getValue() == value;
            }
        };
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
        return Linq.some(
            deck,
            CardUtils.getColorAndValueFilter(
                new Maybe<Colour>(card.getColour()),
                new Maybe<Integer>(card.getValue())
            )
        );
    }

    public static Boolean doesDeckContainCard(ArrayList<Card> deck, CardHint hint) {
        return Linq.some(
            deck,
            CardUtils.getColorAndValueFilter(
                hint.getColourHint(),
                hint.getValueHint()
            )
        );
    }

    public static float calculateProbabilityOfHintBeingAParticularCard(
            ArrayList<Card> possibleCards,
            Card targetCard,
            CardHint hint
        )
    {
        if (!CardUtils.doesDeckContainCard(possibleCards, targetCard)) {
            throw new IllegalArgumentException("Target card must exist in possible cards.");
        }

        Maybe<Colour> colourHint = hint.getColourHint();
        Maybe<Integer> valueHint = hint.getValueHint();

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
            return (float)(1.0 / possibleCards.size());
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
                possibleCards,
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
                possibleCards,
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
}