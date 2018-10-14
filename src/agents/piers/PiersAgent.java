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
            new PlayProbablySafeCardRule(player, (float)1.0),
            IfRule.allowedToDiscardACard(new OsawaDiscardRule(player)),
            IfRule.atLeastNHintsLeft(
                1,
                new TellAnyoneAboutUsefulCardRule(
                    player,
                    (float)0.0,
                    (float)1.0,
                    (float)1.0,
                    (float)1.0,
                    (float)-0.3,
                    (float)10.0
                )
            ),
            IfRule.allowedToDiscardACard(new DiscardRandomRule(player)),
            new FallbackRule()
        );

        return policy.play(s);
	}

}