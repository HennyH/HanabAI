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
    private ArrayList<Card> _possibleCards;


    private CardHint(int playerIndex, int cardIndex, ArrayList<Card> possibleCards) {
        if (possibleCards == null || possibleCards.size() == 0) {
            throw new IllegalArgumentException(
                "Possible cards cannot be null or empty."
            );
        }
        this._playerIndex = playerIndex;
        this._cardIndex = cardIndex;
        this._possibleCards = Linq.distinct(possibleCards);
    }

    public CardHint(int playerIndex, int cardIndex) {
        this(playerIndex, cardIndex, DeckUtils.getHanabiDeck());
    }

    public static CardHint isNot(CardHint hint, Colour colour) {
        if (colour == null) {
            throw new IllegalArgumentException(
                "Must provide a non-null colour."
            );
        }
        return new CardHint(
            hint.getPlayerIndex(),
            hint.getCardIndex(),
            Linq.setDifference(
                hint.getPossibleCards(),
                CardUtils.expandCards(
                    new Maybe<Colour>(colour),
                    new Maybe<Integer>(null)
                )
            )
        );
    }

    public static CardHint isNot(CardHint hint, Integer value) {
        if (value == null) {
            throw new IllegalArgumentException(
                "Must provide a non-null value."
            );
        }
        return new CardHint(
            hint.getPlayerIndex(),
            hint.getCardIndex(),
            Linq.setDifference(
                hint.getPossibleCards(),
                CardUtils.expandCards(
                    new Maybe<Colour>(null),
                    new Maybe<Integer>(value)
                )
            )
        );
    }

    public static CardHint is(CardHint hint, Colour colour) {
        if (colour == null) {
            throw new IllegalArgumentException(
                "Must provide a non-null colour."
            );
        }
        return new CardHint(
            hint.getPlayerIndex(),
            hint.getCardIndex(),
            CardUtils.expandCards(
                new Maybe<Colour>(colour),
                new Maybe<Integer>(null)
            )
        );
    }

    public static CardHint is(CardHint hint, Integer value) {
        if (value == null) {
            throw new IllegalArgumentException(
                "Must provide a non-null value."
            );
        }
        return new CardHint(
            hint.getPlayerIndex(),
            hint.getCardIndex(),
            CardUtils.expandCards(
                new Maybe<Colour>(null),
                new Maybe<Integer>(value)
            )
        );
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("<");
        ArrayList<String> cardPreviews = new ArrayList<String>();

        if (this._possibleCards.size() == DeckUtils.getHanabiDeck().size()) {
            builder.append("**");
        } else {
            for (Colour colour : CardUtils.getPossibleCardColours()) {
                ArrayList<Card> possibleCardsOfColour = Linq.filter(
                    this._possibleCards,
                    CardUtils.getColorFilter(colour)
                );
                if (possibleCardsOfColour.size() == 0) {
                    continue;
                }
                Maybe<Card> singleCard = Linq.single(possibleCardsOfColour);
                if (singleCard.hasValue()) {
                    cardPreviews.add(CardUtils.getShortPreview(singleCard.getValue()));
                } else if (possibleCardsOfColour.size() == CardUtils.getPossibleCardValues().length) {
                    cardPreviews.add(CardUtils.getColourInitial(colour) + "*");
                } else {
                    String numbers = "(";
                    int numbersLeftToAdd = possibleCardsOfColour.size();
                    for (Card card : possibleCardsOfColour) {
                        numbers += ((Integer)card.getValue()).toString();
                        if (numbersLeftToAdd > 1) {
                            numbers += ",";
                        }
                        numbersLeftToAdd -= 1;
                    }
                    numbers += ")";
                    cardPreviews.add(CardUtils.getColourInitial(colour) + numbers);
                }
            }

            int numberOfPreviewsLeftToAdd = cardPreviews.size();
            for (String cardPreview : cardPreviews) {
                builder.append(cardPreview);
                if (numberOfPreviewsLeftToAdd > 1) {
                    builder.append(", ");
                }
                numberOfPreviewsLeftToAdd -= 1;
            }
        }

        builder.append(">");
        return builder.toString();
    }

    public int getPlayerIndex() { return this._playerIndex; }
    public int getCardIndex() { return this._cardIndex; }

    public Maybe<Colour> maybeGetActualColour() {
        if (this._possibleCards.size() == 1) {
            return new Maybe<Colour>(Linq.first(this._possibleCards).getValue().getColour());
        }
        return new Maybe<Colour>(null);
    }

    public Maybe<Integer> maybeGetActualValue() {
        if (this._possibleCards.size() == 1) {
            return new Maybe<Integer>(Linq.first(this._possibleCards).getValue().getValue());
        }
        return new Maybe<Integer>(null);
    }

    public ArrayList<Colour> getPossibleColours() {
        HashSet<Colour> colours = new HashSet<Colour>();
        for (Card card : this._possibleCards) {
            colours.add(card.getColour());
        }
        return new ArrayList<Colour>(Arrays.asList(colours.toArray(new Colour[0])));
    }

    public Integer[] getPossibleValues() {
        HashSet<Integer> colours = new HashSet<Integer>();
        for (Card card : this._possibleCards) {
            colours.add(card.getValue());
        }
        return colours.toArray(new Integer[0]);
    }

    public ArrayList<Card> getPossibleCards() {
        return (ArrayList<Card>)this._possibleCards.clone();
    }
}