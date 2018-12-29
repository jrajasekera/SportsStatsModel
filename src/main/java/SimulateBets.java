import java.util.Date;
import java.util.HashSet;

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

    }

//	public static void getPrediction() {
//		try {
//            // Getting ensemble
//            BigMLClient api = new BigMLClient(
//                bigml.io,
//                "jrajasekera", "f2527f23ebbf021797b83f68acd49fb84b199515",
//                null);
//            JSONObject ensemble = api.getEnsemble("ensemble/5c25ac1beba31d634c0005f2");
//            // Creating local ensemble
//            LocalEnsemble localEnsemble = new LocalEnsemble(ensemble, null, 10);
//            /*
//              This example uses
//                  public HashMap<String, Object> predict(final String inputData,
//                                                         Boolean byName,
//                                                         Integer method,
//                                                         Boolean withConfidence) throws Exception
//              to compute the prediction using plurality as combination method for an empty input.
//              inputData: string representing data, in a JSON object, to compute the
//                         prediction for (e.g. {"000003": 4.1875, "000004": 1.17})
//              byName: boolean indicating if input data uses name of fields
//              method: combination method (default is plurality)
//                0 - plurality
//                1 - confidence weighted
//                2 - probability weighted
//                3 - threshold filtered vote
//              withConfidence: adds the confidence and distribution information
//                              to the prediction
//            */
//            JSONObject inputData = {};
//            boolean byName = true;
//            int method = 0;
//            boolean withConfidence = true;
//            HashMap<Object, Object> prediction
//                = (HashMap<Object, Object>) localEnsemble.predict(inputData,
//                                                                  byName,
//                                                                  combinationMethod,
//                                                                  withConfidence);
//            System.out.println(prediction.get("prediction").toString());
//        } catch (AuthenticationException e) {
//            e.printStackTrace();
//        } catch (InputDataParseException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//	}

}
