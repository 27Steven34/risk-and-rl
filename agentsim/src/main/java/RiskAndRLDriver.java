package main.java;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class RiskAndRLDriver
{
	public static void main(String[] args)
	{
		// File for writing is "output" + identifier + ".txt"/".dat"
		Calendar now = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmssSSS");
		String outputPathName = "";
		String outputPathIdentifier = sdf.format(now.getTime());
		
		outputPathName = "output" + outputPathIdentifier + ".txt";
		
		BufferedWriter out = null;
		try
		{
			out = new BufferedWriter(
					new FileWriter(new File(outputPathName)));
		}
		catch (IOException e1)
		{
			e1.printStackTrace(); // TODO not this
		}
		
		/*
		 * This represents the "dimensions" of the S-A space. The length of
		 * d matches the number of states, and each int in d represents the
		 * number of actions possible for that state.
		 */
		int[] d = null;

		try
		{
			Configuration.setConfig(args);
			d = Configuration.parseSASpace();
		}
		/*
		 * setConfig() throws an IOException when the config file is not found.
		 * This is not important to the successful execution of the program,
		 * but is critical to the proper interpretation of the output.
		 * Inform the user when this happens.
		 */
		catch (IOException e1)
		{
			if (e1.getMessage().equals("config.txt"))
			{
				System.out.println("The configuration file was not found."
						+ " Using default parameters.");
			}
			else System.out.println(e1.getMessage());
		}
		// parseSASpace() throws an Exception (different from the IOException caught above) when
		// the S-A space is not the proper format. This is unrecoverable, so the program quits.
		catch (Exception e1)
		{
			try
			{
				out.write("\n" + e1.getMessage());
			}
			catch (IOException e2)
			{
				// At this point, the program is about to quit,
				// so the IOException is not important.
			}
			System.out.println("\n" + e1.getMessage());
			System.exit(0);
		}
		
		/*
		 * Create all Agents with S-A space dimensions as parameter.
		 * Also uses global parameters provided in config file or
		 * passed as arguments to the program.
		 */
		for (int i = 0; i < Configuration.numberOfAgents; i++)
		{
			new Agent(d, Configuration.selfWeight, Configuration.timeWeight,
					Configuration.resourceShare, State.getAllStates().get(0), 600);
		}
		
		run(out);
	}
	
	/**
	 * Run the environment for the configured number of time steps.
	 * Record statistics and write them to out at each step.
	 * @param out the writer to which output should be sent
	 */
	public static void run(BufferedWriter out)
	{
		// Create the Environment and run the meat of the program: e.trial()
		// for number of times specified by config or as argument in args.
		Environment e = new Environment();
		try
		{
			double optA;// Track proportion of Agents who prefer safe option
			double difference;// the average expected value of A - B;
			double totalResources;// the total resource pool (shared and sum of individuals)
			int n;// the number of remaining agents
			for (int i = 0; i < Configuration.numberOfTrials; i++)
			{
				e.trial();
				optA = 0;
				difference = 0;
				totalResources = e.getResourcePool() + e.getCumulativeNegativeResources();
				for (Agent a : Agent.getAllAgents())
				{
					ArrayList<Double> eR = a.getExpectedRewards(0);
					if (eR.get(0) > eR.get(1))
					{
						optA++;
					}
					difference += eR.get(0) - eR.get(1);
					totalResources += a.getFitness();
				}
				n = Agent.getAllAgents().size();
				out.write(optA/n + "\t" + difference/n + "\t"
						+ ((double) n)/Configuration.numberOfAgents + "\t"
						+ totalResources + "\t" + totalResources/n + "\n");
			}
			out.write(Configuration.parameters().replaceAll(", ", "\t").replaceAll(
					"[\\[\\]]", ""));
			out.flush();
			out.close();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
	}
}
