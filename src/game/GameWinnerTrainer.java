package game;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import cleaner.GameCleaner;

public class GameWinnerTrainer {
	
	protected TeamStats homeStats = new TeamStats();
	protected TeamStats awayStats = new TeamStats();
	
	public GameWinnerTrainer(String data) {
		try {
			GameWinnerTrainer.createTrainerList(new BufferedReader(new FileReader(data)));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/*
	 * Function: 	createTrainerList
	 * Parameters:	BufferedReader, headings
	 * Purpose:		Opens an optimized file already made for training to aid in determining
	 * 				The winner of games
	 * Returns:		ArrayList full of training data
	 */
	public static ArrayList<Instance> createTrainerList(BufferedReader br) throws NumberFormatException, IOException {
		ArrayList<Instance> newData = new ArrayList<Instance>();
		int i = 0;

		String line;
		while((line = br.readLine()) != null) {
			ArrayList<String> r = new ArrayList<String>();
			for(String a: line.split("\",")) {
				for(String b: a.split("\"")[0].split(","))
					r.add(b);
				if(a.split("\"").length > 1)
					r.add(a.split("\"")[1]);
			}


			
			newData.add(new DenseInstance(GameWinnerTrainer.getSizeOfInstances())); 
			for(int j=0; j<TeamStats.fields.length; j++){
				if(i % 3 == 0 || i % 2 == 0){
					newData.get(i).setValue(GameWinnerTrainer.getInstances().attribute(TeamStats.fields[j]+1), Double.parseDouble(r.get(j)));
					newData.get(i).setValue(GameWinnerTrainer.getInstances().attribute(TeamStats.fields[j]+2), Double.parseDouble(r.get(j+TeamStats.fields.length-1)));
				}
			}
			newData.get(i).setValue(GameWinnerTrainer.getInstances().attribute("Game Outcome"), r.get(r.size()-1));
			GameWinnerTrainer.getInstances().add(newData.get(i));
			i++;
		}
		
		return newData;
	}

	/*
	 * Function:	getInstances
	 * Purpose:		Builds the  Classifier's attribute template and stores it in the instances 
	 * 				object for training
	 * Returns: 	Instances object that contains the structure of attributes
	 */
	public static Instances ins = null;
	public static Instances getInstances(){
		 if(ins != null)
			 return ins;

		 ArrayList<Attribute> all = new ArrayList<Attribute>(); 
		 String[] fields = TeamStats.fields;
		 int i = 1;
		 for(String field: fields)
			 all.add(new Attribute(field + i));
		 i++;
		 for(String field: fields)
			 all.add(new Attribute(field + i));
		 
		 // Declare a nominal attribute along with its values
		 ArrayList<String> fvNominalVal = new ArrayList<String>(2);
		 fvNominalVal.add(Integer.toString(0));
		 fvNominalVal.add(Integer.toString(1));
		 fvNominalVal.add(Integer.toString(2));
		
		 Attribute attribute3 = new Attribute("Game Outcome", fvNominalVal);
		 all.add(attribute3);

		 GameWinnerTrainer.size = all.size();
		 ins = new Instances("Rel",all, 37);
		 ins.setClass(ins.attribute("Game Outcome"));
		 return ins;
	}
	private static int size = 37;
	public static int getSizeOfInstances() {
		return size;
	}
	
	/*
	 * Function:	getGameInstance
	 * Parameters:	date, homeTeam,awayTeam
	 * Purpose:		gets all the statistics on event correlation in real time.
	 */
	
	public Instance getGameInstance(LocalDate date, String homeTeam, String awayTeam) {
		homeStats.setTeam(homeTeam);
		awayStats.setTeam(awayTeam);
		homeStats.resetRatios();
		awayStats.resetRatios();
		
		String yr;
		if( date.getMonth().getValue() > 7)
			yr = date.getYear() +""+ (date.getYear()+1);
		else
			yr = (date.getYear()-1) + "" + date.getYear();
		
		String line;
		LinkedList<ArrayList<String>> tenMostRecentEvents = new LinkedList<ArrayList<String>>();
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader("./data/nhl_pbp" + yr + ".csv"));
			//have the last ten lines
			for(int i = 0; i < 10; i++) {
				line = br.readLine();
				ArrayList<String> r = new ArrayList<String>();
				for(String a: line.split("\",")) {
					for(String b: a.split("\"")[0].split(","))
						r.add(b);
					if(a.split("\"").length > 1)
						r.add(a.split("\"")[1]);
				}
				tenMostRecentEvents.addLast(r);
			}
			
			String currentGameID = tenMostRecentEvents.get(0).get(2);
			while((line = br.readLine()) != null) {
				
				ArrayList<String> r = new ArrayList<String>();
				for(String a: line.split("\",")) {
					for(String b: a.split("\"")[0].split(","))
						r.add(b);
					if(a.split("\"").length > 1)
						r.add(a.split("\"")[1]);
				}
				if(LocalDate.parse(r.get(2)).isAfter(date))
					break;
				if(GameCleaner.targetedEvents(r.get(4))) {
					analyze(r, tenMostRecentEvents);
				}

				
				tenMostRecentEvents.removeFirst();
				tenMostRecentEvents.addLast(r);
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Instance newData = new DenseInstance(GameWinnerTrainer.getSizeOfInstances()); 
		newData.setDataset(getInstances());
		for(int j=0; j<TeamStats.fields.length; j++) {
			newData.setValue(GameWinnerTrainer.getInstances().attribute(TeamStats.fields[j]+1), homeStats.getRatio(TeamStats.fields[j]));
			newData.setValue(GameWinnerTrainer.getInstances().attribute(TeamStats.fields[j]+2), awayStats.getRatio(TeamStats.fields[j]));
		}
		return newData;

	}
	
	
	/*
	 * Function:	analyze
	 * Parameters:	row of dataset, arraylist of the ten following rows
	 * Purpose:		Determines if other events occured within the time frame and updates the good or bad events accordingly
	 */
	public void analyze(ArrayList<String> row, LinkedList<ArrayList<String>> tenEvents) {
		for(int i = tenEvents.size()-1; i >= 0; i--) {
			ArrayList<String> fields = tenEvents.get(i);
			if(fields.get(14).equals(awayStats.getTeam()) || !fields.get(14).equals(homeStats.getTeam())) {
				if(GameCleaner.withinTimeFrame((int)Float.parseFloat(row.get(7)), (int)Float.parseFloat(fields.get(7)))){
					if(fields.get(5).contains(fields.get(14)))
						updateTeam(fields.get(14),fields.get(4), row.get(4),1d);
					else
						updateTeam(fields.get(14),fields.get(4), row.get(4), -0.5d);
				}
			}
			
			if(fields.get(13).equals(awayStats.getTeam()) || !fields.get(13).equals(homeStats.getTeam())) {
				if(GameCleaner.withinTimeFrame((int)Float.parseFloat(row.get(7)), (int)Float.parseFloat(fields.get(7)))) {
					if(fields.get(5).contains(fields.get(13)))
						updateTeam(fields.get(13),fields.get(4), row.get(4),1d);
					else
						updateTeam(fields.get(13),fields.get(4), row.get(4), -0.5d);
				}
			}

		}
	}
	
	/*
	 * Function:	updateTeam
	 * Parameters:	team, event1, event2, score
	 * Purpose:		updates the specified team with the event combination with the new score
	 */
	public void updateTeam(String team, String first, String second, double score) {
		String key = first+"To"+second+"Ratio";
		if(homeStats.getTeam().equals(team))
				homeStats.updateField(key, score);
		else if( awayStats.getTeam().equals(team))
			awayStats.updateField(key, score);
	}
	
}
