package game;
import java.util.HashMap;

public class TeamStats {
	protected HashMap<String, Double> ratios = new HashMap<String, Double>();
	protected HashMap<String, Integer> counts = new HashMap<String, Integer>();
	protected HashMap<String, Double> totals = new HashMap<String, Double>();
	static String[] fields = {	"HomeGameRatioWin" ,"PENLToGOALRatio"
			,"FACToGOALRatio" ,"HITToGOALRatio"
			,"SHOTToGOALRatio" ,"PENLToFACRatio"
			,"HITToFACRatio" ,"SHOTToFACRatio"
			,"FACToPENLRatio" ,"HITToPENLRatio"
			,"SHOTToPENLRatio" ,"PENLToHITRatio"
			,"FACToHITRatio" ,"SHOTToHITRatio"
			,"PENLToSHOTRatio" ,"FACToSHOTRatio"
			,"HITToSHOTRatio" ,"PlayersOnIceRelationshipScore"};
	
	
	protected String team = null;
	
	//setter
	public void setTeam(String team){ this.team = team; }
	
	//getters
	public String getTeam() { return team; }
	public double getRatio(String key) { return ratios.get(key); }
	public HashMap<String, Double> getRatios() { return ratios; }
	
	/*
	 * Function: resetRatios
	 * Purpose: resets all the collection of stats for the team
	 */
	public void resetRatios() {
		team = "";
		ratios.clear();
		counts.clear();
		totals.clear();
		for(String field: fields) {
			ratios.put(field, 0d);
			counts.put(field, 0);
			totals.put(field, 0d);
		}
	}
	
	public TeamStats() {
		resetRatios();
	}
	/*
	 * Function: UpdateField
	 * Parameters: field, value
	 * Sets the event correlation field to the value specified
	 */
	public void updateField(String field, double value){
		if(!ratios.containsKey(field)) {
			ratios.put(field, 0d);
			counts.put(field, 0);
			totals.put(field, 0d);
		}
		counts.put(field, counts.get(field)+1);
		totals.put(field, totals.get(field) + value);
		ratios.put(field, totals.get(field)/counts.get(field));
	}
	
}
