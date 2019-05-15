package main.java;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Configuration
{
	/**
	 * the weight that an individual gives to its own experiences
	 * compared to others' experiences. Ranges from 0 to 1.
	 */
	static double selfWeight = 1;
	/**
	 * the weight that an individual gives to recent experiences
	 * compared to its cumulative experience. Ranges from 0 to 1.
	 */
	static double timeWeight = .1;
	/**
	 * the amount of resources shared to group. Ranges from 0 to 1.
	 */
	static double resourceShare = 0;
	/**the number of individuals to start in the environment*/
	static int numberOfAgents = 50;
	/**the number of timesteps to run*/
	static int numberOfTrials = 100;
	/**the resource cost between each timestep*/
	static double costOfLiving = 100;
	/**
	 * denotes the entirety of the S-A space. The space consists of at least
	 * one state, which are enclosed in the first set of {}. Each state consists
	 * of at least one action, which are listed in a set of {} corresponding to
	 * the state in which that action is possible. Finally, each action has a
	 * list of at least one possible reward, which are listed in the lowest level
	 * of {}. This list of possible rewards is followed by a colon and the index
	 * of the state to which that action leads.
	 * <p>
	 * For example, the default S-A space
	 * of "{{{100}:0,{0,300}:0}}" has one state with two possible actions. The
	 * first action always gives a reward of 100, while the second action may give
	 * a reward of 0 or of 300. Both actions lead to the first state (since it is
	 * the only possible state).
	 */
	static String stateActionSpace = "{{{100}:0,{0,300}:0}}";
	
	static double temperature = 20;
	static String runType = "";
	
	/**
	 * sets environment parameters and variables from command-line arguments.
	 * Any variables not provided will take on default states.
	 * @param args the full list of arguments passed to main
	 * @throws IOException when fails to find "config.txt"
	 */
	public static void setConfig(String[] args) throws IOException
	{
		List<String> lines = null;
		Path config = Paths.get("config.txt");
		lines = Files.readAllLines(config);
		
		for (String s : lines)
		{
			// split varName=varValue
			String[] split = s.split("=");
			String varName = split[0];
			if (split.length > 1)// if proper format was provided
			{
				String varValue = split[1];
				// come here to add more "else if"s if more parameters are added
				// set value of parameters based on varName
				if (varName.equals("selfWeight")) selfWeight = Double.parseDouble(varValue);
				else if (varName.equals("timeWeight")) timeWeight = Double.parseDouble(varValue);
				else if (varName.equals("resourceShare")) resourceShare = Double.parseDouble(varValue);
				else if (varName.equals("numberOfAgents")) numberOfAgents = Integer.parseInt(varValue);
				else if (varName.equals("numberOfTrials")) numberOfTrials = Integer.parseInt(varValue);
				else if (varName.equals("costOfLiving")) costOfLiving = Double.parseDouble(varValue);
				else if (varName.equals("stateActionSpace")) stateActionSpace = varValue;
				else System.out.println("Parameter " + varName + " is not valid.");
			}
		}
		
		// Same procedure as above, but uses arguments passed to program instead of the lines
		// read from config. This order ensures that command-line arguments have the final say
		// in a parameter's value.
		for (String s : args)
		{
			String[] split = s.split("=");
			String varName = split[0];
			if (split.length > 1)
			{
				String varValue = split[1];
				if (varName.equals("selfWeight")) selfWeight = Double.parseDouble(varValue);
				else if (varName.equals("timeWeight")) timeWeight = Double.parseDouble(varValue);
				else if (varName.equals("resourceShare")) resourceShare = Double.parseDouble(varValue);
				else if (varName.equals("numberOfAgents")) numberOfAgents = Integer.parseInt(varValue);
				else if (varName.equals("numberOfTrials")) numberOfTrials = Integer.parseInt(varValue);
				else if (varName.equals("costOfLiving")) costOfLiving = Double.parseDouble(varValue);
				else if (varName.equals("stateActionSpace")) stateActionSpace = varValue;
				else if (varName.equals("runType")) runType = varValue;
				else System.out.println("Parameter " + varName + " is not valid.");
			}
		}
	}
	
	/**
	 * Parses the String provided as parameter stateActionSpace and creates the S-A space
	 * @return the "dimensions" of the S-A space as an int[]. The length of this array
	 * matches the number of states, each int in the array represents the number of actions
	 * possible for the state corresponding to the index of that int.
	 * @throws Exception if the provided String for S-A space is not properly formatted.
	 * Note that this also means that there is no S-A space created, and therefore no
	 * reason to continue the program.
	 * @see Configuration#stateActionSpace
	 */
	public static int[] parseSASpace() throws Exception
	{
		// set up tokenization
		ArrayList<String> tokens = new ArrayList<String>();
		ArrayList<Character> numbers = new ArrayList<Character>();
		for (char e : "0123456789".toCharArray())
		{
			numbers.add(e);
		}
		String token = "";
		
		// begin tokenization
		for (char c : stateActionSpace.toCharArray())
		{
			if (numbers.contains(c) || (("" + c).equals(":") && (token.equals(""))))
			{
				token += c;
			}
			else if (("" + c).equals("{") || ("" + c).equals("}"))
			{
				if (token.equals(""))
				{
					token += c;
					tokens.add(token);
					token = "";
				}
				else
				{
					tokens.add(token);
					token = "" + c;
					tokens.add(token);
					token = "";
				}
			}
			else// character is whitespace, comma, or letter
			{
				// we don't want whitespace, commas, or letters,
				// but these can delimit numbers
				if (token.equals("")){}//do nothing
				else
				{
					tokens.add(token);
					token = "";
				}
			}
		}
		if (!token.equals(""))
		{
			tokens.add(token);
			token = "";
		}// end tokenization
		
		// set up parsing
		int level = 0;// tells whether parser is at the State, Action, or reward level
		int bottom = 0;// first Action which belongs to current State
		int top = 0;// final Action which belongs to current State
		ArrayList<Integer> r = new ArrayList<Integer>();//rewards for current Action
		ArrayList<String> nextStateTokens = new ArrayList<String>();//holds ":index" tokens
		
		// begin parsing, creating S-A space
		for (String t : tokens)
		{
			if (t.equals("{"))
			{
				level++;
				if (level > 3) throw new Exception("S-A space level too deep."
						+ "Please make sure that stateActionSpace=" + stateActionSpace
						+ " is valid.");
			}
			else if (t.equals("}"))
			{
				level--;
				if (level == 2)
				{
					@SuppressWarnings("unused")//the creation of the Action is its use
					Action a = new Action(r);
					r = new ArrayList<Integer>();
					top++;
				}
				else if (level == 1)
				{
					ArrayList<Action> actions = new ArrayList<Action>();
					actions.addAll(Action.getAllActions().subList(bottom, top));
					bottom = top;
					@SuppressWarnings("unused")// the creation of the State is its use
					State s = new State(actions);
				}
				else if (level < 0) throw new Exception("S-A space levels unbalanced"
						+ "(extra '}'). Please make sure that stateActionSpace="
						+ stateActionSpace + " is valid.");
			}
			else if (t.startsWith(":")) nextStateTokens.add(t);
			else// it is a number
			{
				if (level == 3) r.add(Integer.parseInt(t));
				else throw new Exception("Number found in improper level of {}. Please"
						+ "make sure that all numbers are in the third level of {}");
			}
		}
		if (level > 0) throw new Exception("S-A space levels unbalanced"
				+ "(not enough '}'). Please make sure that stateActionSpace="
				+ stateActionSpace + " is valid.");
		// end creating S-A space
		
		// begin linking S-A space
		if (nextStateTokens.size() != Action.getAllActions().size()) throw new Exception(
				"The number of Action->State links does not match the total number of Actions."
				+ " Please make sure that there is exactly one \"':' + index\" for"
				+ " each Action in parameter stateActionSpace");
		for (int index = 0; index < nextStateTokens.size(); index++)
		{
			int stateNum = Integer.parseInt(nextStateTokens.get(index).substring(1));
			Action.getAllActions().get(index).setNextState(State.getAllStates().get(stateNum));
		}
		
		// need to return S-A space dimensions
		ArrayList<Integer> dimensions = new ArrayList<Integer>();
		for (State s : State.getAllStates())
		{
			dimensions.add(s.getActions().size());
		}
		int[] d = new int[dimensions.size()];
		for (int i = 0; i < dimensions.size(); i++)
		{
			d[i] = dimensions.get(i);
		}
		return d;
	}
	
	/**
	 * Lists the current parameters of the program as <code>varName=varValue</code> separated by
	 * commas and enclosed in [].
	 * @return this list of parameters as a String
	 */
	public static String parameters()
	{
		return "[selfWeight=" + selfWeight + ", timeWeight=" + timeWeight + ", resourceShare="
				+ resourceShare + ", numberOfAgents=" + numberOfAgents + ", numberOfTrials="
				+ numberOfTrials + ", costOfLiving="
						+ costOfLiving+ ", stateActionSpace=" + stateActionSpace + "]";
		
	}
}
