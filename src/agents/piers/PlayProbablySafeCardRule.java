package agents.piers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import hanabAI.Action;
import hanabAI.ActionType;
import hanabAI.Card;
import hanabAI.Colour;
import hanabAI.IllegalActionException;
import hanabAI.State;

public class PlayProbablySafeCardRule implements IRule {

    private float _confidenceThreshold;
    private int _playerIndex;

    public PlayProbablySafeCardRule(int playerIndex, float confidenceThreshold) {
        this._playerIndex = playerIndex;
        this._confidenceThreshold = confidenceThreshold;
    }

    @Override
    public Action play(State s) {
        /* Figure out which cards would be safe to play. */
        ArrayList<Card> targetCards = StateUtils.getPlayableFireworksCards(s);

        /* Determine all the cards that a card in our hand could possibly be. */
        ArrayList<Card> cardPool = DeckUtils.getHanabiDeck();
        /* If a card is in the fireworks display it isn't possible for that
         * card to be in our hand.
         */
        cardPool = Linq.removeInstanceWise(
            cardPool,
            StateUtils.getPlayedCards(s)
        );
        /* If we know a card has been discarded it isn't possible for a card in
         * our hand to be the same.
         */
        cardPool = Linq.removeInstanceWise(
            cardPool,
            StateUtils.getDiscardedCards(s)
        );
        /* We see everyone else's hands, if someone else has a particular card,
         * it isn't possible for us to maybe have that card in our hand.
         */
        cardPool = Linq.removeInstanceWise(
            cardPool,
            StateUtils.getOtherPlayersCards(s, this._playerIndex)
        );


        /* Get the hints for the current players hand, we'll be going through
         * each of these hints and deciding the probability that it may be a
         * target card.
         */
        CardHint[] hints = StateUtils.getHintsForPlayer(s, this._playerIndex);

        CardHint bestCardHint = null;
        float bestCardSafeProbability = (float)0.0;

        for (CardHint hint : hints) {
            float probability = CardUtils.calculateProbabilityOfHintBeingATargetCard(
                cardPool,
                targetCards,
                hint
            );
            if (probability > bestCardSafeProbability) {
                bestCardSafeProbability = probability;
                bestCardHint = hint;
            }
        }

        System.out.println("----------START PLAY P(x) SAFE-----------");

        System.out.println("POOL:");
        System.out.println(Arrays.toString(cardPool.toArray()));
        System.out.println("TARGETS:");
        System.out.println(Arrays.toString(targetCards.toArray()));
        System.out.println("HINTS:");
        System.out.println(Arrays.toString(hints));
        System.out.println("BEST CHOICE:");
        System.out.println("CARD #: " + (bestCardHint == null ? "NONE" : ((Integer)bestCardHint.getCardIndex()).toString()));
        System.out.println(bestCardSafeProbability);

        if (bestCardSafeProbability >= this._confidenceThreshold) {
            System.out.println("Played!");
            try {
                return new Action(
                    this._playerIndex,
                    s.getName(this._playerIndex),
                    ActionType.PLAY,
                    bestCardHint.getCardIndex()
                );
            } catch (IllegalActionException ex) {
                System.out.println(ex.getStackTrace());
            }
        } else {
            System.out.println("Not Played :(");
        }


        System.out.println("----------END PLAY P(x) SAFE-----------");

        return null;
    }
}