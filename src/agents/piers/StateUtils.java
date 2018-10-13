package agents.piers;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

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

    public static int getNumberOfCardsInPlayersHand(State s, int playerIndex) {
        /* If they have a turn left they musn't have participated in the
         * last round yet meaning they must have 5 cards.
         */
        if (StateUtils.doesPlayerHaveAPossibleTurnLeft(s, playerIndex)) {
            return 5;
        }

        /* At this stage we know they have already made their final play.
         * Depending on what that final play was will determine if they
         * have 5 or 4 cards.
         */
        try {
            Action actionOnFinalTurn = s.getPreviousAction(playerIndex);
            ActionType typeOfFinalAction = actionOnFinalTurn.getType();
            if (typeOfFinalAction == ActionType.DISCARD
                    || typeOfFinalAction == ActionType.PLAY
            ) {
                return 4;
            } else {
                return 5;
            }
        } catch (ArrayIndexOutOfBoundsException ex) {
            return 5;
        }
    }

    public static boolean isInFinalRoundOfPlay(State s) {
        if (s.gameOver()) {
            return false;
        }

        /* By definition, when it is NOT the final round all players must
         * have 5 cards in their hands!
         */

        /* --- Begin proof by contradiction: ---
         *
         * Assume it is NOT the final round:
         *
         * 1. !!BY OUR ASSUMPTION!!  each player hold 5 cards:
         */
        int numberInHands = 5 * (s.getPlayers().length);
        /*
         * 2. Determine the number of cards in the fireworks:
         */
        int numberInFireworks = 0;
        for (Colour c : Colour.values()) {
            numberInFireworks = s.getFirework(c).size();
        }
        /*
         * 3. Determine the number of cards that have been discarded:
         */
        int numberInDiscardPile = s.getDiscards().size();
        /*
         * 4. By definition the number of cards in the draw pile must
         *    follow this equation:
         *
         *      #draw = #deck - (#hands + #fireworks + #discard)
         */
        int numberInDeck = DeckUtils.getHanabiDeck().size();
        int numberInDrawPile = numberInDeck - (numberInHands + numberInFireworks + numberInDiscardPile);
        /*
         * 5. If is physically impossible for there to be a negative number of
         *    cards in the draw pile. We have reached a contradiction, hence
         *    the assumed premise must be wrong. We assumed each player holds
         *    5 cards, hence each player must not hold 5 cards. This would
         *    mean it must be the end of the game (only then can a player
         *    have less than 5 cards).
         */
        if (numberInDrawPile < 0) {
            /* contrdiction reached */
            return true; /* it is in the final round */
        } else {
            /* no contradiction reached */
            return false; /* it is NOT the final round */
        }
    }

    public static boolean doesPlayerHaveAPossibleTurnLeft(State s, int playerIndex) {
        if (!StateUtils.isInFinalRoundOfPlay(s)) {
            return true;
        }

        /* This deals with the case that it is _our_ turn in the final round.
         * Obviously if it is in the final round and we're about to make a
         * play we can't possibly have another move left.
         *
         * Handling this here makes the proceeding logic simpler.
         */
        if (StateUtils.getCurrentPlayer(s) == playerIndex) {
            return false;
        }

        ArrayList<Integer> playersWhoHavePreviouslyPlayedInFinalRound = new ArrayList<Integer>();

        try {
            State pastState = (State)s.getPreviousState();
            while (StateUtils.isInFinalRoundOfPlay(pastState)) {
                playersWhoHavePreviouslyPlayedInFinalRound.add(
                    StateUtils.getCurrentPlayer(pastState)
                );
                pastState = pastState.getPreviousState();
            }

             return !playersWhoHavePreviouslyPlayedInFinalRound.contains(
                 (Integer)playerIndex
            );
        } catch (Exception ex) {
            /* This should never be reached but if it does, it is safest to
             * to assume a player has a turn left.
             */
            return true;
        }
    }

    public static int getNumberOfPlayers(State s) {
        return s.getPlayers().length;
    }

    public static int getCurrentPlayer(State s) {
        return s.getNextPlayer();
    }

    public static int[] getAllPlayerIndexes(State s) {
        int numberOfPlayers = StateUtils.getNumberOfPlayers(s);
        int[] players = new int[numberOfPlayers];
        int j = 0;
        for (int i = 0; i < players.length; i++) {
            players[j++] = i;
        }
        return players;
    }

    public static int[] getPlayersOtherThan(State s, int playerIndex) {
        int numberOfPlayers = StateUtils.getNumberOfPlayers(s);
        int[] otherPlayers = new int[numberOfPlayers - 1];
        int j = 0;
        for (int i = 0; i < numberOfPlayers; i++) {
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

    public static ArrayList<Card> getDiscardedCards(State s) {
        return new ArrayList<Card>(Arrays.asList(s.getDiscards().toArray(new Card[0])));
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
        /* We have an ArrayDeque here because we are traversing the actions
         * in reverse-chronological order, and hence need to put actions we
         * visit later in the loop towards the front of the collection. The
         * deque implementation has an addFirst method we can use to do this.
         */
        ArrayDeque<Action> chronologicalAction = new ArrayDeque<Action>();

        s = (State)s.clone();
        while (true) {
            try {
                Action previousAction = s.getPreviousAction();
                if (previousAction != null) {
                    chronologicalAction.addFirst(previousAction);
                }
                State prevState = s.getPreviousState();
                s = prevState;
            } catch (Exception ex) {
                break;
            }
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
                        if (actionHintColour.hasValue()) {
                            hints[hintIndex] = CardHint.is(
                                hints[hintIndex],
                                actionHintColour.getValue()
                            );
                        } else {
                            hints[hintIndex] = CardHint.is(
                                hints[hintIndex],
                                actionHintValue.getValue()
                            );
                        }
                    } else {
                        if (actionHintColour.hasValue()) {
                            hints[hintIndex] = CardHint.isNot(
                                hints[hintIndex],
                                actionHintColour.getValue()
                            );
                        } else {
                            hints[hintIndex] = CardHint.isNot(
                                hints[hintIndex],
                                actionHintValue.getValue()
                            );
                        }
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

            // Maybe<Colour> actionHintColour = new Maybe<Colour>(null);
            // Maybe<Integer> actionHintValue = new Maybe<Integer>(null);
            // try {
            //     if (a.getType() == ActionType.HINT_COLOUR) {
            //     actionHintColour = new Maybe<Colour>(a.getColour());
            //     } else if (a.getType() == ActionType.HINT_VALUE) {
            //         actionHintValue = new Maybe<Integer>(a.getValue());
            //     }

            //     if (playerIndex == a.getHintReceiver()) {
            //         System.out.println("> Hint colour = " + actionHintColour.toString() + " value = " + actionHintValue.toString() + " --> " + Arrays.toString(runningHints));
            //     }
            // } catch (Exception ex) {}
        }

        // System.out.println("> " + Arrays.toString(runningHints));

        return runningHints;
    }

    public static ArrayList<Card> getOtherPlayersCards(State s, int playerIndex) {
        ArrayList<Card> cards = new ArrayList<Card>();
        for (int otherPlayerIndex : StateUtils.getPlayersOtherThan(s, playerIndex)) {
            for (Card card : s.getHand(otherPlayerIndex)) {
                if (card != null) {
                    cards.add(card);
                }
            }
        }

        return cards;
    }

    public static ArrayList<Card> getPlayedCards(State s) {
        ArrayList<Card> fireworkCards = new ArrayList<Card>();
        for (Colour c : Colour.values()) {
            Stack<Card> fireworks = s.getFirework(c);
            while (!fireworks.empty()) {
                fireworkCards.add(fireworks.pop());
            }
        }
        return fireworkCards;
    }

    public static ArrayList<Card> getPlayableFireworksCards(State s) {
        ArrayList<Card> playableCards = new ArrayList<Card>();
        for (Colour c : Colour.values()) {
            Stack<Card> fireworks = s.getFirework(c);
            if (fireworks.empty()) {
                playableCards.add(new Card(c, 1));
            } else {
                Card topCard = fireworks.pop();
                if (!CardUtils.doesCardHasMaximumValue(topCard)) {
                    /* This helper method handles the case where you pass it
                    * a ?5 card, hence the Maybe<Card> return type.
                    */
                    Maybe<Card> maybeNextCard = CardUtils.getNextHighestCardWithSameColour(
                        topCard
                    );
                    if (maybeNextCard.hasValue()) {
                        playableCards.add(maybeNextCard.getValue());
                    }
                }
            }
        }
        return playableCards;
    }

    public static Maybe<Card> getTopFireworksCardForColour(State s, Colour colour) {
        Stack<Card> fireworks = s.getFirework(colour);
        if (fireworks.empty()) {
            return new Maybe<Card>(null);
        }
        return new Maybe<Card>(fireworks.pop());
    }

    public static Maybe<Card> getPlayableFireworkCardForColour(State s, Colour colour) {
        ArrayList<Card> fireworkCards = StateUtils.getPlayableFireworksCards(s);
        for (Card fireworkCard : fireworkCards) {
            if (fireworkCard.getColour() == colour) {
                return new Maybe<Card>(fireworkCard);
            }
        }

        return new Maybe<Card>(null);
    }

}