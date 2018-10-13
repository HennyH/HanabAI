package agents.piers;

import java.util.Random;

import hanabAI.Action;
import hanabAI.ActionType;
import hanabAI.IllegalActionException;
import hanabAI.State;

public class DiscardRandomRule implements IRule {

    private int _playerIndex;

    public DiscardRandomRule(int playerIndex) {
        this._playerIndex = playerIndex;
    }

    @Override
	public Action play(State s) {
		try {
            return new Action(
                this._playerIndex,
                s.getName(this._playerIndex),
                ActionType.DISCARD,
                new Random().nextInt(StateUtils.getNumberOfCardsInPlayersHand(s, this._playerIndex))
            );
        } catch (IllegalActionException ex) {
            System.out.println(ex.getStackTrace());
        }

        return null;
	}

}