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
    // System.err.println(Arrays.toString(args));
    // Agent[] agents = {new PiersAgent(), new PiersAgent(), new PiersAgent(), new PiersAgent()};
    // if (args.length > 1) {
    //   int numberOfPlayers = Integer.parseInt(args[1]);
    //   ArrayList<Agent> varAgents = new ArrayList<Agent>();
    //   for (int i = 1; i <= numberOfPlayers; i++) {
    //     varAgents.add(new PiersAgent());
    //   }
    //   agents = varAgents.toArray(new Agent[0]);
    // }
    // Hanabi game= new Hanabi(agents);
    // StringBuffer log = new StringBuffer("A simple game for three basic agents:\n");
    // int result = game.play(log);
    // log.append("The final score is "+result+".\n");
    // log.append(critique(result));
    // System.out.print(log);
    try {
      FileOutputStream fos = new FileOutputStream("evolution-log.txt");
      Writer log = new OutputStreamWriter(fos, "UTF8");
      EvolutionRunner.run(
        log,
        8,
        50,
        500,
        (float)0.01,
        500,
        4,
        10
      );
    } catch (Exception ex) {
      System.out.print(ex);
      System.out.print(ex.getMessage());
      System.out.print(Arrays.toString(ex.getStackTrace()));
    }

  }
}


