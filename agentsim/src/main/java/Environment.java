package main.java;

import java.util.ArrayList;

public class Environment
{
	private double resourcePool = 0;
	private double cumulativeNegativeResources = 0;
	
	public Environment()// why not have this class be static?
	{
		
	}
	
	/**
	 * @return the resource pool shared by all Agents
	 */
	public double getResourcePool()
	{
		return resourcePool;
	}

	/**
	 * @return the cumulativeNegativeResources
	 */
	public double getCumulativeNegativeResources()
	{
		return cumulativeNegativeResources;
	}

	/**
	 * For all Agents in this Environment: make choice of Action, receive reward
	 * based on choice, update all expected rewards, move to new State, then "kill"
	 * Agent if it does not have enough fitness.
	 */
	public void trial()
	{
		// matrix of total rewards received for each Action
		ArrayList<ArrayList<Integer>> rewards = new ArrayList<ArrayList<Integer>>();
		// matrix of total number of Agents which chose each Action
		ArrayList<ArrayList<Integer>> numberChosen = new ArrayList<ArrayList<Integer>>();
		// initialize both matrices to proper dimensions with all zeroes
		for (State s : State.getAllStates())
		{
			ArrayList<Integer> r = new ArrayList<Integer>();
			ArrayList<Integer> c = new ArrayList<Integer>();
			for (int i = 0; i < s.getActions().size(); i++)
			{
				r.add(0);
				c.add(0);
			}
			rewards.add(r);
			numberChosen.add(c);
		}
		
		// everyone takes action, receives reward
		for (Agent a : Agent.getAllAgents())
		{
			State s = a.getCurrentState();
			int index = s.getStateNumber();
			int choice = a.makeChoice(index);
			int reward = s.getActions().get(choice).giveReward();
			numberChosen.get(index).set(choice, numberChosen.get(index).get(choice) + 1);
			a.addFitness(reward, choice);
			resourcePool += a.getResourceShare()*reward;
			rewards.get(index).set(choice, rewards.get(index).get(choice) + reward);
		}
		
		// everyone shares experience, updates choice probabilities,
		// Agents move to new States
		for (Agent a : Agent.getAllAgents())
		{
			a.updateChoices(numberChosen, rewards);
			a.setCurrentState(a.getCurrentState().getActions().get(a.getChoice()).getNextState());
		}
		
		// take away fitness from all Agents and pool
		double totalSharedCost = 0;
		for (Agent a : Agent.getAllAgents())
		{
			totalSharedCost += a.getResourceShare()*Configuration.costOfLiving;
		}
		if (resourcePool < totalSharedCost) totalSharedCost = resourcePool;
		for (Agent a : Agent.getAllAgents())
		{// NOTE: this block does not support nonhomogenous resource sharing
			if (a.getResourceShare()*Configuration.costOfLiving
					< totalSharedCost/Agent.getAllAgents().size())
			{
				resourcePool -= a.getResourceShare()*Configuration.costOfLiving;
				a.addRemoveFitnessNoShare((a.getResourceShare() - 1)*Configuration.costOfLiving);
			}
			else
			{
				resourcePool -= totalSharedCost/Agent.getAllAgents().size();
				a.addRemoveFitnessNoShare(totalSharedCost/Agent.getAllAgents().size()
						- Configuration.costOfLiving);
			}
		}
		
		// remove agents who do not have enough fitness
		for (int i = 0; i < Agent.getAllAgents().size(); i++)
		{
			if (Agent.getAllAgents().get(i).getFitness() < 0)
			{
				cumulativeNegativeResources += Agent.getAllAgents().get(i).getFitness();
				Agent.getAllAgents().remove(i--);
			}
		}
	}
}
