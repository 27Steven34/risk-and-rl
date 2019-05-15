package main.java;

import java.util.ArrayList;

public class State
{
	/** The list containing all States contained in the S-A space. */
	private static ArrayList<State> allStates = new ArrayList<State>();
	/** The index of this State in <code><i>allStates</i></code>. */
	private int stateNumber = 0;
	/**The Actions available in this State. */
	private ArrayList<Action> actions = new ArrayList<Action>();
	/**
	 * The true expected value of rewards obtained in this State,
	 * if all Actions are equally likely to be chosen.
	 */
	private double expectedReward = 0;
	
	/**
	 * Instantiates this State with Actions contained in <code>act</code>.
	 * @param act	the list of Actions available in this State
	 */
	public State(ArrayList<Action> act)
	{
		actions = act;
		
		for (Action action : actions)
		{
			expectedReward += action.expectedReward();
		}
		expectedReward = expectedReward / actions.size();
		stateNumber = allStates.size();
		allStates.add(this);
	}
	
	/**
	 * 
	 * @return	the list containing all States
	 */
	public static ArrayList<State> getAllStates()
	{
		return allStates;
	}
	
	/**
	 * 
	 * @return	the index of this State in <code>allStates</code>
	 */
	public int getStateNumber()
	{
		return stateNumber;
	}
	
	/**
	 * 
	 * @return	the Actions available in this State
	 */
	public ArrayList<Action> getActions()
	{
		return actions;
	}
	
	/**
	 * 
	 * @return	the true expected value of rewards for this
	 * State, if all Actions are equally likely to be chosen
	 */
	public double getExpectedReward()
	{
		return expectedReward;
	}
	
	/**
	 * Set the Actions available to this State as <code>newActions</code>.
	 * @param newActions	the list of Actions to set
	 */
	public void setActions(ArrayList<Action> newActions)
	{
		actions = newActions;
		
		for (Action action : actions)
		{
			expectedReward += action.expectedReward();
		}
		expectedReward = expectedReward / actions.size();
	}
}
