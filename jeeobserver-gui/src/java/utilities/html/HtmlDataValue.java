package utilities.html;

import java.text.DecimalFormat;
import jeeobserver.utilities.SizeUtilities;
import jeeobserver.utilities.TimeUtilities;
import utilities.ApplicationUtilities;

public class HtmlDataValue implements Comparable {

    public static final int TYPE_VALUE = 1;

    public static final int TYPE_CHECK_BOX = 2;

    public static final int TYPE_COMBO = 3;

    public static final int TYPE_PROGRESS = 4;

    public static final int TYPE_VALUE_AND_UNIT = 5;

    public static final int TYPE_CHECK_LINK = 6;

    public static final int VALUE_TYPE_TIME = 1;

    public static final int VALUE_TYPE_SIZE = 2;

    private int type;

    private Comparable valueSortable;

    private String value;

    private String unit;

    private String toolTip;

    private Boolean valueBoolean;

    private Double valueDouble;

    private String targetInputName;

    private int targetInputValue;

    private String styleClass;

    public HtmlDataValue(double value, int type) {
        if (!Double.isNaN(value)) {
            if (type == VALUE_TYPE_TIME) {
                this.value = ApplicationUtilities.decimalFormatter.format(TimeUtilities.fromNanosToValue(value));
                this.unit = TimeUtilities.fromNanosToUnit(value);
                this.valueSortable = value;
                this.type = TYPE_VALUE_AND_UNIT;
            } else if (type == VALUE_TYPE_SIZE) {
                this.value = ApplicationUtilities.decimalFormatter.format(SizeUtilities.fromBytesToValue(value));
                this.unit = SizeUtilities.fromBytesToUnit(value);
                this.valueSortable = value;
                this.type = TYPE_VALUE_AND_UNIT;
            }
        } else {
            this.value = "-";
            this.valueSortable = value;
            this.type = type;
        }
    }

    public HtmlDataValue(double value) {
        this(value, ApplicationUtilities.decimalFormatter);
    }

    public HtmlDataValue(double value, DecimalFormat formatter) {
        if (!Double.isNaN(value)) {
            this.value = formatter.format(value);
            this.valueSortable = value;
            this.type = TYPE_VALUE;
        } else {
            this.value = "-";
            this.valueSortable = value;
            this.type = TYPE_VALUE;
        }
    }

    public HtmlDataValue(String value) {
        this.value = value;
        this.valueSortable = value;
        this.type = TYPE_VALUE;
    }

    public HtmlDataValue(String value, Comparable valueSortable) {
        this.value = value;
        this.valueSortable = valueSortable;
        this.type = TYPE_VALUE;
    }

    public HtmlDataValue(String value, String unit, Comparable valueSortable) {
        this.value = value;
        this.unit = unit;
        this.valueSortable = valueSortable;
        this.type = TYPE_VALUE_AND_UNIT;
    }

    public HtmlDataValue(String value, Comparable valueSortable, int type) {
        this.value = value;
        this.valueSortable = valueSortable;
        this.type = type;
    }

    public HtmlDataValue(Boolean value, int type, String targetInputName, int targetInputValue, String styleClass, String toolTip) {
        this.value = value.toString();
        this.valueBoolean = value;
        this.valueSortable = "";
        this.type = type;
        this.toolTip = toolTip;

        this.targetInputName = targetInputName;
        this.targetInputValue = targetInputValue;
        this.styleClass = styleClass;
    }

    public HtmlDataValue(String value, int type, String targetInputName, int targetInputValue, String styleClass, String toolTip) {
        this.value = value.toString();
        this.valueSortable = value;
        this.type = type;
        this.toolTip = toolTip;

        this.targetInputName = targetInputName;
        this.targetInputValue = targetInputValue;
        this.styleClass = styleClass;
    }

    public HtmlDataValue(Double value, DecimalFormat decimalFormat, int type) {
        this.value = decimalFormat.format(value);
        this.valueDouble = value;
        this.valueSortable = value;
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public String getUnit() {
        return unit;
    }

    public String getValue() {
        return value;
    }

    public Comparable getValueSortable() {
        return valueSortable;
    }

    public Boolean getValueBoolean() {
        return valueBoolean;
    }

    public Double getValueDouble() {
        return valueDouble;
    }

    public String getTargetInputName() {
        return targetInputName;
    }

    public int getTargetInputValue() {
        return targetInputValue;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public String getToolTip() {
        return toolTip;
    }

    public int compareTo(Object o) {
        return ((HtmlDataValue) o).getValueSortable().compareTo(this.valueSortable);
    }
}
