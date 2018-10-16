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
            new PlaySafeCardRule(player),
            IfRule.atLeastNHLivesLeft(
                2,
                new PlayProbablySafeCardRule(player, (float)0.7)
            ),
            new TellAnyoneAboutPlayableCardRule(
                player,
                (float)1.0,
                (float)1.0,
                (float)1.0,
                (float)-0.3,
                (float)10.0,
                (float)2.0,
                (float)1.5
            ),
            new OsawaDiscardRule(player),
            new TellAnyoneAboutUselessCardRule(
                player,
                (float)1.0,
                (float)1.0,
                (float)1.0,
                (float)-0.3,
                (float)10.0,
                (float)2.0,
                (float)1.5
            ),
            new TellAnyoneAboutUsefulCardRule(
                player,
                (float)0.0,
                (float)1.0,
                (float)1.0,
                (float)1.0,
                (float)-0.3,
                (float)10.0,
                (float)2.0,
                (float)1.5
            ),
            new DiscardRandomRule(player),
            new FallbackRule(player)
        );

        return policy.play(s);
	}

}