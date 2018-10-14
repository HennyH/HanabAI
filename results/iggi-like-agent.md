```java
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
                (float)2.0
            ),
            new OsawaDiscardRule(player),
            new TellAnyoneAboutUselessCardRule(
                player,
                (float)1.0,
                (float)1.0,
                (float)1.0,
                (float)-0.3,
                (float)10.0,
                (float)2.0
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
                (float)2.0
            ),
            new FallbackRule(player)
        );
```