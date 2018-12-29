import java.awt.Color;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.statistics.HistogramType;

public class Histogram {

    public static JFreeChart createHistogram(double[] data, String title,
            String xLabel, int bins) {

        HistogramDataset dataset = new HistogramDataset();
        dataset.setType(HistogramType.RELATIVE_FREQUENCY);
        dataset.addSeries("Hist", data, bins);
        String xAxis = xLabel;
        String yAxis = "Frequency";
        String plotTitle = title;
        PlotOrientation orientation = PlotOrientation.VERTICAL;

        boolean show = false;
        boolean toolTips = false;
        boolean urls = false;
        JFreeChart chart = ChartFactory.createHistogram(plotTitle, xAxis, yAxis,
                dataset, orientation, show, toolTips, urls);

        // format chart
        chart.setBackgroundPaint(Color.WHITE);

        //        XYPlot plot = chart.getXYPlot();
        //        XYAnnotation annotation = new XYTextAnnotation("Hello World!", 30,
        //                0.18);
        //        plot.addAnnotation(annotation);

        return chart;
    }

    public static void createChart(ArrayList<Double> dataArrayList, String statName,
            String title, String fileName) {
    	double[] dataArray = Utilities.arrayListToArrayDouble(dataArrayList);
    	
        int bins = (int) (1 + 3.332 * Math.log10(dataArray.length)); //K = 1 + 3. 322 logN (Sturge’s Rule)
        String yLabel = statName;
        JFreeChart j = createHistogram(dataArray, title, yLabel, bins);
        File f = new File(fileName);
        try {
            ChartUtilities.saveChartAsPNG(f, j, 600, 800);
        } catch (IOException e) {
        	System.out.println("ERROR: Could not create graph " + title + " at " + fileName + ".");
            e.printStackTrace();
        }

    }
    
}
