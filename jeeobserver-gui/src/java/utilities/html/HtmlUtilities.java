package utilities.html;

import java.awt.Color;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import jeeobserver.server.Condition;
import jeeobserver.server.Condition.BooleanCondition;
import jeeobserver.server.Condition.NumberCondition;
import jeeobserver.server.Condition.SizeCondition;
import jeeobserver.server.Condition.TextCondition;
import jeeobserver.server.Condition.TimePeriodCondition;
import jeeobserver.server.TimePeriod;
import jeeobserver.server.TimeSchedule;
import utilities.ApplicationUtilities;
import utilities.chart.ChartPlotArea;
import utilities.chart.ChartPlotPie;

public class HtmlUtilities {

    private static final DecimalFormat percentageIntegerFormat = new DecimalFormat("0%");

    private static final DecimalFormat percentageDecimalFormat = new DecimalFormat("0.0%");

    public static String getCheckBoxHtml(String label, String targetId, String value, String styleClass, Color backgroudColor, boolean checked, boolean enabled) {
        return getCheckBoxHtml(label, targetId, value, styleClass, backgroudColor, checked, null, null, null, enabled);
    }

    public static String getLogIndexImage(double logIndex, boolean goodParameter) {

        if (Double.isNaN(logIndex)) {
            return "";
        }

        String stylesClass = null;

        //Good parameter
        if (goodParameter) {
            if (logIndex > 0.1d) {
                stylesClass = "detailValueArrowGreenUp";
            } else if (Double.isNaN(logIndex) || (logIndex >= -0.1d && logIndex <= 0.1d)) {
                stylesClass = "detailValueArrowGrayRight";
            } else if (logIndex >= -0.2d && logIndex < -0.1d) {
                stylesClass = "detailValueArrowOrangeDown";
            } else if (logIndex < -0.2d) {
                stylesClass = "detailValueArrowRedDown";
            }
        } else {
            if (logIndex < -0.1d) {
                stylesClass = "detailValueArrowGreenDown";
            } else if (Double.isNaN(logIndex) || (logIndex >= -0.1d && logIndex <= 0.1d)) {
                stylesClass = "detailValueArrowGrayRight";
            } else if (logIndex > 0.1d && logIndex <= 0.2d) {
                stylesClass = "detailValueArrowOrangeUp";
            } else if (logIndex > 0.2d) {
                stylesClass = "detailValueArrowRedUp";
            }
        }

        return "<div class=\"detailValueArrow " + stylesClass + "\"></div>";

    }

    public static String getCheckBoxHtml(String label, String targetId, String value, String styleClass, Color backgroudColor, boolean checked, String toolTip) {
        return getCheckBoxHtml(label, targetId, value, styleClass, backgroudColor, checked, null, null, toolTip, true);
    }

    public static String getLogScaleCombo(boolean logScale) {
        HtmlTable htmlTable = new HtmlTable(null, new int[]{0}, "comboOptions floatRight", null);

        //htmlTable.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_LogScale"), "#selectedLogScale", String.valueOf(!logScale), "selectLogScale", null, logScale, true), "comboOptionLast");

        htmlTable.addCell(HtmlUtilities.getCheckBoxHtml("<div class=\"logScaleCombo\">&nbsp;</div>", "#selectedLogScale", String.valueOf(!logScale), "selectLogScale", null, logScale, true), "comboOptionLast");

        return htmlTable.getHtmlCode();
    }

    public static String getTrendLinesCombo(boolean trendLines) {
        HtmlTable htmlTable = new HtmlTable(null, new int[]{0}, "comboOptions floatRight", null);

        htmlTable.addCell(HtmlUtilities.getCheckBoxHtml("<div class=\"trendLinesCombo\">&nbsp;</div>", "#selectedTrendLines", String.valueOf(!trendLines), "selectTrendLines", null, trendLines, true), "comboOptionLast floatRight");

        return htmlTable.getHtmlCode();
    }

    public static String getChartOptions(int currentPeriod, int minPeriod, int longTermSamplingPeriod, boolean logScale, boolean trendLines) {
        HtmlTable htmlTableChartOptions = new HtmlTable(new int[]{5, 5, 90});

        //Log scale check box
        htmlTableChartOptions.addCell(HtmlUtilities.getLogScaleCombo(logScale), "top");

        htmlTableChartOptions.addCell(HtmlUtilities.getTrendLinesCombo(trendLines), "top");

        //Sampling period check boxes
        htmlTableChartOptions.addCell(HtmlUtilities.getSamplingPeriodCombo(currentPeriod, minPeriod, longTermSamplingPeriod), "top");


        return htmlTableChartOptions.getHtmlCode();
    }

    public static String getSamplingPeriodCombo(int currentPeriod, int minPeriod, int longTermSamplingPeriod) {

        String toolTip = ApplicationUtilities.getMessage("label_SelectChartSamplingPeriod");

        if (longTermSamplingPeriod != minPeriod) {
            toolTip = ApplicationUtilities.getMessage("label_SelectChartSamplingPeriodLongTermWarning");
        }

        HtmlTable htmlTablePeriod = new HtmlTable(null, new int[]{0, 0, 0, 0, 0, 0}, "comboOptions floatRight", toolTip);

        htmlTablePeriod.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_Month"), "#selectedSamplingPeriod", String.valueOf(TimePeriod.UNIT_MONTH), "selectSamplingPeriod" + (TimePeriod.UNIT_MONTH >= longTermSamplingPeriod && longTermSamplingPeriod != minPeriod ? " bold" : ""), null, currentPeriod == TimePeriod.UNIT_MONTH, TimePeriod.UNIT_MONTH >= minPeriod));
        htmlTablePeriod.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_Week"), "#selectedSamplingPeriod", String.valueOf(TimePeriod.UNIT_WEEK), "selectSamplingPeriod" + (TimePeriod.UNIT_WEEK >= longTermSamplingPeriod && longTermSamplingPeriod != minPeriod ? " bold" : ""), null, currentPeriod == TimePeriod.UNIT_WEEK, TimePeriod.UNIT_WEEK >= minPeriod));
        htmlTablePeriod.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_Day"), "#selectedSamplingPeriod", String.valueOf(TimePeriod.UNIT_DAY), "selectSamplingPeriod" + (TimePeriod.UNIT_DAY >= longTermSamplingPeriod && longTermSamplingPeriod != minPeriod ? " bold" : ""), null, currentPeriod == TimePeriod.UNIT_DAY, TimePeriod.UNIT_DAY >= minPeriod));
        htmlTablePeriod.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_Hour"), "#selectedSamplingPeriod", String.valueOf(TimePeriod.UNIT_HOUR), "selectSamplingPeriod" + (TimePeriod.UNIT_HOUR >= longTermSamplingPeriod && longTermSamplingPeriod != minPeriod ? " bold" : ""), null, currentPeriod == TimePeriod.UNIT_HOUR, TimePeriod.UNIT_HOUR >= minPeriod));
        htmlTablePeriod.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_Minute"), "#selectedSamplingPeriod", String.valueOf(TimePeriod.UNIT_MINUTE), "selectSamplingPeriod" + (TimePeriod.UNIT_MINUTE >= longTermSamplingPeriod && longTermSamplingPeriod != minPeriod ? " bold" : ""), null, currentPeriod == TimePeriod.UNIT_MINUTE, TimePeriod.UNIT_MINUTE >= minPeriod));
        htmlTablePeriod.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_Second"), "#selectedSamplingPeriod", String.valueOf(TimePeriod.UNIT_SECOND), "selectSamplingPeriod" + (TimePeriod.UNIT_SECOND >= longTermSamplingPeriod && longTermSamplingPeriod != minPeriod ? " bold" : ""), null, currentPeriod == TimePeriod.UNIT_SECOND, TimePeriod.UNIT_SECOND >= minPeriod), "comboOptionLast");

        return htmlTablePeriod.getHtmlCode();
    }

    public static String getCombo(String values[][], String targetId, String currentValue) {
        HtmlTable htmlTablePeriod = new HtmlTable(null, new int[]{0, 0, 0, 0, 0, 0}, "comboOptions floatRight", null);

        int count = 0;
        for (String value[] : values) {
            boolean checked = false;
            if (value[0].equals(currentValue)) {
                checked = true;
            }

            String styleClass = null;
            if (count == (values.length - 1)) {
                styleClass = "comboOptionLast";
            }

            htmlTablePeriod.addCell(HtmlUtilities.getCheckBoxHtml(value[1], targetId, value[0], "selectSamplingPeriod", null, checked, true), styleClass);

            count = count + 1;
        }

        return htmlTablePeriod.getHtmlCode();
    }

    public static String getCheckBoxHtml(String label, String targetId, String value, String styleClassSelector, Color backgroudColor, boolean checked, String checkBoxStyleClass, String checkBoxLabelStyleClass, String toolTip, boolean enabled) {

        String checkedStyleClass = "";

        if (checked) {
            checkedStyleClass = "checkBoxSelected";
        }

        String style = "";
        if (backgroudColor != null) {
            style = style + "background-color: " + String.format("#%06X", (0xFFFFFF & backgroudColor.getRGB())) + ";";
        }

        if (label == null) {
            style = style + "margin-right: 0px;";
        }

        if (checkBoxStyleClass == null) {
            checkBoxStyleClass = "";
        }

        if (checkBoxLabelStyleClass == null) {
            checkBoxLabelStyleClass = "";
        }

        String toolTipString = "";
        if (toolTip != null) {
            toolTipString = "title=\"" + toolTip + "\"";
        }

        String onClickString = "";
        if (targetId != null && enabled) {
            onClickString = "onclick=\"$('" + targetId + "').val('" + value + "')\"";
        }

        if (!enabled) {
            styleClassSelector = "disabled";
        }


        StringBuilder result = new StringBuilder();

        if (label != null) {
            result.append("<table cellspacing=\"0\" cellpadding=\"0\">\n");
            result.append("<tr>");

            result.append("<td>");
        }
        result.append("<div class=\"checkBox ").append(styleClassSelector).append(" ").append(checkBoxStyleClass).append(" ").append(checkedStyleClass).append("\" style=\"").append(style).append("\"").append(toolTipString).append(" ").append(onClickString).append(">&#160;</div>");

        if (label != null) {
            result.append("</td>");

            result.append("<td>");
            result.append("<div class=\"checkBoxLabel ").append(styleClassSelector).append(" ").append(checkBoxLabelStyleClass).append("\" style=\"\" ").append(onClickString).append(">").append(label).append("</div>");
            result.append("</td>");

            result.append("</tr>");
            result.append("</table>");
        }

        return result.toString();
    }

    public static String getCheckLinkHtml(String id, String label, String targetId, String value, String styleClassSelector, String toolTip) {

        String toolTipString = "";
        if (toolTip != null) {
            toolTipString = "title=\"" + toolTip + "\"";
        }

        String idString = "";
        if (id != null) {
            idString = "id=\"" + id + "\"";
        }

        return "<a href=\"#\" " + idString + " class=\"" + styleClassSelector + "\" onclick=\"$('" + targetId + "').val('" + value + "')\" " + toolTipString + ">" + label + "</a>";
    }
    
    
    public static String getLinkHtml(String label, String href, String styleClass) {
        return "<a href=\"" + href + "\" class=\"" + styleClass + "\">" + label + "</a>";
    }

    public static String getMinimalProgressBarHtml(double value) {

        StringBuilder result = new StringBuilder();

        if (!Double.isNaN(value)) {
            int width = 100;

            int height = 6;

            //result.append("<div class=\"progressBarWrap\" style=\"font-size: 1px; float: left; margin-left: -5px; margin-top: -4px; width: ").append(width).append("px; height: ").append(height).append("px;\" title=").append(percentageIntegerFormat.format(value)).append(">\n");
            result.append("<div class=\"progressBarWrap\" style=\"font-size: 1px; float: left; margin: 0px; width: ").append(width).append("%; height: ").append(height).append("px;\" title=").append(percentageIntegerFormat.format(value)).append(">\n");
            result.append("<div class=\"progressBarValue\" style=\"font-size: 1px; width: ").append(percentageIntegerFormat.format(value)).append("; height: ").append(height).append("px;\">&nbsp;</div>\n");
            result.append("</div>\n");
        }
        return result.toString();
    }

    public static String getProgressBarHtml(double value) {

        StringBuilder result = new StringBuilder();

        result.append("<div class=\"progressBarWrap\" style=\"width: 120px; \">\n");
        result.append("<span class=\"progressBarText\">");
        result.append(percentageDecimalFormat.format(value));
        result.append("</span>\n");
        result.append("<div class=\"progressBarValue\" style=\"width: ").append(percentageIntegerFormat.format(value)).append("\">&nbsp;</div>\n");
        result.append("</div>\n");

        return result.toString();
    }

    public static String getImageHtml(String path, int width, int height) {
        StringBuilder result = new StringBuilder();

        result.append("<img src=\"").append(path).append("\" style=\"vertical-align: middle; width: ").append(width).append("; height: ").append(height).append(";\"></img>");

        return result.toString();
    }

    public static String getUlHtml(Collection<String> items) {
        StringBuilder result = new StringBuilder();
        result.append("<ul>");

        for (String item : items) {
            result.append("<li>").append(item).append("</li>");
        }

        result.append("</ul>");

        return result.toString();
    }

    public static String getInputTextHtml(String id, String value, boolean readonly, String styleClass, int size, int maxlength) {
        StringBuilder result = new StringBuilder();

        String readonlyString = "";
        if (readonly) {
            readonlyString = "readonly=\"true\"";
        }

        String styleClassString = "";
        if (styleClass != null) {
            styleClassString = "class=\"" + styleClass + "\"";
        }

        result.append("<input type=\"text\" id=\"").append(id).append("\" name=\"").append(id).append("\" value=\"").append(value).append("\" size=\"").append(size).append("\" maxlength=\"").append(maxlength).append("\" ").append(readonlyString).append(" ").append(styleClassString).append("></input>");

        return result.toString();
    }

    public static String getInputTextareaHtml(String id, String value, boolean readonly, String styleClass, int height, String htmlRenderer) {
        StringBuilder result = new StringBuilder();



        String readonlyString = "";
        if (readonly) {
            readonlyString = "readonly=\"true\"";
            //result.append(getInputHiddenHtml(id, value));
        }

        String styleClassString = "";
        if (styleClass != null) {
            styleClassString = "class=\"" + styleClass + "\"";
            //styleClassString = "class=\"ckeditor\"";
        }
        String iframeStyle = "";
        if (readonly) {
            iframeStyle = " border: 0px; background-color: #FFFFFF;";
        }

        String styleString = " style=\"width: 100%; " + iframeStyle + "\"";
        //styleClassString = "class=\"ckeditor\"";


        if (readonly) {
            result.append("<div class=\"jqte_editor\"").append(styleString).append(">").append(value).append("</div>");
            //result.append("<iframe ").append("src =\"").append(htmlRenderer).append("\" ").append(styleString).append(">").append(" ").append(readonlyString).append(" ").append(styleClassString).append(">").append(value).append("</iframe>");
        } else {
            result.append("<textarea id=\"").append(id).append("\" name=\"").append(id).append("\" ").append(styleString).append(readonlyString).append(" ").append(styleClassString).append(">").append(value).append("</textarea>");
        }
        //}

        //result.append("<script>CKEDITOR.replace('").append(id).append("');</script>");

        return result.toString();
    }

    public static String getInputSelectHtml(String id, String value, String[][] values, boolean readonly, String styleClass, int size) {
        StringBuilder result = new StringBuilder();

        if (!readonly) {

            String styleClassString = "";
            if (styleClass != null) {
                styleClassString = "class=\"" + styleClass + "\"";
                //styleClassString = "class=\"ckeditor\"";
            }

            result.append("<select id=\"").append(id).append("\" name=\"").append(id).append("\"").append(styleClassString).append(">");
            for (String current[] : values) {
                String selectedString = "";
                if (current[0].equals(value)) {
                    selectedString = "selected=\"true\"";
                }

                result.append("<option value=\"").append(current[0]).append("\" ").append(selectedString).append(">").append(current[1]).append("</option>");
            }
            result.append("</select>");
        } else {
            for (String current[] : values) {
                if (current[0].equals(value)) {
                    return getInputTextHtml(id, current[1], readonly, styleClass, size, 120);
                }
            }
        }

        return result.toString();
    }

    public static String getTextConditionHtml(String id, TextCondition condition, boolean readonly, boolean mandatoryEqual) {

        if (readonly && (condition == null || condition.getOperator() == Condition.OPERATOR_NO_CONDITION)) {
            return "<span class=\"disabled\">" + ApplicationUtilities.getMessage("label_ConditionNotInserted") + "</span>";
        }

        StringBuilder result = new StringBuilder();

        String values[][];
        if (mandatoryEqual) {
            values = new String[][]{{
                    String.valueOf(Condition.OPERATOR_TEXT_EQUAL), ApplicationUtilities.getMessage("label_equalTo")}};
        } else {
            values = new String[][]{
                {String.valueOf(Condition.OPERATOR_NO_CONDITION), ""},
                {String.valueOf(Condition.OPERATOR_TEXT_EQUAL), ApplicationUtilities.getMessage("label_equalTo")},
                {String.valueOf(Condition.OPERATOR_TEXT_NOT_EQUAL), ApplicationUtilities.getMessage("label_notEqual")},
                {String.valueOf(Condition.OPERATOR_TEXT_CONTAINS), ApplicationUtilities.getMessage("label_contains")},
                {String.valueOf(Condition.OPERATOR_TEXT_NOT_CONTAINS), ApplicationUtilities.getMessage("label_notContains")},
                {String.valueOf(Condition.OPERATOR_TEXT_BEGIN_WITH), ApplicationUtilities.getMessage("label_beginWith")},
                {String.valueOf(Condition.OPERATOR_TEXT_BEGIN_WITH), ApplicationUtilities.getMessage("label_endWith")},
                {String.valueOf(Condition.OPERATOR_TEXT_REGEX), ApplicationUtilities.getMessage("label_Regex")}
            };
        }

        String operator = "";

        String value = "";

        if (condition
                != null) {
            operator = String.valueOf(condition.getOperator());
            if (condition.getValue() != null) {
                value = condition.getValue();
            }
        }

        result.append(getInputSelectHtml(id + "Operator", operator, values, readonly, "ruleOperator bold", 12));
        result.append("&nbsp;");
        result.append(getInputTextHtml(id + "Value", value, readonly, "bold", 100, 1000));

        return result.toString();
    }

    public static String getTimeScheduleHtml(String id, TimeSchedule schedule, boolean readonly) {

        StringBuilder result = new StringBuilder();

        DecimalFormat decimalFormat = new DecimalFormat("00");

        if (!readonly) {

            String hours[][] = new String[24][2];

            for (int i = 0; i < 24; i++) {
                hours[i][0] = String.valueOf(i);
                hours[i][1] = String.valueOf(decimalFormat.format(i));
            }

            String minutes[][] = new String[60][2];

            for (int i = 0; i < 60; i++) {
                minutes[i][0] = String.valueOf(i);
                minutes[i][1] = String.valueOf(decimalFormat.format(i));
            }



            if (schedule instanceof TimeSchedule.DailySchedule) {
                result.append(getInputSelectHtml(id + "Hour", String.valueOf(((TimeSchedule.DailySchedule) schedule).getHour()), hours, readonly, "maskedInteger bold right", 2));

                //result.append(getInputTextHtml(id + "Hour", String.valueOf(((TimeSchedule.DailySchedule) schedule).getHour()), readonly, "maskedInteger bold right", 2, 2));
                result.append(":");

                result.append(getInputSelectHtml(id + "Minute", String.valueOf(((TimeSchedule.DailySchedule) schedule).getMinute()), minutes, readonly, "maskedInteger bold left", 2));

                //result.append(getInputTextHtml(id + "Minute", String.valueOf(((TimeSchedule.DailySchedule) schedule).getMinute()), readonly, "maskedInteger bold right", 2, 2));
            }
        } else {
            result = result.append(getInputTextHtml(id, decimalFormat.format(((TimeSchedule.DailySchedule) schedule).getHour()) + "&nbsp;:&nbsp;" + decimalFormat.format(((TimeSchedule.DailySchedule) schedule).getMinute()), readonly, "maskedInteger bold left", 10, 5));
        }

        return result.toString();
    }

    public static String getTimeConditionHtml(String id, TimePeriodCondition condition, boolean isLimit, boolean readonly) {

        if (readonly && (condition == null || condition.getOperator() == Condition.OPERATOR_NO_CONDITION)) {
            return "<span class=\"disabled\">" + ApplicationUtilities.getMessage("label_ConditionNotInserted") + "</span>";
        }

        StringBuilder result = new StringBuilder();

        String values[][];

        if (isLimit) {
            values = new String[][]{
                {String.valueOf(Condition.OPERATOR_NO_CONDITION), ""},
                {String.valueOf(Condition.OPERATOR_NUMBER_GREATER_THAN), ApplicationUtilities.getMessage("label_atLeastFor")}
            };
        } else {
            values = new String[][]{
                {String.valueOf(Condition.OPERATOR_NO_CONDITION), ""},
                {String.valueOf(Condition.OPERATOR_NUMBER_EQUAL), ApplicationUtilities.getMessage("label_equalTo")},
                {String.valueOf(Condition.OPERATOR_NUMBER_GREATER_THAN), ApplicationUtilities.getMessage("label_greaterThan")},
                {String.valueOf(Condition.OPERATOR_NUMBER_LESS_THAN), ApplicationUtilities.getMessage("label_lessThan")}
            };
        }

        String sizes[][] = new String[][]{
            {String.valueOf(TimePeriod.UNIT_MILLISECOND), ApplicationUtilities.getMessage("label_Milliseconds")},
            {String.valueOf(TimePeriod.UNIT_SECOND), ApplicationUtilities.getMessage("label_Seconds")},
            {String.valueOf(TimePeriod.UNIT_MINUTE), ApplicationUtilities.getMessage("label_Minutes")},
            {String.valueOf(TimePeriod.UNIT_HOUR), ApplicationUtilities.getMessage("label_Hours")},
            {String.valueOf(TimePeriod.UNIT_DAY), ApplicationUtilities.getMessage("label_Days")}
        };

        String operator = "";
        String value = "";
        String unit = "";

        if (condition != null) {
            operator = String.valueOf(condition.getOperator());
            if (condition.getOperator() != Condition.OPERATOR_NO_CONDITION) {
                value = ApplicationUtilities.integerFormatter.format(condition.getValue().getValue());
                unit = String.valueOf(condition.getValue().getUnit());
            } else {
                value = "";
                unit = "";
            }
        }

        result.append(getInputSelectHtml(id + "Operator", operator, values, readonly, "ruleOperator bold", 12));
        result.append("&nbsp;");
        result.append(getInputTextHtml(id + "Value", value, readonly, "maskedInteger bold right", 10, 4));
        result.append("&nbsp;");
        result.append(getInputSelectHtml(id + "Unit", unit, sizes, readonly, "bold", 10));

        return result.toString();
    }

    public static String getSizeConditionHtml(String id, SizeCondition condition, boolean readonly) {

        if (readonly && (condition == null || condition.getOperator() == Condition.OPERATOR_NO_CONDITION)) {
            return "<span class=\"disabled\">" + ApplicationUtilities.getMessage("label_ConditionNotInserted") + "</span>";
        }

        StringBuilder result = new StringBuilder();
        String values[][] = new String[][]{
            {String.valueOf(Condition.OPERATOR_NO_CONDITION), ""},
            {String.valueOf(Condition.OPERATOR_NUMBER_EQUAL), ApplicationUtilities.getMessage("label_equalTo")},
            {String.valueOf(Condition.OPERATOR_NUMBER_GREATER_THAN), ApplicationUtilities.getMessage("label_greaterThan")},
            {String.valueOf(Condition.OPERATOR_NUMBER_LESS_THAN), ApplicationUtilities.getMessage("label_lessThan")}
        };

        String sizes[][] = new String[][]{
            {String.valueOf(SizeCondition.SIZE_UNIT_B), "b"},
            {String.valueOf(SizeCondition.SIZE_UNIT_KB), "Kb"},
            {String.valueOf(SizeCondition.SIZE_UNIT_MB), "Mb"},
            {String.valueOf(SizeCondition.SIZE_UNIT_GB), "Gb"},
            {String.valueOf(SizeCondition.SIZE_UNIT_TB), "Tb"}
        };


        String operator = "";
        String value = "";
        String unit = "";


        if (condition != null) {
            operator = String.valueOf(condition.getOperator());
            if (condition.getOperator() != Condition.OPERATOR_NO_CONDITION) {
                value = ApplicationUtilities.integerFormatter.format(condition.getValue());
                unit = String.valueOf(condition.getUnit());
            } else {
                value = "";
                unit = "";
            }
        }

        result.append(getInputSelectHtml(id + "Operator", operator, values, readonly, "ruleOperator bold", 12));
        result.append("&nbsp;");
        result.append(getInputTextHtml(id + "Value", value, readonly, "maskedInteger bold right", 10, 4));
        result.append("&nbsp;");
        result.append(getInputSelectHtml(id + "Unit", unit, sizes, readonly, "bold", 10));

        return result.toString();
    }

    public static String getNumberPercentageConditionHtml(String id, NumberCondition condition, int maxlength, boolean readonly) {

        if (readonly && (condition == null || condition.getOperator() == Condition.OPERATOR_NO_CONDITION)) {
            return "<span class=\"disabled\">" + ApplicationUtilities.getMessage("label_ConditionNotInserted") + "</span>";
        }

        StringBuilder result = new StringBuilder();

        String values[][] = new String[][]{
            {String.valueOf(Condition.OPERATOR_NO_CONDITION), ""},
            {String.valueOf(Condition.OPERATOR_NUMBER_EQUAL), ApplicationUtilities.getMessage("label_equalTo")},
            {String.valueOf(Condition.OPERATOR_NUMBER_GREATER_THAN), ApplicationUtilities.getMessage("label_greaterThan")},
            {String.valueOf(Condition.OPERATOR_NUMBER_LESS_THAN), ApplicationUtilities.getMessage("label_lessThan")}
        };


        String operator = "";
        String value = "";


        if (condition != null) {
            operator = String.valueOf(condition.getOperator());
            if (condition.getOperator() != Condition.OPERATOR_NO_CONDITION) {
                value = ApplicationUtilities.integerFormatter.format(condition.getValue());
            } else {
                value = "";
            }
        }

        result.append(getInputSelectHtml(id + "Operator", operator, values, readonly, "ruleOperator bold", 12));
        result.append("&nbsp;");
        result.append(getInputTextHtml(id + "Value", value, readonly, "maskedInteger bold right", 10, maxlength));
        result.append("&nbsp;");

        String valuePercentage = "";
        if (condition != null && condition.getOperator() != Condition.OPERATOR_NO_CONDITION) {
            valuePercentage = "%";
        }
        result.append(getInputTextHtml(id + "Percentage", valuePercentage, true, "bold", 3, 1));

        return result.toString();


    }

    public static String getNumberSelectConditionHtml(String id, NumberCondition condition, boolean readonly, String values[][]) {

        if (readonly && (condition == null || condition.getOperator() == Condition.OPERATOR_NO_CONDITION)) {
            return "<span class=\"disabled\">" + ApplicationUtilities.getMessage("label_ConditionNotInserted") + "</span>";
        }

        StringBuilder result = new StringBuilder();

        String value = "";

        if (condition != null) {
            if (condition.getOperator() != Condition.OPERATOR_NO_CONDITION) {
                value = ApplicationUtilities.integerFormatter.format(condition.getValue());
            } else {
                value = "";
            }
        }

        result.append(getInputHiddenHtml(id + "Operator", String.valueOf(Condition.OPERATOR_NUMBER_EQUAL)));
        result.append(getInputSelectHtml(id + "Value", value, values, readonly, "maskedInteger bold ruleOperator", 12));


        return result.toString();
    }

    public static String getBooleanConditionHtml(String id, BooleanCondition condition, boolean readonly) {

        if (readonly && (condition == null || condition.getOperator() == Condition.OPERATOR_NO_CONDITION)) {
            return "<span class=\"disabled\">" + ApplicationUtilities.getMessage("label_ConditionNotInserted") + "</span>";
        }

        StringBuilder result = new StringBuilder();

        String value = "";

        if (condition != null) {
            if (condition.getOperator() != Condition.OPERATOR_NO_CONDITION) {
                value = String.valueOf(condition.isValue());
            } else {
                value = "";
            }
        }


        String[][] values = new String[][]{
            {"", ""},
            {String.valueOf(Boolean.TRUE), ApplicationUtilities.getMessage("label_Yes")},
            {String.valueOf(Boolean.FALSE), ApplicationUtilities.getMessage("label_No")},};

        result.append(getInputHiddenHtml(id + "Operator", String.valueOf(Condition.OPERATOR_BOOLEAN_EQUAL)));
        result.append(getInputSelectHtml(id + "Value", value, values, readonly, "ruleOperator bold", 12));


        return result.toString();
    }

    public static String getInputHiddenHtml(String id, String value) {
        StringBuilder result = new StringBuilder();

        result.append("<input type=\"hidden\" id=\"").append(id).append("\" name=\"").append(id).append("\" value=\"").append(value).append("\"></input>");

        return result.toString();
    }

    public static String getButtontHtml(String id, String value, String action) {
        StringBuilder result = new StringBuilder();

        result.append("<a id=\"").append(id).append("\" name=\"").append(id).append("\" href=\"").append(action).append("\"" + " class=\"button\">").append(value).append("</a>");

        return result.toString();
    }

    public static String getAjaxButtontHtmlOld(String id, String action, String execute, Map<String, String> renders, String label) {
        StringBuilder result = new StringBuilder();

        //String id = String.valueOf(Math.abs(action.hashCode()));

        String rendersString = "";
        for (String render : renders.keySet()) {
            rendersString = rendersString + "									$(\"" + render + "\").load(\"" + renders.get(render) + "\");\n";
        }

        result.append("<a id=\"").append(id).append("\" href=\"#\" class=\"button\">").append(label).append("<script>" + "$(\"#").append(id).append("\").live(\"click\", function() {\n");

        if (action != null) {
            result.append("				 $.ajax({\n"
                    + "						type: 'POST',\n"
                    + "						url: \"").append(action).append("\",\n"
                    + "						data: $(\"").append(execute).append("\").serialize(),\n"
                    + "						success: function(response) {\n"
                    + "							 if (response.redirect) {\n"
                    + "									window.location.href = response.redirect;\n"
                    + "							 } else {\n");
        }
        result.append(rendersString);
        if (action != null) {
            result.append("							 }\n"
                    + "						}\n"
                    + "				 });\n");
        }
        result.append("			});\n"
                + "" + "</script>"
                + "</a>");

        return result.toString();
    }

    public static String getTimeChart(Map<String, ChartPlotArea> plots, boolean logScale, boolean trendLines) {
        return getTimeChart(plots, 5, true, true, logScale, trendLines);
    }

    public static String getTimeChart(Map<String, ChartPlotArea> plots, int gridLines, boolean vAxisText, boolean hAxisText, boolean logScale, boolean trendLines) {

        int height = ApplicationUtilities.getPropertyInteger(ApplicationUtilities.TIME_CHART_HEIGHT_PARAMETER, ApplicationUtilities.DEFAULT_TIME_CHART_HEIGHT);
        int width = ApplicationUtilities.getPropertyInteger(ApplicationUtilities.PAGE_WIDTH_PARAMETER, ApplicationUtilities.DEFAULT_PAGE_WIDTH);

        StringBuilder result = new StringBuilder();

        int plotsCount = 0;
        for (String key : plots.keySet()) {
            ChartPlotArea plot = plots.get(key);
            if (!plot.getTitles().isEmpty() || !plot.getValues().isEmpty()) {
                plotsCount = plotsCount + 1;
                //plots.remove(key);
            }
        }

        if (plotsCount == 0) {
            return "No chart to show";
        }

        int chartHeight = height / plotsCount;

        if (chartHeight < 150) {
            //chartHeight = 150;
        }

        gridLines = height / ((height / (10 + plotsCount)) * plotsCount);


        if ((gridLines * plotsCount) / height > 0) {
            // gridLines
        }
        for (String key : plots.keySet()) {
            ChartPlotArea plot = plots.get(key);
            if (!plot.getTitles().isEmpty() || !plot.getValues().isEmpty()) {
                plotsCount = plotsCount - 1;
                result.append(getTimeChart(key, plots.get(key), width, chartHeight, gridLines, vAxisText, (plotsCount == 0) ? hAxisText : false, logScale, trendLines));
            }
        }

        return result.toString();

    }

    public static String getTimeChart(String id, ChartPlotArea plot, int width, int height, int gridLines, boolean vAxisText, boolean hAxisText, boolean logScale, boolean trendLines) {

        String type = ApplicationUtilities.getPropertyString(ApplicationUtilities.TIME_CHART_TYPE_PARAMETER, ApplicationUtilities.DEFAULT_TIME_CHART_TYPE);

        StringBuilder result = new StringBuilder();

        if (plot.getValues().isEmpty()) {
            result.append("No points to show.");
        } else if ((width / plot.getValues().size()) < -3) {
            result.append("Troppi punti da visualizzare, selezione un periodo di campionatura differente.");
        } else {


            result.append("<script type=\"text/javascript\">\n");

            result.append("google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});\n");
            //result.append("google.setOnLoadCallback(drawChart);\n";

            result.append("$(function() {drawChart").append(id).append("();});\n");

            result.append("function drawChart").append(id).append("() {\n");

            result.append("var data = new google.visualization.DataTable();\n");
            result.append("data.addColumn('datetime', 'Date');\n");

            for (String title : plot.getTitles()) {
                result.append("data.addColumn('number', '").append(title).append("');\n");
                //Tooltip for single column
                //result.append("data.addColumn({type:'string', role:'annotation'});\n");
            }



            result.append(" data.addRows([\n");

            boolean firstPassed = false;
            for (Date date : plot.getValues().keySet()) {
                if (firstPassed) {
                    result.append(",");
                }

                List<Double> values = plot.getValues().get(date);

                firstPassed = true;

                String valuesString = "";
                for (double value : values) {
                    valuesString = valuesString + ", " + getAreaChartValueString(value);
                    //valuesString = valuesString + ", " + "'ciao'";
                }



                result.append("[").append(getAreaChartDateString(date)).append(valuesString).append("]\n");
            }

            result.append("]);\n");


            result.append("var options = {\n");

            if (plot.getColors() != null && !plot.getColors().isEmpty()) {

                String colorsString = "";
                for (Color color : plot.getColors()) {
                    if (!colorsString.equals("")) {
                        colorsString = colorsString + ", ";
                    }
                    colorsString = colorsString + "'" + String.format("#%06X", (0xFFFFFF & color.getRGB())) + "'";
                }
                result.append("colors: [").append(colorsString).append("],\n");
            }
            result.append("legend: {position: 'none'},\n");
            //result.append("crosshair: { trigger: 'both', opacity: 0.1, orientation: 'both' },\n");
            
            result.append("chartArea:{left:0, top:5, width: \"100%\", height: ").append(height - 20).append("},\n");

            String formatString = "";
            if (plot.getFormat() != null) {
                formatString = "format: '" + plot.getFormat() + "', ";
            }

            String vAxisViewWindow = "viewWindowMode: 'maximized'";

            if (plot.getMinMaxValue() != null) {
                vAxisViewWindow = "viewWindowMode: 'explicit', viewWindow:{ min:" + getAreaChartValueString(plot.getMinMaxValue()[0]) + " , max:" + getAreaChartValueString(plot.getMinMaxValue()[1]) + "}";
            }

            String hAxisViewWindow = "viewWindowMode: 'maximized',";
            if (plot.getMinMaxDate() != null) {
                hAxisViewWindow = "viewWindowMode: 'explicit', viewWindow:{ min: " + getAreaChartDateString(plot.getMinMaxDate()[0]) + ", max: " + getAreaChartDateString(plot.getMinMaxDate()[1]) + "}";
            }

            String vAxisTextPosition = "none";
            if (vAxisText) {
                vAxisTextPosition = "in";
            }

            String hAxisTextPosition = "none";
            if (hAxisText) {
                hAxisTextPosition = "out";
            }

            result.append("vAxis: {").append(formatString).append("logScale: ").append(logScale).append(", textPosition: '").append(vAxisTextPosition).append("', maxTextLines: 1, ").append(vAxisViewWindow).append(", gridlines:{count: ").append(gridLines).append(", color: '#E5EAF1'}, textStyle:{color: '#000000'}, baselineColor: '#9CABC1'},\n");
            result.append("hAxis: {logScale: false, textPosition: '").append(hAxisTextPosition).append("', maxTextLines: 1, ").append(hAxisViewWindow).append(", gridlines:{count: ").append(gridLines).append(", color: '#E5EAF1'}, textStyle:{color: '#000000'}, baselineColor: '#FFFFFF'} ,\n");
            result.append("width: ").append(width).append(",\n");
            result.append("height: ").append(height).append(",\n");

            result.append("pointSize: ").append(ApplicationUtilities.getPropertyInteger(ApplicationUtilities.TIME_CHART_POINT_SIZE_PARAMETER, ApplicationUtilities.DEFAULT_TIME_CHART_POINT_SIZE)).append(",\n");

            //result.append("pointSize: ").append("2").append(",\n");
            result.append("lineWidth: ").append(ApplicationUtilities.getPropertyInteger(ApplicationUtilities.TIME_CHART_LINE_WIDTH_PARAMETER, ApplicationUtilities.DEFAULT_TIME_CHART_LINE_WIDTH)).append(",\n");
            result.append("isStacked: false,\n");
            //result.append("backgroundColor: { fill:'transparent'},\n");
            result.append("backgroundColor: { fill:'FFFFFF'},\n");
            //result.append("tooltip: { trigger: 'selection' },\n");
            
            result.append("fontSize: 11,\n");
            result.append("fontName: 'Arial, sans-serif',\n");
            
            if (trendLines) {
                result.append("trendlines: { 0: {type: 'linear', lineWidth: 1} , 1: {type: 'linear', lineWidth: 1} , 2: {type: 'linear', lineWidth: 1} },\n");
            }

            result.append("};\n");

            result.append("var chart = new google.visualization.").append(type).append("(document.getElementById('chart_div_").append(id).append("'));\n");
            result.append("chart.draw(data, options);\n");

            
            //Drow lines
//            result.append("var newLine = document.createElementNS('http://www.w3.org/2000/svg', 'line');");
//result.append("newLine.setAttribute('id', 'lineId');");
//result.append("newLine.setAttribute('style', 'stroke:rgb(0,0,0); stroke-width:3;');        ");
//result.append("newLine.setAttribute('x1', chart.getChartLayoutInterface().getXLocation(" + getAreaChartDateString(new Date())+ "));");
//result.append("newLine.setAttribute('y1', chart.getChartLayoutInterface().getChartAreaBoundingBox().top);");
//result.append("newLine.setAttribute('x2', chart.getChartLayoutInterface().getXLocation(" + getAreaChartDateString(new Date())+ "));");
//result.append("newLine.setAttribute('y2', chart.getChartLayoutInterface().getChartAreaBoundingBox().height + chart.getChartLayoutInterface().getChartAreaBoundingBox().top);");
//result.append("$(\"svg\").append(newLine);");
            
            
            
            result.append("}\n");

            result.append("</script>\n");
        }
        result.append("\n");

        result.append("<div id=\"chart_div_").append(id).append("\" style=\"background-color: transparent; border: 0px solid #000000;\"></div>\n");

        return result.toString();
    }

    public static String getTotalChart(String id, ChartPlotPie plot) {

        String type = ApplicationUtilities.getPropertyString(ApplicationUtilities.TOTAL_CHART_TYPE_PARAMETER, ApplicationUtilities.DEFAULT_TOTAL_CHART_TYPE);

        StringBuilder result = new StringBuilder();

        int height = ApplicationUtilities.getPropertyInteger(ApplicationUtilities.TOTAL_CHART_HEIGHT_PARAMETER, ApplicationUtilities.DEFAULT_TOTAL_CHART_HEIGHT);
        int width = ApplicationUtilities.getPropertyInteger(ApplicationUtilities.TOTAL_CHART_WIDTH_PARAMETER, ApplicationUtilities.DEFAULT_TOTAL_CHART_WIDTH);

        if (plot.getValues().isEmpty()) {
            result.append("Nessun punto da visualizzare.");
        } else if ((width / plot.getValues().size()) < 3) {
            result.append("Troppi punti da visualizzare, selezione un periodo di campionatura differente.");
        } else {


            result.append("<script type=\"text/javascript\">\n");

            result.append("google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});\n");
            //result.append("google.setOnLoadCallback(drawChart);\n";

            result.append("$(function() {drawChart").append(id).append("();});\n");

            result.append("function drawChart").append(id).append("() {\n");

            result.append("var data = google.visualization.arrayToDataTable([\n");

            result.append("['', '']\n");

            for (String key : plot.getValues().keySet()) {
                result.append(",['").append(key).append("', ").append(plot.getValues().get(key)).append("]\n");
            }

            result.append("]);\n");


            result.append("var options = {\n");

            if (plot.getColors() != null && !plot.getColors().isEmpty()) {

                String colorsString = "";
                for (String key : plot.getColors().keySet()) {
                    if (!colorsString.equals("")) {
                        colorsString = colorsString + ", ";
                    }
                    colorsString = colorsString + "'" + String.format("#%06X", (0xFFFFFF & plot.getColors().get(key).getRGB())) + "'";
                }
                result.append("colors: [").append(colorsString).append("],\n");
            }
            result.append("legend: {position: 'none'},\n");
            //result.append("legend: {position: 'right'},\n";

            result.append("width: ").append(width + 20).append(",\n");
            result.append("height: ").append(height + 20).append(",\n");
            result.append("isStacked: false,\n");
            
            result.append("fontSize: 11,\n");
            result.append("fontName: 'Arial, sans-serif',\n");
            
            result.append("is3D: false,\n");
            //result.append("pieResidueSliceColor: '#FFF',\n");
            //result.append("pieSliceBorderColor: '#000',\n");

            result.append("pieHole: 0.4,\n");

            result.append("pieSliceTextStyle: {color: '#FFF'},\n");

            result.append("slices: {1: {offset: 0.01}, 2: {offset: 0.01}, 3: {offset: 0.01}, 4: {offset: 0.01}},\n");

            result.append("backgroundColor: { fill:'transparent'},\n");
            result.append("tooltip: { text:'percentage'},\n");
            result.append("pieSliceText: 'label',\n");


            
            result.append("chartArea:{top: 10, left: 10, width:\"").append(width).append("\", height:\"").append(height).append("\"}\n");

            result.append("};\n");

            result.append("var chart = new google.visualization.").append(type).append("(document.getElementById('chart_div_").append(id).append("'));\n");
            result.append("chart.draw(data, options);\n");

            result.append("}\n");

            result.append("</script>\n");
        }
        result.append("\n");

        result.append("<div id=\"chart_div_").append(id).append("\" style=\"background-color: transparent; border: 0px solid #000000; margin-left: auto; margin-right: auto; width: ").append(width + 20).append("px; height: ").append(height + 20).append("px;\"></div>\n");

        return result.toString();
    }

    public static String getColumnChart(String id, ChartPlotPie plot, int width, int height) {
        StringBuilder result = new StringBuilder();

        if (plot.getValues().isEmpty()) {
            result.append("Nessun punto da visualizzare.");
        } else if ((width / plot.getValues().size()) < 3) {
            result.append("Troppi punti da visualizzare, selezione un periodo di campionatura differente.");
        } else {


            result.append("<script type=\"text/javascript\">\n");

            result.append("google.load(\"visualization\", \"1\", {packages:[\"corechart\"]});\n");
            //result.append("google.setOnLoadCallback(drawChart);\n";

            result.append("$(function() {drawChart").append(id).append("();});\n");

            result.append("function drawChart").append(id).append("() {\n");

            result.append("var data = google.visualization.arrayToDataTable([\n");

            result.append("['serie', 'val1', 'val2'],['1', 10, 20]\n");

            for (String key : plot.getValues().keySet()) {
                //result.append(",['1', '" + key + "', " + plot.getValues().get(key) + "]\n";
            }

            result.append("]);\n");


            result.append("var options = {\n");

            if (plot.getColors() != null && !plot.getColors().isEmpty()) {

                String colorsString = "";
                for (String key : plot.getColors().keySet()) {
                    if (!colorsString.equals("")) {
                        colorsString = colorsString + ", ";
                    }
                    colorsString = colorsString + "'" + String.format("#%06X", (0xFFFFFF & plot.getColors().get(key).getRGB())) + "'";
                }
                result.append("colors: [").append(colorsString).append("],\n");
            }
            result.append("legend: {position: 'none'},\n");

            result.append("width: ").append(width + 0).append(",\n");
            result.append("height: ").append(height + 0).append(",\n");
            result.append("isStacked: true,\n");
            //result.append("fontSize: 10,\n");
            result.append("is3D: true,\n");
            result.append("isStacked: true,\n");


            result.append("backgroundColor: {fill:'transparent'},\n");
            result.append("tooltip: { text:'percentage'},\n");
            result.append("pieSliceText: 'label',\n");
            result.append("hAxis: { textPosition:'auto', baselineColor:'#FFFFFF', gridlines: {color:'#FFFFFF'}},\n");
            result.append("vAxis: { textPosition:'none', baselineColor:'#FFFFFF', gridlines: {color:'#FFFFFF'}},\n");


            //result.append("fontName: 'Arial, sans-serif',\n");
            result.append("chartArea:{top: 0, left: 0, width:\"").append(width).append("\", height:\"").append(height).append("\"}\n");

            result.append("};\n");

            result.append("var chart = new google.visualization.BarChart(document.getElementById('chart_div_").append(id).append("'));\n");
            result.append("chart.draw(data, options);\n");

            result.append("}\n");

            result.append("</script>\n");
        }
        result.append("\n");

        result.append("<div id=\"chart_div_").append(id).append("\" style=\"background-color: transparent; border: 0px solid #000000; margin-left: auto; margin-right: auto; width: ").append(width + 20).append("px; height: ").append(height + 20).append("px;\"></div>\n");

        return result.toString();
    }

    private static String getAreaChartValueString(double value) {
        DecimalFormat valueFormat = new DecimalFormat("0.###");
        DecimalFormatSymbols valueFormatSymbols = new DecimalFormatSymbols();
        valueFormatSymbols.setDecimalSeparator('.');
        valueFormat.setDecimalFormatSymbols(valueFormatSymbols);
        if (!Double.isNaN(value)) {
            return valueFormat.format(value);
        } else {
            return "null";
        }
    }

    private static String getAreaChartDateString(Date date) {
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(date);
        return "new Date(" + calendar.get(Calendar.YEAR) + ", " + calendar.get(Calendar.MONTH) + ", " + calendar.get(Calendar.DAY_OF_MONTH) + ", " + calendar.get(Calendar.HOUR_OF_DAY) + ", " + calendar.get(Calendar.MINUTE) + ", " + calendar.get(Calendar.SECOND) + ")";
    }
}
