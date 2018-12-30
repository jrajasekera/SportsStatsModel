import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;

import flanagan.analysis.BoxCox;
import flanagan.analysis.Normality;
import flanagan.analysis.Stat;

public class PrepDataPlayersToGames {

    public static void main(String[] args) {
        System.out.println("Driver Program Started");

        String playerData = "src/main/resources/NBAPlayerStatsCumulative.csv";
        String delim = ",";
        boolean firstRowLabels = true;
        boolean usePreviousYearStats = true;
        // read input file data to List of rows, get column labels
        Utilities.printProgressMessage("Reading CSV File");
        ArrayList<String> statLabelsOrdered = new ArrayList<>();
        ArrayList<String[]> dataByRow = readCsv(playerData, delim,
                firstRowLabels, statLabelsOrdered);
        Utilities.printProgressCompletion();

        // determine which columns are numeric
        HashMap<String, Boolean> isNumericStats = colTypes(statLabelsOrdered,
                dataByRow);

        // create all players
        Utilities.printProgressMessage("Creating Players");
        HashSet<Player> players = createPlayersFromRowString(dataByRow,
                statLabelsOrdered, isNumericStats);
        Utilities.printProgressCompletion();

        String[] seasonOrderArr = { "12/13", "13/14", "14/15", "15/16", "16/17",
                "17/18" };
        if (usePreviousYearStats) {
            // Use player stats from previous year
            Utilities.printProgressMessage("Pushing back player stats 1 year");
            usePastYearStats(players, seasonOrderArr);
            Utilities.printProgressCompletion();
        }

        // put players in teams and seasons
        Utilities.printProgressMessage("Assigning teams and seasons");
        HashMap<String, Season> seasons = assignTeamsAndSeasons(players,
                statLabelsOrdered, isNumericStats);
        Utilities.printProgressCompletion();

        // calculate team stats
        Utilities.printProgressMessage("Calculating team stats");
        String weightingMetric = "MP";
        calculateTeamStats(weightingMetric, seasons);
        Utilities.printProgressCompletion();

        // calculate probability before transforming
        Utilities.printProgressMessage("Calculating Normality of team stats");
        HashMap<String, ArrayList<String>> nonNormalStats1 = findNonNormalTeamDistros(
                seasons, statLabelsOrdered, isNumericStats);
        Utilities.printProgressCompletion();

        // Normalize non-normal stat distros with BoxCox transform
        Utilities.printProgressMessage("Transforming distributions(BoxCox)");
        transformTeamStatsBoxCox(nonNormalStats1, seasons);
        Utilities.printProgressCompletion();

        // test team stats for normality, find non-normal team stat distros in each season
        Utilities.printProgressMessage("Calculating Normality of team stats");
        HashMap<String, ArrayList<String>> nonNormalStats = findNonNormalTeamDistros(
                seasons, statLabelsOrdered, isNumericStats);
        Utilities.printProgressCompletion();

        //print non normal stats
        System.out.println("Stats unable to be normalized");
        for (String season : nonNormalStats.keySet()) {
            System.out
                    .println("_______S" + season + " Non-normal Stats_______");
            for (String stat : nonNormalStats.get(season)) {
                System.out.println(stat);
            }
        }
        System.out.println();

        Utilities.printProgressMessage("Converting stats to percentile values");
        convertStatsToPercentiles(seasons, statLabelsOrdered, isNumericStats);
        Utilities.printProgressCompletion();

        Utilities.printProgressMessage("Reading game data from CSV file");
        String gameListCsv = "src/main/resources/NBAGameListCumulative.csv";
        ArrayList<String> colLabelsGameList = new ArrayList<>();
        ArrayList<String[]> gameDataByRow = readCsv(gameListCsv, delim, true,
                colLabelsGameList);
        Utilities.printProgressCompletion();

        // remove first year games if stats are pushed back
        if (usePreviousYearStats) {
            Utilities.printProgressMessage("Removing N/A games from game Data");
            removeNonApplicableGames(gameDataByRow, colLabelsGameList,
                    seasonOrderArr[0]);
            Utilities.printProgressCompletion();
        }

        Utilities.printProgressMessage("Creating games");
        HashSet<Game> games = createGamesFromRowString(gameDataByRow,
                colLabelsGameList, seasons);
        Utilities.printProgressCompletion();

        String[] objectives = { "winner", "totalPoints", "spread" };
        for (String objective : objectives) {
            String[] printStats = { "Age", "PER", "TS%", "3PAr", "FTr", "ORB%",
                    "DRB%", "TRB%", "AST%", "STL%", "BLK%", "TOV%", "USG%",
                    "OWS", "DWS", "WS", "WS/48", "OBPM", "DBPM", "BPM",
                    "VORP" };
            String outputFile = "output/TEMPTEMPgameOutcome_" + objective;
            if (usePreviousYearStats) {
                outputFile += "_PushedBackStats.csv";
            } else {
                outputFile += "_SameYearStats.csv";
            }

            Utilities.printProgressMessage("Printing game data (Objective:"
                    + objective + ") to CSV file");
            printGameDataToCSV(games, printStats, outputFile, objective);
            Utilities.printProgressCompletion();
        }

    }

    public static HashSet<Game> prepGameDataForBettingSimulation() {
        String playerData = "src/main/resources/NBAPlayerStatsCumulative.csv";
        String delim = ",";
        boolean firstRowLabels = true;
        boolean usePreviousYearStats = true;
        // read input file data to List of rows, get column labels
        Utilities.printProgressMessage("Reading CSV File");
        ArrayList<String> statLabelsOrdered = new ArrayList<>();
        ArrayList<String[]> dataByRow = readCsv(playerData, delim,
                firstRowLabels, statLabelsOrdered);
        Utilities.printProgressCompletion();

        // determine which columns are numeric
        HashMap<String, Boolean> isNumericStats = colTypes(statLabelsOrdered,
                dataByRow);

        // create all players
        Utilities.printProgressMessage("Creating Players");
        HashSet<Player> players = createPlayersFromRowString(dataByRow,
                statLabelsOrdered, isNumericStats);
        Utilities.printProgressCompletion();

        String[] seasonOrderArr = { "12/13", "13/14", "14/15", "15/16", "16/17",
                "17/18" };
        if (usePreviousYearStats) {
            // Use player stats from previous year
            Utilities.printProgressMessage("Pushing back player stats 1 year");
            usePastYearStats(players, seasonOrderArr);
            Utilities.printProgressCompletion();
        }

        // put players in teams and seasons
        Utilities.printProgressMessage("Assigning teams and seasons");
        HashMap<String, Season> seasons = assignTeamsAndSeasons(players,
                statLabelsOrdered, isNumericStats);
        Utilities.printProgressCompletion();

        // calculate team stats
        Utilities.printProgressMessage("Calculating team stats");
        String weightingMetric = "MP";
        calculateTeamStats(weightingMetric, seasons);
        Utilities.printProgressCompletion();

        // calculate probability before transforming
        Utilities.printProgressMessage("Calculating Normality of team stats");
        HashMap<String, ArrayList<String>> nonNormalStats1 = findNonNormalTeamDistros(
                seasons, statLabelsOrdered, isNumericStats);
        Utilities.printProgressCompletion();

        // Normalize non-normal stat distros with BoxCox transform
        Utilities.printProgressMessage("Transforming distributions(BoxCox)");
        transformTeamStatsBoxCox(nonNormalStats1, seasons);
        Utilities.printProgressCompletion();

        // test team stats for normality, find non-normal team stat distros in each season
//        Utilities.printProgressMessage("Calculating Normality of team stats");
//        HashMap<String, ArrayList<String>> nonNormalStats = findNonNormalTeamDistros(
//                seasons, statLabelsOrdered, isNumericStats);
//        Utilities.printProgressCompletion();
//
//        //print non normal stats
//        System.out.println("Stats unable to be normalized");
//        for (String season : nonNormalStats.keySet()) {
//            System.out
//                    .println("_______S" + season + " Non-normal Stats_______");
//            for (String stat : nonNormalStats.get(season)) {
//                System.out.println(stat);
//            }
//        }
//        System.out.println();

        Utilities.printProgressMessage("Converting stats to percentile values");
        convertStatsToPercentiles(seasons, statLabelsOrdered, isNumericStats);
        Utilities.printProgressCompletion();

        Utilities.printProgressMessage("Reading game data from CSV file");
        String gameListCsv = "src/main/resources/NBAGameListCumulative.csv";
        ArrayList<String> colLabelsGameList = new ArrayList<>();
        ArrayList<String[]> gameDataByRow = readCsv(gameListCsv, delim, true,
                colLabelsGameList);
        Utilities.printProgressCompletion();

        // remove first year games if stats are pushed back
        if (usePreviousYearStats) {
            Utilities.printProgressMessage("Removing N/A games from game Data");
            removeNonApplicableGames(gameDataByRow, colLabelsGameList,
                    seasonOrderArr[0]);
            Utilities.printProgressCompletion();
        }

        Utilities.printProgressMessage("Creating games");
        HashSet<Game> games = createGamesFromRowString(gameDataByRow,
                colLabelsGameList, seasons);
        Utilities.printProgressCompletion();

        return games;
    }

    public static void removeNonApplicableGames(
            ArrayList<String[]> gameDataByRow,
            ArrayList<String> colLabelsGameList, String seasonToRemove) {
        ArrayList<String[]> tmp = new ArrayList<>();
        int seasonIndex = colLabelsGameList.indexOf("Season");
        for (String[] gameRow : gameDataByRow) {
            if (!gameRow[seasonIndex].equals(seasonToRemove)) {
                tmp.add(gameRow);
            }
        }
        gameDataByRow.clear();
        gameDataByRow.addAll(tmp);
    }

    public static void printGameDataToCSV(HashSet<Game> games,
            String[] printStats, String outputFile, String objective) {
        try {
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(outputFile));
            writer.write(printStats[0]);
            for (int i = 1; i < printStats.length; i++) {
                writer.write("," + printStats[i]);
            }
            writer.write("," + objective);
            writer.newLine();
            for (Game g : games) {
                writer.write(g.printGame(printStats, objective));
                writer.newLine();
            }
            writer.close();
        } catch (IOException e) {
            System.out.println("ERROR: Could not print to " + outputFile);
            e.printStackTrace();
        }
    }

    public static HashSet<Game> createGamesFromRowString(
            ArrayList<String[]> gameDataByRow,
            ArrayList<String> colLabelsGameList,
            HashMap<String, Season> seasons) {
        HashSet<Game> games = new HashSet<>();
        ArrayList<String> StatLabelsOrdered = seasons.values().iterator()
                .next().StatLabelsOrdered;
        HashMap<String, Boolean> isNumericStats = seasons.values().iterator()
                .next().isNumericStats;
        for (String[] gameRow : gameDataByRow) {
            String season = gameRow[colLabelsGameList.indexOf("Season")];
            String date = gameRow[colLabelsGameList.indexOf("Date")];
            String visitorName = gameRow[colLabelsGameList
                    .indexOf("Visitor/Neutral")];
            String homeName = gameRow[colLabelsGameList
                    .indexOf("Home/Neutral")];
            int visitorPoints = Integer
                    .valueOf(gameRow[colLabelsGameList.indexOf("V_PTS")]);
            int homePoints = Integer
                    .valueOf(gameRow[colLabelsGameList.indexOf("H_PTS")]);
            String OT = gameRow[colLabelsGameList.indexOf("OT?")];

            Team visitorTeam = seasons.get(season).teams.get(visitorName);
            Team homeTeam = seasons.get(season).teams.get(homeName);

            Game newGame = new Game(homeTeam, visitorTeam, homePoints,
                    visitorPoints, OT, date, season, StatLabelsOrdered,
                    isNumericStats);
            games.add(newGame);
        }
        return games;
    }

    public static void convertStatsToPercentiles(
            HashMap<String, Season> seasons,
            ArrayList<String> statLabelsOrdered,
            HashMap<String, Boolean> isNumericStats) {
        for (Season s : seasons.values()) {
            for (String stat : statLabelsOrdered) {
                if (isNumericStats.get(stat)) {
                    ArrayList<Double> data = new ArrayList<>();
                    for (Team t : s.teams.values()) {
                        data.add(t.numStats.get(stat));
                    }
                    double[] distroDetails = Utilities.findMeanAndStdDev(data);
                    double mean = distroDetails[0];
                    double stdDev = distroDetails[1];

                    for (Team t : s.teams.values()) {
                        double x = t.numStats.get(stat);
                        double p = Stat.gaussianCDF(mean, stdDev, x);
                        t.numStats.put(stat, p);
                    }
                }
            }
        }
    }

    public static void findOptimalTransformation(
            HashMap<String, ArrayList<String>> nonNormalStats,
            HashMap<String, Season> seasons) {
        double significanceLevel = 0.05;

        for (String season : nonNormalStats.keySet()) {
            for (String stat : nonNormalStats.get(season)) {
                // find best transformation for given stat

                // gather data
                ArrayList<Double> data = new ArrayList<>();
                for (Team t : seasons.get(season).teams.values()) {
                    data.add(t.numStats.get(stat));
                }
                double[] dataArray = Utilities.arrayListToArrayDouble(data);

                // try box cox
                BoxCox bc = new BoxCox(dataArray);
                double lambda1 = bc.lambdaOne();
                double lambda2 = bc.lambdaTwo();
                double[] dataBoxCox = new double[dataArray.length];
                for (int i = 0; i < dataBoxCox.length; i++) {
                    if (lambda1 != 0) {
                        dataBoxCox[i] = (Math.pow(dataArray[i] + lambda2,
                                lambda1) - 1.0) / lambda1;
                    } else {
                        dataBoxCox[i] = Math.log(dataArray[i] + lambda2);
                    }
                }
                double pValBoxCox = normalDistributionPVal(dataBoxCox,
                        significanceLevel);

                // try cube root
                double[] dataCubed = new double[dataArray.length];
                for (int i = 0; i < dataCubed.length; i++) {
                    dataCubed[i] = Math.cbrt(dataArray[i]);
                }
                double pValCubed = normalDistributionPVal(dataCubed,
                        significanceLevel);

                if (pValBoxCox < significanceLevel) {
                    System.out.println(stat + " (BoxCox) " + season
                            + " is Not Normal, P = " + pValBoxCox);
                    System.out.println(stat + " (Cube Root) " + season
                            + " is Not Normal, P = " + pValCubed);
                }

            }
        }

    }

    public static void createChartForNonNormalStats(
            HashMap<String, ArrayList<String>> nonNormalStats,
            HashMap<String, Season> seasons, String prefix) {
        for (String season : nonNormalStats.keySet()) {
            for (String stat : nonNormalStats.get(season)) {
                ArrayList<Double> dataArrayList = new ArrayList<>();
                for (Team t : seasons.get(season).teams.values()) {
                    dataArrayList.add(t.numStats.get(stat));
                }

                String title = "Team " + stat + " " + prefix + " Distribution S"
                        + season;
                String fileName = "charts/"
                        + title.replaceAll("/", "-").replaceAll(" ", "_")
                        + ".png";
                Histogram.createChart(dataArrayList, stat, title, fileName);
            }
        }
    }

    public static HashMap<String, ArrayList<String>> tmp() {
        HashMap<String, ArrayList<String>> nonNormalStats = new HashMap<>();

        ArrayList<String> s12_13 = new ArrayList<>();
        s12_13.add("Age");
        s12_13.add("TS%");
        s12_13.add("3PAr");
        s12_13.add("DRB%");
        s12_13.add("TRB%");
        s12_13.add("BLK%");
        s12_13.add("USG%");
        s12_13.add("3P");
        s12_13.add("3PA");
        nonNormalStats.put("12/13", s12_13);

        ArrayList<String> s13_14 = new ArrayList<>();
        s13_14.add("FTr");
        s13_14.add("TRB%");
        s13_14.add("BLK%");
        s13_14.add("USG%");
        s13_14.add("ORB");
        s13_14.add("TOV");
        nonNormalStats.put("13/14", s13_14);

        ArrayList<String> s14_15 = new ArrayList<>();
        s14_15.add("DRB%");
        s14_15.add("TRB%");
        s14_15.add("USG%");
        s14_15.add("3P");
        nonNormalStats.put("14/15", s14_15);

        ArrayList<String> s15_16 = new ArrayList<>();
        s15_16.add("MP");
        s15_16.add("DRB%");
        s15_16.add("TRB%");
        s15_16.add("USG%");
        nonNormalStats.put("15/16", s15_16);

        ArrayList<String> s16_17 = new ArrayList<>();
        s16_17.add("USG%");
        s16_17.add("3P");
        s16_17.add("3PA");
        nonNormalStats.put("16/17", s16_17);

        ArrayList<String> s17_18 = new ArrayList<>();
        s17_18.add("3PAr");
        s17_18.add("DRB%");
        s17_18.add("DWS");
        s17_18.add("2P");
        s17_18.add("2PA");
        s17_18.add("ORB");
        s17_18.add("STL");
        nonNormalStats.put("17/18", s17_18);

        return nonNormalStats;
    }

    public static void transformTeamStatsBoxCox(
            HashMap<String, ArrayList<String>> nonNormalStats,
            HashMap<String, Season> seasons) {

        for (String season : nonNormalStats.keySet()) {
            ArrayList<String> seasonNonNormalStats = nonNormalStats.get(season);
            for (String stat : seasonNonNormalStats) {

                // get values from this season and stat
                ArrayList<Double> dataArrayList = new ArrayList<Double>();
                for (Team t : seasons.get(season).teams.values()) {
                    dataArrayList.add(t.numStats.get(stat));
                }
                double[] dataArr = Utilities
                        .arrayListToArrayDouble(dataArrayList);

                // calculate constants for box cox
                BoxCox bc = new BoxCox(dataArr);
                double lambda1 = bc.lambdaOne();
                double lambda2 = bc.lambdaTwo();
                for (Team t : seasons.get(season).teams.values()) {
                    if (lambda1 != 0) {
                        t.numStats.put(stat,
                                (Math.pow(t.numStats.get(stat) + lambda2,
                                        lambda1) - 1.0) / lambda1);
                    } else {
                        t.numStats.put(stat,
                                Math.log(t.numStats.get(stat) + lambda2));
                    }
                }

            }
        }

        //		for (int i = 0; i < stats.length; i++) {
        //			// get values for stat distribution, load into dataArrayList
        //			ArrayList<Double> dataArrayList = new ArrayList<Double>();
        //
        //			// convert to array of values
        //			double[] dataArr = Utilities.arrayListToArrayDouble(dataArrayList);
        //
        //			// calculate constants for box cox
        //			BoxCox bc = new BoxCox(dataArr);
        //			double lambda1 = bc.lambdaOne();
        //			double lambda2 = bc.lambdaTwo();
        //
        ////			for (Season s : seasons) {
        ////				for (Team t : s.getTeams()) {
        ////					if (lambda1 != 0) {
        ////						t.setStatNumb(stats[i], (Math.pow(t.getStatNumb(stats[i]) + lambda2, lambda1) - 1) / lambda1);
        ////					} else {
        ////						t.setStatNumb(stats[i], Math.log(t.getStatNumb(stats[i]) + lambda2));
        ////					}
        ////
        ////				}
        ////			}
        //
        //
        //		}
    }

    public static void usePastYearStats(HashSet<Player> players,
            String[] seasonOrderArr) {
        HashSet<Player> pastYearStatsPlayers = new HashSet<>();

        HashMap<String, ArrayList<Player>> distinctPlayers = getDistinctPlayers(
                players);

        ArrayList<String> seasonOrder = Utilities
                .ArrayToArrayListString(seasonOrderArr);

        for (String playerName : distinctPlayers.keySet()) {
            ArrayList<Player> playerHistory = distinctPlayers.get(playerName);

            // create set of seasons not changed yet
            HashSet<String> unvisitedSeasons = playedSeasons(playerHistory);

            for (int i = 0; i < seasonOrder.size(); i++) {
                if (unvisitedSeasons.contains(seasonOrder.get(i)) && // if unvisited season
                        ((i + 1) < seasonOrder.size())
                        && unvisitedSeasons.contains(seasonOrder.get(i + 1))) { // AND next season exists

                    unvisitedSeasons.remove(seasonOrder.get(i));
                    String seasonBeingChanged = seasonOrder.get(i);

                    ArrayList<Player> singleYearPlayerHistory = new ArrayList<>();
                    for (Player record : playerHistory) {
                        if (record.stringStats.get("Season")
                                .equals(seasonBeingChanged)) {
                            singleYearPlayerHistory.add(record);
                        }
                    }

                    if (singleYearPlayerHistory.size() > 1) { // combine years to new player obj
                        // sum weight metric, make copies of data
                        String weightMetric = "MP";
                        double weightMetricSum = 0;
                        double[] weights = new double[singleYearPlayerHistory
                                .size()];
                        //String[][] data = new String[singleYearPlayerHistory.size()][singleYearPlayerHistory.get(0).data.length];
                        for (int j = 0; j < singleYearPlayerHistory
                                .size(); j++) {
                            //data[j] = Arrays.copyOf(singleYearPlayerHistory.get(j).data, singleYearPlayerHistory.get(j).data.length);
                            weights[j] = singleYearPlayerHistory.get(j).numStats
                                    .get(weightMetric);
                            weightMetricSum += weights[j];
                        }

                        // calculate weights
                        for (int j = 0; j < weights.length; j++) {
                            weights[j] = weights[j] / weightMetricSum;
                        }

                        ArrayList<String> StatLabelsOrdered = singleYearPlayerHistory
                                .get(0).StatLabelsOrdered;
                        HashMap<String, Boolean> isNumericStats = singleYearPlayerHistory
                                .get(0).isNumericStats;

                        String[] combinedData = new String[singleYearPlayerHistory
                                .get(0).data.length];
                        for (int j = 0; j < StatLabelsOrdered.size(); j++) {
                            String stat = StatLabelsOrdered.get(j);
                            if (isNumericStats.get(stat)) {
                                double sum = 0;
                                for (int k = 0; k < singleYearPlayerHistory
                                        .size(); k++) {
                                    sum += singleYearPlayerHistory
                                            .get(k).numStats.get(stat)
                                            * weights[k];
                                }
                                combinedData[j] = Double.toString(sum);
                            } else {
                                combinedData[j] = singleYearPlayerHistory
                                        .get(0).stringStats.get(stat);
                            }
                        }

                        combinedData[StatLabelsOrdered
                                .indexOf("Season")] = seasonOrder.get(i + 1);
                        combinedData[StatLabelsOrdered.indexOf("Age")] = Double
                                .toString(
                                        singleYearPlayerHistory.get(0).numStats
                                                .get("Age") + 1);

                        // create new combined player
                        Player oldPlayer = new Player(combinedData,
                                StatLabelsOrdered, isNumericStats);
                        ArrayList<Player> newPlayers = new ArrayList<>();
                        ArrayList<String> nextSeasonTeams = nextSeasonTeams(
                                seasonBeingChanged, seasonOrder, playerHistory);
                        for (String team : nextSeasonTeams) {
                            Player newPlayer = oldPlayer.copy();
                            newPlayer.stringStats.put("Tm", team);
                            newPlayers.add(newPlayer);
                        }
                        pastYearStatsPlayers.addAll(newPlayers);
                    } else {
                        // make copy of player
                        Player oldPlayer = singleYearPlayerHistory.remove(0);

                        // update team(s)
                        ArrayList<Player> newPlayers = new ArrayList<>();
                        ArrayList<String> nextSeasonTeams = nextSeasonTeams(
                                seasonBeingChanged, seasonOrder, playerHistory);
                        for (String team : nextSeasonTeams) {
                            Player newPlayer = oldPlayer.copy();
                            newPlayer.stringStats.put("Tm", team);
                            newPlayer.stringStats.put("Season",
                                    seasonOrder.get(i + 1));
                            newPlayer.numStats.put("Age",
                                    newPlayer.numStats.get("Age") + 1);
                            newPlayers.add(newPlayer);
                        }

                        pastYearStatsPlayers.addAll(newPlayers);
                    }
                }
            }
        }

        players.clear();
        players.addAll(pastYearStatsPlayers);
    }

    public static ArrayList<String> nextSeasonTeams(String currentSeason,
            ArrayList<String> seasonOrder, ArrayList<Player> playerHistory) {
        ArrayList<String> nextSeasonTeams = new ArrayList<>();

        String nextSeason = seasonOrder
                .get(seasonOrder.indexOf(currentSeason) + 1);
        for (Player p : playerHistory) {
            if (p.stringStats.get("Season").equals(nextSeason)) {
                nextSeasonTeams.add(p.stringStats.get("Tm"));
            }
        }

        return nextSeasonTeams;
    }

    public static HashSet<String> playedSeasons(
            ArrayList<Player> playerHistory) {
        HashSet<String> playedSeasons = new HashSet<>();
        for (Player p : playerHistory) {
            String recordSeason = "";
            try {
                recordSeason = p.stringStats.get("Season");
            } catch (Exception e) {
                System.out.println(
                        "Error: Player does not have a 'Season' field.");
                System.exit(0);
            }
            playedSeasons.add(recordSeason);

        }
        return playedSeasons;
    }

    public static HashMap<String, ArrayList<Player>> getDistinctPlayers(
            HashSet<Player> players) {
        HashMap<String, ArrayList<Player>> distinctPlayers = new HashMap<>();

        for (Player p : players) {
            String playerName = "";
            try {
                playerName = p.stringStats.get("Player");
            } catch (Exception e) {
                System.out.println(
                        "ERROR: Player does not have 'Player' field (name).");
                System.exit(0);
            }

            if (distinctPlayers.containsKey(playerName)) {
                distinctPlayers.get(playerName).add(p);
            } else {
                ArrayList<Player> newPlayer = new ArrayList<>();
                newPlayer.add(p);
                distinctPlayers.put(playerName, newPlayer);
            }
        }

        return distinctPlayers;
    }

    public static HashMap<String, ArrayList<String>> findNonNormalTeamDistros(
            HashMap<String, Season> seasons,
            ArrayList<String> statLabelsOrdered,
            HashMap<String, Boolean> isNumericStats) {

        HashMap<String, ArrayList<String>> nonNormalStats = new HashMap<>();

        boolean createHistogram = false;
        for (String season : seasons.keySet()) {
            for (String stat : statLabelsOrdered) {
                if (isNumericStats.get(stat)) {
                    boolean isNormal = isTeamStatNormal(stat,
                            seasons.get(season), createHistogram);
                    if (!isNormal) {
                        if (nonNormalStats.containsKey(season)) { // if season key exists
                            nonNormalStats.get(season).add(stat);
                        } else {
                            ArrayList<String> newList = new ArrayList<>();
                            newList.add(stat);
                            nonNormalStats.put(season, newList);
                        }
                    }
                }
            }
        }
        return nonNormalStats;
    }

    public static boolean isTeamStatNormal(String stat, Season season,
            boolean createHistogram) {
        double significanceLevel = 0.05;
        ArrayList<Double> dataList = new ArrayList<>();

        for (Team t : season.teams.values()) {
            dataList.add(t.numStats.get(stat));
        }

        if (createHistogram) {
            String title = "Team " + stat + " S" + season.season;
            String fileName = "charts/"
                    + title.replaceAll(" ", "_").replaceAll("/", "-") + ".png";
            Histogram.createChart(dataList, stat, title, fileName);
        }

        return isNormalDistribution(dataList, significanceLevel);
    }

    public static boolean isNormalDistribution(ArrayList<Double> dataList,
            double significanceLevel) {
        // convert arrayList to array
        double[] data = Utilities.arrayListToArrayDouble(dataList);

        //Create normality object
        Normality n = new Normality(data);

        // set significance level
        n.resetSignificanceLevel(significanceLevel);

        // calculate normality
        double pVal = n.shapiroWilkPvalue();
        if (pVal < n.getSignificanceLevel()) {
            return false;
        } else {
            return true;
        }
    }

    public static double normalDistributionPVal(double[] data,
            double significanceLevel) {
        //Create normality object
        Normality n = new Normality(data);

        // set significance level
        n.resetSignificanceLevel(significanceLevel);

        // calculate normality
        double pVal = n.shapiroWilkPvalue();
        return pVal;
    }

    public static void calculateTeamStats(String weightingMetric,
            HashMap<String, Season> seasons) {
        for (Season s : seasons.values()) {
            s.calculateTeamStats(weightingMetric);
        }
    }

    public static void createFreqChartFromTeamSet(Collection<Team> teams,
            String statName) {
        String title = statName.replaceAll("/", "-") + " Frequency";
        String file = "charts/" + title + ".png";

        ArrayList<Double> data = new ArrayList<>();

        for (Team t : teams) {
            data.add(t.numStats.get(statName));
        }

        Histogram.createChart(data, statName, title, file);
    }

    public static void createFreqChartFromPlayerSet(HashSet<Player> players,
            String statName) {
        String title = statName.replaceAll("/", "-") + " Frequency";
        String file = "charts/" + title + ".png";

        ArrayList<Double> data = new ArrayList<>();
        for (Player p : players) {
            data.add(p.numStats.get(statName));
        }

        Histogram.createChart(data, statName, title, file);
    }

    public static HashMap<String, Season> assignTeamsAndSeasons(
            HashSet<Player> players, ArrayList<String> colLabels,
            HashMap<String, Boolean> isNumericColMap) {
        //check that Season is a column label
        if (!colLabels.contains("Season")) {
            System.out.println("ERROR: 'Season' field cannot be found.");
            System.exit(0);
        }

        HashMap<String, Season> seasons = new HashMap<>();

        // key is season, value is a set of players
        HashMap<String, HashSet<Player>> playersSplitBySeason = new HashMap<>();
        for (Player p : players) {
            String season = "";
            try {
                season = p.stringStats.get("Season");
            } catch (Exception e) {
                System.out
                        .println("Error: A player is missing 'Season' field. ");
            }

            if (playersSplitBySeason.containsKey(season)) { // season already created
                playersSplitBySeason.get(season).add(p);
            } else { // add player to corresponding season
                HashSet<Player> tmpSet = new HashSet<>();
                tmpSet.add(p);
                playersSplitBySeason.put(season, tmpSet);
            }
        }

        for (HashMap.Entry<String, HashSet<Player>> entry : playersSplitBySeason
                .entrySet()) {
            String season = entry.getKey();
            HashSet<Player> playersInSeason = entry.getValue();

            Season tmpSeason = new Season(season, playersInSeason, colLabels,
                    isNumericColMap);

            seasons.put(season, tmpSeason);
        }

        return seasons;
    }

    public static HashSet<Player> createPlayersFromRowString(
            ArrayList<String[]> dataByRow, ArrayList<String> colLabels,
            HashMap<String, Boolean> isNumericColMap) {
        HashSet<Player> players = new HashSet<>();
        for (int i = 0; i < dataByRow.size(); i++) {
            String[] row = dataByRow.get(i);
            Player p = new Player(row, colLabels, isNumericColMap);
            players.add(p);
        }
        return players;
    }

    // determines which columns are numeric
    public static HashMap<String, Boolean> colTypes(ArrayList<String> colLabels,
            ArrayList<String[]> dataByRow) {
        HashMap<String, Boolean> isNumericColMap = new HashMap<>();
        for (String label : colLabels) {
            isNumericColMap.put(label, true);
        }

        for (String[] row : dataByRow) {
            for (int i = 0; i < colLabels.size(); i++) {
                String label = colLabels.get(i);
                String dataPoint = row[i];
                try {
                    if (!dataPoint.equals("")) {
                        Double.parseDouble(dataPoint);
                    }
                } catch (NumberFormatException e) {
                    isNumericColMap.put(label, false);
                }
            }
        }
        return isNumericColMap;
    }

    // reads csv to ArrayList of string arrays. Also gets label names.
    public static ArrayList<String[]> readCsv(String csvFile, String delim,
            boolean firstRowLabels, ArrayList<String> colLabels) {
        String line = "";
        ArrayList<String[]> data = new ArrayList<String[]>();
        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {

            // skip first line if labeled
            if (firstRowLabels) {
                String[] row = br.readLine().split(delim);
                for (String label : row) {
                    colLabels.add(label);
                }
            }

            while ((line = br.readLine()) != null) {
                String[] row = line.split(delim);
                data.add(row);
            }
        } catch (IOException e) {
            System.out.println("Error reading CSV file");
            e.printStackTrace();
        }
        return data;
    }

}
