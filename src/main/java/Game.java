import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class Game {

	Team homeTeam, visitorTeam;

	int homePoints, visitorPoints;
	String OT, season;
	Date date;

	int winner, spread, totalPoints;

	ArrayList<String> StatLabelsOrdered;
	HashMap<String, Boolean> isNumericStats;	
	HashMap<String, Double> numStats = new HashMap<>();	

	public Game(Team homeTeam, Team visitorTeam, int homePoints, int visitorPoints, String OT, String date, String season, ArrayList<String> StatLabelsOrdered,
			HashMap<String, Boolean> isNumericStats) {
		this.homeTeam = homeTeam;
		this.visitorTeam = visitorTeam;
		this.StatLabelsOrdered = StatLabelsOrdered;
		this.isNumericStats = isNumericStats;
		this.homePoints = homePoints;
		this.visitorPoints = visitorPoints;
		this.OT = OT;
		this.season = season;
		
		
		DateFormat formatter = new SimpleDateFormat("E MMM dd yyyy");
		try {
			this.date = (Date)formatter.parse(date);
		} catch (ParseException e) {
			System.out.println("ERROR: Incorrect date format.");
			e.printStackTrace();
		}

		calculateGameStats();
	}

	public void calculateGameStats() {
		for(String stat: this.StatLabelsOrdered) {
			if(isNumericStats.get(stat)) {
				double visitor = this.visitorTeam.numStats.get(stat);
				double home = this.homeTeam.numStats.get(stat);
				this.numStats.put(stat, home - visitor);
			}
		}

		spread = homePoints - visitorPoints;
		totalPoints = homePoints + visitorPoints;
		if(spread >= 0) {
			winner = 1;
		}
		else {
			winner = 0;
		}
		
	}


	public String printGame(String[] stats, String objective) {
		StringBuilder output = new StringBuilder();
		for(String stat: stats) {
			if(StatLabelsOrdered.contains(stat) && isNumericStats.get(stat)) {
				output.append(","+numStats.get(stat));
			} else {
				System.out.println("ERROR: " + stat + " is not a numeric stat.");
				System.exit(0);
			}
		}
		
		// delete first comma
		output.deleteCharAt(0);
		
		switch (objective) { 
        case "winner": 
        	output.append("," + winner); 
            break; 
        case "spread": 
        	output.append("," + spread); 
            break; 
        case "totalPoints": 
        	output.append("," + totalPoints); 
            break; 
        default: 
        	System.out.println("ERROR: Objective " + objective + " does not exist.");
        	System.exit(0);
            break; 
        } 
		
		return output.toString();
	}
	
	public void printGame(String[] stats) {
		
	}
}
