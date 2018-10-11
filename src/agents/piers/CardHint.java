package agents.piers;

import hanabAI.Colour;

public class CardHint
{
    private int _playerIndex;
    private int _cardIndex;
    private Maybe<Colour> _colour;
    private Maybe<Integer> _value;

    public CardHint(int playerIndex, int cardIndex) {
        this._playerIndex = playerIndex;
        this._cardIndex = cardIndex;
        this._colour = new Maybe<Colour>(null);
        this._value = new Maybe<Integer>(null);
    }

    public CardHint(int playerIndex, int cardIndex, Colour color) {
        this._playerIndex = playerIndex;
        this._cardIndex = cardIndex;
        this._colour = new Maybe<Colour>(color);
        this._value = new Maybe<Integer>(null);
    }

    public CardHint(int playerIndex, int cardIndex, Integer value) {
        this._playerIndex = playerIndex;
        this._cardIndex = cardIndex;
        this._colour = new Maybe<Colour>(null);
        this._value = new Maybe<Integer>(value);
    }

    public CardHint(int playerIndex, int cardIndex, Colour color, Integer value) {
        this._playerIndex = playerIndex;
        this._cardIndex = cardIndex;
        this._colour = new Maybe<Colour>(color);
        this._value = new Maybe<Integer>(value);
    }

    @Override
    public String toString() {
        return
            (this._colour.hasValue() ? this._colour.getValue().toString() : "?") +
            (this._value.hasValue() ? this._value.getValue().toString() : "?");
    }

    public int getPlayerIndex() { return this._playerIndex; }
    public int getCardIndex() { return this._cardIndex; }
    public Maybe<Colour> getColourHint() { return this._colour; }
    public Maybe<Integer> getValueHint() { return this._value; }
}