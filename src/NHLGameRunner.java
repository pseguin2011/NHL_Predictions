import java.time.LocalDate;
import java.time.Year;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import game.GamePredictionClassifier;
import game.GameWinnerTrainer;
import season.GASeasonSimulator;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

public class NHLGameRunner {
	public static void main(String[] args) throws Exception {

		
		NHLGameRunner gameRunner = new NHLGameRunner();

		RandomForest rfClassifier = new RandomForest();
		GameWinnerTrainer trainer = new GameWinnerTrainer("./data/Parsed Game Data/games.txt");

		rfClassifier.buildClassifier(GameWinnerTrainer.getInstances());
		String option = "";
		Scanner scan = new Scanner(System.in);
		boolean logging = false;
		
		do {
			switch(option) {
				case "1":
				case "Game Prediction":
					//Prompts for Date, Team 1 and Team 2
					String home = "", away = "";
					LocalDate date;
					
					//Prompt for Date
					System.out.print("Enter the date that the two teams would play (YYYY-MM-DD): ");
					try {
						date = LocalDate.parse(scan.next());
					} catch(Exception e) {
						System.out.println("\nInvalid Date Format");
						break;
					}
					//Prompts for Home Team
					System.out.print("Enter the Home Team Name: ");
					home = scan.next() + scan.nextLine();
					//Prompts for Away Team
					System.out.print("Enter the Away Team Name: ");
					away = scan.next() + scan.nextLine();
					//determines the winner
					
					double decision = rfClassifier.classifyInstance(trainer.getGameInstance(date, home, away));
					//Print out the winner
					if(decision == 0d)
						System.out.println("The Algorithm Predicted that: " + home + " would win.");
					else
						System.out.println("The Algorithm Predicted that: " + away + " would win.");
					break;
				case "2":
				case "Season Prediction":
					//request season to simulate
					System.out.print("Enter a season (YYYY-YYYY): ");
					String[] years = scan.next().split("-");
					try {
						//if valid hockey season within the information we have start the simulation
						if (	(Integer.parseInt(years[0]) == (Integer.parseInt(years[1]) - 1)) &&
								(Integer.parseInt(years[0]) > 1990) &&
								(Integer.parseInt(years[1]) <= Year.now().getValue())) {
							gameRunner.runSeasonSimulator(years[0]+years[1], logging);
						}
						else {
							throw new Exception();
						}
					} catch (Exception e) {
						System.out.println("Invalid Year Entry");
						e.printStackTrace();
					}					
					break;
				
				case "3":
				case "Game Stats":
					//gets the Ranker
					weka.filters.supervised.attribute.AttributeSelection as = new  weka.filters.supervised.attribute.AttributeSelection();
				    Ranker ranker = new Ranker();
				    InfoGainAttributeEval infoGainAttrEval = new InfoGainAttributeEval();
				    //finds attributes
				    as.setEvaluator(infoGainAttrEval);
				    as.setSearch(ranker);
				    as.setInputFormat(GameWinnerTrainer.getInstances());
				    //runs training
				    Instances trainData = Filter.useFilter(GameWinnerTrainer.getInstances(), as);
				    //evaluates correctly and incorrectly classified instances
				    Evaluation evaluation = new Evaluation(trainData);
				    //Prints stats
				    evaluation.crossValidateModel(rfClassifier, trainData, 10, new Random(2));
			        System.out.println(evaluation.toSummaryString("\nResults\n======\n", true));
			        System.out.println(evaluation.toClassDetailsString());
			        System.out.println("Results For Class -1- ");
			        System.out.println("Precision=  " + evaluation.precision(0));
			        System.out.println("Recall=  " + evaluation.recall(0));
			        System.out.println("F-measure=  " + evaluation.fMeasure(0));
			        System.out.println("Results For Class -2- ");
			        System.out.println("Precision=  " + evaluation.precision(1));
			        System.out.println("Recall=  " + evaluation.recall(1));
			        System.out.println("F-measure=  " + evaluation.fMeasure(1));
					break;
				case "4":
					logging = !logging;
			}
			
			// Prompt the user
			System.out.println();
			System.out.println("Welcome to NHL Game Predictions");
			System.out.println("Please Select an option: ");
			System.out.println("(0) Exit");
			System.out.println("(1) Game Prediction");
			System.out.println("(2) Season Prediction");
			System.out.println("(3) Game Prediction Stats");
			System.out.println("(4) Enable / Disable Logging");
			System.out.println("Logging: " + (logging?"Enabled":"Disabled"));
			System.out.print("Selection: ");
			option = scan.next() + scan.nextLine();
			System.out.println();
			
		} while(!(option.equals("exit") || option.equals("0")));
		scan.close();
	}
	
	
	/*
	 * Function: 	runSeasonSimulator
	 * Parameters: 	years, reqLog (request if logging should be displayed)
	 * Purpose:		Run a simulation on the specified years and sort the ranks of the results
	 * 				then print them out in a nicely formatted table
	 */
	public void runSeasonSimulator(String years, boolean reqLog) {
		//Create Simulator
		GASeasonSimulator ga = new GASeasonSimulator(years);
		ga.runSimulation(reqLog);
		
		// Sort Rank of all teams
		ArrayList<String> topTen = new ArrayList<String>(ga.teamOutcomes.size());
		boolean added;
		for(String key: ga.teamOutcomes.keySet()){
			added = false;
			for(int i = 0; i< topTen.size(); i++)
				if(		ga.teamWins.get(key) > ga.teamWins.get(topTen.get(i))
						|| ga.teamLosses.get(key) < ga.teamLosses.get(topTen.get(i))){
						topTen.add(i, key);
						added = true;
						break;
				}
			if(!added)
				topTen.add(key);
		}
		
		
		//Print rank of all Teams
		String format = "| %1$-4s |\t%2$-20s\t| %3$-5s | %4$-5s |\n";
		System.out.println();
		System.out.println();
		System.out.println(	" -------------------------------------------------------");
		System.out.println(	"|                  Ranking of teams                     |");
		System.out.println(	"|-------------------------------------------------------|");
		System.out.printf(format,"Rank", "Team", "Win","Loss");
		System.out.println(	"|-------------------------------------------------------|");
		for(int i  = 0; i < topTen.size(); i++) {
			String key = topTen.get(i);
			System.out.printf(format, i+1, key, ga.teamWins.get(key), ga.teamLosses.get(key));
		}
		System.out.println(	" -------------------------------------------------------");
	}

}
