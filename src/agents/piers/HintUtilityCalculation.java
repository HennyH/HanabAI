package agents.piers;

import java.util.ArrayList;

import hanabAI.ActionType;
import hanabAI.Colour;

public class HintUtilityCalculation {

    private int _playerRecievingHintIndex;
    private float _utility;
    private ActionType _hintActionType;
    private Maybe<Colour> _hintedColour;
    private Maybe<Integer> _hintedValue;
    private ArrayList<Integer> _pointedAtCardIndexes;

    public HintUtilityCalculation(
            int playerRecievingHintIndex,
            float utility,
            ActionType hintActionType,
            Maybe<Colour> hintedColour,
            Maybe<Integer> hintedValue,
            ArrayList<Integer> pointedAtCardIndexes
    ) {
        if (hintedColour.hasValue() && hintedValue.hasValue()) {
            throw new IllegalArgumentException(
                "Cannot provide both a colour and value hint at the same time."
            );
        }
        if (!hintedColour.hasValue() && !hintedValue.hasValue()) {
            throw new IllegalArgumentException(
                "Must provide either a colour or value hint."
            );
        }
        if (hintActionType != ActionType.HINT_COLOUR && hintActionType != ActionType.HINT_VALUE) {
            throw new IllegalArgumentException(
                "The action type must be a hint action type."
            );
        }

        this._playerRecievingHintIndex = playerRecievingHintIndex;
        this._utility = utility;
        this._hintActionType = hintActionType;
        this._hintedColour = hintedColour;
        this._hintedValue = hintedValue;
        this._pointedAtCardIndexes = pointedAtCardIndexes;
    }

    public int getPlayerRecievingHintIndex() { return this._playerRecievingHintIndex; }
    public float getUtility() { return this._utility; }
    public ActionType getHintActionType() { return this._hintActionType; }
    public Maybe<Colour> getHintedColour() { return this._hintedColour; }
    public Maybe<Integer> getHintedValue() { return this._hintedValue; }

    public boolean[] getCardPointedAtArray() {
        boolean[] pointedAt = new boolean[5];
        for (Integer cardIndex : this._pointedAtCardIndexes) {
            pointedAt[cardIndex] = true;
        }
        return pointedAt;
    }

}