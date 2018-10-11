package agents.piers;

import hanabAI.Action;
import hanabAI.State;

public interface IRule {
    public Action play(State s);
}