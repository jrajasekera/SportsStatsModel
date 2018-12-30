import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import org.bigml.binding.AuthenticationException;
import org.bigml.binding.BigMLClient;
import org.bigml.binding.InputDataParseException;
import org.bigml.binding.LocalEnsemble;
import org.bigml.binding.PredictionMethod;
import org.json.simple.JSONObject;

public class SimulateBets {

    private static String username = "jrajasekera";
    private static String key = "f2527f23ebbf021797b83f68acd49fb84b199515";

    public static void main(String[] args) {
        System.out.println("SimulateBets Started");

        HashSet<Game> games = PrepDataPlayersToGames
                .prepGameDataForBettingSimulation();
        Iterator<Game> allGames = games.iterator();
        allGames.next();
        allGames.next();
        Game game = allGames.next();
        String homeTeam = game.homeTeam.team;
        String visitorTeam = game.visitorTeam.team;
        Date date = game.date;
        System.out.println(
                visitorTeam + " vs. " + homeTeam + " " + date.toString());
        String[] stats = { "Age", "PER", "TS%", "3PAr", "FTr", "ORB%", "DRB%",
                "TRB%", "AST%", "STL%", "BLK%", "TOV%", "USG%", "OWS", "DWS",
                "WS", "WS/48", "OBPM", "DBPM", "BPM", "VORP" };
        JSONObject gameStats = game.getJSONGameStats(stats);
        getPrediction(gameStats);
    }

    public static void getPrediction(JSONObject inputData) {
        try {
            // Getting ensemble)
            BigMLClient api = new BigMLClient(username, key, null);
            JSONObject ensemble = api
                    .getEnsemble("ensemble/5c25ac1beba31d634c0005f2");

            // Creating local ensemble
            LocalEnsemble localEnsemble = new LocalEnsemble(ensemble);

            // make prediction
            PredictionMethod combinationMethod = PredictionMethod.PLURALITY;
            boolean details = true;
            boolean useMedian = true;
            HashMap<String, Object> prediction = localEnsemble.predict(
                    inputData, combinationMethod, null, null, null, null,
                    useMedian, details);

            double confidence = (double) prediction.get("confidence");
            int outcome = Integer
                    .parseInt(prediction.get("prediction").toString());
            System.out.println(
                    "Prediction: " + outcome + "\nConfidence: " + confidence);

        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (InputDataParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
