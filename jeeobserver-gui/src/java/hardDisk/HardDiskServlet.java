package hardDisk;

import java.awt.Color;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jeeobserver.server.HardDiskParameters;
import jeeobserver.server.HardDiskParameters.DeleteStatisticsParameter;
import jeeobserver.server.HardDiskParameters.SearchElementsParameter;
import jeeobserver.server.HardDiskParameters.SearchStatisticsParameter;
import jeeobserver.server.HardDiskStatistics;
import jeeobserver.server.HardDiskStatistics.Element;
import jeeobserver.server.HardDiskStatistics.TotalStatistics;
import jeeobserver.server.JeeObserverClient;
import jeeobserver.server.JeeObserverServerException;
import jeeobserver.server.RequestParameter;
import jeeobserver.server.TimePeriod;
import jeeobserver.utilities.MathUtilities;
import jeeobserver.utilities.SizeUtilities;
import login.ApplicationSessionBean;
import utilities.ApplicationUtilities;
import utilities.CommonServlet;
import utilities.chart.ChartPlotArea;
import utilities.chart.ChartPlotPie;
import utilities.html.HtmlDataColumn;
import utilities.html.HtmlDataTable;
import utilities.html.HtmlDataValue;
import utilities.html.HtmlDetailTable;
import utilities.html.HtmlTable;
import utilities.html.HtmlUtilities;

public class HardDiskServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String SERVLET = "hardDisk";

    public static final String COOKIE_RESULT_SELECTED = SERVLET + "ResultSelected";

    public static final String COOKIE_RESULTS_EXPANDED = SERVLET + "ResultsExpanded";

    public static final String COOKIE_STATISTICS_SELECTED = SERVLET + "StatisticsSelected";

    public static final String COOKIE_RESULTS_SORT_COLUMN = SERVLET + "ResultsSortColumn";

    public static final String COOKIE_RESULTS_SORT_ORDER = SERVLET + "ResultsSortOrder";

    public static final String CHART_TOTAL = "total";

    public static final String CHART_USABLE = "usable";

    public static final String CHART_FREE = "free";

    public static final String CHART_USED = "used";

    public static final String CHART_USED_PERCENTAGE = "usedPercentage";

    public static final Color CHART_COLOR_TOTAL = CommonServlet.CHART_COLOR_ORANGE_2;

    public static final Color CHART_COLOR_USABLE = CommonServlet.CHART_COLOR_GREEN_2;

    public static final Color CHART_COLOR_FREE = CommonServlet.CHART_COLOR_GREEN_1;

    public static final Color CHART_COLOR_USED = CommonServlet.CHART_COLOR_ORANGE_1;

    public static final Color CHART_COLOR_USED_PERCENTAGE = CommonServlet.CHART_COLOR_BLUE_1;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null || action.equals("")) {
            throw new ServletException("Action is a mandatory parameter");
        } else if (action.equals("show")) {
            this.show(request, response);
        } else if (action.equals("delete")) {
            this.delete(request, response);
        } else if (action.equals("ajaxRenderBodyChartImage")) {
            this.ajaxRenderBodyChartImage(request, response);
        } else if (action.equals("ajaxRenderBodyChartLegend")) {
            this.ajaxRenderBodyChartLegend(request, response);
        } else if (action.equals("ajaxRenderBodyResultsTable")) {
            this.ajaxRenderBodyResultsTable(request, response);
        } else if (action.equals("export")) {
            this.export(request, response);
        } else {
            throw new ServletException(String.format("Action parameter '%s' unknown.", action));
        }
    }

    private void show(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        sessionBean.setCurrentServlet(HardDiskServlet.SERVLET);

        request.setAttribute("title", ApplicationUtilities.getMessage("page_hardDisk_WindowTitle"));
        request.setAttribute("subTitle", ApplicationUtilities.getMessage("page_hardDisk_WindowSubTitle"));

        //Statistics colors

        sessionBean.setHardDiskStatisticsColors(new HashMap<String, Color>());

        sessionBean.getHardDiskStatisticsColors().put(CHART_TOTAL, CHART_COLOR_TOTAL);
        sessionBean.getHardDiskStatisticsColors().put(CHART_USED, CHART_COLOR_USED);

        sessionBean.getHardDiskStatisticsColors().put(CHART_FREE, CHART_COLOR_FREE);
        sessionBean.getHardDiskStatisticsColors().put(CHART_USABLE, CHART_COLOR_USABLE);

        sessionBean.getHardDiskStatisticsColors().put(CHART_USED_PERCENTAGE, CHART_COLOR_USED_PERCENTAGE);


        String chartStatistics = ApplicationUtilities.getParameterString("chart", request);
        if (chartStatistics != null) {
            sessionBean.setHardDiskStatisticsSelected(new HashSet(Arrays.asList(chartStatistics.split("_"))));
        }

        if (sessionBean.getHardDiskStatisticsSelected().isEmpty()) {
            sessionBean.getHardDiskStatisticsSelected().add(CHART_USED_PERCENTAGE);
        }

        Integer selectedProject = ApplicationUtilities.getParameterInteger("selected", request);
        if (selectedProject != null) {
            sessionBean.setHardDiskResultSelected(selectedProject);
        }


        searchTotalStatistics(request);
        searchTimeStatistics(request);

        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/secure/hardDisk/hardDisk.jsp");
        dispatcher.forward(request, response);
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getUserPrincipal() != null && !request.isUserInRole("admin")) {
            return;
        }

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        try {
            JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

            client.deleteHardDiskStatistics(new DeleteStatisticsParameter(ApplicationUtilities.getRegexPattern(sessionBean.getResearchContext(), false), ApplicationUtilities.getRegexPattern(sessionBean.getResearchPath(), false), sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()));

        } catch (JeeObserverServerException ex) {
            ApplicationUtilities.addError(request, ex.getMessage());
            throw new ServletException(ex.getMessage());
        }
    }

    //Static methods
    public static void searchTotalStatistics(HttpServletRequest request) {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        try {
            JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

            //Load all elements for research filter menu
            sessionBean.setResearchContexts(new TreeSet<String>());
            sessionBean.setResearchContextPaths(new TreeMap<String, Set<String>>());

            Collection<Element> elements = client.searchHardDiskElements(new SearchElementsParameter(null, null));

            boolean contextFound = false;
            boolean pathFound = false;

            for (Element element : elements) {

                if (element.getContext().equals(sessionBean.getResearchContext())) {
                    contextFound = true;
                }

                sessionBean.getResearchContexts().add(element.getContext());
                if (sessionBean.getResearchContextPaths().get(element.getContext()) == null) {
                    sessionBean.getResearchContextPaths().put(element.getContext(), new TreeSet<String>());
                }

                sessionBean.getResearchContextPaths().get(element.getContext()).add(element.getPath());

                if (element.getContext().equals(sessionBean.getResearchContext()) && element.getPath().equals(sessionBean.getResearchPath())) {
                    pathFound = true;
                }
            }

            if (!elements.isEmpty()) {
                if (!contextFound) {
                    sessionBean.setResearchContext(sessionBean.getResearchContexts().iterator().next());
                }
                if (!pathFound) {
                    sessionBean.setResearchPath(null);
                }
                sessionBean.setMenuSearchEnabled(true);
            } else {
                sessionBean.setMenuSearchEnabled(false);
            }

            //Total statistics
            sessionBean.setHardDiskStatisticsTotal(client.searchHardDiskTotalStatistics(new SearchStatisticsParameter(ApplicationUtilities.getRegexPattern(sessionBean.getResearchContext(), false), ApplicationUtilities.getRegexPattern(sessionBean.getResearchPath(), false), sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo(), RequestParameter.GROUP_BY_CONTEXT_PATH, new TimePeriod(sessionBean.getResearchSamplingPeriod()), true)));

            boolean selectedFound = false;
            for (TotalStatistics statistics : sessionBean.getHardDiskStatisticsTotal()) {
                if (sessionBean.getHardDiskResultSelected() != null && statistics.getId() == sessionBean.getHardDiskResultSelected()) {
                    selectedFound = true;
                }
            }

            if ((!selectedFound || sessionBean.getHardDiskResultSelected() == null) && !sessionBean.getHardDiskStatisticsTotal().isEmpty()) {
                sessionBean.setHardDiskResultSelected(sessionBean.getHardDiskStatisticsTotal().iterator().next().getId());
            }

            if (sessionBean.getHardDiskStatisticsTotal().isEmpty()) {
                sessionBean.setStatisticsExtracted(false);
            } else {
                sessionBean.setStatisticsExtracted(true);
            }

        } catch (JeeObserverServerException ex) {
            ApplicationUtilities.addError(request, ex.getMessage());
        }
    }

    public static void searchTimeStatistics(HttpServletRequest request) {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        try {
            JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

            String context = null;
            String path = null;

            for (HardDiskStatistics.TotalStatistics statistics : sessionBean.getHardDiskStatisticsTotal()) {
                if (statistics.getId() == sessionBean.getHardDiskResultSelected().intValue()) {
                    context = statistics.getContext();
                    path = statistics.getPath();
                }
            }

            sessionBean.setHardDiskStatisticsTime(client.searchHardDiskTimeStatistics(new HardDiskParameters.SearchStatisticsParameter(ApplicationUtilities.getRegexPattern(context, false), ApplicationUtilities.getRegexPattern(path, false), sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo(), RequestParameter.GROUP_BY_CONTEXT_PATH, new TimePeriod(sessionBean.getResearchSamplingPeriod()), false)));

        } catch (JeeObserverServerException ex) {
            ApplicationUtilities.addError(request, ex.getMessage());
        }
    }

    //Ajax Renders
    private void ajaxRenderBodyChartImage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        //Find correct scale
        double bytesMax = 0;

        for (HardDiskStatistics statistics : sessionBean.getHardDiskStatisticsTime()) {
            if (sessionBean.getHardDiskStatisticsSelected().contains(CHART_TOTAL)) {
                bytesMax = Math.max(bytesMax, MathUtilities.avg(statistics.getCount(), statistics.getTotalBytesStatistics().getSum()));
            }
            if (sessionBean.getHardDiskStatisticsSelected().contains(CHART_USABLE)) {
                bytesMax = Math.max(bytesMax, MathUtilities.avg(statistics.getCount(), statistics.getUsableBytesStatistics().getSum()));
            }
            if (sessionBean.getHardDiskStatisticsSelected().contains(CHART_FREE)) {
                bytesMax = Math.max(bytesMax, MathUtilities.avg(statistics.getCount(), statistics.getFreeBytesStatistics().getSum()));
            }
            if (sessionBean.getHardDiskStatisticsSelected().contains(CHART_USED)) {
                bytesMax = Math.max(bytesMax, MathUtilities.avg(statistics.getCount(), statistics.getUsedBytesStatistics().getSum()));
            }
        }

        String bytesUnit = SizeUtilities.fromBytesToUnit(bytesMax);

        //Count how many statistics to show
        Map<String, ChartPlotArea> plots = new HashMap();

        plots.put("bytes", new ChartPlotArea(ApplicationUtilities.integerFormatter.toPattern() + " " + bytesUnit, null, new Date[]{sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()}));
        plots.put("percentage", new ChartPlotArea(ApplicationUtilities.percentageFormatter.toPattern(), new double[]{0, 1}, new Date[]{sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()}));

        if (sessionBean.getHardDiskStatisticsSelected().contains(CHART_TOTAL)) {
            plots.get("bytes").addColum(ApplicationUtilities.getMessage("label_TotalSpace"), sessionBean.getHardDiskStatisticsColors().get(CHART_TOTAL));
        }
        if (sessionBean.getHardDiskStatisticsSelected().contains(CHART_USABLE)) {
            plots.get("bytes").addColum(ApplicationUtilities.getMessage("label_UsableSpace"), sessionBean.getHardDiskStatisticsColors().get(CHART_USABLE));
        }
        if (sessionBean.getHardDiskStatisticsSelected().contains(CHART_FREE)) {
            plots.get("bytes").addColum(ApplicationUtilities.getMessage("label_FreeSpace"), sessionBean.getHardDiskStatisticsColors().get(CHART_FREE));
        }
        if (sessionBean.getHardDiskStatisticsSelected().contains(CHART_USED)) {
            plots.get("bytes").addColum(ApplicationUtilities.getMessage("label_UsedSpace"), sessionBean.getHardDiskStatisticsColors().get(CHART_USED));
        }
        if (sessionBean.getHardDiskStatisticsSelected().contains(CHART_USED_PERCENTAGE)) {
            plots.get("percentage").addColum(ApplicationUtilities.getMessage("label_UsedPercentage"), sessionBean.getHardDiskStatisticsColors().get(CHART_USED_PERCENTAGE));
        }

        for (HardDiskStatistics statistics : sessionBean.getHardDiskStatisticsTime()) {

            if (sessionBean.getHardDiskStatisticsSelected().contains(CHART_TOTAL)) {
                plots.get("bytes").addValue(statistics.getDate(), SizeUtilities.fromBytesToValue(MathUtilities.avg(statistics.getCount(), statistics.getTotalBytesStatistics().getSum()), bytesUnit));
            }
            if (sessionBean.getHardDiskStatisticsSelected().contains(CHART_USABLE)) {
                plots.get("bytes").addValue(statistics.getDate(), SizeUtilities.fromBytesToValue(MathUtilities.avg(statistics.getCount(), statistics.getUsableBytesStatistics().getSum()), bytesUnit));
            }
            if (sessionBean.getHardDiskStatisticsSelected().contains(CHART_FREE)) {
                plots.get("bytes").addValue(statistics.getDate(), SizeUtilities.fromBytesToValue(MathUtilities.avg(statistics.getCount(), statistics.getFreeBytesStatistics().getSum()), bytesUnit));
            }
            if (sessionBean.getHardDiskStatisticsSelected().contains(CHART_USED)) {
                plots.get("bytes").addValue(statistics.getDate(), SizeUtilities.fromBytesToValue(MathUtilities.avg(statistics.getCount(), statistics.getUsedBytesStatistics().getSum()), bytesUnit));
            }
            if (sessionBean.getHardDiskStatisticsSelected().contains(CHART_USED_PERCENTAGE)) {
                plots.get("percentage").addValue(statistics.getDate(), MathUtilities.percent(MathUtilities.avg(statistics.getCount(), statistics.getUsedBytesStatistics().getSum()), MathUtilities.avg(statistics.getCount(), statistics.getTotalBytesStatistics().getSum())));
            }
        }

        String result = HtmlUtilities.getTimeChart(plots, sessionBean.isResearchLogScale(), sessionBean.isResearchTrendLines());

        PrintWriter out = response.getWriter();
        try {
            out.write(result);
        } finally {
            //out.close();
        }
    }

    private void ajaxRenderBodyResultsTable(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        if (sessionBean.getHardDiskResultsSortColumn() == null) {
            sessionBean.setHardDiskResultsSortColumn(0);
        }

        HtmlDataTable htmlDataTable = new HtmlDataTable("hardDiskResultList", sessionBean.getHardDiskResultsSortColumn(), sessionBean.getHardDiskResultsSortOrder());

        htmlDataTable.addColumn(new HtmlDataColumn("", 1, "left", false));
        htmlDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_Path"), 73, "left", true));

        //htmlDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_FreeSpace"), 12, "right", true));
        //htmlDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_UsableSpace"), 12, "right", true));

        htmlDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_TotalSpace"), 10, "right", true));
        htmlDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_UsedSpace"), 10, "right", true));

        htmlDataTable.addColumn(new HtmlDataColumn("%", 5, "left", true));
        htmlDataTable.addColumn(new HtmlDataColumn(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/table/trend.png", 8, 8), 1, "right", true));


        //

        for (TotalStatistics statistics : sessionBean.getHardDiskStatisticsTotal()) {

            HtmlDataValue values[] = new HtmlDataValue[6];

            boolean selected = Boolean.FALSE;

            if (sessionBean.getHardDiskResultSelected().equals(statistics.getId())) {
                selected = Boolean.TRUE;
            }

            boolean expanded = Boolean.FALSE;

            if (sessionBean.getHardDiskResultsExpanded().contains(statistics.getId())) {
                expanded = Boolean.TRUE;
            }

            values[0] = new HtmlDataValue(selected, HtmlDataValue.TYPE_CHECK_BOX, "#selectedId", statistics.getId(), "selectElement", ApplicationUtilities.getMessage("label_SelectElement"));

            values[1] = new HtmlDataValue(statistics.getPath(), HtmlDataValue.TYPE_CHECK_LINK, "#expandedId", statistics.getId(), "expandDetailButton", ApplicationUtilities.getMessage("label_button_ShowDetail"));

            values[2] = new HtmlDataValue(statistics.getTotalBytesTrend().getAverageLastDetected(), HtmlDataValue.VALUE_TYPE_SIZE);

            values[3] = new HtmlDataValue(statistics.getUsedBytesTrend().getAverageLastDetected(), HtmlDataValue.VALUE_TYPE_SIZE);

            values[4] = new HtmlDataValue(MathUtilities.percent(statistics.getUsedBytesTrend().getAverageLastDetected(), statistics.getTotalBytesTrend().getAverageLastDetected()), ApplicationUtilities.percentageFormatter, HtmlDataValue.TYPE_PROGRESS);

            values[5] = new HtmlDataValue(HtmlUtilities.getLogIndexImage(statistics.getUsedBytesTrend().getAverageTrendIndex(), false));

            if (expanded) {
                htmlDataTable.addValues(values, this.getDetailPopup(statistics, sessionBean.getResearchSamplingPeriod()), expanded);
            } else {
                htmlDataTable.addValues(values);
            }
        }
        PrintWriter out = response.getWriter();
        try {
            htmlDataTable.writeHtml(out);
        } finally {
            //out.close();
        }
    }

    private void ajaxRenderBodyChartLegend(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        PrintWriter out = response.getWriter();

        try {

            HtmlTable htmlTableContainer = new HtmlTable(100, new int[]{50, 10, 40});

            //Statistics legend check boxes
            HtmlTable htmlTableLegend = new HtmlTable(new int[]{25, 25, 25, 25});

            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_FreeSpace"), "#selectedStatistics", CHART_FREE, "selectStatistics", sessionBean.getHardDiskStatisticsColors().get(CHART_FREE), sessionBean.getHardDiskStatisticsSelected().contains(CHART_FREE), true));
            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_UsableSpace"), "#selectedStatistics", CHART_USABLE, "selectStatistics", sessionBean.getHardDiskStatisticsColors().get(CHART_USABLE), sessionBean.getHardDiskStatisticsSelected().contains(CHART_USABLE), true));
            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_UsedSpace"), "#selectedStatistics", CHART_USED, "selectStatistics", sessionBean.getHardDiskStatisticsColors().get(CHART_USED), sessionBean.getHardDiskStatisticsSelected().contains(CHART_USED), true));
            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_TotalSpace"), "#selectedStatistics", CHART_TOTAL, "selectStatistics", sessionBean.getHardDiskStatisticsColors().get(CHART_TOTAL), sessionBean.getHardDiskStatisticsSelected().contains(CHART_TOTAL), true));

            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_UsedPercentage"), "#selectedStatistics", CHART_USED_PERCENTAGE, "selectStatistics", sessionBean.getHardDiskStatisticsColors().get(CHART_USED_PERCENTAGE), sessionBean.getHardDiskStatisticsSelected().contains(CHART_USED_PERCENTAGE), true));

            htmlTableContainer.addCell(htmlTableLegend.getHtmlCode(), "top");

            htmlTableContainer.addCell(HtmlUtilities.getChartOptions(sessionBean.getResearchSamplingPeriod(), sessionBean.getSamplingPeriod().getUnit(), sessionBean.getSamplingPeriod().getUnit(), sessionBean.isResearchLogScale(), sessionBean.isResearchTrendLines()), "top right");

            out.write(htmlTableContainer.getHtmlCode());

        } finally {
            //out.close();
        }
    }

    private String getDetailPopup(TotalStatistics statistics, int period) throws ServletException, IOException {

        HtmlTable htmlTableContainer = new HtmlTable(100, new int[]{100}, "detailTable");

        //Usage table

        HtmlDetailTable htmlTable = new HtmlDetailTable(HtmlDetailTable.TYPE_FULL, null, ApplicationUtilities.getMessage("label_Utilization"), true, true, false, true, period);

        htmlTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, true, true, true, false, true, ApplicationUtilities.getMessage("label_Free") + ":", statistics.getFreeBytesStatistics(), statistics.getFreeBytesTrend(), statistics.getCount());
        htmlTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, true, true, true, false, true, ApplicationUtilities.getMessage("label_Usable") + ":", statistics.getUsableBytesStatistics(), statistics.getUsableBytesTrend(), statistics.getCount());
        htmlTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, false, true, true, false, true, ApplicationUtilities.getMessage("label_Used") + ":", statistics.getUsedBytesStatistics(), statistics.getUsedBytesTrend(), statistics.getCount());

        ChartPlotPie heapMemoryPlot = new ChartPlotPie();

        heapMemoryPlot.addValue(ApplicationUtilities.getMessage("label_Free"), statistics.getFreeBytesStatistics().getSum() / statistics.getCount(), HardDiskServlet.CHART_COLOR_FREE);
        heapMemoryPlot.addValue(ApplicationUtilities.getMessage("label_Used"), statistics.getUsedBytesStatistics().getSum() / statistics.getCount(), HardDiskServlet.CHART_COLOR_USED);

        htmlTable.addTotalChart("hardDiskUsage" + Math.abs(statistics.getId()), ApplicationUtilities.getMessage("label_Usage"), heapMemoryPlot, true, false);



        htmlTable.addStatisticsSeparator();

        htmlTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, false, true, true, false, true, ApplicationUtilities.getMessage("label_Total") + ":", statistics.getTotalBytesStatistics(), statistics.getTotalBytesTrend(), statistics.getCount());

        htmlTableContainer.addCell(htmlTable.getHtmlCode(), "detailTableContainer");

        htmlTableContainer.addSamplingCell(statistics.getDateStatistics().getMinimum(), statistics.getDateStatistics().getMaximum(), statistics.getCount(), 1);

        return htmlTableContainer.getHtmlCode();
    }

    private void export(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Pragma", "public");
        response.setHeader("Expires", "0");
        response.setHeader("Content-Description", "File Transfer");
        response.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        response.setHeader("Cache-Control: private", "false");
        response.setContentType("application/xls");
        response.addHeader("Content-disposition", "attachment; filename=\"jeeobserver_statistics.csv\"");
        response.setHeader("Content-Transfer-Encoding", "binary");

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        StringBuilder result = new StringBuilder();


        result.append(ApplicationUtilities.getMessage("label_Context"));
        result.append(";");

        result.append(ApplicationUtilities.getMessage("label_Path"));
        result.append(";");



        result.append(ApplicationUtilities.getMessage("label_FreeSpace"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Avg"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_FreeSpace"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Min"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_FreeSpace"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Max"));
        result.append(" (b)");
        result.append(";");

        result.append(ApplicationUtilities.getMessage("label_UsableSpace"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Avg"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_UsableSpace"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Min"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_UsableSpace"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Max"));
        result.append(" (b)");
        result.append(";");


        result.append(ApplicationUtilities.getMessage("label_UsedSpace"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Avg"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_UsedSpace"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Min"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_UsedSpace"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Max"));
        result.append(" (b)");
        result.append(";");

        result.append(ApplicationUtilities.getMessage("label_TotalSpace"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Avg"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_TotalSpace"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Min"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_TotalSpace"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Max"));
        result.append(" (b)");
        result.append(";");


        result.append("\n");

        DecimalFormat numberFormat = new DecimalFormat("0");

        for (HardDiskStatistics.TotalStatistics statistics : sessionBean.getHardDiskStatisticsTotal()) {
            result.append(statistics.getContext());
            result.append(";");

            result.append(statistics.getPath());
            result.append(";");

            result.append(numberFormat.format(statistics.getFreeBytesStatistics().getSum() / statistics.getCount()));
            result.append(";");
            result.append(numberFormat.format(statistics.getFreeBytesStatistics().getMinimum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getFreeBytesStatistics().getMaximum()));
            result.append(";");

            result.append(numberFormat.format(statistics.getUsableBytesStatistics().getSum() / statistics.getCount()));
            result.append(";");
            result.append(numberFormat.format(statistics.getUsableBytesStatistics().getMinimum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getUsableBytesStatistics().getMaximum()));
            result.append(";");


            result.append(numberFormat.format(statistics.getUsedBytesStatistics().getSum() / statistics.getCount()));
            result.append(";");
            result.append(numberFormat.format(statistics.getUsedBytesStatistics().getMinimum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getUsedBytesStatistics().getMaximum()));
            result.append(";");

            result.append(numberFormat.format(statistics.getTotalBytesStatistics().getSum() / statistics.getCount()));
            result.append(";");
            result.append(numberFormat.format(statistics.getTotalBytesStatistics().getMinimum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getTotalBytesStatistics().getMaximum()));
            result.append(";");



            result.append("\n");
        }


        response.getWriter().append(result.toString());

    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Hard Disk Servlet";
    }
}
