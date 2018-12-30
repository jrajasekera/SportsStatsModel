import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

import org.bigml.binding.AuthenticationException;
import org.bigml.binding.BigMLClient;
import org.bigml.binding.InputDataParseException;
import org.bigml.binding.LocalEnsemble;
import org.json.simple.JSONObject;

public class SimulateBets {
    public static void main(String[] args) {
        System.out.println("SimulateBets Started");

        HashSet<Game> games = PrepDataPlayersToGames
                .prepGameDataForBettingSimulation();

        Game game = games.iterator().next();
        String homeTeam = game.homeTeam.team;
        String visitorTeam = game.visitorTeam.team;
        Date date = game.date;
        System.out.println(
                visitorTeam + " vs. " + homeTeam + " " + date.toString());
        String[] stats = { "Age", "PER", "TS%", "3PAr", "FTr", "ORB%", "DRB%",
                "TRB%", "AST%", "STL%", "BLK%", "TOV%", "USG%", "OWS", "DWS",
                "WS", "WS/48", "OBPM", "DBPM", "BPM", "VORP" };
        JSONObject gameStats = game.getJSONGameStats(stats);
        System.out.println(gameStats.toJSONString());
        getPrediction(gameStats);
    }

    public static void getPrediction(JSONObject inputData) {
        try {
            // Getting ensemble)
            String username = "jrajasekera";
            String key = "f2527f23ebbf021797b83f68acd49fb84b199515";
            BigMLClient api = new BigMLClient(username, key, null);
            JSONObject ensemble = api
                    .getEnsemble("ensemble/5c25ac1beba31d634c0005f2");
            // Creating local ensemble
            LocalEnsemble localEnsemble = new LocalEnsemble(ensemble);

            /*
             * This example uses public HashMap<String, Object> predict(final
             * String inputData, Boolean byName, Integer method, Boolean
             * withConfidence) throws Exception to compute the prediction using
             * plurality as combination method for an empty input. inputData:
             * string representing data, in a JSON object, to compute the
             * prediction for (e.g. {"000003": 4.1875, "000004": 1.17}) byName:
             * boolean indicating if input data uses name of fields method:
             * combination method (default is plurality) 0 - plurality 1 -
             * confidence weighted 2 - probability weighted 3 - threshold
             * filtered vote withConfidence: adds the confidence and
             * distribution information to the prediction
             */

            boolean byName = true;
            int method = 0;
            boolean withConfidence = true;
            HashMap<String, Object> prediction = localEnsemble.predict(
                    inputData, null, null, null, null, null, true, null);
            double confidence = (double) prediction.get("confidence");
            int outcome = Integer
                    .parseInt(prediction.get("prediction").toString());
            System.out.println(
                    "Prediction: " + outcome + ", Confidence: " + confidence);
        } catch (AuthenticationException e) {
            e.printStackTrace();
        } catch (InputDataParseException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
