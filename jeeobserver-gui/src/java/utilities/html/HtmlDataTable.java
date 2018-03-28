package utilities.html;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class HtmlDataTable {

    private String id;

    public final static int SORT_UNSORTED = 0;

    public final static int SORT_ASC = 1;

    public final static int SORT_DESC = 2;

    private Map<Integer, HtmlDataColumn> columns;

    private List<HtmlDataRow> rows;

    private int sortColumn;

    private int sortOrder;

    public void addColumn(HtmlDataColumn column) {
        this.columns.put(this.columns.size(), column);
    }

    public void addValues(HtmlDataValue values[], String hiddenContextUrl, boolean expanded) {

        HtmlDataRow row = new HtmlDataRow(values, values[sortColumn], hiddenContextUrl, expanded);

        this.rows.add(row);
    }

    public void addValues(HtmlDataValue values[]) {

        HtmlDataRow row = new HtmlDataRow(values, values[sortColumn]);

        this.rows.add(row);
    }

    public HtmlDataTable(String id, Integer sortColumn, Integer sortOrder) {

        this.id = id;

        if (sortColumn != null) {
            this.sortColumn = sortColumn;
        } else {
            this.sortColumn = 1;
        }

        if (sortOrder != null) {
            this.sortOrder = sortOrder;
        } else {
            this.sortOrder = SORT_UNSORTED;
        }


        this.columns = new TreeMap();
        this.rows = new ArrayList();
    }

    public String getHtmlCode() {
        StringBuilder result = new StringBuilder();

        result.append("<table id=\"").append(this.id).append("\" width=\"100%\" cellspacing=\"0\" cellpadding=\"0\" class=\"resultTable\">\n");

        int columnIndex = 0;

        if (this.sortOrder == SORT_UNSORTED || this.sortOrder == SORT_DESC) {
            this.sortOrder = SORT_ASC;
        } else {
            this.sortOrder = SORT_DESC;
        }

        for (HtmlDataColumn column : columns.values()) {

            String sortingClass = "";

            if (column.isSortable()) {
                sortingClass = "columnUnsorted";

                if (columnIndex == this.sortColumn) {
                    switch (this.sortOrder) {
                        case SORT_ASC:
                            sortingClass = "columnAscending";
                            break;
                        case SORT_DESC:
                            sortingClass = "columnDescending";
                            break;
                    }
                }
            }

            result.append("<th class=\"").append(column.getStyleClass()).append(" resultTableHeader\" colspan=\"1\">");

            //result = result + "<div class=\"" + sortingClass + "\">");


            String style = "";
            if (column.isSortable()) {
                style = "cursor: pointer;";
            }

            result.append("<span class=\"sortColumn ").append(sortingClass).append("\" style=\"").append(style).append("\" onclick=\"$('#resultsSortColumn').val('").append(columnIndex).append("'); $('#resultsSortOrder').val('").append(this.sortOrder).append("');\">");
            result.append(column.getLabel());
            result.append("</span>\n");
            //result = result + "<div class=\"" + sortingClass + "\">&#160;</div>\n");

            //result = result + "</div>");

            result.append("</th>\n");

            columnIndex = columnIndex + 1;
        }

        Collections.sort(this.rows);

        if (sortOrder != SORT_UNSORTED && sortOrder == SORT_DESC) {
            Collections.reverse(this.rows);
        }

        for (int i = 0; i < this.rows.size(); i++) {

            HtmlDataRow row = rows.get(i);

            HtmlDataValue values[] = row.getValues();

            if (row.getHiddenContentHtml() != null) {
                result.append("<tr class=\"resultTableRow resultTableRowExpanded\">");
            } else {
                result.append("<tr class=\"resultTableRow\">");
            }

            columnIndex = 0;

            for (int j = 0; j < values.length; j++) {

                HtmlDataColumn column = columns.get(j);

                String sortedClass = "";

                if (column.isSortable()) {
                    if (columnIndex == this.sortColumn) {
                        sortedClass = "columnSorted";
                    }
                }

                result.append("<td class=\"").append(column.getStyleClass()).append(" ").append(sortedClass).append(" resultTableCell\" style=\"width: ").append(column.getWidth()).append("%\">");

                if (values[j].getType() == HtmlDataValue.TYPE_VALUE) {
                    if (!row.isExpanded()) {
                        result.append(values[j].getValue());
                    }
                }
                if (values[j].getType() == HtmlDataValue.TYPE_VALUE_AND_UNIT) {

                    if (!row.isExpanded()) {
                        result.append("<table width=\"100%\" cellspacing=\"0\" cellpadding=\"0\">\n");
                        result.append("<tr>");
                        result.append("<td style=\"text-align: right;\">");
                        result.append(values[j].getValue());
                        result.append("<td style=\"text-align: left; padding-left: 4px; width: 5px;\">");
                        result.append(values[j].getUnit());
                        result.append("</td>");
                        result.append("</tr>");
                        result.append("</table>");
                    }
                } else if (values[j].getType() == HtmlDataValue.TYPE_CHECK_BOX) {
                    //if (!row.isExpanded()) {
                    result.append(HtmlUtilities.getCheckBoxHtml(null, values[j].getTargetInputName(), String.valueOf(values[j].getTargetInputValue()), values[j].getStyleClass(), null, values[j].getValueBoolean(), values[j].getToolTip()));
                    //}
                } else if (values[j].getType() == HtmlDataValue.TYPE_CHECK_LINK) {
                    //if (!row.isExpanded()) {
                    result.append(HtmlUtilities.getCheckLinkHtml(null, values[j].getValue(), values[j].getTargetInputName(), String.valueOf(values[j].getTargetInputValue()), values[j].getStyleClass(), values[j].getToolTip()));
                    //}
                } else if (values[j].getType() == HtmlDataValue.TYPE_PROGRESS) {
                    if (!row.isExpanded()) {
                        result.append(HtmlUtilities.getProgressBarHtml(values[j].getValueDouble()));
                    }
                }

                result.append("</td>");

                columnIndex = columnIndex + 1;
            }

            result.append("</tr>");

            if (row.getHiddenContentHtml() != null) {
                result.append("<tr id=\"").append(this.id).append("_").append(i).append("\" style=\"display: ;\">");
                result.append("<td class=\"boxContent\" colspan=\"").append(values.length).append("\">");
                result.append(row.getHiddenContentHtml());
                result.append("</td>");
                result.append("</tr>");
                result.append("<tr>");
                result.append("<td class=\"boxContentSepartor\" colspan=\"").append(values.length).append("\">");

                result.append("</td>");
                result.append("</tr>");

            }
        }

        result.append("</table>");

        return result.toString();
    }

    public void writeHtml(PrintWriter printWriter) {

        printWriter.write(this.getHtmlCode());


    }
}
