```java
@Override
    public Action doAction(State s) {
        int player = StateUtils.getCurrentPlayer(s);
		IRule policy = new RuleSequenceRule(
            /* IfRule(lives > 1 ∧¬ deck.hasCardsLeft) Then (PlayProbablySafeCard(0.0)) */
            IfRule.atLeastNHLivesLeft(
                2,
                new RuleSequenceRule(
                    IfRule.isLastTurn(
                        player,
                        new PlayProbablySafeCardRule(player, (float)0.0)
                    ),
                    new PlaySafeCardRule(player)
                )
            ),
            /* PlaySafeCard */
            new PlaySafeCardRule(player),
            /* IfRule (lives > 1) Then (PlayProbablySafeCard(0.6)) */
            IfRule.atLeastNHLivesLeft(
                2,
                new PlayProbablySafeCardRule(player, (float)0.6)
            ),
            /* TellAnyoneAboutUsefulCard */
            IfRule.atLeastNHintsLeft(
                1,
                new TellAnyoneAboutUsefulCardRule(
                        player,
                        (float)3.0,
                        (float)1.0,
                        (float)1.0,
                        (float)1.0,
                        (float)-0.3,
                        (float)10.0,
                        (float)2.0
                )
            ),
            /* IfRule (information < 4) Then (TellDispensable) */
            IfRule.atLeastNHintsLeft(
                1,
                IfRule.atMostNHintsLeft(
                    3,
                    new TellAnyoneAboutUselessCardRule(
                        player,
                        (float)0.0,
                        (float)1.0,
                        (float)1.0,
                        (float)-0.3,
                        (float)10.0,
                        (float)2.0
                    )
                )
            ),
            /* OsawaDiscard */
            IfRule.allowedToDiscardACard(new OsawaDiscardRule(player)),
            /* TellRandomly */
            IfRule.atLeastNHintsLeft(
                1,
                new TellAnyoneAboutUsefulCardRule(
                        player,
                        (float)0.0,
                        (float)1.0,
                        (float)1.0,
                        (float)1.0,
                        (float)-0.3,
                        (float)10.0,
                        (float)2.0
                )
            ),
            IfRule.allowedToDiscardACard(new DiscardRandomRule(player)),
            new FallbackRule()
        );
```

-----------

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
            new DiscardRandomRule(player),
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
            new FallbackRule(player)
        );

        return policy.play(s);
	}