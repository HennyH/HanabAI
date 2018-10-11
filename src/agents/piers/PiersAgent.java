package agents.piers;

import java.util.Arrays;

import agents.BasicAgent;
import hanabAI.Action;
import hanabAI.Agent;
import hanabAI.State;

public class PiersAgent implements Agent {

    @Override
    public String toString() {
        return "PIERS";
    }

    @Override
    public Action doAction(State s) {
		Action maybePlay = new PlayProbablySafeCardRule(
            s.getNextPlayer(),
            (float)0.5
        ).play(s);
        if (maybePlay != null) {
            return maybePlay;
        }
        BasicAgent a = new BasicAgent();
        a.init(s);
        return a.doAction(s);
	}

}