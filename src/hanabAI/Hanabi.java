package hanabAI;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;

import agents.BasicAgent;
import agents.piers.PiersAgent;
import agents.piers.StateUtils;
import agents.piers.evolution.EvolutionRunner;
import agents.piers.evolution.Genome;

/**
 * A class for running a single game of Hanabi.
 * An array of 2-5 agents is provided, a deal is initialised and players takes turns until the game ends and the score is reported.
 * @author Tim French
 * */
public class Hanabi{

  private Agent[] players;
  private State state;
  private java.util.Stack<Card> deck;

  /**
   * Initilaises the game.
   * @throws IllegalArgumentException if there are not the right number of player
   * */
  public Hanabi(Agent[] agents) throws IllegalArgumentException{
    //check agents between 2 and 5
    players = agents;
    deck = Card.shuffledDeck();
    String[] s = new String[agents.length];
    for(int i=0; i<s.length; i++)s[i] = agents[i].toString();
    state = new State(s, deck);
  }

  /**
   * Plays the game.
   * The agents will execute their strategies until the game is complete and a number is returned.
   * @return the score for the game
   **/
  public int play(){
    try{
      while(!state.gameOver()){
        int p = state.getNextPlayer();
        State localState = state.hideHand(p);
        state = state.nextState(players[p].doAction(localState),deck);
      }
      return state.getScore();
    }
    catch(IllegalActionException e){return -1;}
  }

  /**
   * Plays the game.
   * The agents will execute their strategies until the game is complete and a number is returned.
   * @param log a StringBuffer containing a description of the game
   * @return the score of the game
   **/
  public int play(StringBuffer log){
    try{
      while(!state.gameOver()){
        int p = state.getNextPlayer();
        State localState = state.hideHand(p);
        state = state.nextState(players[p].doAction(localState),deck);
      }
      log.append(StateUtils.formatGameHistory(state));
      return state.getScore();
    }
    catch(IllegalActionException e){
      log.append(StateUtils.formatGameHistory(state));
      e.printStackTrace();
      log.append(e.toString());
      log.append(e.getStackTrace());
      return -1;
    }
  }

  public static String critique(int score){
    if(score==0) return "Tragic: The pyrotechnicians are obliterated by their own incompetence.\n";
    if(score<6) return "Horrible: boos from the crowd.\n";
    if(score<11) return "Poor: a smattering of applause.\n";
    if(score<16) return "Honourable: but no one will remember it.\n";
    if(score<21) return "Excellent: the crowd is delighted.\n";
    if(score<25) return "Extraordinary: no one will forget it.\n";
    else return "Legendary: adults and children alike are speechless, with starts in their eyes.\n";
  }

  /**
   * This main method is provided to run a simple test game with provided agents.
   * The agent implementations should be in the default package.
   * */
  public static void main(String[] args){
    System.err.println(Arrays.toString(args));
    String modelDna = "{R=PS;L=1-3;H=0-0;U=0.063310;W=0.06330971, 0.117145516, 0.15623689, 0.10644951, -0.24107927, 0.12780342, 0.4718204, 0.7247719}>{R=PPS;L=1-3;H=0-8;U=0.668297;W=0.6682967, 0.11869569, 0.100679815, 0.12953922, -0.2827804, 0.12144014, 0.48272705, 0.778931}>{R=TAP;L=1-3;H=0-8;U=0.159808;W=0.15980834, 0.12995258, 0.21847118, 0.11413119, -0.10630783, 0.05989501, 0.43849492, 0.5681055}>{R=PS;L=1-3;H=0-8;U=0.018239;W=0.018238984, 0.12550148, 0.08266041, 0.118032835, -0.28588778, 0.08660595, 0.487055, 0.79237664}>{R=TAP;L=3-3;H=5-4;U=0.039606;W=0.03960633, 0.10325708, 0.13658598, 0.13309912, -0.30415744, 0.12176984, 0.4976891, 0.757141}>{R=TAU;L=1-3;H=0-8;U=0.094780;W=0.094779514, 0.17015794, 0.10871784, 0.08933269, -0.24344909, 0.1750237, 0.33825153, 0.6318985}>{R=OD;L=0-3;H=0-8;U=0.024424;W=0.024423653, 0.15138336, 0.14075342, 0.09473344, -0.18523018, 0.14681208, 0.5336484, 0.7439554}>{R=TAU;L=0-3;H=0-8;U=0.803651;W=0.803651, 0.39848068, -0.06410061, -0.28458428, 0.060878616, 0.3048276, 0.35715824, 0.27665156}>{R=OD;L=3-3;H=2-8;U=0.000367;W=3.671875E-4, 0.099656254, 0.10459375, 0.097593755, -0.29634374, 0.09715626, 0.50240624, 0.7982188}>{R=TAD;L=3-1;H=8-0;U=0.815959;W=0.81595933, 0.055366226, -0.32371068, 0.7377975, 0.13841186, 0.35326564, -0.8821597, 0.73575056}>{R=OD;L=3-3;H=2-0;U=0.788791;W=0.78879106, 0.5902765, 0.1043749, -0.37653336, 0.68054354, 0.57003605, -0.36801544, -0.07650026}";
    Genome model = Genome.parseDna(modelDna);
    Agent[] agents = {Genome.asAgent(model, 0), Genome.asAgent(model, 1), Genome.asAgent(model, 2), Genome.asAgent(model, 3)};
    if (args.length > 1) {
      int numberOfPlayers = Integer.parseInt(args[1]);
      ArrayList<Agent> varAgents = new ArrayList<Agent>();
      for (int i = 0; i < numberOfPlayers; i++) {
        varAgents.add(Genome.asAgent(model, i));
      }
      agents = varAgents.toArray(new Agent[0]);
    }
    Hanabi game= new Hanabi(agents);
    StringBuffer log = new StringBuffer("A simple game for three basic agents:\n");
    int result = game.play(log);
    log.append("The final score is "+result+".\n");
    log.append(critique(result));
    System.out.print(log);
    // try {
    //   FileOutputStream fos = new FileOutputStream("evolution-log.txt");
    //   Writer log = new OutputStreamWriter(fos, "UTF8");
    //   String modelDna = "{R=PS;L=1-3;H=0-0;U=0.063310;W=(0.06330971, 0.117145516, 0.15623689, 0.10644951, -0.24107927, 0.12780342, 0.4718204, 0.7247719)}>{R=PPS;L=1-3;H=0-8;U=0.668297;W=(0.6682967, 0.11869569, 0.100679815, 0.12953922, -0.2827804, 0.12144014, 0.48272705, 0.778931)}>{R=TAP;L=1-3;H=0-8;U=0.159808;W=(0.15980834, 0.12995258, 0.21847118, 0.11413119, -0.10630783, 0.05989501, 0.43849492, 0.5681055)}>{R=PS;L=1-3;H=0-8;U=0.018239;W=(0.018238984, 0.12550148, 0.08266041, 0.118032835, -0.28588778, 0.08660595, 0.487055, 0.79237664)}>{R=TAP;L=3-3;H=5-4;U=0.039606;W=(0.03960633, 0.10325708, 0.13658598, 0.13309912, -0.30415744, 0.12176984, 0.4976891, 0.757141)}>{R=TAU;L=1-3;H=0-8;U=0.094780;W=(0.094779514, 0.17015794, 0.10871784, 0.08933269, -0.24344909, 0.1750237, 0.33825153, 0.6318985)}>{R=OD;L=0-3;H=0-8;U=0.024424;W=(0.024423653, 0.15138336, 0.14075342, 0.09473344, -0.18523018, 0.14681208, 0.5336484, 0.7439554)}>{R=TAU;L=0-3;H=0-8;U=0.803651;W=(0.803651, 0.39848068, -0.06410061, -0.28458428, 0.060878616, 0.3048276, 0.35715824, 0.27665156)}>{R=OD;L=3-3;H=2-8;U=0.000367;W=(3.671875E-4, 0.099656254, 0.10459375, 0.097593755, -0.29634374, 0.09715626, 0.50240624, 0.7982188)}>{R=TAD;L=3-1;H=8-0;U=0.815959;W=(0.81595933, 0.055366226, -0.32371068, 0.7377975, 0.13841186, 0.35326564, -0.8821597, 0.73575056)}>{R=OD;L=3-3;H=2-0;U=0.788791;W=(0.78879106, 0.5902765, 0.1043749, -0.37653336, 0.68054354, 0.57003605, -0.36801544, -0.07650026)})";
    //   EvolutionRunner.run(
    //     log,
    //     10,
    //     100,
    //     200,
    //     (float)0.8,
    //     (float)0.01,
    //     500,
    //     4,
    //     20
    //   );
    // } catch (Exception ex) {
    //   System.out.print(ex);
    //   System.out.print(ex.getMessage());
    //   System.out.print(Arrays.toString(ex.getStackTrace()));
    // }

  }
}


