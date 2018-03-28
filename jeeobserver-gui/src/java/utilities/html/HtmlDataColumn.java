package utilities.html;

public class HtmlDataColumn {

    private String label;

    private int width;

    private String styleClass;

    private boolean selected;

    private boolean sortable;

    public HtmlDataColumn(String label, int width, boolean sortable) {
        this.label = label;
        this.width = width;
        this.sortable = sortable;
    }

    public HtmlDataColumn(String label, int width, String styleClass, boolean sortable) {
        this(label, width, sortable);
        this.styleClass = styleClass;
    }

    public String getStyleClass() {
        return styleClass;
    }

    public String getLabel() {
        return label;
    }

    public int getWidth() {
        return width;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSortable() {
        return sortable;
    }
}
