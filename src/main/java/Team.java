import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

public class Team {

	String team;
	HashSet<Player> players;	
	ArrayList<String> StatLabelsOrdered;
	HashMap<String, Boolean> isNumericStats;
	double weightMetricSum;
	
	HashMap<String, Double> numStats = new HashMap<>();	
	
	public Team(String team, HashSet<Player> players, ArrayList<String> StatLabelsOrdered, HashMap<String, Boolean> isNumericStats) {
		this.team = team;
		this.players = players;
		this.StatLabelsOrdered = StatLabelsOrdered;
		this.isNumericStats = isNumericStats;				
	}
		
	public void calculateTeamStats(String metricName) {
		setPlayerWeights(metricName);
		for(String stat: StatLabelsOrdered) {
			double teamStatVal = 0;
			if(isNumericStats.get(stat)) {
				for(Player p: this.players) {
					teamStatVal += p.playerWeight * p.numStats.get(stat);
				}
				this.numStats.put(stat, teamStatVal);
			}
		}
	}
	
	public void calculateWeightMetricSum(String metricName) {
		weightMetricSum = 0;
		for(Player p: players) {
			weightMetricSum += p.numStats.get(metricName);
		}
	}
	
	public void setPlayerWeights(String metricName) {
		calculateWeightMetricSum(metricName);
		for(Player p: players) {
			double playerMetricValue = p.numStats.get(metricName);
			p.playerWeight = playerMetricValue/this.weightMetricSum;
		}
	}

	public void printTeamStats() {
		StringBuilder output = new StringBuilder();
		
		output.append("Team: " + team + " |");
		
		for(String stat: StatLabelsOrdered) {
			if(isNumericStats.get(stat)) {
				output.append(stat + ": " + Utilities.rightpad(numStats.get(stat).toString(), 6) + " | ");
			}
		}
		System.out.println(output.toString());
	}
}
