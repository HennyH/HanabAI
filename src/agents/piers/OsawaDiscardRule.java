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
        /* Anything in the discard pile will never be played again */
        ArrayList<Card> futurePlayableCards = Linq.removeInstanceWise(
            DeckUtils.getHanabiDeck(),
            new ArrayList<Card>(Arrays.asList(s.getDiscards().toArray(new Card[0])))
        );
        /* Anything whose value is less than or equal to the current value
         * of the highest firework of its colour won't ever be successfully
         * played.
         */
        futurePlayableCards = Linq.filter(
            futurePlayableCards,
            new Func<Card, Boolean>() {
                @Override
                public Boolean apply(Card card) {
                    Maybe<Card> topCard = StateUtils.getTopFireworksCardForColour(
                        s,
                        card.getColour()
                    );
                    if (topCard.hasValue()) {
                        return card.getValue() > topCard.getValue().getValue();
                    }
                    return false;
                }
            }
        );

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