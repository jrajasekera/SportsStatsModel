import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Utilities {

    public static String rightpad(String text, int length) {
        return String.format("%-" + length + "." + length + "s", text);
    }

    public static String fillRight(String text, String fillChar, int length) {

        int unfilled = length - text.length();
        if (unfilled > 0) {
            StringBuilder result = new StringBuilder(text);
            for (int i = 0; i < unfilled; i++) {
                result.append(fillChar);
            }
            return result.toString();
        } else {
            text = text.substring(0, length - 1) + fillChar;
            return text;
        }
    }

    public static void printProgressMessage(String text) {
        System.out.print(fillRight(text, ".", 54));
    }

    public static void printProgressCompletion() {
        System.out.println("COMPLETED\n");
    }

    public static void printHashMap(HashMap mp) {
        Iterator it = mp.entrySet().iterator();
        while (it.hasNext()) {
            HashMap.Entry pair = (HashMap.Entry) it.next();
            System.out.println(pair.getKey() + " = " + pair.getValue());
            it.remove(); // avoids a ConcurrentModificationException
        }
    }

    public static void printPlayerStatsFromSet(Set<Player> players) {
        for (Player p : players) {
            p.printStats();
        }
    }

    public static double[] arrayListToArrayDouble(ArrayList<Double> arrayList) {

        double[] array = new double[arrayList.size()];
        for (int i = 0; i < array.length; i++) {
            array[i] = arrayList.get(i);
        }

        return array;
    }

    public static ArrayList<String> ArrayToArrayListString(String[] arr) {
        ArrayList<String> al = new ArrayList<>();

        for (String i : arr) {
            al.add(i);
        }

        return al;
    }

    public static double[] findMeanAndStdDev(ArrayList<Double> arrayList) {
        double[] meanAndStdDev = new double[2];

        double sum = 0;
        for (double i : arrayList) {
            sum += i;
        }
        double mean = sum / arrayList.size();

        double totalDev = 0;
        for (double i : arrayList) {
            totalDev += Math.pow(i - mean, 2);
        }
        double stdDev = Math.sqrt(totalDev / arrayList.size());

        meanAndStdDev[0] = mean;
        meanAndStdDev[1] = stdDev;

        return meanAndStdDev;
    }

    public static boolean isNotBlankOrNA(String text) {
        if (!text.equals("#N/A") && !text.equals("")) {
            return true;
        }
        return false;
    }
}
