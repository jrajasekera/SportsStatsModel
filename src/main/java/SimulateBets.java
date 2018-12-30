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

public class SimulateBets {

    private static String username = "jrajasekera";
    private static String key = "f2527f23ebbf021797b83f68acd49fb84b199515";

    public static void main(String[] args) {
        System.out.println("SimulateBets Started");

        String[] stats = { "Age", "PER", "TS%", "3PAr", "FTr", "ORB%", "DRB%",
                "TRB%", "AST%", "STL%", "BLK%", "TOV%", "USG%", "OWS", "DWS",
                "WS", "WS/48", "OBPM", "DBPM", "BPM", "VORP" };

        // create games for backtesting
        HashSet<Game> games = PrepDataPlayersToGames
                .prepGameDataForBettingSimulation();

        BigMLClient api = getBigMLClient();

        Iterator<Game> allGames = games.iterator();
        allGames.next();
        allGames.next();
        Game game = allGames.next();
        String homeTeam = game.homeTeam.team;
        String visitorTeam = game.visitorTeam.team;
        Date date = game.date;
        System.out.println(
                visitorTeam + " vs. " + homeTeam + " " + date.toString());

        // get stat data for specific game
        JSONObject gameStats = game.getJSONGameStats(stats);

        // predic winner
        String winnerEnsembleID = "ensemble/5c25ac1beba31d634c0005f2";
        LocalEnsemble winnerLocalEnsemble = createLocalEnsemble(
                winnerEnsembleID, api);
        int winner = predictWinner(gameStats, winnerLocalEnsemble, api);
        System.out.println("Predicted Winner: " + winner);

        // predict spread
        String spreadDeepNetID = "deepnet/5c269998eba31d634b0004d7";
        LocalDeepnet spreadLocalDeepNet = createLocalDeepNet(spreadDeepNetID,
                api);
        double spread = predictSpread(gameStats, spreadLocalDeepNet, api);
        System.out.println("Predicted Spread:" + spread);

        //predict total points
        String totalPointsEnsembleID = "ensemble/5c25b33800a1e551d8001c6e";
        LocalEnsemble totalPointsLocalEnsemble = createLocalEnsemble(
                totalPointsEnsembleID, api);
        double totalPoints = predictTotalPoints(gameStats,
                totalPointsLocalEnsemble, api);
        System.out.println("Predicted Total Points: " + totalPoints);
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
