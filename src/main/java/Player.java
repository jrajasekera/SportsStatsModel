import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class Player {

	HashMap<String, Double> numStats = new HashMap<>();
	HashMap<String, String> stringStats = new HashMap<>();	
	
	ArrayList<String> StatLabelsOrdered;
	HashMap<String, Boolean> isNumericStats;
	String[] data;
	
	double playerWeight;

	public Player(String[] stats, ArrayList<String> colLabels, HashMap<String, Boolean> isNumericColMap) {
		this.StatLabelsOrdered = colLabels;
		this.isNumericStats = isNumericColMap;
		this.data = stats;
		
		assignStatsToPlayer();
		
		
	}
	
	public Player copy() {
		String[] stats = Arrays.copyOf(this.data, this.data.length);		
		ArrayList<String> colLabels = this.StatLabelsOrdered;
		HashMap<String, Boolean> isNumericColMap = this.isNumericStats;
		Player p = new Player(stats, colLabels, isNumericColMap);		
		return p;
	}
	
	public void assignStatsToPlayer() {
		for(int i = 0; i < this.StatLabelsOrdered.size(); i++) {
			String label = this.StatLabelsOrdered.get(i);
			String dataPoint = this.data[i];
			
			if(this.isNumericStats.get(label)) {
				if(!dataPoint.equals("")) {
					this.numStats.put(label, Double.parseDouble(dataPoint));
				}
				else {
					this.numStats.put(label, 0.0);
				}
			}
			else {
				if(dataPoint.equals("")) {
					dataPoint = "N/A";
				}
				this.stringStats.put(label, dataPoint);
			}
		}
	}
	
	public void printStats() {
		StringBuilder output = new StringBuilder(); 
		String tmp = "";
		for(String label: this.StatLabelsOrdered) {
			if(this.isNumericStats.get(label)) {
				tmp = Utilities.rightpad(this.numStats.get(label).toString(), 6);
			}
			else {
				tmp = Utilities.rightpad(this.stringStats.get(label), 20);
			}
			output.append(label + ": " + tmp + " | ");
			
		}
		System.out.println(output.toString());
	}
	
}
