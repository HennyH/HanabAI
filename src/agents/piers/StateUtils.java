package agents.piers;

import java.util.ArrayDeque;
import java.util.ArrayList;

import hanabAI.Action;
import hanabAI.ActionType;
import hanabAI.Card;
import hanabAI.Colour;
import hanabAI.IllegalActionException;
import hanabAI.State;

public class StateUtils {

    public static State getInitialGameState(State s) {
        s = (State)s.clone();
        while (true) {
            try {
                State prevState = s.getPreviousState();
                s = prevState;
            } catch (Exception ex) {
                break;
            }
        }
        return s;
    }

    public static int getNumberOfPlayers(State s) {
        return s.getPlayers().length;
    }

    public static int[] getPlayersOtherThan(State s, int playerIndex) {
        int numberOfPlayers = StateUtils.getNumberOfPlayers(s);
        int[] otherPlayers = new int[numberOfPlayers - 1];
        int j = 0;
        for (int i = 0; i < otherPlayers.length; i++) {
            if (i == playerIndex) {
                continue;
            }
            otherPlayers[j++] = i;
        }
        return otherPlayers;
    }

    public static int getHandSize(State s) {
        return StateUtils.getInitialGameState(s).getHand(0).length;
    }

    public static boolean hasCardBeenDiscarded(State s, Card c) {
        for (Card discCard : s.getDiscards()) {
            if (discCard.getColour() == c.getColour() && discCard.getValue() == c.getValue()) {
                return true;
            }
        }
        return false;
    }

    public static boolean hasCardBeenPlacedInFireworks(State s, Card c) {
        for (Card cardInFireworks : s.getFirework(c.getColour())) {
            if (cardInFireworks.equals(c)) {
                return true;
            }
        }

        return false;
    }

    public static Action[] getChronologicalActions(State s) {
        s = (State)s.clone();

        /* We have an ArrayDeque here because we are traversing the actions
         * in reverse-chronological order, and hence need to put actions we
         * visit later in the loop towards the front of the collection. The
         * deque implementation has an addFirst method we can use to do this.
         */
        ArrayDeque<Action> chronologicalAction = new ArrayDeque<Action>();
        for (int i = 1; i <= s.getOrder(); i++) {
            Action a = s.getPreviousAction();
            if (a != null) {
                chronologicalAction.addFirst(a);
            }
            s = s.getPreviousState();
        }

        return chronologicalAction.toArray(new Action[0]);
    }

    public static boolean isHintAction(Action a) {
        return a.getType() == ActionType.HINT_COLOUR ||
            a.getType() == ActionType.HINT_VALUE;
    }

    public static boolean isHintActionForPlayer(Action a, int playerIndex) {
        try {
            return StateUtils.isHintAction(a) &&
                a.getHintReceiver() == playerIndex;
        } catch (IllegalActionException ex) {
            System.out.println(ex.getStackTrace());
            return false;
        }
    }

    public static boolean isDiscardAction(Action a) {
        return a.getType() == ActionType.DISCARD;
    }

    public static boolean isActionOfPlayer(Action a, int playerIndex) {
        return a.getPlayer() == playerIndex;
    }

    public static CardHint[] applyActionToPlayerCardHints(CardHint[] hints, int playerIndex, Action a) {
        hints = hints.clone();

        try {
            if (StateUtils.isDiscardAction(a)) {
                int hintIndex = a.getCard();
                hints[hintIndex] = new CardHint(playerIndex, hintIndex);
            } else if (StateUtils.isHintActionForPlayer(a, playerIndex)) {
                boolean[] matches = a.getHintedCards();
                Maybe<Colour> actionHintColour = new Maybe<Colour>(null);
                Maybe<Integer> actionHintValue = new Maybe<Integer>(null);
                if (a.getType() == ActionType.HINT_COLOUR) {
                    actionHintColour = new Maybe<Colour>(a.getColour());
                } else if (a.getType() == ActionType.HINT_VALUE) {
                    actionHintValue = new Maybe<Integer>(a.getValue());
                }

                for (int hintIndex = 0; hintIndex < matches.length; hintIndex++) {
                    if (matches[hintIndex] == true) {
                        Maybe<Colour> currentColourInfo = hints[hintIndex].getColourHint();
                        Maybe<Integer> currentValueInfo = hints[hintIndex].getValueHint();
                        Colour newColour = actionHintColour.hasValue()
                            ? actionHintColour.getValue()
                            : (currentColourInfo.hasValue() ? currentColourInfo.getValue() : (Colour)null);
                        Integer newValue = actionHintValue.hasValue()
                            ? actionHintValue.getValue()
                            : (currentValueInfo.hasValue() ? currentValueInfo.getValue() : (Integer)null);
                        hints[hintIndex] = new CardHint(
                            playerIndex,
                            hintIndex,
                            newColour,
                            newValue
                        );
                    }
                }
            }
        } catch (IllegalActionException ex) {
            System.out.println(ex.getStackTrace());
        }

        return hints;
    }

    public static CardHint[] getHintsForPlayer(State s, int playerIndex) {
        Action[] chronologicalActions = StateUtils.getChronologicalActions(s);
        int handSize = StateUtils.getHandSize(s);
        CardHint[] runningHints = new CardHint[handSize];

        /* Initially create an array of 'empty' hint informations. When we
         * recieve information we will update them, and upon discarding a card
         * remove them.
         */
        for (int i = 0; i < handSize; i++) {
            runningHints[i] = new CardHint(playerIndex, i);
        }

        for (Action a : chronologicalActions) {
            runningHints = StateUtils.applyActionToPlayerCardHints(
                runningHints,
                playerIndex,
                a
            );
        }

        return runningHints;
    }

    public static Card[] getOtherPlayersCards(State s, int playerIndex) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int otherPlayerIndex : StateUtils.getPlayersOtherThan(s, playerIndex)) {
            for (Card card : s.getHand(otherPlayerIndex)) {
                if (card != null) {
                    cards.add(card);
                }
            }
        }

        return cards.toArray(new Card[0]);
    }
}