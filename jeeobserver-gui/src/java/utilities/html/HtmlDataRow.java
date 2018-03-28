package utilities.html;

public class HtmlDataRow implements Comparable<HtmlDataRow> {

    private HtmlDataValue[] values;

    private Comparable valueSortable;

    private String hiddenContentHtml;

    private boolean expanded = false;

    public HtmlDataRow(HtmlDataValue[] values, Comparable valueSortable) {
        this(values, valueSortable, null, false);
    }

    public HtmlDataRow(HtmlDataValue[] values, Comparable valueSortable, String hiddenContentHtml, boolean expanded) {
        this.valueSortable = valueSortable;
        this.values = values;
        this.hiddenContentHtml = hiddenContentHtml;
        this.expanded = expanded;
    }

    public Comparable getValueSortable() {
        return valueSortable;
    }

    public void setValueSortable(Comparable valueSortable) {
        this.valueSortable = valueSortable;
    }

    public HtmlDataValue[] getValues() {
        return values;
    }

    public void setValues(HtmlDataValue[] values) {
        this.values = values;
    }

    public String getHiddenContentHtml() {
        return hiddenContentHtml;
    }

    public void setHiddenContentHtml(String hiddenContentHtml) {
        this.hiddenContentHtml = hiddenContentHtml;
    }

    public int compareTo(HtmlDataRow o) {
        return o.getValueSortable().compareTo(this.valueSortable);
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }
}
