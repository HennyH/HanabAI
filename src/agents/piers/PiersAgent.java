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
        int player = StateUtils.getCurrentPlayer(s);
		IRule policy = new RuleSequenceRule(
            new PlayProbablySafeCardRule(player, (float)0.6),
            IfRule.atLeastNHintsLeft(
                6,
                new TellAnyoneAboutUsefulCardRule(
                    player,
                    (float)1.0,
                    (float)1.0,
                    (float)1.0,
                    (float)1.0,
                    (float)5.0
                )
            )
        );

        Action a = policy.play(s);
        if (a != null) {
            return a;
        }

        BasicAgent fallback = new BasicAgent();
        fallback.init(s);
        return fallback.doAction(s);
	}

}