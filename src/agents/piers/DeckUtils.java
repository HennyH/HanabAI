package agents.piers;

import java.util.ArrayList;

import hanabAI.Card;
import hanabAI.Colour;
import hanabAI.State;

public class DeckUtils {

    public static ArrayList<Card> getHanabiDeck() {
        ArrayList<Card> deck = new ArrayList<Card>();
        for (Colour colour : Colour.values()) {
            for (int value = 1; value <= 5; value++) {
                deck.add(new Card(colour, value));
            }
        }

        return deck;
    }

    public static Func<Card, Boolean> getCardsInFireworksFilter(State s) {
        return new Func<Card, Boolean>() {
            @Override
            public Boolean apply(Card card) {
                if (StateUtils.hasCardBeenPlacedInFireworks(s, card)) {
                    /* filter out */
                    return false;
                }
                /* keep in */
                return true;
            }
        };
    }

    public static Func<Card, Boolean> getCardsInDiscardPileFilter(State s) {
        return new Func<Card, Boolean>() {
            @Override
            public Boolean apply(Card card) {
                if (StateUtils.hasCardBeenDiscarded(s, card)) {
                    /* filter out */
                    return false;
                }
                /* keep in */
                return true;
            }
        };
    }

    public static Func<Card, Boolean> getInOtherPlayersHandFilter(State s, int playerIndex) {
        return new Func<Card, Boolean>() {
            @Override
            public Boolean apply(Card card) {
                for (Card otherPlayersCard : StateUtils.getOtherPlayersCards(s, playerIndex)) {
                    if (otherPlayersCard.equals(card)) {
                        /* filter out */
                        return false;
                    }
                }
                /* keep in */
                return true;
            }
        };
    }

    public static Func<Card, Boolean> getKnownCardsFilter(State s, int playerIndex) {
        return new Func<Card, Boolean>() {
            @Override
            public Boolean apply(Card card) {
                for (Card otherPlayersCard : StateUtils.getOtherPlayersCards(s, playerIndex)) {
                    if (otherPlayersCard.equals(card)) {
                        /* filter out */
                        return false;
                    }
                }
                for (CardHint hint : StateUtils.getHintsForPlayer(s, playerIndex)) {
                    if (CardUtils.hintMatchesCard(card, hint)) {
                        /* filter out */
                        return false;
                    }
                }
                /* keep in */
                return true;
            }
        };
    }
}