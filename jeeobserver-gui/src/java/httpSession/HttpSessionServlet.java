package httpSession;

import action.ActionServlet;
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
import jeeobserver.server.HttpSessionParameters;
import jeeobserver.server.HttpSessionParameters.DeleteStatisticsParameter;
import jeeobserver.server.HttpSessionParameters.SearchElementsParameter;
import jeeobserver.server.HttpSessionParameters.SearchStatisticsParameter;
import jeeobserver.server.HttpSessionStatistics;
import jeeobserver.server.HttpSessionStatistics.Element;
import jeeobserver.server.HttpSessionStatistics.TotalStatistics;
import jeeobserver.server.JeeObserverClient;
import jeeobserver.server.JeeObserverServerException;
import jeeobserver.server.RequestParameter;
import jeeobserver.server.TimePeriod;
import jeeobserver.utilities.MathUtilities;
import jeeobserver.utilities.TimeUtilities;
import static jvm.JvmServlet.searchTimeStatistics;
import static jvm.JvmServlet.searchTotalStatistics;
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

public class HttpSessionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String SERVLET = "httpSession";

    public static final String COOKIE_RESULT_SELECTED = SERVLET + "ResultSelected";

    public static final String COOKIE_RESULTS_EXPANDED = SERVLET + "ResultsExpanded";

    public static final String COOKIE_STATISTICS_SELECTED = SERVLET + "StatisticsSelected";

    public static final String COOKIE_RESULTS_SELECTED = SERVLET + "ResultsSelected";

    public static final String COOKIE_RESULTS_SORT_COLUMN = SERVLET + "ResultsSortColumn";

    public static final String COOKIE_RESULTS_SORT_ORDER = SERVLET + "ResultsSortOrder";

    public static final String CHART_SESSIONS_CREATED = "sessionCreated";

    public static final String CHART_SESSIONS_DESTROYED = "sessionDestroyed";

    public static final String CHART_SESSIONS_DESTROYED_TIME_AVERAGE = "sessionDestroyedTimeAverage";

    public static final Color CHART_COLOR_SESSIONS_CREATED = CommonServlet.CHART_COLOR_GREEN_1;

    public static final Color CHART_COLOR_SESSIONS_DESTROYED = CommonServlet.CHART_COLOR_ORANGE_1;

    public static final Color CHART_COLOR_SESSIONS_DESTROYED_TIME_AVERAGE = CommonServlet.CHART_COLOR_BLUE_1;

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

        sessionBean.setCurrentServlet(HttpSessionServlet.SERVLET);

        request.setAttribute("title", ApplicationUtilities.getMessage("page_httpSession_WindowTitle"));
        request.setAttribute("subTitle", ApplicationUtilities.getMessage("page_httpSession_WindowSubTitle"));

        //Statistics colors

        sessionBean.setHttpSessionStatisticsColors(new HashMap<String, Color>());

        sessionBean.getHttpSessionStatisticsColors().put(CHART_SESSIONS_CREATED, CHART_COLOR_SESSIONS_CREATED);
        sessionBean.getHttpSessionStatisticsColors().put(CHART_SESSIONS_DESTROYED, CHART_COLOR_SESSIONS_DESTROYED);

        sessionBean.getHttpSessionStatisticsColors().put(CHART_SESSIONS_DESTROYED_TIME_AVERAGE, CHART_COLOR_SESSIONS_DESTROYED_TIME_AVERAGE);

        String chartStatistics = ApplicationUtilities.getParameterString("chart", request);
        if (chartStatistics != null) {
            sessionBean.setHttpSessionStatisticsSelected(new HashSet(Arrays.asList(chartStatistics.split("_"))));
        }

        if (sessionBean.getHttpSessionStatisticsSelected().isEmpty()) {
            sessionBean.getHttpSessionStatisticsSelected().add(CHART_SESSIONS_CREATED);
        }

        Integer selectedProject = ApplicationUtilities.getParameterInteger("selected", request);
        if (selectedProject != null) {
            sessionBean.setHttpSessionResultSelected(selectedProject);
        }


        searchTotalStatistics(request);
        searchTimeStatistics(request);

        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/secure/httpSession/httpSession.jsp");
        dispatcher.forward(request, response);
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getUserPrincipal() != null  && !request.isUserInRole("admin")) {
            return;
        }

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        try {
            JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

            client.deleteHttpSessionStatistics(new DeleteStatisticsParameter(ApplicationUtilities.getRegexPattern(sessionBean.getResearchContext(), false), ApplicationUtilities.getRegexPattern(sessionBean.getResearchProject(), false), sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()));

        } catch (JeeObserverServerException ex) {
            ApplicationUtilities.addError(request, ex.getMessage());
            throw new ServletException(ex);
        }
    }

    //Static methods
    public static void searchTotalStatistics(HttpServletRequest request) {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        try {
            JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

            //Load all elements for research filter menu
            sessionBean.setResearchContexts(new TreeSet<String>());
            sessionBean.setResearchContextProjects(new TreeMap<String, Set<String>>());

            Collection<Element> elements = client.searchHttpSessionElements(new SearchElementsParameter(null, null));

            boolean contextFound = false;
            boolean projectFound = false;

            for (Element element : elements) {

                if (element.getContext().equals(sessionBean.getResearchContext())) {
                    contextFound = true;
                }

                sessionBean.getResearchContexts().add(element.getContext());
                if (sessionBean.getResearchContextProjects().get(element.getContext()) == null) {
                    sessionBean.getResearchContextProjects().put(element.getContext(), new TreeSet<String>());
                }

                sessionBean.getResearchContextProjects().get(element.getContext()).add(element.getProject());

                if (element.getContext().equals(sessionBean.getResearchContext()) && element.getProject().equals(sessionBean.getResearchProject())) {
                    projectFound = true;
                }
            }

            if (!elements.isEmpty()) {
                if (!contextFound) {
                    sessionBean.setResearchContext(sessionBean.getResearchContexts().iterator().next());
                }
                if (!projectFound) {
                    sessionBean.setResearchProject(null);
                }
                sessionBean.setMenuSearchEnabled(true);
            } else {
                sessionBean.setMenuSearchEnabled(false);
            }

            //Total statistics
            sessionBean.setHttpSessionStatisticsTotal(client.searchHttpSessionTotalStatistics(new SearchStatisticsParameter(ApplicationUtilities.getRegexPattern(sessionBean.getResearchContext(), false), ApplicationUtilities.getRegexPattern(sessionBean.getResearchProject(), false), sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo(), RequestParameter.GROUP_BY_CONTEXT_PROJECT, new TimePeriod(sessionBean.getResearchSamplingPeriod()), true)));

            boolean selectedFound = false;
            for (TotalStatistics statistics : sessionBean.getHttpSessionStatisticsTotal()) {
                if (sessionBean.getHttpSessionResultSelected() != null && statistics.getId() == sessionBean.getHttpSessionResultSelected()) {
                    selectedFound = true;
                }
            }

            if ((!selectedFound || sessionBean.getHttpSessionResultSelected() == null) && !sessionBean.getHttpSessionStatisticsTotal().isEmpty()) {
                sessionBean.setHttpSessionResultSelected(sessionBean.getHttpSessionStatisticsTotal().iterator().next().getId());
            }

            if (sessionBean.getHttpSessionStatisticsTotal().isEmpty()) {
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
            String project = null;

            for (HttpSessionStatistics.TotalStatistics statistics : sessionBean.getHttpSessionStatisticsTotal()) {

                if (statistics.getId() == sessionBean.getHttpSessionResultSelected().intValue()) {
                    context = statistics.getContext();
                    project = statistics.getProject();
                }
            }

            sessionBean.setHttpSessionStatisticsTime(client.searchHttpSessionTimeStatistics(new HttpSessionParameters.SearchStatisticsParameter(ApplicationUtilities.getRegexPattern(context, false), ApplicationUtilities.getRegexPattern(project, false), sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo(), RequestParameter.GROUP_BY_CONTEXT_PROJECT, new TimePeriod(sessionBean.getResearchSamplingPeriod()), false)));
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
        double nanosAverageMax = 0;

        for (HttpSessionStatistics statistics : sessionBean.getHttpSessionStatisticsTime()) {
            if (sessionBean.getHttpSessionStatisticsSelected().contains(CHART_SESSIONS_DESTROYED_TIME_AVERAGE)) {
                if (!Double.isNaN(statistics.getSessionsDestroyedStatistics().getSum())) {
                    nanosAverageMax = Math.max(nanosAverageMax, MathUtilities.avg(statistics.getSessionsDestroyedStatistics().getSum(), statistics.getSessionsDestroyedTimeNanosStatistics().getSum()));
                }
            }
        }

        String nanosAverageUnit = TimeUtilities.fromNanosToUnit(nanosAverageMax);


        //Count how many statistics to show
        Map<String, ChartPlotArea> plots = new HashMap<String, ChartPlotArea>();

        plots.put("sessionNumber", new ChartPlotArea(ApplicationUtilities.integerFormatter.toPattern(), null, new Date[]{sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()}));
        plots.put("sessionTime", new ChartPlotArea(ApplicationUtilities.decimalFormatter.toPattern() + " " + nanosAverageUnit, null, new Date[]{sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()}));

        if (sessionBean.getHttpSessionStatisticsSelected().contains(CHART_SESSIONS_CREATED)) {
            plots.get("sessionNumber").addColum(ApplicationUtilities.getMessage("label_CreatedSessions"), sessionBean.getHttpSessionStatisticsColors().get(CHART_SESSIONS_CREATED));
        }

        if (sessionBean.getHttpSessionStatisticsSelected().contains(CHART_SESSIONS_DESTROYED)) {
            plots.get("sessionNumber").addColum(ApplicationUtilities.getMessage("label_TerminatedSessions"), sessionBean.getHttpSessionStatisticsColors().get(CHART_SESSIONS_DESTROYED));
        }

        if (sessionBean.getHttpSessionStatisticsSelected().contains(CHART_SESSIONS_DESTROYED_TIME_AVERAGE)) {
            plots.get("sessionTime").addColum(ApplicationUtilities.getMessage("label_AverageDuration"), sessionBean.getHttpSessionStatisticsColors().get(CHART_SESSIONS_DESTROYED_TIME_AVERAGE));
        }


        for (HttpSessionStatistics statistics : sessionBean.getHttpSessionStatisticsTime()) {

            if (sessionBean.getHttpSessionStatisticsSelected().contains(CHART_SESSIONS_CREATED)) {
                plots.get("sessionNumber").addValue(statistics.getDate(), statistics.getSessionsCreatedStatistics().getSum());
            }
            if (sessionBean.getHttpSessionStatisticsSelected().contains(CHART_SESSIONS_DESTROYED)) {
                plots.get("sessionNumber").addValue(statistics.getDate(), statistics.getSessionsDestroyedStatistics().getSum());
            }
            if (sessionBean.getHttpSessionStatisticsSelected().contains(CHART_SESSIONS_DESTROYED_TIME_AVERAGE)) {
                plots.get("sessionTime").addValue(statistics.getDate(), TimeUtilities.fromNanosToValue(MathUtilities.avg(statistics.getSessionsDestroyedStatistics().getSum(), statistics.getSessionsDestroyedTimeNanosStatistics().getSum()), nanosAverageUnit));
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

        if (sessionBean.getHttpSessionResultsSortColumn() == null) {
            sessionBean.setHttpSessionResultsSortColumn(0);
        }

        HtmlDataTable htmlDataTable = new HtmlDataTable("httpSessionResultList", sessionBean.getHttpSessionResultsSortColumn(), sessionBean.getHttpSessionResultsSortOrder());

        htmlDataTable.addColumn(new HtmlDataColumn("", 1, "left", false));
        htmlDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_Project"), 51, "left", true));

        htmlDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_CreatedSessions"), 15, "right", true));
        htmlDataTable.addColumn(new HtmlDataColumn(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/table/trend.png", 8, 8), 1, "right", true));

        htmlDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_TerminatedSessions"), 15, "right", true));
        htmlDataTable.addColumn(new HtmlDataColumn(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/table/trend.png", 8, 8), 1, "right", true));

        htmlDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_AverageDuration"), 15, "right", true));
        htmlDataTable.addColumn(new HtmlDataColumn(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/table/trend.png", 8, 8), 1, "right", true));

        for (TotalStatistics statistics : sessionBean.getHttpSessionStatisticsTotal()) {

            HtmlDataValue values[] = new HtmlDataValue[8];

            Boolean selected = Boolean.FALSE;

            if (sessionBean.getHttpSessionResultSelected().equals(statistics.getId())) {
                selected = Boolean.TRUE;
            }

            boolean expanded = Boolean.FALSE;

            if (sessionBean.getHttpSessionResultsExpanded().contains(statistics.getId())) {
                expanded = Boolean.TRUE;
            }

            values[0] = new HtmlDataValue(selected, HtmlDataValue.TYPE_CHECK_BOX, "#selectedId", statistics.getId(), "selectElement", ApplicationUtilities.getMessage("label_SelectElement"));

            values[1] = new HtmlDataValue(statistics.getProject(), HtmlDataValue.TYPE_CHECK_LINK, "#expandedId", statistics.getId(), "expandDetailButton", ApplicationUtilities.getMessage("label_button_ShowDetail"));

            values[2] = new HtmlDataValue(statistics.getSessionsCreatedStatistics().getSum(), ApplicationUtilities.integerFormatter);
            //values[3] = new HtmlDataValue(statistics.getSessionsCreatedTrend().getSumTrendIndex(), ApplicationUtilities.indexFormatter);
            values[3] = new HtmlDataValue(HtmlUtilities.getLogIndexImage(statistics.getSessionsCreatedTrend().getSumTrendIndex(), false));

            values[4] = new HtmlDataValue(statistics.getSessionsDestroyedStatistics().getSum(), ApplicationUtilities.integerFormatter);
            //values[5] = new HtmlDataValue(statistics.getSessionsDestroyedTrend().getSumTrendIndex(), ApplicationUtilities.indexFormatter);
            values[5] = new HtmlDataValue(HtmlUtilities.getLogIndexImage(statistics.getSessionsDestroyedTrend().getSumTrendIndex(), false));

            values[6] = new HtmlDataValue(statistics.getSessionsDestroyedTimeNanosStatistics().getSum() / statistics.getSessionsDestroyedStatistics().getSum(), HtmlDataValue.VALUE_TYPE_TIME);
            //values[7] = new HtmlDataValue(statistics.getSessionsDestroyedTimeNanosTrend().getAverageTrendIndex(), ApplicationUtilities.indexFormatter);
            values[7] = new HtmlDataValue(HtmlUtilities.getLogIndexImage(statistics.getSessionsDestroyedTimeNanosTrend().getAverageTrendIndex(), false));

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
            HtmlTable htmlTableLegend = new HtmlTable(new int[]{50, 50});

            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_CreatedSessions"), "#selectedStatistics", CHART_SESSIONS_CREATED, "selectStatistics", sessionBean.getHttpSessionStatisticsColors().get(CHART_SESSIONS_CREATED), sessionBean.getHttpSessionStatisticsSelected().contains(CHART_SESSIONS_CREATED), true));
            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_TerminatedSessions"), "#selectedStatistics", CHART_SESSIONS_DESTROYED, "selectStatistics", sessionBean.getHttpSessionStatisticsColors().get(CHART_SESSIONS_DESTROYED), sessionBean.getHttpSessionStatisticsSelected().contains(CHART_SESSIONS_DESTROYED), true));

            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_AverageDuration"), "#selectedStatistics", CHART_SESSIONS_DESTROYED_TIME_AVERAGE, "selectStatistics", sessionBean.getHttpSessionStatisticsColors().get(CHART_SESSIONS_DESTROYED_TIME_AVERAGE), sessionBean.getHttpSessionStatisticsSelected().contains(CHART_SESSIONS_DESTROYED_TIME_AVERAGE), true));

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

        HtmlDetailTable htmlTable = new HtmlDetailTable(HtmlDetailTable.TYPE_FULL, null, ApplicationUtilities.getMessage("label_HTTPSessions"), false, false, true, false, period);

        htmlTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, false, false, true, true, false, ApplicationUtilities.getMessage("label_Created") + ":", statistics.getSessionsCreatedStatistics(), statistics.getSessionsCreatedTrend(), statistics.getCount());
        htmlTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, false, false, true, true, false, ApplicationUtilities.getMessage("label_Terminated") + ":", statistics.getSessionsDestroyedStatistics(), statistics.getSessionsDestroyedTrend(), statistics.getCount());

        ChartPlotPie plotCpuTimeTotal = new ChartPlotPie();

        plotCpuTimeTotal.addValue(ApplicationUtilities.getMessage("label_Active"), statistics.getSessionsCreatedStatistics().getSum() - statistics.getSessionsDestroyedStatistics().getSum(), ActionServlet.CHART_COLOR_CPU_TOTAL);
        plotCpuTimeTotal.addValue(ApplicationUtilities.getMessage("label_Terminated"), statistics.getSessionsDestroyedStatistics().getSum(), ActionServlet.CHART_COLOR_TIME_TOTAL);

        htmlTable.addTotalChart("actionChartCpuTimeTotal" + Math.abs(statistics.getId()), ApplicationUtilities.getMessage("label_TotalCPUTime"), plotCpuTimeTotal, false, true);


        //htmlTable.addStatisticsSeparator("Durata", null, true, true, false, true);
        htmlTable.addStatisticsSeparator(null, ApplicationUtilities.getMessage("label_Duration"), true, true, false, true);

        htmlTable.addStatistics(HtmlTable.CELL_TYPE_TIME, false, true, true, false, true, ApplicationUtilities.getMessage("label_Time") + ":", statistics.getSessionsDestroyedTimeNanosStatistics(), statistics.getSessionsDestroyedTimeNanosTrend(), (int) statistics.getSessionsDestroyedStatistics().getSum());

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

        result.append(ApplicationUtilities.getMessage("label_Created"));
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_Terminated"));
        result.append(";");

        result.append(ApplicationUtilities.getMessage("label_Duration"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Avg"));
        result.append(" (ns)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_Duration"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Min"));
        result.append(" (ns)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_Duration"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Max"));
        result.append(" (ns)");
        result.append(";");


        result.append("\n");

        DecimalFormat numberFormat = new DecimalFormat("0");

        for (HttpSessionStatistics.TotalStatistics statistics : sessionBean.getHttpSessionStatisticsTotal()) {
            result.append(statistics.getContext());
            result.append(";");

            result.append(numberFormat.format(statistics.getSessionsCreatedStatistics().getSum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getSessionsDestroyedStatistics().getSum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getSessionsDestroyedTimeNanosStatistics().getSum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getSessionsDestroyedTimeNanosStatistics().getMinimum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getSessionsDestroyedTimeNanosStatistics().getMaximum()));
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
        return "Http Session Servlet";
    }
}
