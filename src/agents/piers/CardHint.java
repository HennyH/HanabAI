package agents.piers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import hanabAI.Card;
import hanabAI.Colour;

public class CardHint
{
    private int _playerIndex;
    private int _cardIndex;
    private HashSet<Colour> _possibleColours;
    private HashSet<Integer> _possibleValues;

    private void setPossibilities(HashSet<Colour> possibleColours, HashSet<Integer> possibleValues) {
        if (
                (possibleColours != null && possibleColours.size() == 0) ||
                (possibleValues != null && possibleValues.size() == 0)
        ) {
            throw new IllegalArgumentException(
                "A card must have at least one possible value and colour."
            );
        }

        /* If we pass in a null set it means that it could be any
         * of the possible colours/values.
         */
        if (possibleColours == null) {
            possibleColours = new HashSet<Colour>();
            for (Colour colour : Colour.values()) {
                possibleColours.add(colour);
            }
        }
        if (possibleValues == null) {
            possibleValues = new HashSet<Integer>();
            for (int value : CardUtils.getPossibleCardValues()) {
                possibleValues.add(value);
            }
        }

        this._possibleColours = possibleColours;
        this._possibleValues = possibleValues;
    }

    public CardHint(int playerIndex, int cardIndex) {
        this._playerIndex = playerIndex;
        this._cardIndex = cardIndex;
        this.setPossibilities(null, null);
    }

    private CardHint(int playerIndex, int cardIndex, HashSet<Colour> possibleColours, HashSet<Integer> possibleValues) {
        this(playerIndex, cardIndex);
        this.setPossibilities(possibleColours, possibleValues);
    }

    public static CardHint isNot(CardHint hint, Colour colour) {
        @SuppressWarnings("unchecked")
        HashSet<Colour> possibleColours = (HashSet<Colour>)hint._possibleColours.clone();
        @SuppressWarnings("unchecked")
        HashSet<Integer> possibleValues = (HashSet<Integer>)hint._possibleValues.clone();
        possibleColours.remove(colour);
        return new CardHint(
            hint.getPlayerIndex(),
            hint.getCardIndex(),
            possibleColours,
            possibleValues
        );
    }

    public static CardHint isNot(CardHint hint, Integer value) {
        @SuppressWarnings("unchecked")
        HashSet<Colour> possibleColours = (HashSet<Colour>)hint._possibleColours.clone();
        @SuppressWarnings("unchecked")
        HashSet<Integer> possibleValues = (HashSet<Integer>)hint._possibleValues.clone();
        possibleValues.remove(value);
        return new CardHint(
            hint.getPlayerIndex(),
            hint.getCardIndex(),
            possibleColours,
            possibleValues
        );
    }

    public static CardHint is(CardHint hint, Colour colour) {
        @SuppressWarnings("unchecked")
        HashSet<Integer> possibleValues = (HashSet<Integer>)hint._possibleValues.clone();

        HashSet<Colour> possibleColours = new HashSet<Colour>();
        possibleColours.add(colour);

        return new CardHint(
            hint.getPlayerIndex(),
            hint.getCardIndex(),
            possibleColours,
            possibleValues
        );
    }

    public static CardHint is(CardHint hint, Integer value) {
        @SuppressWarnings("unchecked")
        HashSet<Colour> possibleColours = (HashSet<Colour>)hint._possibleColours.clone();

        HashSet<Integer> possibleValues = new HashSet<Integer>();
        possibleValues.add(value);

        return new CardHint(
            hint.getPlayerIndex(),
            hint.getCardIndex(),
            possibleColours,
            possibleValues
        );
    }

    public static ArrayList<Card> expandPossibleCards(CardHint hint) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (Colour colour : hint.getPossibleColours()) {
            for (Integer value : hint.getPossibleValues()) {
                cards.add(new Card(colour, value));
            }
        }
        return cards;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("[");
        /* Turn the colours into: R,G,W or * for any */
        if (this._possibleColours.size() == Colour.values().length) {
            builder.append("*");
        } else {
            Colour[] colours = this._possibleColours.toArray(new Colour[0]);
            int coloursLeftToPrint = colours.length;
            Arrays.sort(colours);
            for (Colour possibleColour : colours) {
                String letter = possibleColour.name().substring(0, 1);
                builder.append(letter);
                if (coloursLeftToPrint >= 1) {
                    builder.append(",");
                }
                coloursLeftToPrint -= 1;
            }
        }
        /* Seperate the different types of info with a pipe. */
        builder.append("|");
        /* Turn the colours into: 1,2,3 or * for any */
        if (this._possibleValues.size() == CardUtils.getPossibleCardValues().length) {
            builder.append("*");
        } else {
            Integer[] values = this._possibleValues.toArray(new Integer[0]);
            int valuesLeftToPrint = values.length;
            Arrays.sort(values);
            for (Integer possibleValue : values) {
                builder.append(possibleValue);
                if (valuesLeftToPrint >= 1) {
                    builder.append(",");
                }
                valuesLeftToPrint -= 1;
            }
        }
        builder.append("]");
        return builder.toString();
    }

    public int getPlayerIndex() { return this._playerIndex; }
    public int getCardIndex() { return this._cardIndex; }

    public Maybe<Colour> maybeGetActualColour() {
        if (this._possibleColours.size() == 1) {
            return new Maybe<Colour>(this._possibleColours.toArray(new Colour[0])[0]);
        }
        return new Maybe<Colour>(null);
    }

    public Maybe<Integer> maybeGetActualValue() {
        if (this._possibleValues.size() == 1) {
            return new Maybe<Integer>(this._possibleValues.toArray(new Integer[0])[0]);
        }
        return new Maybe<Integer>(null);
    }

    public Colour[] getPossibleColours() {
        return this._possibleColours.toArray(new Colour[0]);
    }

    public Integer[] getPossibleValues() {
        return this._possibleValues.toArray(new Integer[0]);
    }
}