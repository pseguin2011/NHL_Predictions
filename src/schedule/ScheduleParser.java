package schedule;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import org.json.*;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ScheduleParser {
	//URL for schedule and team
	String schedulePage = "https://statsapi.web.nhl.com/api/v1/schedule?season=";
	String teamsPage = "https://statsapi.web.nhl.com/api/v1/teams?season=";
	//Placeholder
	String year = "20162017";
	int numberOfGames = 1200;
	//DataSets
	ArrayList<ArrayList<Integer>> games;
	HashMap<Integer, String> teams;
	
	
	public ScheduleParser(String year) {
		this.year = year;
		importScheduleFromWeb();
		importTeamsFromWeb();
	}
	
	public static void main(String[] args) {
		ScheduleParser p = new ScheduleParser("20172018");
		System.out.println(p.getPreviousSeasonOverallRatios("20172018").toString());
	}
	
	/*
	 * Function:	getNumberOfGames
	 * Purpose:		returns the number of games found in the season
	 */
	public int getNumberOfGames() { return numberOfGames; }

	/*
	 * Function: 	getTeamsList
	 * Purpose:		getter for the teams ArrayList
	 */
	public HashMap<Integer, String> getTeamsList() { return teams; }
	
	/*
	 * Function: 	getGameSchedule
	 * Purpose:		getter for the games ArrayList
	 */
	public ArrayList<ArrayList<Integer>> getGameSchedule() { return games; }
	
	
	/*
	 * Function:	importScheduleFromWeb
	 * Purpose:		requests the schedule of all the games from the NHL API
	 * and saves them to the games ArrayList
	 */
	public void importScheduleFromWeb() {
		URL site;
		try {
			//request
			site = new URL(schedulePage + year);
	        URLConnection yc = site.openConnection();
	        yc.setConnectTimeout(40000); 
	        
	        BufferedReader in = new BufferedReader(
	                                new InputStreamReader(
	                                yc.getInputStream()));
	        
	        //response
	        JsonParser reader = new JsonParser();
	        JsonElement element = reader.parse(in);
	        
	        JsonObject obj = element.getAsJsonObject();
	        
	        games = new ArrayList<ArrayList<Integer>>();
			//gets the page as json
	        JSONObject json = new JSONObject(obj.toString());
			//finds the number of games
	        numberOfGames = json.getInt("totalGames");
			//loops through all the dates that games occur
	        for(int i = 0; i < json.getJSONArray("dates").length(); i++){
	        	//gets all games on the date in question
	        	JSONArray dateGames = json.getJSONArray("dates").getJSONObject(i).getJSONArray("games");
	        	for(int j = 0; j< dateGames.length(); j++){
	        		ArrayList<Integer> game = new ArrayList<Integer>();
	        		game.add(dateGames.getJSONObject(j).getJSONObject("teams").getJSONObject("away").getJSONObject("team").getInt("id"));
	        		game.add(dateGames.getJSONObject(j).getJSONObject("teams").getJSONObject("home").getJSONObject("team").getInt("id"));
	        		games.add(game);
	        	}
			}
			in.close();
		} catch (MalformedURLException e) {
			System.out.println("The URL is no longer valid");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			System.out.println("The JSON requested has changed");
			e.printStackTrace();
		}
	}
	
	/*
	 * Function:	importTeamsFromWeb
	 * Purpose:		requests the team list of all the games from the NHL API
	 * and saves them to the teams HashMap
	 */
	public void importTeamsFromWeb() {
		URL site;
		try {
			//request
			site = new URL(teamsPage + year);
	        URLConnection yc = site.openConnection();
	        BufferedReader in = new BufferedReader(
	                                new InputStreamReader(
	                                yc.getInputStream()));
	        //response
	        String inputLine;
	        String content = "";
	        while ((inputLine = in.readLine()) != null) {
	        	content += inputLine;
			}
	        
	        teams = new HashMap<Integer, String>();
	        //gets the page content as JSON
			JSONObject json = new JSONObject(content);
			//finds all teams
			for(int i = 0; i < json.getJSONArray("teams").length(); i++){
	        	JSONArray theteams = json.getJSONArray("teams");
	        		teams.put(
	        				theteams.getJSONObject(i).getInt("id"),
	        				theteams.getJSONObject(i).getString("name"));
			}
			in.close();
		} catch (MalformedURLException e) {
			System.out.println("URL is no longer valid for the Teams");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			System.out.println("The JSON has changed for the team request");
			e.printStackTrace();
		}
	}
	
	
	/*
	 * Function:	getPreviousSeasonOverallRatios
	 * Parameters:	Season
	 * Purpose:		get all the team information for the year before the season requested
	 * Returns:		A HashMap with the teams and their win-loss ratios
	 * 
	 */
	public HashMap<String, Double> getPreviousSeasonOverallRatios(String season) {
		if(teams == null) {
			getTeamsList();
		}
		//DataSets
		HashMap<String,Double> totals = new HashMap<String,Double>();
		HashMap<String,Integer> totalCounts = new HashMap<String,Integer>();
		HashMap<String, Double> teamRatiosOverall = new HashMap<String, Double>();

		String line;
		BufferedReader br;
		try {
			//for every game in the previous year
			br = new BufferedReader(new FileReader("./data/Games/" + (Integer.parseInt(season.substring(0, 4))-1) + 
							"-" + (Integer.parseInt(season.substring(4,8)) -1) + "-Games.csv"));
			//skip the first one
			br.readLine();
			while((line = br.readLine()) != null) {
				//start at 0 wins 0 losses
				ArrayList<Double> results = new ArrayList<Double>();
				results.add(0d);
				results.add(0d);
				//find scores and teams involved
			    int awayScore = Integer.parseInt(line.split(",")[5]);
			    int homeScore = Integer.parseInt(line.split(",")[12]);
			    String awayTeam = line.split(",")[17];
			    String homeTeam = line.split(",")[10];
			    
			    //if we need to add the away team to the HashMap for the first time
			    if(!totals.containsKey(awayTeam)) {
			    	totals.put(awayTeam, 0d);
			    	totalCounts.put(awayTeam, 0);
			    	teamRatiosOverall.put(awayTeam, 0d);
			    }

			    //if we need to add the home team to the HashMap for the first time
			    if(!totals.containsKey(homeTeam)) {
			    	totals.put(homeTeam, 0d);
			    	totalCounts.put(homeTeam, 0);
			    	teamRatiosOverall.put(homeTeam, 0d);			    	
			    }
			    
			    //if the home team won
			    if(homeScore > awayScore)
			    	totals.put(homeTeam, totals.get(homeTeam)+1d);
			    //if the away team won
			    else if(awayScore > homeScore)
			    	totals.put(awayTeam, totals.get(awayTeam)+1d);
			    
			    //add to the total counts
		    	totalCounts.put(awayTeam, totalCounts.get(awayTeam) + 1);
		    	totalCounts.put(homeTeam, totalCounts.get(homeTeam) + 1);
			}
			br.close();
			
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//save all team win-loss ratios
		for(String key: totals.keySet())
			teamRatiosOverall.put(key, totals.get(key)/totalCounts.get(key));
		
		return teamRatiosOverall;

	}
}
