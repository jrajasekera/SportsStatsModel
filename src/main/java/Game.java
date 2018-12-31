import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.json.simple.JSONObject;

public class Game {

    Team homeTeam, visitorTeam;

    int homePoints, visitorPoints;
    String OT, season;
    Date date;

    int winner, spread, totalPoints;

    ArrayList<String> StatLabelsOrdered;
    HashMap<String, Boolean> isNumericStats;
    HashMap<String, Double> numStats = new HashMap<>();

    public Game(Team homeTeam, Team visitorTeam, int homePoints,
            int visitorPoints, String OT, String date, String season,
            ArrayList<String> StatLabelsOrdered,
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
            this.date = formatter.parse(date);
        } catch (ParseException e) {
            System.out.println("ERROR: Incorrect date format.");
            e.printStackTrace();
        }

        this.calculateGameStats();
    }

    public void calculateGameStats() {
        for (String stat : this.StatLabelsOrdered) {
            if (this.isNumericStats.get(stat)) {
                double visitor = this.visitorTeam.numStats.get(stat);
                double home = this.homeTeam.numStats.get(stat);
                this.numStats.put(stat, home - visitor);
            }
        }

        this.spread = this.homePoints - this.visitorPoints;
        this.totalPoints = this.homePoints + this.visitorPoints;
        if (this.spread >= 0) {
            this.winner = 1;
        } else {
            this.winner = 0;
        }

    }

    public String printGame(String[] stats, String objective) {
        StringBuilder output = new StringBuilder();
        for (String stat : stats) {
            if (this.StatLabelsOrdered.contains(stat)
                    && this.isNumericStats.get(stat)) {
                output.append("," + this.numStats.get(stat));
            } else {
                System.out
                        .println("ERROR: " + stat + " is not a numeric stat.");
                System.exit(0);
            }
        }

        // delete first comma
        output.deleteCharAt(0);

        switch (objective) {
            case "winner":
                output.append("," + this.winner);
                break;
            case "spread":
                output.append("," + this.spread);
                break;
            case "totalPoints":
                output.append("," + this.totalPoints);
                break;
            default:
                System.out.println(
                        "ERROR: Objective " + objective + " does not exist.");
                System.exit(0);
                break;
        }

        return output.toString();
    }

    public JSONObject getJSONGameStats(String[] stats) {
        HashMap<String, Double> gameStats = new HashMap<>();
        for (String stat : stats) {
            if (this.StatLabelsOrdered.contains(stat)
                    && this.isNumericStats.get(stat)) {
                gameStats.put(stat, this.numStats.get(stat));
            } else {
                System.out
                        .println("ERROR: " + stat + " is not a numeric stat.");
                System.exit(0);
            }
        }

        JSONObject json = new JSONObject(gameStats);
        return json;
    }

    MoneyLine moneyLine = new MoneyLine();

    public class MoneyLine {
        Double homeOdds;
        Double visitorOdds;
    }

    PointSpread pointSpread = new PointSpread();

    public class PointSpread {
        Double visitorSpread;
        Double homeSpread;
        Double visitorOdds;
        Double homeOdds;
    }

    OverUnder overUnder = new OverUnder();

    public class OverUnder {
        Double total;
        Double visitorOdds;
        Double homeOdds;
    }

}
