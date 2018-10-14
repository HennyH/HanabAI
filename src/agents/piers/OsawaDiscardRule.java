package agents.piers;

import java.util.ArrayList;
import java.util.Arrays;

import hanabAI.Action;
import hanabAI.ActionType;
import hanabAI.Card;
import hanabAI.IllegalActionException;
import hanabAI.State;

public class OsawaDiscardRule implements IRule {

    public int _playerIndex;

    public OsawaDiscardRule(int playerIndex) {
        this._playerIndex = playerIndex;
    }

    @Override
	public Action play(State s) {
        /* If we know the colour and value of the card and it is not immedietly
         * playable we will discard it.
         */
        for (CardHint viewOfCard : StateUtils.getHintsForPlayer(s, this._playerIndex)) {
            if (
                    viewOfCard.maybeGetActualColour().hasValue()
                    && viewOfCard.maybeGetActualValue().hasValue()
                    && !CardUtils.isCardSafeToPlayFromOwnView(s, viewOfCard)
            ) {
                try {
                    return new Action(
                        this._playerIndex,
                        s.getName(this._playerIndex),
                        ActionType.DISCARD,
                        viewOfCard.getCardIndex()
                    );
                } catch (IllegalActionException ex) {
                    System.out.println("Unreachable code.");
                }
            }
        }

        /* Figure out what cards could be played in the future. */
        ArrayList<Card> futurePlayableCards = StateUtils.getFuturePlayableCards(s);

        for (CardHint viewOfCard : StateUtils.getHintsForPlayer(s, this._playerIndex)) {
            for (Card futurePlayableCard : futurePlayableCards) {
                if (!CardUtils.hintMatchesCard(futurePlayableCard, viewOfCard)) {
                    try {
                        return new Action(
                            this._playerIndex,
                            s.getName(this._playerIndex),
                            ActionType.DISCARD,
                            viewOfCard.getCardIndex()
                        );
                    } catch (IllegalActionException ex) {
                        System.out.println("Unreachable code.");
                    }
                }
            }
        }

        return null;
	}

}