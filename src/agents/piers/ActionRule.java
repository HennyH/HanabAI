package agents.piers;

import hanabAI.Action;
import hanabAI.State;

public class ActionRule implements IRule {

    private Func<State, Action> _actionFactory;

    public ActionRule(Func<State, Action> actionFactory) {
        this._actionFactory = actionFactory;
    }

	@Override
	public Action play(State s) {
        return this._actionFactory.apply(s);
	}
}