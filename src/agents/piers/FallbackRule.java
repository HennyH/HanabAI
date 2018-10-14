package agents.piers;

import agents.BasicAgent;
import hanabAI.Action;
import hanabAI.State;

public class FallbackRule implements IRule {

    @Override
	public Action play(State s) {
        BasicAgent agent = new BasicAgent();
        agent.init(s);
        return agent.doAction(s);
	}

}