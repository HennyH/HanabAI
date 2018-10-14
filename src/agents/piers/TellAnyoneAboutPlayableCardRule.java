package agents.piers;

import hanabAI.Action;
import hanabAI.State;

public class TellAnyoneAboutPlayableCardRule implements IRule {

    private int _playerIndex;

    public TellAnyoneAboutPlayableCardRule(int playerIndex) {
        this._playerIndex = playerIndex;
    }

    @Override
	public Action play(State s) {
		return null;
    }

}