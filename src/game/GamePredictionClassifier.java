package game;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;

public class GamePredictionClassifier {
	String dataFolder = "./data/";
	String[] fileTypes = {"shifts","pbp", "player", "game"};
	Date gameDate;
	String homeTeamID, awayTeamID;
	int gameID;
	ArrayList<String[]> rows = new ArrayList<String[]>();
	ArrayList<String> heads;
	
	public static void main(String[] args) throws Exception {
		GamePredictionClassifier g = new GamePredictionClassifier();
		g.getFileDataAndClean("./data/Parsed Game Data/games.txt");
		g.makeHomeWinningPrediction();
	}
	
	/*
	 * Function:	getFileDataAndClean
	 * Parameters: 	fileName
	 * Purpose:		Creates Training Data from the parsed file specified
	 */
	public ArrayList<Instance> getFileDataAndClean(String fileName) {
		BufferedReader br = null;
		ArrayList<Instance> stuff = null;
			try {
					br = new BufferedReader(new FileReader(fileName));
					stuff = GameWinnerTrainer.createTrainerList(br);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			return stuff;
	}
	
	/*
	 * Function: 	makeHomeWinningPrediction
	 * Purpose:		Runs a simulation for two teams over a period of time to see
	 * 				if the classifier will make a different decision over the specified months
	 */
	public void makeHomeWinningPrediction() throws Exception {

		RandomForest rfClassifier = new RandomForest();
		GameWinnerTrainer trainer = new GameWinnerTrainer("./data/Parsed Game Data/games.txt");
		rfClassifier.buildClassifier(GameWinnerTrainer.getInstances());
		for(int i = 1; i < 4; i++)
		{
			double decision = rfClassifier.classifyInstance(trainer.getGameInstance(LocalDate.of(2017, i, 1), "MTL","PIT"));
			if(decision == 0d)
				System.out.println("Coyotes Won");
			else
				System.out.println("Penguins Won");
		}
		
		for(int i = 8; i < 13; i++)
		{
			double decision = rfClassifier.classifyInstance(trainer.getGameInstance(LocalDate.of(2017, i, 1), "MTL","PIT"));
			if(decision == 0d)
				System.out.println("Coyotes Won");
			else
				System.out.println("Penguins Won");
		}
		System.out.println();
		System.out.println();
		System.out.println();
		System.out.println();
		
		
		weka.filters.supervised.attribute.AttributeSelection as = new  weka.filters.supervised.attribute.AttributeSelection();
	    Ranker ranker = new Ranker();

	    

	    InfoGainAttributeEval infoGainAttrEval = new InfoGainAttributeEval();
	    as.setEvaluator(infoGainAttrEval);
	    as.setSearch(ranker);
	    as.setInputFormat(GameWinnerTrainer.getInstances());
	    Instances trainData = Filter.useFilter(GameWinnerTrainer.getInstances(), as);
	    Evaluation evaluation = new Evaluation(trainData);
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
	}

	
}
