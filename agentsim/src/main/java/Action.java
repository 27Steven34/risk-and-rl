package main.java;

import java.util.ArrayList;
import java.util.Random;

public class Action
{
	/** A list containing every Action in the entire S-A space. */
	private static ArrayList<Action> allActions = new ArrayList<Action>();
	/** A list containing all rewards available for this Action. */
	private ArrayList<Integer> rewards = new ArrayList<Integer>();
	/** Which State to go to after choosing this Action. */
	private State nextState = null;
	/**
	 * The true average reward for this option, if all rewards
	 * in <code>rewards</code> are equally likely.
	 */
	private int expectedReward = 0;
	/**
	 * A psedorandom number generator dedicated to generating
	 * numbers for use by this Action.
	 */
	private Random prng = null;
	
	/**
	 * Uses a list of all possible rewards to instantiate this Action.
	 * @param	rew the list of all possible rewards
	 */
	public Action(ArrayList<Integer> rew)
	{
		rewards = rew;
		
		for (int reward : rewards)
		{
			expectedReward += reward;
		}
		expectedReward = expectedReward / rewards.size();
		allActions.add(this);
		prng = new Random();
	}
	
	/**
	 * 
	 * @return	the list containing every Action which has been created
	 */
	public static ArrayList<Action> getAllActions()
	{
		return allActions;
	}
	
	/**
	 * 
	 * @return	the next State to go to once this Action is taken
	 */
	public State getNextState()
	{
		return nextState;
	}

	/**
	 * Set the next State to go to once
	 * this Action is taken.
	 * @param state	the State to set
	 */
	public void setNextState(State state)
	{
		nextState = state;
	}
	
	/**
	 * Pseudorandomly chooses a reward for this Action from
	 * the possible rewards for this Action.
	 * @return	the chosen reward
	 */
	public int giveReward()
	{
		int i = (int) Math.floor(rewards.size()*prng.nextDouble());
		return rewards.get(i);
	}

	/**
	 * The true expected value of this Action, if all rewards
	 * are equally likely.
	 * @return	the expected reward for this Action
	 */
	public double expectedReward()
	{
		return expectedReward;
	}
}
