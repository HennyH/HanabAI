package agents.piers.evolution;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import hanabAI.Agent;
import hanabAI.Hanabi;

public class SimulationCallable implements Callable<SimulationCallable.Result> {

    private Genome _genome;
    private int _numberOfPlayers;
    private int _numberOfRounds;

    public SimulationCallable(
            Genome genome,
            int numberOfPlayers,
            int numberOfRounds
    ) {
        this._genome = genome;
        this._numberOfPlayers = numberOfPlayers;
        this._numberOfRounds = numberOfRounds;
    }

    public class Result {
        public final Genome genome;
        public final ArrayList<Integer> scores;

        protected Result(Genome genome, ArrayList<Integer> scores) {
            this.genome = genome;
            this.scores = scores;
        }

    }

    @Override
    public Result call() throws Exception {
        ArrayList<Integer> scores = new ArrayList<>();
        for (int round = 1; round <= this._numberOfRounds; round++) {
            Agent[] agents = new Agent[this._numberOfPlayers];
            for (int playerIndex = 0; playerIndex < this._numberOfPlayers; playerIndex++) {
                agents[playerIndex] = Genome.asAgent(this._genome, playerIndex);
            }
            Hanabi game = new Hanabi(agents);
            scores.add(game.play());
        }

        return new Result(this._genome, scores);
    }
}