```java
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
```