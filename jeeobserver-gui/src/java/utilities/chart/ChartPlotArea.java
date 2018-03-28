package utilities.chart;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class ChartPlotArea {

    private final List<Color> colors;

    private final List<String> titles;

    private final Map<Date, List<Double>> values;

    private final String format;

    private final double[] minMaxValue;

    private final Date[] minMaxDate;

    public ChartPlotArea(String format, double[] minMaxValue, Date[] minMaxDate) {
        this.colors = new ArrayList<Color>();
        this.titles = new ArrayList<String>();
        this.values = new TreeMap<Date, List<Double>>();

        this.minMaxValue = minMaxValue;
        this.minMaxDate = minMaxDate;

        this.format = format;
    }

    public void addColum(String title, Color color) {
        this.titles.add(title);
        this.colors.add(color);
    }

    public void addValue(Date date, Long value) {
        this.addValue(date, new Double(value));
    }

    public void addValue(Date date, Double value) {
        if (!values.containsKey(date)) {
            values.put(date, new ArrayList<Double>());
        }

        values.get(date).add(value);
    }

    public List<Color> getColors() {
        return colors;
    }

    public List<String> getTitles() {
        return titles;
    }

    public Map<Date, List<Double>> getValues() {
        return values;
    }

    public String getFormat() {
        return format;
    }

    public double[] getMinMaxValue() {
        return minMaxValue;
    }

    public Date[] getMinMaxDate() {
        return minMaxDate;
    }
}
