package agents.piers;

import hanabAI.Action;
import hanabAI.State;

public class IfRule implements IRule {

    private Func<State, Boolean> _condition;
    private IRule _consequence;

    public IfRule(Func<State, Boolean> condition, IRule consequence) {
        this._condition = condition;
        this._consequence = consequence;
    }

    @Override
    public Action play(State s) {
        if (this._condition.apply(s)) {
            return this._consequence.play(s);
        }

        return null;
    }

    public static IfRule isLastTurn(int playerIndex, IRule consequence) {
        return new IfRule(
            new Func<State, Boolean>() {
                @Override
                public Boolean apply(State s) {
                    return !StateUtils.doesPlayerHaveAPossibleTurnLeft(s, playerIndex);
                }
            },
            consequence
        );
    }

    public static IfRule allowedToDiscardACard(IRule consequence) {
        return new IfRule(
            new Func<State, Boolean>() {
                @Override
                public Boolean apply(State s) {
                    /* You cannot discard a card when there are hint
                     * tokens left.
                     */
                    return s.getHintTokens() < 8;
                }
            },
            consequence
        );
    }

    public static IfRule atLeastNHintsLeft(int n, IRule consequence) {
        return new IfRule(
            new Func<State, Boolean>() {
                @Override
                public Boolean apply(State s) {
                    return s.getHintTokens() >= n;
                }
            },
            consequence
        );
    }

    public static IfRule atMostNHintsLeft(int n, IRule consequence) {
        return new IfRule(
            new Func<State, Boolean>() {
                @Override
                public Boolean apply(State s) {
                    return s.getHintTokens() <= n;
                }
            },
            consequence
        );
    }

    public static IfRule atLeastNHLivesLeft(int n, IRule consequence) {
        return new IfRule(
            new Func<State, Boolean>() {
                @Override
                public Boolean apply(State s) {
                    return s.getFuseTokens() >= n;
                }
            },
            consequence
        );
    }

    public static IfRule atLeastXHintsAndYLivesLeft(int hints, int lives, IRule consequence) {
        return new IfRule(
            new Func<State, Boolean>() {
                @Override
                public Boolean apply(State s) {
                    return s.getFuseTokens() >= lives && s.getHintTokens() >= hints;
                }
            },
            consequence
        );
    }
}



