package utilities.html;

import java.util.Date;
import jeeobserver.utilities.SizeUtilities;
import jeeobserver.utilities.TimeUtilities;
import utilities.ApplicationUtilities;

public class HtmlTable {

    public static final int CELL_TYPE_INTEGER = 0;

    public static final int CELL_TYPE_DECIMAL = 1;

    public static final int CELL_TYPE_TIME = 2;

    public static final int CELL_TYPE_SIZE = 3;

    public static final int CELL_TYPE_PERCENTAGE = 4;

    public static final int CELL_TYPE_TIME_PERCENTAGE = 5;

    public static final int CELL_TYPE_SIZE_PERCENTAGE = 6;

    public static final int CELL_TYPE_DATE = 7;

    public static final int CELL_TYPE_INDEX = 8;

    private int columns;

    private int[] columnsWidth;

    private int currentColumn;

    String htmlCode;

    public HtmlTable(Integer width, int[] columnsWidth) {
        this(width, columnsWidth, null, null);
    }

    public HtmlTable(int[] columnsWidth) {
        this(null, columnsWidth, null, null);
    }

    public HtmlTable(Integer width, int[] columnsWidth, String styleClass) {
        this(width, columnsWidth, styleClass, null);
    }

    public HtmlTable(Integer width, int[] columnsWidth, String styleClass, String toolTip) {

        String toolTipString = "";
        if (toolTip != null) {
            toolTipString = "title=\"" + toolTip + "\"";
        }

        String styleClassString = "";
        if (styleClass != null) {
            styleClassString = "class=\"" + styleClass + "\"";
        }

        String widthString = "";
        if (width != null) {
            widthString = "style=\"width: " + width.toString() + "%;\"";
        }


        this.htmlCode = "<table cellspacing=\"0\" cellpadding=\"0\" " + widthString + " " + styleClassString + " " + toolTipString + ">\n";


        this.columnsWidth = columnsWidth;
        this.columns = columnsWidth.length;

        this.currentColumn = 0;
    }

    public void addMinSeparatorCell() {
        this.terminateRow();
        this.addCell("", "minSeparatorCell", columns);
    }

    public void addSeparatorCell() {
        this.terminateRow();
        this.addCell("", "separatorCell", columns);
    }

    public void addCell(int type, double value, String valueStyleClass, int colspan) {

        String valueString = null;
        String unit = null;

        String styleClass = valueStyleClass;
        if (styleClass == null) {
            styleClass = "";
        }

        if (!Double.isNaN(value)) {
            if (type == CELL_TYPE_INTEGER) {
                valueString = ApplicationUtilities.integerFormatter.format(value);
            } else if (type == CELL_TYPE_DECIMAL) {
                valueString = ApplicationUtilities.decimalFormatter.format(value);
            } else if (type == CELL_TYPE_PERCENTAGE) {
                valueString = ApplicationUtilities.percentageFormatter.format(value);
            } else if (type == CELL_TYPE_TIME_PERCENTAGE) {
                valueString = ApplicationUtilities.percentageFormatter.format(value);
            } else if (type == CELL_TYPE_SIZE_PERCENTAGE) {
                valueString = ApplicationUtilities.percentageFormatter.format(value);
            } else if (type == CELL_TYPE_TIME) {
                valueString = ApplicationUtilities.decimalFormatter.format(TimeUtilities.fromNanosToValue(value));
                unit = TimeUtilities.fromNanosToUnit(value);
            } else if (type == CELL_TYPE_SIZE) {
                valueString = ApplicationUtilities.decimalFormatter.format(SizeUtilities.fromBytesToValue(value));
                unit = SizeUtilities.fromBytesToUnit(value);
            } else if (type == CELL_TYPE_INDEX) {
                valueString = ApplicationUtilities.indexFormatter.format(value);
            }

            if (unit != null) {
                valueString = valueString + " " + unit;
            }

        } else {
            valueString = "-";
        }

        this.addCell(valueString, styleClass, colspan);
    }

    public void addSamplingCell(Date dateMinimum, Date dateMaximum, int count, int colspan) {


        StringBuilder result = new StringBuilder();

        result.append("<span class=\"disabled\">");
        result.append(ApplicationUtilities.getMessage("label_DatesRange")).append(": ");
        result.append("</span>");

        result.append("<span class=\"bold\">");
        result.append(ApplicationUtilities.dateFullFormatter.format(dateMinimum));
        result.append("</span>");

        result.append("&nbsp;<span class=\"disabled\"> / </span>&nbsp;");

        //result.append("<span class=\"disabled\">";
        //result.append(ApplicationUtilities.getMessage("label_To") + ": ";
        //result.append("</span>";

        result.append("<span class=\"bold\">");
        result.append(ApplicationUtilities.dateFullFormatter.format(dateMaximum));
        result.append("</span>");

        result.append("&nbsp;&nbsp;|&nbsp;&nbsp;");
        //result.append("<br/>";

        result.append("<span class=\"disabled\">");
        result.append(ApplicationUtilities.getMessage("label_Samplings")).append(": ");
        result.append("</span>");

        result.append("<span class=\"bold\">");
        result.append(ApplicationUtilities.integerFormatter.format(count));
        result.append("</span>");

        //result.append("&nbsp;&nbsp;-&nbsp;&nbsp;";

        //result.append("<span class=\"disabled\">";
        //result.append(ApplicationUtilities.getMessage("label_Frequency") + ": ";
        //result.append("</span>";
        if (count > 0) {
            long nanos = ((dateMaximum.getTime() - dateMinimum.getTime()) * TimeUtilities.MILLIS_NANOS) / count;

            result.append(" (<span class=\"bold\">");
            result.append(ApplicationUtilities.decimalFormatter.format(TimeUtilities.fromNanosToValue(nanos)));
            result.append(" ");
            result.append(TimeUtilities.fromNanosToUnit(nanos));
            result.append("</span>)");
        }
        this.addCell(result.toString(), "", colspan);

    }

    public void addCell(String value) {
        this.addCell(value, null);
    }

    public void addCell(String value, String styleClass) {
        this.addCell(value, styleClass, null);
    }

    public void addCell(String value, String styleClass, Integer colspan) {
        this.addCell(value, styleClass, colspan, null);
    }

    public void addCell(String value, String styleClass, Integer colspan, String tooltip) {

        if (this.currentColumn % this.columns == 0) {
            this.htmlCode = this.htmlCode + "<tr>\n";
        }

        String colspanString = "";
        if (colspan != null) {
            colspanString = "colspan=\"" + colspan + "\" ";
            this.currentColumn = this.currentColumn + colspan;
        } else {
            this.currentColumn = this.currentColumn + 1;
        }

        String classString = "";
        if (styleClass != null) {
            classString = "class=\"" + styleClass + "\" ";
        }

        String widthString = "";
        if (columnsWidth[this.currentColumn - 1] != 0) {
            //Only if colspan is 1 or null
            if (colspan == null || colspan == 1) {
                widthString = "style=\"width: " + columnsWidth[this.currentColumn - 1] + "%;\"";
            }
        }

        String tooltipString = "";
        if (tooltip != null) {
            tooltipString = "title=\"" + tooltip + "\"";
        }

        this.htmlCode = this.htmlCode + "<td " + colspanString + classString + " " + widthString + " " + tooltipString + ">\n";
        this.htmlCode = this.htmlCode + value;
        this.htmlCode = this.htmlCode + "\n</td>\n";

        if (this.currentColumn % this.columns == 0) {
            if (currentColumn > 0) {
                this.htmlCode = this.htmlCode + "</tr>\n";
                this.currentColumn = 0;
            }
        }

    }

    public void terminateRow() {
        for (int i = currentColumn; i < columns - 1; i++) {
            this.addCell("");
        }
        this.currentColumn = 0;
    }

    public String getHtmlCode() {
        this.terminateRow();
        this.htmlCode = this.htmlCode + "</table>\n";
        return htmlCode;
    }

    public int getColumns() {
        return this.columns;
    }
}
