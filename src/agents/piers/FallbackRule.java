package agents.piers;

import agents.BasicAgent;
import hanabAI.Action;
import hanabAI.Agent;
import hanabAI.State;

public class FallbackRule implements IRule {

    private int _playerIndex;

    public FallbackRule(int playerIndex) {
        this._playerIndex = playerIndex;
    }

    @Override
	public Action play(State s) {
        FallbackRule thisRule = this;
        BasicAgent agent = new BasicAgent() {
            @Override
            public String toString() {
                return s.getName(thisRule._playerIndex);
            }
        };
        agent.init(s);
        return agent.doAction(s);
	}

}