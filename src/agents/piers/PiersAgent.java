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
                new PlayProbablySafeCardRule(player, (float)0.70074034)
            ),
            new TellAnyoneAboutPlayableCardRule(
                player,
                (float)0.09869383,
                (float)0.10183655,
                (float)0.09780155,
                (float)-0.2976542,
                (float)0.100575045,
                (float)0.50125265,
                (float)0.796629
            ),
            new OsawaDiscardRule(player),
            new TellAnyoneAboutUselessCardRule(
                player,
                (float)0.09642475,
                (float)0.101364344,
                (float)0.097746216,
                (float)-0.2989164,
                (float)0.099828675,
                (float)0.4998047,
                (float)0.7992938
            ),
            new TellAnyoneAboutUsefulCardRule(
                player,
                (float)7.7793625E-4,
                (float)0.09551717,
                (float)0.10293407,
                (float)0.09529035,
                (float)-0.29709414,
                (float)0.099703714,
                (float)0.50281525,
                (float)0.80379754
            ),
            new DiscardRandomRule(player),
            new FallbackRule(player)
        );

        return policy.play(s);
	}

}