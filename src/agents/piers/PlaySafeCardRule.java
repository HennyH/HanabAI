package agents.piers;

import hanabAI.Action;
import hanabAI.State;

public class PlaySafeCardRule implements IRule {

    private int _playerIndex;

    public PlaySafeCardRule(int playerIndex) {
        this._playerIndex = playerIndex;
    }

    @Override
	public Action play(State s) {
        /* A 'safe' card is one that has a 1.0 probability of being safe. */
		return new PlayProbablySafeCardRule(this._playerIndex, (float)1.0).play(s);
	}

}