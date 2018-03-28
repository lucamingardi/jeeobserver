package utilities.chart;

import java.awt.Color;
import java.util.Map;
import java.util.TreeMap;

public class ChartPlotPie {

    private final Map<String, Color> colors;

    private final Map<String, Double> values;

    public ChartPlotPie() {
        this.colors = new TreeMap<String, Color>();
        this.values = new TreeMap<String, Double>();
    }

    public void addValue(String label, double value, Color color) {
        if (Double.isNaN(value)) {
            this.values.put(label, 0d);
        } else {
            if (value < 0) {
                this.values.put(label, 0d);
            } else {
                this.values.put(label, value);
            }
        }
        this.colors.put(label, color);
    }

    public Map<String, Double> getValues() {
        return values;
    }

    public Map<String, Color> getColors() {
        return colors;
    }
}
