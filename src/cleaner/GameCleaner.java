package cleaner;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import org.apache.hadoop.util.ToolRunner;


public class GameCleaner {
	static int GAME_WIN = 2;
	static int GAME_TIE = 1;
	static int GAME_LOSS = 0;
	String dataFolder = "/home/pseguin/Desktop/Data/";
	HashMap<String, Double> ratios = new HashMap<String, Double>();
	HashMap<String, Integer> counts = new HashMap<String, Integer>();
	HashMap<String, Double> totals = new HashMap<String, Double>();
	HashMap<String, HashMap<String, HashMap<String,Double>>> teams = new HashMap<String, HashMap<String,HashMap<String,Double>>>();
	BufferedWriter writer = null;
	
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		GameCleaner g = new GameCleaner();
		g.makePrediction();
		g.writer.close();
	}
	
	public void makePrediction() {
		
		String[] csvFiles = {dataFolder + "nhl_pbp20172018.csv"};
		BufferedReader br = null;
		String line = "";
		String csvSplitBy = ",";
		int thegameID = -1;
		ArrayList<String> heads;
		String[] fields;
		for(String file: csvFiles){
			try {
					br = new BufferedReader(new FileReader(file));
					
//					heads = new ArrayList(Arrays.asList(br.readLine().split(csvSplitBy)));
					br.readLine();
					saveAsPredictionData(br);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

		}
	}
	
	public GameCleaner() {
		//Game_Id, Team, Date, TeamRank, PlayingHome, Did_Win, 
		try {
			writer = new BufferedWriter(new FileWriter(dataFolder + "Parsed Game Data/games.txt"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void saveAsPredictionData(BufferedReader br) throws IOException {
		String line;
		LinkedList<ArrayList<String>> tenMostRecentEvents = new LinkedList<ArrayList<String>>();
		//have the last ten lines
		for(int i=0; i< 10; i++) {
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
			
			if(targetedEvents(r.get(4))) {
				analyze(r, tenMostRecentEvents);
			}

			if(currentGameID != r.get(2)) {
				currentGameID = r.get(2);
				updateHomeGameWinRatio(tenMostRecentEvents.get(0).get(14), getGameOutcome(tenMostRecentEvents.get(0).get(1)));
				//outputTeamRatios(tenMostRecentEvents.get(0).get(13),tenMostRecentEvents.get(0).get(14), tenMostRecentEvents.getLast());
				trainWithCurrentRatiosForTeams(r);
			}
			tenMostRecentEvents.removeFirst();
			tenMostRecentEvents.addLast(r);
		}
	}
	
	
	private static ArrayList<String> columns = new ArrayList<String>(Arrays.asList(
			"","Game_Id","Date","Period","Event","Description","Time_Elapsed","Seconds_Elapsed", "Strength",
			"Ev_Zone","Type","Ev_Team","Home_Zone","Away_Team","Home_Team",
			"p1_name","p1_ID","p2_name","p2_ID","p3_name","p3_ID",
			"awayPlayer1","awayPlayer1_id","awayPlayer2","awayPlayer2_id","awayPlayer3","awayPlayer3_id","awayPlayer4","awayPlayer4_id","awayPlayer5","awayPlayer5_id","awayPlayer6","awayPlayer6_id",
			"homePlayer1","homePlayer1_id","homePlayer2","homePlayer2_id","homePlayer3","homePlayer3_id","homePlayer4","homePlayer4_id","homePlayer5","homePlayer5_id","homePlayer6","homePlayer6_id",
			"Away_Players","Home_Players","Away_Score","Home_Score",
			"Away_Goalie","Away_Goalie_Id","Home_Goalie","Home_Goalie_Id",
			"xC","yC","Home_Coach","Away_Coach"
			));
	
	public static boolean targetedEvents(String event) {
		return event.equals("GOAL") || event.equals("PENL") || event.equals("FAC") || event.equals("HIT") || event.equals("SHOT");
	}
	
	
	public void analyze(ArrayList<String> row, LinkedList<ArrayList<String>> tenEvents) {
		for(int i = tenEvents.size()-1; i >= 0; i--) {
			ArrayList<String> fields = tenEvents.get(i);

			if(withinTimeFrame((int)Float.parseFloat(row.get(7)), (int)Float.parseFloat(fields.get(7)))){//fields.get(2).equals(row.get(2)) && targetedEvents(row.get(4)) && targetedEvents(fields.get(4))) {
				if(fields.get(5).contains(fields.get(14))){
					updateTeam(fields.get(14),fields.get(4), row.get(4),1d);
					updateTeam(fields.get(13),fields.get(4), row.get(4),-0.5d);
				}
				else {
					updateTeam(fields.get(14),fields.get(4), row.get(4), -0.5d);
					updateTeam(fields.get(13),fields.get(4), row.get(4), 1d);
				}
			}
		}
	}
	public void updateTeam(String team, String first, String second, double score) {
		String key = first+"To"+second+"Ratio";
		if(!teams.containsKey(team))
			initializeTeam(team);
		if(teams.get(team).get("ratios").containsKey(key)){
			teams.get(team).get("counts").put(key,teams.get(team).get("counts").get(key)+1);
			teams.get(team).get("totals").put(key,teams.get(team).get("totals").get(key)+ score);
			teams.get(team).get("ratios").put(key,teams.get(team).get("totals").get(key)/teams.get(team).get("counts").get(key));
		}
	}
	
	public void updateHomeGameWinRatio(String homeTeam, int gameOutcome){
		double score = (gameOutcome == GAME_WIN)?2d:(gameOutcome == GAME_LOSS)?1d:0.5d;
		teams.get(homeTeam).get("counts").put("HomeGameRatioWin",teams.get(homeTeam).get("counts").get("HomeGameRatioWin")+1);
		teams.get(homeTeam).get("totals").put("HomeGameRatioWin",teams.get(homeTeam).get("totals").get("HomeGameRatioWin")+ score);
		teams.get(homeTeam).get("ratios").put("HomeGameRatioWin",teams.get(homeTeam).get("totals").get("HomeGameRatioWin")/teams.get(homeTeam).get("counts").get("HomeGameRatioWin"));
	}
	
	public void initializeTeam(String team){
		teams.put(team, new HashMap<String,HashMap<String,Double>>());
		teams.get(team).put("ratios", new HashMap<String,Double>());
		teams.get(team).put("counts", new HashMap<String,Double>());
		teams.get(team).put("totals", new HashMap<String,Double>());
		
		teams.get(team).get("ratios").put("HomeGameRatioWin", 0d);
		teams.get(team).get("ratios").put("PENLToGOALRatio",0d);
		teams.get(team).get("ratios").put("FACToGOALRatio",0d);
		teams.get(team).get("ratios").put("HITToGOALRatio",0d);
		teams.get(team).get("ratios").put("SHOTToGOALRatio",0d);
		teams.get(team).get("ratios").put("PENLToFACRatio",0d);
		teams.get(team).get("ratios").put("HITToFACRatio",0d);
		teams.get(team).get("ratios").put("SHOTToFACRatio",0d);
		teams.get(team).get("ratios").put("FACToPENLRatio",0d);
		teams.get(team).get("ratios").put("HITToPENLRatio",0d);
		teams.get(team).get("ratios").put("SHOTToPENLRatio",0d);
		teams.get(team).get("ratios").put("PENLToHITRatio",0d);
		teams.get(team).get("ratios").put("FACToHITRatio",0d);
		teams.get(team).get("ratios").put("SHOTToHITRatio",0d);
		teams.get(team).get("ratios").put("PENLToSHOTRatio",0d);
		teams.get(team).get("ratios").put("FACToSHOTRatio",0d);
		teams.get(team).get("ratios").put("HITToSHOTRatio",0d);
		teams.get(team).get("ratios").put("PlayersOnIceRelationshipScore",0d);
		
		teams.get(team).get("counts").put("HomeGameRatioWin", 0d);
		teams.get(team).get("counts").put("PENLToGOALRatio",0d);
		teams.get(team).get("counts").put("FACToGOALRatio",0d);
		teams.get(team).get("counts").put("HITToGOALRatio",0d);
		teams.get(team).get("counts").put("SHOTToGOALRatio",0d);
		teams.get(team).get("counts").put("PENLToFACRatio",0d);
		teams.get(team).get("counts").put("HITToFACRatio",0d);
		teams.get(team).get("counts").put("SHOTToFACRatio",0d);
		teams.get(team).get("counts").put("FACToPENLRatio",0d);
		teams.get(team).get("counts").put("HITToPENLRatio",0d);
		teams.get(team).get("counts").put("SHOTToPENLRatio",0d);
		teams.get(team).get("counts").put("PENLToHITRatio",0d);
		teams.get(team).get("counts").put("FACToHITRatio",0d);
		teams.get(team).get("counts").put("SHOTToHITRatio",0d);
		teams.get(team).get("counts").put("PENLToSHOTRatio",0d);
		teams.get(team).get("counts").put("FACToSHOTRatio",0d);
		teams.get(team).get("counts").put("HITToSHOTRatio",0d);
		teams.get(team).get("counts").put("PlayersOnIceRelationshipScore",0d);
		
		teams.get(team).get("totals").put("HomeGameRatioWin", 0d);
		teams.get(team).get("totals").put("PENLToGOALRatio",0d);
		teams.get(team).get("totals").put("FACToGOALRatio",0d);
		teams.get(team).get("totals").put("HITToGOALRatio",0d);
		teams.get(team).get("totals").put("SHOTToGOALRatio",0d);
		teams.get(team).get("totals").put("PENLToFACRatio",0d);
		teams.get(team).get("totals").put("HITToFACRatio",0d);
		teams.get(team).get("totals").put("SHOTToFACRatio",0d);
		teams.get(team).get("totals").put("FACToPENLRatio",0d);
		teams.get(team).get("totals").put("HITToPENLRatio",0d);
		teams.get(team).get("totals").put("SHOTToPENLRatio",0d);
		teams.get(team).get("totals").put("PENLToHITRatio",0d);
		teams.get(team).get("totals").put("FACToHITRatio",0d);
		teams.get(team).get("totals").put("SHOTToHITRatio",0d);
		teams.get(team).get("totals").put("PENLToSHOTRatio",0d);
		teams.get(team).get("totals").put("FACToSHOTRatio",0d);
		teams.get(team).get("totals").put("HITToSHOTRatio",0d);
		teams.get(team).get("totals").put("PlayersOnIceRelationshipScore",0d);
	}	
	
	public void trainWithCurrentRatiosForTeams(ArrayList<String> fields){
		int outcome = getGameOutcome(fields.get(1));
		String homeTeam = fields.get(13);
		String awayTeam = fields.get(14);
		StringBuffer row1 = new StringBuffer("");
		StringBuffer row2 = new StringBuffer("");
		HashMap<String,Double> fields1 = teams.get(homeTeam).get("ratios");
		HashMap<String,Double> fields2 = teams.get(awayTeam).get("ratios");
		
		for(String key1: fields1.keySet())
			row1.append(String.format("%10.10f", fields1.get(key1)) + ",");
		for(String key2: fields2.keySet())
			row2.append(String.format("%10.10f",fields2.get(key2)) + ",");
		try {
			if(outcome == GAME_LOSS) {
				writer.write(row1.toString() + row2.toString() + 2 + "\n");
				writer.write(row2.toString() + row1.toString() + 0 + "\n");
			} else if(outcome == GAME_WIN) {
				writer.write(row1.toString() + row2.toString() + 0 + "\n");
				writer.write(row2.toString() + row1.toString() + 2 + "\n");
			} else {
				writer.write(row1.toString() + row2.toString() + 1 + "\n");
				writer.write(row2.toString() + row1.toString() + 1 + "\n");
			}
			writer.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}	
	}
	
	public int getGameOutcome(String gameID){
		int outcome = GAME_TIE;
		String line;
		GameWinnerFields.getInstance().setGameID(gameID);
		String[] args = {"/home/pseguin/Desktop/Data/nhl_pbp20082009.csv", "/home/pseguin/Desktop/Data/GameWinner"};
		try {
			ToolRunner.run(new GameWinnerFinder(), args);
			BufferedReader br = new BufferedReader(new FileReader(args[1] + "/part-r-00000"));
			String[] team1 = br.readLine().split("	");
			String[] team2 = br.readLine().split("	");
			if(Integer.parseInt(team1[1]) > Integer.parseInt(team2[1]))
				outcome = GAME_WIN;
			else if(Integer.parseInt(team2[1]) > Integer.parseInt(team1[1]))
				outcome = GAME_LOSS;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return outcome;
	}
	
	
	static public boolean withinTimeFrame(int a, int b) {
		return Math.abs(a-b) < 100;
	}

}
