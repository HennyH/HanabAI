package agents.piers;

import hanabAI.Action;
import hanabAI.State;

public class RuleSequenceRule implements IRule {

    private IRule[] _ruleSequence;

    public RuleSequenceRule(IRule... rules) {
        this._ruleSequence = rules;
    }

    @Override
	public Action play(State s) {
		for (IRule rule : this._ruleSequence) {
            Action maybeAction = rule.play(s);
            if (maybeAction != null) {
                return maybeAction;
            }
        }
        return null;
	}

}