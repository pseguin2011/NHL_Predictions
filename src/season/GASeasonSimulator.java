package season;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.stream.Stream;

import org.jgap.Chromosome;
import org.jgap.Configuration;
import org.jgap.FitnessFunction;
import org.jgap.Gene;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.InvalidConfigurationException;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.IntegerGene;

import schedule.ScheduleParser;

public class GASeasonSimulator {
	Configuration conf;
	FitnessFunction fitness;
	int numGames = -1;
	ArrayList<ArrayList<Double>> ratios;
	public HashMap<String, Double> teamOutcomes;
	public HashMap<String, Integer> teamWins;
	public HashMap<String, Integer> teamLosses;
	File[] listOfFiles = null;
	ScheduleParser parser;
	public HashMap<String, Double> prevRatios;
	
	public GASeasonSimulator(String season) {
		// Data structures
		ratios = new ArrayList<ArrayList<Double>>();
		teamOutcomes = new HashMap<String, Double>();
		teamWins = new HashMap<String, Integer>();
		teamLosses = new HashMap<String, Integer>();
		
		// Parser
		parser = new ScheduleParser(season);
		System.out.println("Schedule Ready");
		numGames = parser.getNumberOfGames();
		numGames *= 0.85;
		System.out.println(numGames + " games");
		
		// Once the schedule is ready, start initializing the previous year data 
		File folder = new File("./data/Games");
		listOfFiles = folder.listFiles();
		initRatios();
		
		// Retrieve the previous year's team data
		prevRatios = parser.getPreviousSeasonOverallRatios(season);
		for(int key:parser.getTeamsList().keySet()) {
			teamOutcomes.put(parser.getTeamsList().get(key), 0d);
			teamLosses.put(parser.getTeamsList().get(key), 0);
			teamWins.put(parser.getTeamsList().get(key), 0);
		}
		
		//setup for GA
		conf = new DefaultConfiguration();
		fitness = new SeasonFitness();
		Gene[] sampleGenes = getSampleGene();
		
		try {
			Chromosome sampleChromosome = new Chromosome(conf);
			sampleChromosome.setGenes(sampleGenes);
			conf.setFitnessFunction(fitness);
			conf.setSampleChromosome(sampleChromosome);
			conf.setPopulationSize(sampleGenes.length);
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
	}
	
	
	/*
	 * Function: runSimulation
	 * Parameters: printOutput
	 * Purpose: using the sample chromosome, this function will evolve the fittest one until there are
	 * 			20 iterations of the same chromosome or we have evolved 500 times.
	 * 	
	 */
	public IChromosome runSimulation(boolean printOutput) {
		IChromosome fittest = null;
		int count = 0, changedCount = 0;
		try {
			Genotype population = Genotype.randomInitialGenotype(conf);
			boolean changed = false;
			population.evolve();
			
			//evolve 500 times or until 20 iterations have been made
			while(!changed && count++ < 500){
				fittest = population.getFittestChromosome();
				population.evolve();
				//if logging is enabled
				if(printOutput)
					System.out.println("Current Best Fittness Value: " + fittest.getFitnessValue());
				changedCount = (fittest.getFitnessValue() == population.getFittestChromosome().getFitnessValue())? changedCount + 1 : 0;
				changed = changedCount > 20;
			}
			
			 
			//now that the fittest chromosome is determined, save the game wins and losses that the prediction has made.
			for(int i = 0; i< fittest.getGenes().length; i++){
				IntegerGene g = (IntegerGene)fittest.getGene(i);
				String team1 = parser.getTeamsList().get(parser.getGameSchedule().get(i).get(0));
				String team2 = parser.getTeamsList().get(parser.getGameSchedule().get(i).get(1));
				if(team1 != null && team2 != null) {
					teamOutcomes.put(team1, teamOutcomes.get(team1) + (g.intValue() == 0?1d:0d));
					teamOutcomes.put(team2, teamOutcomes.get(team2) + (g.intValue() == 1?1d:0d));
					
					teamLosses.put	(team1,	teamLosses.get(team1) 	+ (g.intValue() == 0?1:0));
					teamWins.put	(team1,	teamWins.get(team1) 	+ (g.intValue() == 1?1:0));
					
					teamLosses.put	(team2,	teamLosses.get(team2) 	+ (g.intValue() == 0?1:0));
					teamWins.put	(team2,	teamWins.get(team2) 	+ (g.intValue() == 1?1:0));
				}
			}
		} catch (InvalidConfigurationException e) {
			e.printStackTrace();
		}
		
		System.out.println("Evolved " + (count-1) + " times");
		return fittest;
	}
	
	
	/*
	 * Function:	getSampleGene
	 * Parameters:	None
	 * Purpose:		Create an array of genes as the sample for the GA to use,
	 * 				assigns IntegerGenes as 1 or 0 representing Away(0) or Home(1)
	 */
	private Gene[] getSampleGene() {
		Gene[] genes = new Gene[numGames];
			try {
				for(int i = 0; i < numGames; i++) {
					genes[i] = new IntegerGene(conf, 0, 1);
				}
			} catch (InvalidConfigurationException e) {
				e.printStackTrace();
			}
		return genes;
	}
	
	/*
	 * Function:	initRatios
	 * Parameters:	None
	 * Purpose:		Using previous game year data, this function will loop through all years
	 * 				and document the wins, and losses of every game index
	 */
	private void initRatios() {
		
		String line;
		//for each which game of the current year
		for(int index=1; index < numGames + 1; index++) {
			ArrayList<Double> results = new ArrayList<Double>();
			results.add(0d);
			results.add(0d);
			int num = 0;
			//go through the game index of other years
			for(File file: listOfFiles) {
				try (Stream<String> lines = Files.lines(Paths.get("./data/Games/" + file.getName()))) {
					Stream<String> newStream = lines.skip(index);
					//add to away wins or home wins accordingly
					if (!Stream.empty().equals(newStream)) {
							Optional<String> a;
							if((a = newStream.findFirst()).isPresent()) {
							line = a.get();
							int awayScore = Integer.parseInt(line.split(",")[5]);
						    int homeScore = Integer.parseInt(line.split(",")[12]);
						    if(homeScore > awayScore)
						    	results.set(0, results.get(0) + 1);
						    else if(awayScore > homeScore)
						    	results.set(1, results.get(0) + 1);
						    num++;
						}
					}
					lines.close();			    
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			//set the ratio
			if (num == 0)
				num = 1;
			results.set(0, results.get(0)/num);
			results.set(1, results.get(1)/num);
			ratios.add(results);
		}
	}
	
	/*
	 * Function:	getHomeWinRatio
	 * Parameters:	index
	 */
	private double getHomeWinRatio(int index) {
		return ratios.get(index).get(0);
	}

	/*
	 * Function:	getHomeWinRatio
	 * Parameters:	index
	 */
	private double getAwayWinRatio(int index) {
		return ratios.get(index).get(1);
	}
	
	
	
	/*
	 * Class
	 * SeasonFitness
	 * Determines the fitness of a season prediction
	 */
	private class SeasonFitness extends FitnessFunction {
		
		/*
		 * Function: 	evaluate
		 * Parameters:	Chromosome
		 * Purpose:		determines the fitness of the current prediction(Chromosome)
		 * 				based on the prediction it made, the number of times that game index resulted
		 * 				in such an outcome (Home winning or Away Winning) and the number of times the
		 * 				predicted winning team won the year before in comparison to the losing team.
		 * Returns:		The sum of the predictions with regards to the actual oucomes 
		 */
		@Override
		protected double evaluate(IChromosome arg0) {
			Gene[] genes = arg0.getGenes();
			double fitness = 0d;
			for(int i = 0; i < numGames; i++) {
				if(((IntegerGene)genes[i]).intValue() == 0) {
					if(prevRatios.containsKey(parser.getTeamsList().get(parser.getGameSchedule().get(i).get(0))))
						fitness += getHomeWinRatio(i) * prevRatios.get(parser.getTeamsList().get(parser.getGameSchedule().get(i).get(0)));//*((IntegerGene)genes[i]).intValue();
				} else if (((IntegerGene)genes[i]).intValue() == 1) {
					if(prevRatios.containsKey(parser.getTeamsList().get(parser.getGameSchedule().get(i).get(1))))
						fitness += getAwayWinRatio(i) * prevRatios.get(parser.getTeamsList().get(parser.getGameSchedule().get(i).get(1)));//*((IntegerGene)genes[i]).intValue();			
				}
			}
			return fitness;
		}
		
	}
}
