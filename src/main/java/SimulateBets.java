import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.bigml.binding.AuthenticationException;
import org.bigml.binding.BigMLClient;
import org.bigml.binding.LocalDeepnet;
import org.bigml.binding.LocalEnsemble;
import org.bigml.binding.PredictionMethod;
import org.json.simple.JSONObject;

import flanagan.analysis.Stat;

public class SimulateBets {

    private static String username = "jrajasekera";
    private static String key = "f2527f23ebbf021797b83f68acd49fb84b199515";

    public static void main(String[] args) {

        System.out.println("SimulateBets Started");

        Utilities.printProgressMessage("Creating Local Models for prediction");
        BigMLClient api = getBigMLClient();
        String winnerEnsembleID = "ensemble/5c25ac1beba31d634c0005f2";
        LocalEnsemble winnerLocalEnsemble = createLocalEnsemble(
                winnerEnsembleID, api);
        String spreadDeepNetID = "deepnet/5c269998eba31d634b0004d7";
        LocalDeepnet spreadLocalDeepNet = createLocalDeepNet(spreadDeepNetID,
                api);
        String totalPointsEnsembleID = "ensemble/5c25b33800a1e551d8001c6e";
        LocalEnsemble totalPointsLocalEnsemble = createLocalEnsemble(
                totalPointsEnsembleID, api);
        Utilities.printProgressCompletion();

        String[] stats = { "Age", "PER", "TS%", "3PAr", "FTr", "ORB%", "DRB%",
                "TRB%", "AST%", "STL%", "BLK%", "TOV%", "USG%", "OWS", "DWS",
                "WS", "WS/48", "OBPM", "DBPM", "BPM", "VORP" };

        // create games for backtesting
        HashSet<Game> games = PrepDataPlayersToGames
                .prepGameDataForBettingSimulation();

        Iterator<Game> allGames = games.iterator();

        int totalGames = 0;
        int correctWinner = 0;

        ArrayList<Double> spreadDiff = new ArrayList<>();
        ArrayList<Double> totalPointsDiff = new ArrayList<>();

        while (allGames.hasNext()) {
            Game game = allGames.next();
            String homeTeam = game.homeTeam.team;
            String visitorTeam = game.visitorTeam.team;
            Date date = game.date;

            // get stat data for specific game
            JSONObject gameStats = game.getJSONGameStats(stats);

            // predict winner
            int winner = predictWinner(gameStats, winnerLocalEnsemble, api);

            // predict spread
            double spread = predictSpread(gameStats, spreadLocalDeepNet, api);

            //predict total points
            double totalPoints = predictTotalPoints(gameStats,
                    totalPointsLocalEnsemble, api);

            if (homeTeam.equals("PHX") && visitorTeam.equals("LAC")
                    && game.season.equals("16/17")) {

                System.out.println("\n" + homeTeam + " vs. " + visitorTeam + " "
                        + date.toString());

                System.out.println("\n____Money_Line____");
                System.out.println("Predicted Winner: " + winner);
                System.out.println("Actual Winner: " + game.winner);
                if (game.moneyLine.homeOdds != null
                        && game.moneyLine.visitorOdds != null) {
                    System.out.println(
                            "MoneyLine Home Odds: " + game.moneyLine.homeOdds);
                    System.out.println("MoneyLine Visitor Odds: "
                            + game.moneyLine.visitorOdds);
                } else {
                    System.out.println("No Money Line available");
                }

                System.out.println("\n____Point_Spread____");
                System.out.println("Predicted Spread:" + spread);
                System.out.println("Actual Spread: " + game.spread);
                if (game.pointSpread.homeSpread != null
                        && game.pointSpread.visitorSpread != null
                        && game.pointSpread.homeOdds != null
                        && game.pointSpread.visitorOdds != null) {
                    System.out.println("Point Spread Home Spread:"
                            + game.pointSpread.homeSpread);
                    System.out.println("Point Spread Visitor Spread:"
                            + game.pointSpread.visitorSpread);
                    System.out.println("Point Spread Home Odds:"
                            + game.pointSpread.homeOdds);
                    System.out.println("Point Spread Visitor Odds:"
                            + game.pointSpread.visitorOdds);
                } else {
                    System.out.println("No Point Spread Avaliable");
                }

                System.out.println("\n____Over_Under____");
                System.out.println("Predicted Total Points: " + totalPoints);
                System.out.println("Actual Total Points: " + game.totalPoints);
                if (game.overUnder.total != null
                        && game.overUnder.homeOdds != null
                        && game.overUnder.visitorOdds != null) {
                    System.out.println(
                            "Over/Under total: " + game.overUnder.total);
                    System.out.println(
                            "Over/Under Home Odds: " + game.overUnder.homeOdds);
                    System.out.println("Over/Under Visitor Odds: "
                            + game.overUnder.visitorOdds);
                } else {
                    System.out.println("No Over Under Available");
                }

            }

            spreadDiff.add(Math.abs(game.spread - spread));
            totalPointsDiff.add(Math.abs(game.totalPoints - totalPoints));
            if (winner == game.winner) {
                correctWinner++;
            }
            totalGames++;
        }
        double correctWinPer = ((double) correctWinner) / ((double) totalGames);
        Stat spreadStats = new Stat(
                Utilities.arrayListToArrayDouble(spreadDiff));
        Stat totalPointStats = new Stat(
                Utilities.arrayListToArrayDouble(totalPointsDiff));

        System.out.println("___Winner___\nCorrect%: " + correctWinPer);
        System.out.println("___Spread___\nStd Error: " + spreadStats.mean());
        System.out.println(
                "___Total Points___\nStd Error: " + totalPointStats.mean());
    }

    public static double predictTotalPoints(JSONObject inputData,
            LocalEnsemble localEnsemble, BigMLClient api) {
        try {
            // make prediction
            PredictionMethod combinationMethod = PredictionMethod.PLURALITY;
            boolean useMedian = true;
            HashMap<String, Object> prediction = localEnsemble.predict(
                    inputData, combinationMethod, null, null, null, null,
                    useMedian, false);

            // get prediction values
            double confidence = (double) prediction.get("confidence");
            double totalPoints = (double) prediction.get("prediction");

            return totalPoints;
        } catch (Exception e) {
            System.out.println("ERROR: Could not predict total points");
            e.printStackTrace();
        }
        return -1.0;
    }

    public static int predictWinner(JSONObject inputData,
            LocalEnsemble localEnsemble, BigMLClient api) {
        try {
            // make prediction
            PredictionMethod combinationMethod = PredictionMethod.PLURALITY;
            boolean useMedian = true;
            HashMap<String, Object> prediction = localEnsemble.predict(
                    inputData, combinationMethod, null, null, null, null,
                    useMedian, false);

            // get prediction values
            double confidence = (double) prediction.get("confidence");
            int outcome = Integer
                    .parseInt(prediction.get("prediction").toString());

            return outcome;
        } catch (Exception e) {
            System.out.println("ERROR: Could not predict winner");
            e.printStackTrace();
        }
        return -1;
    }

    public static double predictSpread(JSONObject inputData,
            LocalDeepnet localDeepNet, BigMLClient api) {
        try {
            HashMap<String, Object> prediction = localDeepNet.predict(inputData,
                    null, null, false);
            double spread = (double) prediction.get("probability");
            return spread;
        } catch (Exception e) {
            System.out.println("ERROR: Could not predict spread");
            e.printStackTrace();
            System.exit(0);
        }
        return 0;
    }

    public static BigMLClient getBigMLClient() {
        try {
            BigMLClient api = new BigMLClient(username, key, null);
            return api;
        } catch (AuthenticationException e) {
            System.out
                    .println("ERROR: Could not establish connection to BigML");
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    public static LocalEnsemble createLocalEnsemble(String ensembleID,
            BigMLClient api) {
        JSONObject ensemble = api.getEnsemble(ensembleID);
        try {
            LocalEnsemble localEnsemble = new LocalEnsemble(ensemble);
            return localEnsemble;
        } catch (Exception e) {
            System.out.println("ERROR: Could not create local ensemble");
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }

    public static LocalDeepnet createLocalDeepNet(String deepNetID,
            BigMLClient api) {
        JSONObject deepnet = api.getDeepnet(deepNetID);
        try {
            LocalDeepnet localDeepnet = new LocalDeepnet(deepnet);
            return localDeepnet;
        } catch (Exception e) {
            System.out.println("ERROR: Could not create local DeepNet");
            e.printStackTrace();
            System.exit(0);
        }
        return null;
    }

}
