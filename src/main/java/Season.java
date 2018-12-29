import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Season {

	String season;
	HashMap<String, Team> teams = new HashMap<>();
	ArrayList<String> StatLabelsOrdered;
	HashMap<String, Boolean> isNumericStats;
	HashSet<Player> players;

	public Season(String season,HashSet<Player> players, ArrayList<String> StatLabelsOrdered, HashMap<String, Boolean> isNumericStats) {
		this.season = season;
		this.players = players;
		this.isNumericStats = isNumericStats;
		this.StatLabelsOrdered = StatLabelsOrdered;
		createTeams();
	}

	public void createTeams() {
		if(!this.StatLabelsOrdered.contains("Tm")) {
			System.out.println("ERROR: 'Tm' field cannot be found.");
			System.exit(0);
		}

		HashMap<String, HashSet<Player>> playersSplitByTeam = new HashMap<>();

		for(Player p: this.players) {
			String team = "";
			try {
				team = p.stringStats.get("Tm");
			} catch (Exception e) {
				System.out.println("Error: A player is missing 'Tm' field. ");
			}

			if(playersSplitByTeam.containsKey(team)) { // team already created
				playersSplitByTeam.get(team).add(p);
			}
			else { // add player to new team
				HashSet<Player> tmpSet = new HashSet<>();
				tmpSet.add(p);
				playersSplitByTeam.put(team, tmpSet);
			}
		}
		

		for (HashMap.Entry<String, HashSet<Player>> entry : playersSplitByTeam.entrySet()) {
		    String team = entry.getKey();
		    HashSet<Player> playersInTeam = entry.getValue();
		    
		    Team tmpTeam = new Team(team, playersInTeam, StatLabelsOrdered, isNumericStats);
		    
		    this.teams.put(team, tmpTeam);
		}

	}

	public void calculateTeamStats(String metricName) {
		for(Team t: this.teams.values()) {
			t.calculateTeamStats(metricName);
		}		
	}
}
