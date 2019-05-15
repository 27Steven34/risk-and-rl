package main.java;

import java.util.ArrayList;

public class Agent
{
	/** A list containing every Agent which exists in the S-A space. */
	private static ArrayList<Agent> allAgents = new ArrayList<Agent>();
	/** The current amount of fitness this Agent possesses. */
	private long fitness = 0;
	/** The <i>perceived</i> expected rewards for each Action as calculated by this Agent. */
	private ArrayList<ArrayList<Double>> expectedRewards = new ArrayList<ArrayList<Double>>();
	/**
	 * The amount of weight (0-1) given by this Agent to this
	 * Agent. All remaining weight is divided among the other
	 * Agents in <code><i>allAgents</i></code>.
	 */
	private double selfWeight = 1;
	/**
	 * The amount of weight (0-1) given by this Agent to new
	 * rewards obtained. All other weight is given to the
	 * previously calculated reward.
	 */
	private double timeWeight = .1;
	/**
	 * The amount of (incoming and outgoing) resources shared
	 * to the rest of the population. All other resources are
	 * kept for this Agent.
	 */
	private double resourceShare = 0;
	/** The current State of this Agent. */
	private State currentState = null;
	/** The choice most recently made by this Agent. */
	private int choice = 0;
	/** The reward most recently obtained by this Agent. */
	private double newReward = 0;
	
	/**
	 * @param dimensions a list of the number of options in each state
	 * @param state the initial State of the Agent
	 */
	public Agent(int[] dimensions, State state)
	{
		for (int i = 0; i < dimensions.length; i++)
		{
			ArrayList<Double> e = new ArrayList<Double>();
			for (int j = 0; j < dimensions[i]; j++)
			{
				e.add(State.getAllStates().get(i).getExpectedReward());
			}
			expectedRewards.add(e);
		}
		setCurrentState(state);
		allAgents.add(this);
	}
	
	/**
	 * @param dimensions a list of the number of options in each state
	 * @param W the weight of own experiences vs others
	 * @param T the weight of recent experiences vs cumulative
	 * @param state the initial State of the Agent
	 * @param fitness the initial fitness of the Agent
	 */
	public Agent(int[] dimensions, double W, double T, double R, State state, int fitness)
	{
		for (int i = 0; i < dimensions.length; i++)
		{
			ArrayList<Double> e = new ArrayList<Double>();
			for (int j = 0; j < dimensions[i]; j++)
			{
				e.add(State.getAllStates().get(i).getExpectedReward());
			}
			expectedRewards.add(e);
		}
		selfWeight = W;
		timeWeight = T;
		resourceShare = R;
		setCurrentState(state);
		this.fitness = fitness;
		allAgents.add(this);
	}
	
	/**
	 * 
	 * @return	the list containing all Agents
	 */
	public static ArrayList<Agent> getAllAgents()
	{
		return allAgents;
	}
	
	/**
	 * 
	 * @return	the current amount of fitness this Agent has
	 */
	public long getFitness()
	{
		return fitness;
	}
	
	/**
	 * 
	 * @return	the amount (0-1) of resources shared by this Agent
	 */
	public double getResourceShare()
	{
		return resourceShare;
	}
	
	/**
	 * 
	 * @return the most recently obtained reward for this Agent
	 */
	public double getNewReward()
	{
		return newReward;
	}
	
	/**
	 * Returns the perceived expected reward for the Actions
	 * in the State at index <code>index</code>.
	 * @param index	the index of the State
	 * @return	the expected reward for Actions in that State
	 */
	public ArrayList<Double> getExpectedRewards(int index)
	{
		return expectedRewards.get(index);
	}
	
	/**
	 * 
	 * @return	the current State of this Agent
	 */
	public State getCurrentState()
	{
		return currentState;
	}
	
	/**
	 * 
	 * @return	the most recent choice made by this Agent
	 */
	public int getChoice()
	{
		return choice;
	}
	
	/**
	 * Chooses an Action based on a Boltzmann distribution
	 * created from the perceived expected rewards of the
	 * Actions of the State at index <code>index</code>.
	 * @param state the State from which to select Actions
	 * @return the index of the Action chosen
	 */
	public int makeChoice(int state)
	{
		double z = 0;
		for (double reward : expectedRewards.get(state))
		{
			z += Math.pow(Math.E, reward/Configuration.temperature);
		}
		double pVal = Math.random();
		for (int i = 0; i < expectedRewards.get(state).size(); i++)
		{
			pVal -= Math.pow(Math.E,
					expectedRewards.get(state).get(i)/Configuration.temperature)/z;
			if (pVal < 0)
			{
				return i;
			}
		}
		// This line should never be hit, but just in case...
		return expectedRewards.get(state).size() - 1;
	}

	/**
	 * Adds fitness to the Agent and notes the reward for easy access during
	 * update of expected rewards.
	 * @param value the amount of fitness to add
	 * @param choice the choice made that gave this reward
	 */
	public void addFitness(double value, int choice)
	{
		fitness += (1 - resourceShare)*value;
		newReward = value;
		this.choice = choice;
	}
	
	/**
	 * Adds or removes fitness without taking resource sharing into account.
	 * @param value the amount of fitness to add (negative values remove) 
	 */
	public void addRemoveFitnessNoShare(double value)
	{
		fitness += value;
	}

	/**
	 * Sets the current State of this Agent.
	 * @param newState	the State to set
	 */
	public void setCurrentState(State newState)
	{
		currentState = newState;
	}
	
	/**
	 * Updates the matrix of expected rewards to reflect choices made and rewards
	 * obtained by all Agents in this timestep.
	 * @param numbers the matrix of number of Agents who chose each Action
	 * @param rewards the matrix of total rewards obtained for each Action
	 */
	public void updateChoices(ArrayList<ArrayList<Integer>> numbers,
			ArrayList<ArrayList<Integer>> rewards)// update expected rewards
	{
		// update chosen option
		int index = currentState.getStateNumber();// index of current State
		int rTot = rewards.get(index).get(choice);// total rewards obtained for chosen Action
		int cTot = numbers.get(index).get(choice);// total number of Agents which chose this Action
		
		// Formula for updating this choice is carried out below.
		double newER = expectedRewards.get(index).get(choice);
		// If 100% knowledge sharing
		if (selfWeight == 0)
		{
			newER *= 1 - timeWeight;
			newER += (double)rTot/cTot * timeWeight;
		}
		else// if less than 100% knowledge sharing
		{
			if (cTot > 1)// someone else chose this option as well
			{
				newER = (double) (rTot - newReward) / (cTot - 1);
				newER *= 1 - selfWeight;
				newER += selfWeight*newReward;
				newER *= timeWeight;
				newER += (1 - timeWeight)*expectedRewards.get(index).get(choice);
			}
			else// only this agent chose this option
			{
				newER *= 1 - timeWeight;
				newER += newReward*timeWeight;
			}
		}
		expectedRewards.get(index).set(choice, newER);// set new expected reward for this Action
		
		// update all other options
		for (int i = 0; i < expectedRewards.size(); i++)
		{
			for (int j = 0; j < expectedRewards.get(i).size(); j++)
			{
				if ((i != index) || (j != choice))// don't want to repeat chosen Action
				{
					rTot = rewards.get(i).get(j);// total rewards obtained for this Action
					cTot = numbers.get(i).get(j);// total number of Agents which chose this Action
					if (cTot > 0)// if nobody chose, keep the same. Else follow through formula.
					{
						newER = (double)rTot/cTot;
						newER *= 1-selfWeight;
						newER += selfWeight*expectedRewards.get(i).get(j);
						newER *= timeWeight;
						newER += (1 - timeWeight)*expectedRewards.get(i).get(j);
						expectedRewards.get(i).set(j, newER);
					}
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "Agent [fitness=" + fitness + ", expectedRewards=" + expectedRewards
				+ ", selfWeight=" + selfWeight + ", timeWeight=" + timeWeight
				+ "]";
	}
}
