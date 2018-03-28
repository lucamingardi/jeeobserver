package action;

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
import jeeobserver.ActionObserver;
import jeeobserver.server.ActionParameters;
import jeeobserver.server.ActionParameters.DeleteStatisticsParameter;
import jeeobserver.server.ActionParameters.SearchElementsParameter;
import jeeobserver.server.ActionParameters.SearchStatisticsParameter;
import jeeobserver.server.ActionStatistics;
import jeeobserver.server.ActionStatistics.Element;
import jeeobserver.server.ActionStatistics.TotalStatistics;
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

public class ActionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String SERVLET = "action";

    public static final String COOKIE_RESULT_SELECTED_SERVLET = SERVLET + "ResultSelectedServlet";

    public static final String COOKIE_RESULT_SELECTED_JSF = SERVLET + "ResultSelectedJsf";

    public static final String COOKIE_RESULT_SELECTED_EJB = SERVLET + "ResultSelectedEjb";

    public static final String COOKIE_RESULT_SELECTED_JAXWS = SERVLET + "ResultSelectedJaxWs";

    public static final String COOKIE_RESULT_SELECTED_JDBC = SERVLET + "ResultSelectedJdbc";

    public static final String COOKIE_RESULT_SELECTED_CUSTOM = SERVLET + "ResultSelectedCustom";

    public static final String COOKIE_RESULTS_EXPANDED = SERVLET + "ResultsExpanded";

    public static final String COOKIE_STATISTICS_SELECTED = SERVLET + "StatisticsSelected";

    public static final String COOKIE_RESULTS_SORT_COLUMN = SERVLET + "ResultsSortColumn";

    public static final String COOKIE_RESULTS_SORT_ORDER = SERVLET + "ResultsSortOrder";

    public static final String CHART_CPU_AVERAGE = "cpuAverage";

    public static final String CHART_CPU_TOTAL = "cpuTotal";

    public static final String CHART_TIME_AVERAGE = "timeAverage";

    public static final String CHART_TIME_TOTAL = "timeTotal";

    public static final String CHART_EXECUTIONS = "executions";

    public static final String CHART_EXCEPTIONS = "exceptions";

    public static final String CHART_USERS = "users";

    public static final Color CHART_COLOR_CPU_AVERAGE = CommonServlet.CHART_COLOR_GREEN_2;

    public static final Color CHART_COLOR_CPU_TOTAL = CommonServlet.CHART_COLOR_GREEN_1;

    public static final Color CHART_COLOR_TIME_AVERAGE = CommonServlet.CHART_COLOR_ORANGE_2;

    public static final Color CHART_COLOR_TIME_TOTAL = CommonServlet.CHART_COLOR_ORANGE_1;

    public static final Color CHART_COLOR_EXECUTIONS = CommonServlet.CHART_COLOR_BLUE_1;

    public static final Color CHART_COLOR_EXCEPTIONS = CommonServlet.CHART_COLOR_RED_1;

    public static final Color CHART_COLOR_USERS = CommonServlet.CHART_COLOR_PURPLE_3;

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

        sessionBean.setCurrentServlet(ActionServlet.SERVLET);

        sessionBean.setResearchCategory(ApplicationUtilities.getParameterInteger("category", request));
        if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JDBC) {
            request.setAttribute("title", ApplicationUtilities.getMessage("page_action_jdbc_WindowTitle"));
            request.setAttribute("subTitle", ApplicationUtilities.getMessage("page_action_jdbc_WindowSubTitle"));
        } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JSF) {
            request.setAttribute("title", ApplicationUtilities.getMessage("page_action_jsf_WindowTitle"));
            request.setAttribute("subTitle", ApplicationUtilities.getMessage("page_action_jsf_WindowSubTitle"));
        } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_SERVLET) {
            request.setAttribute("title", ApplicationUtilities.getMessage("page_action_servlet_WindowTitle"));
            request.setAttribute("subTitle", ApplicationUtilities.getMessage("page_action_servlet_WindowSubTitle"));
        } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_EJB) {
            request.setAttribute("title", ApplicationUtilities.getMessage("page_action_ejb_WindowTitle"));
            request.setAttribute("subTitle", ApplicationUtilities.getMessage("page_action_ejb_WindowSubTitle"));
        } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JAXWS) {
            request.setAttribute("title", ApplicationUtilities.getMessage("page_action_jaxws_WindowTitle"));
            request.setAttribute("subTitle", ApplicationUtilities.getMessage("page_action_jaxws_WindowSubTitle"));
        } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_CUSTOM) {
            request.setAttribute("title", ApplicationUtilities.getMessage("page_action_custom_WindowTitle"));
            request.setAttribute("subTitle", ApplicationUtilities.getMessage("page_action_custom_WindowSubTitle"));
        }


        //Statistics colors

        sessionBean.setActionStatisticsColors(new HashMap<String, Color>());

        sessionBean.getActionStatisticsColors().put(CHART_CPU_AVERAGE, CHART_COLOR_CPU_AVERAGE);
        sessionBean.getActionStatisticsColors().put(CHART_CPU_TOTAL, CHART_COLOR_CPU_TOTAL);

        sessionBean.getActionStatisticsColors().put(CHART_TIME_AVERAGE, CHART_COLOR_TIME_AVERAGE);
        sessionBean.getActionStatisticsColors().put(CHART_TIME_TOTAL, CHART_COLOR_TIME_TOTAL);

        sessionBean.getActionStatisticsColors().put(CHART_EXECUTIONS, CHART_COLOR_EXECUTIONS);
        sessionBean.getActionStatisticsColors().put(CHART_EXCEPTIONS, CHART_COLOR_EXCEPTIONS);

        sessionBean.getActionStatisticsColors().put(CHART_USERS, CHART_COLOR_USERS);


        String chartStatistics = ApplicationUtilities.getParameterString("chart", request);
        if (chartStatistics != null) {
            sessionBean.setActionStatisticsSelected(new HashSet(Arrays.asList(chartStatistics.split("_"))));
        }

        if (sessionBean.getActionStatisticsSelected().isEmpty()) {
            sessionBean.getActionStatisticsSelected().add(CHART_CPU_AVERAGE);
            sessionBean.getActionStatisticsSelected().add(CHART_TIME_AVERAGE);
        }

        Integer selectedProject = ApplicationUtilities.getParameterInteger("selected", request);
        if (selectedProject != null) {


            if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JDBC) {
                sessionBean.setActionJdbcResultSelected(selectedProject);
            } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JSF) {
                sessionBean.setActionJsfResultSelected(selectedProject);
            } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_SERVLET) {
                sessionBean.setActionServletResultSelected(selectedProject);
            } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_EJB) {
                sessionBean.setActionEjbResultSelected(selectedProject);
            } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JAXWS) {
                sessionBean.setActionJaxWsResultSelected(selectedProject);
            } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_CUSTOM) {
                sessionBean.setActionCustomResultSelected(selectedProject);
            }
        }


        searchTotalStatistics(request);
        searchTimeStatistics(request);

        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/secure/action/action.jsp");
        dispatcher.forward(request, response);
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getUserPrincipal() != null && !request.isUserInRole("admin")) {
            return;
        }

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        try {
            JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

            client.deleteActionStatistics(new DeleteStatisticsParameter(sessionBean.getResearchCategory(), ApplicationUtilities.getRegexPattern(sessionBean.getResearchContext(), false), ApplicationUtilities.getRegexPattern(sessionBean.getResearchProject(), false), ApplicationUtilities.getRegexPattern(sessionBean.getResearchElement(), sessionBean.isResearchElementRegex()), sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()));

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
            sessionBean.setResearchContextProjects(new TreeMap<String, Set<String>>());

            Collection<Element> elements = client.searchActionElements(new SearchElementsParameter(sessionBean.getResearchCategory(), null));

            boolean contextFound = false;
            boolean projectFound = false;

            for (ActionStatistics.Element element : elements) {

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
                    sessionBean.setResearchGrouping(RequestParameter.GROUP_BY_CONTEXT_PROJECT);
                }
                sessionBean.setMenuSearchEnabled(true);
            } else {
                sessionBean.setMenuSearchEnabled(false);
            }



            //Total statistics
            sessionBean.setActionStatisticsTotal(client.searchActionTotalStatistics(new SearchStatisticsParameter(sessionBean.getResearchCategory(), ApplicationUtilities.getRegexPattern(sessionBean.getResearchContext(), false), ApplicationUtilities.getRegexPattern(sessionBean.getResearchProject(), false), ApplicationUtilities.getRegexPattern(sessionBean.getResearchElement(), sessionBean.isResearchElementRegex()), sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo(), sessionBean.getResearchGrouping(), new TimePeriod(sessionBean.getResearchSamplingPeriod()), true)));

            Integer resultSelected = 0;
            if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_SERVLET) {
                resultSelected = sessionBean.getActionServletResultSelected();
            } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JSF) {
                resultSelected = sessionBean.getActionJsfResultSelected();
            } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_EJB) {
                resultSelected = sessionBean.getActionEjbResultSelected();
            } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JAXWS) {
                resultSelected = sessionBean.getActionJaxWsResultSelected();
            } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JDBC) {
                resultSelected = sessionBean.getActionJdbcResultSelected();
            } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_CUSTOM) {
                resultSelected = sessionBean.getActionCustomResultSelected();
            }

            boolean projectSelectedFound = false;
            for (TotalStatistics statistics : sessionBean.getActionStatisticsTotal()) {
                if (resultSelected != null && statistics.getId() == resultSelected) {
                    projectSelectedFound = true;
                }
            }

            if ((!projectSelectedFound || resultSelected == null) && !sessionBean.getActionStatisticsTotal().isEmpty()) {
                if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_SERVLET) {
                    sessionBean.setActionServletResultSelected(sessionBean.getActionStatisticsTotal().iterator().next().getId());
                } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JSF) {
                    sessionBean.setActionJsfResultSelected(sessionBean.getActionStatisticsTotal().iterator().next().getId());
                } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_EJB) {
                    sessionBean.setActionEjbResultSelected(sessionBean.getActionStatisticsTotal().iterator().next().getId());
                } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JAXWS) {
                    sessionBean.setActionJaxWsResultSelected(sessionBean.getActionStatisticsTotal().iterator().next().getId());
                } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JDBC) {
                    sessionBean.setActionJdbcResultSelected(sessionBean.getActionStatisticsTotal().iterator().next().getId());
                } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_CUSTOM) {
                    sessionBean.setActionCustomResultSelected(sessionBean.getActionStatisticsTotal().iterator().next().getId());
                }
            }

            if (sessionBean.getActionStatisticsTotal().isEmpty()) {
                sessionBean.setStatisticsExtracted(false);
            } else {
                sessionBean.setStatisticsExtracted(true);
            }

        } catch (Exception ex) {
            ApplicationUtilities.addError(request, ex.getMessage());
        }

    }

    public static void searchTimeStatistics(HttpServletRequest request) {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        try {
            JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

            String context = null;
            String project = null;
            String element = null;

            for (ActionStatistics.TotalStatistics statistics : sessionBean.getActionStatisticsTotal()) {

                int resultSelected = 0;
                if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_SERVLET) {
                    resultSelected = sessionBean.getActionServletResultSelected().intValue();
                } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JSF) {
                    resultSelected = sessionBean.getActionJsfResultSelected().intValue();
                } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_EJB) {
                    resultSelected = sessionBean.getActionEjbResultSelected().intValue();
                } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JAXWS) {
                    resultSelected = sessionBean.getActionJaxWsResultSelected().intValue();
                } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JDBC) {
                    resultSelected = sessionBean.getActionJdbcResultSelected().intValue();
                } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_CUSTOM) {
                    resultSelected = sessionBean.getActionCustomResultSelected().intValue();
                }

                if (statistics.getId() == resultSelected) {
                    context = sessionBean.getResearchContext();
                    if (sessionBean.getResearchGrouping() == RequestParameter.GROUP_BY_CONTEXT_PROJECT) {
                        project = statistics.getProject();
                        element = sessionBean.getResearchElement();
                    } else if (sessionBean.getResearchGrouping() == RequestParameter.GROUP_BY_CONTEXT_PROJECT_ELEMENT) {
                        project = statistics.getProject();
                        element = statistics.getElement();
                    }
                }
            }

            sessionBean.setActionStatisticsTime(client.searchActionTimeStatistics(new ActionParameters.SearchStatisticsParameter(sessionBean.getResearchCategory(), ApplicationUtilities.getRegexPattern(context, false), ApplicationUtilities.getRegexPattern(project, false), ApplicationUtilities.getRegexPattern(element, false), sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo(), sessionBean.getResearchGrouping(), new TimePeriod(sessionBean.getResearchSamplingPeriod()), false)));

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
        double nanosTotalMax = 0;

        for (ActionStatistics statistics : sessionBean.getActionStatisticsTime()) {
            if (sessionBean.getActionStatisticsSelected().contains(CHART_CPU_AVERAGE)) {
                nanosAverageMax = Math.max(nanosAverageMax, MathUtilities.avg(statistics.getCount(), statistics.getCpuNanosStatistics().getSum()));
            }
            if (sessionBean.getActionStatisticsSelected().contains(CHART_CPU_TOTAL)) {
                nanosTotalMax = Math.max(nanosTotalMax, statistics.getCpuNanosStatistics().getSum());
            }
            if (sessionBean.getActionStatisticsSelected().contains(CHART_TIME_AVERAGE)) {
                nanosAverageMax = Math.max(nanosAverageMax, MathUtilities.avg(statistics.getCount(), statistics.getTimeNanosStatistics().getSum()));
            }
            if (sessionBean.getActionStatisticsSelected().contains(CHART_TIME_TOTAL)) {
                nanosTotalMax = Math.max(nanosTotalMax, statistics.getTimeNanosStatistics().getSum());
            }
        }

        String nanosAverageUnit = TimeUtilities.fromNanosToUnit(nanosAverageMax);
        String nanosTotalUnit = TimeUtilities.fromNanosToUnit(nanosTotalMax);


        //Count how many statistics to show
        Map<String, ChartPlotArea> plots = new HashMap();

        plots.put("nanosAverage", new ChartPlotArea(ApplicationUtilities.decimalFormatter.toPattern() + " " + nanosAverageUnit, null, new Date[]{sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()}));
        plots.put("nanosTotal", new ChartPlotArea(ApplicationUtilities.decimalFormatter.toPattern() + " " + nanosTotalUnit, null, new Date[]{sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()}));
        plots.put("executions", new ChartPlotArea(ApplicationUtilities.integerFormatter.toPattern(), null, new Date[]{sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()}));
        plots.put("users", new ChartPlotArea(ApplicationUtilities.integerFormatter.toPattern(), null, new Date[]{sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()}));


        if (sessionBean.getActionStatisticsSelected().contains(CHART_CPU_AVERAGE)) {
            plots.get("nanosAverage").addColum(ApplicationUtilities.getMessage("label_AverageCPU"), sessionBean.getActionStatisticsColors().get(CHART_CPU_AVERAGE));
        }
        if (sessionBean.getActionStatisticsSelected().contains(CHART_CPU_TOTAL)) {
            plots.get("nanosTotal").addColum(ApplicationUtilities.getMessage("label_TotalCPU"), sessionBean.getActionStatisticsColors().get(CHART_CPU_TOTAL));
        }
        if (sessionBean.getActionStatisticsSelected().contains(CHART_TIME_AVERAGE)) {
            plots.get("nanosAverage").addColum(ApplicationUtilities.getMessage("label_AverageTime"), sessionBean.getActionStatisticsColors().get(CHART_TIME_AVERAGE));
        }
        if (sessionBean.getActionStatisticsSelected().contains(CHART_TIME_TOTAL)) {
            plots.get("nanosTotal").addColum(ApplicationUtilities.getMessage("label_TotalTime"), sessionBean.getActionStatisticsColors().get(CHART_TIME_TOTAL));
        }
        if (sessionBean.getActionStatisticsSelected().contains(CHART_EXECUTIONS)) {
            plots.get("executions").addColum(ApplicationUtilities.getMessage("label_Invocations"), sessionBean.getActionStatisticsColors().get(CHART_EXECUTIONS));
        }
        if (sessionBean.getActionStatisticsSelected().contains(CHART_EXCEPTIONS)) {
            plots.get("executions").addColum(ApplicationUtilities.getMessage("label_Exceptions"), sessionBean.getActionStatisticsColors().get(CHART_EXCEPTIONS));
        }

        if (sessionBean.getActionStatisticsSelected().contains(CHART_USERS)) {
            plots.get("users").addColum(ApplicationUtilities.getMessage("label_Users"), sessionBean.getActionStatisticsColors().get(CHART_USERS));
        }


        for (ActionStatistics statistics : sessionBean.getActionStatisticsTime()) {

            if (sessionBean.getActionStatisticsSelected().contains(CHART_CPU_AVERAGE)) {
                plots.get("nanosAverage").addValue(statistics.getDate(), TimeUtilities.fromNanosToValue(MathUtilities.avg(statistics.getCount(), statistics.getCpuNanosStatistics().getSum()), nanosAverageUnit));
            }
            if (sessionBean.getActionStatisticsSelected().contains(CHART_CPU_TOTAL)) {
                plots.get("nanosTotal").addValue(statistics.getDate(), TimeUtilities.fromNanosToValue(statistics.getCpuNanosStatistics().getSum(), nanosTotalUnit));
            }
            if (sessionBean.getActionStatisticsSelected().contains(CHART_TIME_AVERAGE)) {
                plots.get("nanosAverage").addValue(statistics.getDate(), TimeUtilities.fromNanosToValue(MathUtilities.avg(statistics.getCount(), statistics.getTimeNanosStatistics().getSum()), nanosAverageUnit));
            }
            if (sessionBean.getActionStatisticsSelected().contains(CHART_TIME_TOTAL)) {
                plots.get("nanosTotal").addValue(statistics.getDate(), TimeUtilities.fromNanosToValue(statistics.getTimeNanosStatistics().getSum(), nanosTotalUnit));
            }
            if (sessionBean.getActionStatisticsSelected().contains(CHART_EXECUTIONS)) {
                plots.get("executions").addValue(statistics.getDate(), statistics.getExecutionsStatistics().getSum());
            }

            if (sessionBean.getActionStatisticsSelected().contains(CHART_EXCEPTIONS)) {
                plots.get("executions").addValue(statistics.getDate(), statistics.getExceptionsStatistics().getSum());
            }

            if (sessionBean.getActionStatisticsSelected().contains(CHART_USERS)) {
                plots.get("users").addValue(statistics.getDate(), statistics.getUsersStatistics().getSum());
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

        if (sessionBean.getActionResultsSortColumn() == null) {
            sessionBean.setActionResultsSortColumn(0);
        }

        HtmlDataTable htmlDataTable = new HtmlDataTable("actionResultList", sessionBean.getActionResultsSortColumn(), sessionBean.getActionResultsSortOrder());

        htmlDataTable.addColumn(new HtmlDataColumn("", 1, "left", false));
        htmlDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_Name"), 47, "left", true));

        htmlDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_Invocations"), 5, "right", true));
        htmlDataTable.addColumn(new HtmlDataColumn(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/table/trend.png", 8, 8), 1, "right", true));
        htmlDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_Exceptions"), 5, "right", true));
        htmlDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_Users"), 10, "right", true));

        htmlDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_AverageCPU"), 10, "right", true));
        htmlDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_TotalCPU"), 5, "right", true));
        htmlDataTable.addColumn(new HtmlDataColumn(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/table/trend.png", 8, 8), 1, "right", true));

        htmlDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_AverageTime"), 10, "right", true));
        //htmlDataTable.addColumn(new HtmlDataColumn(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/table/trend.png", 8, 8), 1, "right", true));
        htmlDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_TotalTime"), 5, "right", true));
        htmlDataTable.addColumn(new HtmlDataColumn(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/table/trend.png", 8, 8), 1, "right", true));


        for (TotalStatistics statistics : sessionBean.getActionStatisticsTotal()) {

            HtmlDataValue values[] = new HtmlDataValue[12];

            boolean selected = Boolean.FALSE;


            Integer resultSelected = 0;
            if (statistics.getCategory() == ActionObserver.CATEGORY_SERVLET) {
                resultSelected = sessionBean.getActionServletResultSelected();
            } else if (statistics.getCategory() == ActionObserver.CATEGORY_JSF) {
                resultSelected = sessionBean.getActionJsfResultSelected();
            } else if (statistics.getCategory() == ActionObserver.CATEGORY_EJB) {
                resultSelected = sessionBean.getActionEjbResultSelected();
            } else if (statistics.getCategory() == ActionObserver.CATEGORY_JAXWS) {
                resultSelected = sessionBean.getActionJaxWsResultSelected();
            } else if (statistics.getCategory() == ActionObserver.CATEGORY_JDBC) {
                resultSelected = sessionBean.getActionJdbcResultSelected();
            } else if (statistics.getCategory() == ActionObserver.CATEGORY_CUSTOM) {
                resultSelected = sessionBean.getActionCustomResultSelected();
            }

            if (resultSelected.equals(statistics.getId())) {
                selected = Boolean.TRUE;
            }

            boolean expanded = Boolean.FALSE;


            if (sessionBean.getActionResultsExpanded().contains(statistics.getId())) {
                expanded = Boolean.TRUE;
            }

            values[0] = new HtmlDataValue(selected, HtmlDataValue.TYPE_CHECK_BOX, "#selectedId", statistics.getId(), "selectElement", ApplicationUtilities.getMessage("label_SelectElement"));

            String label = "";
            if (sessionBean.getResearchGrouping() == RequestParameter.GROUP_BY_CONTEXT_PROJECT) {
                label = statistics.getProject();
            } else if (sessionBean.getResearchGrouping() == RequestParameter.GROUP_BY_CONTEXT_PROJECT_ELEMENT) {
                label = statistics.getElement();
            }


            values[1] = new HtmlDataValue(label, HtmlDataValue.TYPE_CHECK_LINK, "#expandedId", statistics.getId(), "expandDetailButton", ApplicationUtilities.getMessage("label_button_ShowDetail"));

            values[2] = new HtmlDataValue(statistics.getExecutionsStatistics().getSum(), ApplicationUtilities.integerFormatter);
            //values[3] = new HtmlDataValue(statistics.getExecutionsTrend().getSumTrendIndex(), ApplicationUtilities.indexFormatter);
            values[3] = new HtmlDataValue(HtmlUtilities.getLogIndexImage(statistics.getExecutionsTrend().getSumTrendIndex(), false));

            values[4] = new HtmlDataValue(statistics.getExceptionsStatistics().getSum(), ApplicationUtilities.integerFormatter);
            values[5] = new HtmlDataValue(statistics.getUsersStatistics().getSum(), ApplicationUtilities.integerFormatter);

            values[6] = new HtmlDataValue(statistics.getCpuNanosStatistics().getSum() / statistics.getCount(), HtmlDataValue.VALUE_TYPE_TIME);
            values[7] = new HtmlDataValue(statistics.getCpuNanosStatistics().getSum(), HtmlDataValue.VALUE_TYPE_TIME);
            //values[8] = new HtmlDataValue(statistics.getCpuNanosTrend().getSumTrendIndex(), ApplicationUtilities.indexFormatter);
            values[8] = new HtmlDataValue(HtmlUtilities.getLogIndexImage(statistics.getCpuNanosTrend().getSumTrendIndex(), false));

            values[9] = new HtmlDataValue(statistics.getTimeNanosStatistics().getSum() / statistics.getCount(), HtmlDataValue.VALUE_TYPE_TIME);
            //values[10] = new HtmlDataValue(HtmlUtilities.getLogIndexImage(statistics.getTimeNanosTrend().getAverageTrendIndex(), false));
            values[10] = new HtmlDataValue(statistics.getTimeNanosStatistics().getSum(), HtmlDataValue.VALUE_TYPE_TIME);
            //values[11] = new HtmlDataValue(statistics.getTimeNanosTrend().getSumTrendIndex(), ApplicationUtilities.indexFormatter);
            values[11] = new HtmlDataValue(HtmlUtilities.getLogIndexImage(statistics.getTimeNanosTrend().getSumTrendIndex(), false));


            //values[4] = new HtmlDataValue(MathUtilities.percent(statistics.getCpuNanosStatistics().getAverage(), statistics.getCpuNanosStatistics().getAverage()), ApplicationUtilities.percentageFormatter, HtmlDataValue.TYPE_PROGRESS);

            if (expanded) {
                htmlDataTable.addValues(values, this.getDetailPopup(statistics, sessionBean.getResearchSamplingPeriod(), sessionBean.getResearchGrouping(), sessionBean.getResearchCategory()), expanded);
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

            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_AverageCPU"), "#selectedStatistics", CHART_CPU_AVERAGE, "selectStatistics", sessionBean.getActionStatisticsColors().get(CHART_CPU_AVERAGE), sessionBean.getActionStatisticsSelected().contains(CHART_CPU_AVERAGE), true));
            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_TotalCPU"), "#selectedStatistics", CHART_CPU_TOTAL, "selectStatistics", sessionBean.getActionStatisticsColors().get(CHART_CPU_TOTAL), sessionBean.getActionStatisticsSelected().contains(CHART_CPU_TOTAL), true));
            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_AverageTime"), "#selectedStatistics", CHART_TIME_AVERAGE, "selectStatistics", sessionBean.getActionStatisticsColors().get(CHART_TIME_AVERAGE), sessionBean.getActionStatisticsSelected().contains(CHART_TIME_AVERAGE), true));
            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_TotalTime"), "#selectedStatistics", CHART_TIME_TOTAL, "selectStatistics", sessionBean.getActionStatisticsColors().get(CHART_TIME_TOTAL), sessionBean.getActionStatisticsSelected().contains(CHART_TIME_TOTAL), true));

            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_Invocations"), "#selectedStatistics", CHART_EXECUTIONS, "selectStatistics", sessionBean.getActionStatisticsColors().get(CHART_EXECUTIONS), sessionBean.getActionStatisticsSelected().contains(CHART_EXECUTIONS), true));
            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_Exceptions"), "#selectedStatistics", CHART_EXCEPTIONS, "selectStatistics", sessionBean.getActionStatisticsColors().get(CHART_EXCEPTIONS), sessionBean.getActionStatisticsSelected().contains(CHART_EXCEPTIONS), true));
            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_Users"), "#selectedStatistics", CHART_USERS, "selectStatistics", sessionBean.getActionStatisticsColors().get(CHART_USERS), sessionBean.getActionStatisticsSelected().contains(CHART_USERS), true));

            htmlTableContainer.addCell(htmlTableLegend.getHtmlCode(), "top");

            htmlTableContainer.addCell(HtmlUtilities.getChartOptions(sessionBean.getResearchSamplingPeriod(), sessionBean.getSamplingPeriod().getUnit(), sessionBean.getSamplingPeriod().getUnit(), sessionBean.isResearchLogScale(), sessionBean.isResearchTrendLines()), "top right");

            out.write(htmlTableContainer.getHtmlCode());

        } finally {
            //out.close();
        }
    }

    private String getDetailPopup(TotalStatistics statistics, int period, int grouping, int category) throws ServletException, IOException {

        HtmlTable htmlTableContainer = new HtmlTable(100, new int[]{100}, "detailTable");


        boolean varianceVisisble = true;
        if (grouping == RequestParameter.GROUP_BY_CONTEXT_PROJECT) {
            //varianceVisisble = false;
        }

        HtmlDetailTable htmlTable = new HtmlDetailTable(HtmlDetailTable.TYPE_FULL, null, ApplicationUtilities.getMessage("label_CPUAndTime"), true, varianceVisisble, true, true, period);

        htmlTable.addStatistics(HtmlTable.CELL_TYPE_TIME, false, true, varianceVisisble, true, true, ApplicationUtilities.getMessage("label_Cpu") + ":", statistics.getCpuNanosStatistics(), statistics.getCpuNanosTrend(), statistics.getCount());
        htmlTable.addStatistics(HtmlTable.CELL_TYPE_TIME, false, true, varianceVisisble, true, true, ApplicationUtilities.getMessage("label_Time") + ":", statistics.getTimeNanosStatistics(), statistics.getTimeNanosTrend(), statistics.getCount());

        ChartPlotPie plotCpuTimeAverage = new ChartPlotPie();

        plotCpuTimeAverage.addValue(ApplicationUtilities.getMessage("label_CPU"), statistics.getCpuNanosStatistics().getSum() / statistics.getExecutionsStatistics().getSum(), ActionServlet.CHART_COLOR_CPU_AVERAGE);
        plotCpuTimeAverage.addValue(ApplicationUtilities.getMessage("label_Time"), statistics.getTimeNanosStatistics().getSum() / statistics.getExecutionsStatistics().getSum(), ActionServlet.CHART_COLOR_TIME_AVERAGE);

        ChartPlotPie plotCpuTimeTotal = new ChartPlotPie();

        plotCpuTimeTotal.addValue(ApplicationUtilities.getMessage("label_CPU"), statistics.getCpuNanosStatistics().getSum(), ActionServlet.CHART_COLOR_CPU_TOTAL);
        plotCpuTimeTotal.addValue(ApplicationUtilities.getMessage("label_Time"), statistics.getTimeNanosStatistics().getSum(), ActionServlet.CHART_COLOR_TIME_TOTAL);

        htmlTable.addTotalChart("actionChartCpuTimeTotal" + Math.abs(statistics.getId()), ApplicationUtilities.getMessage("label_TotalCPUTime"), plotCpuTimeTotal, false, true);


        htmlTable.addStatisticsSeparator();
        htmlTable.addStatisticsSeparator(null, ApplicationUtilities.getMessage("label_Invocations"), false, false, true, false);

        htmlTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, false, false, true, true, false, ApplicationUtilities.getMessage("label_Total") + ":", statistics.getExecutionsStatistics(), statistics.getExecutionsTrend(), statistics.getCount());
        htmlTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, false, false, true, true, false, ApplicationUtilities.getMessage("label_ExceptionsThrowed") + ":", statistics.getExceptionsStatistics(), statistics.getExceptionsTrend(), statistics.getCount());

        ChartPlotPie plotInvocations = new ChartPlotPie();

        double exceptions = statistics.getExceptionsStatistics().getSum();
        if (Double.isNaN(exceptions)) {
            exceptions = 0;
        }

        plotInvocations.addValue(ApplicationUtilities.getMessage("label_Invocations"), statistics.getExecutionsStatistics().getSum() - exceptions, ActionServlet.CHART_COLOR_EXECUTIONS);
        plotInvocations.addValue(ApplicationUtilities.getMessage("label_ExceptionTerminated"), exceptions, ActionServlet.CHART_COLOR_EXCEPTIONS);

        //htmlTable.addPieChart("actionChartInvocationTotal" + Math.abs(statistics.getName().hashCode()), ApplicationUtilities.getMessage("label_Invocations"), plotInvocations, 200, 100, false, true);


        htmlTable.addStatisticsSeparator();
        if (category == ActionObserver.CATEGORY_JDBC) {
            htmlTable.addStatisticsSeparator(null, ApplicationUtilities.getMessage("label_JDBCUsers"), false, false, true, false);
        } else if (category == ActionObserver.CATEGORY_CUSTOM) {
            htmlTable.addStatisticsSeparator(null, ApplicationUtilities.getMessage("label_Users"), false, false, true, false);
        } else {
            htmlTable.addStatisticsSeparator(null, ApplicationUtilities.getMessage("label_JAASUsers"), false, false, true, false);
        }

        htmlTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, false, false, false, true, false, ApplicationUtilities.getMessage("label_DistinctUsers") + ":", statistics.getUsersStatistics(), statistics.getUsersTrend(), statistics.getCount());

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
        result.append(ApplicationUtilities.getMessage("label_Category"));
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_Name"));
        result.append(";");

        result.append(ApplicationUtilities.getMessage("label_CPU"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Avg"));
        result.append(" (ns)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_CPU"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Tot"));
        result.append(" (ns)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_CPU"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Min"));
        result.append(" (ns)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_CPU"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Max"));
        result.append(" (ns)");
        result.append(";");


        result.append(ApplicationUtilities.getMessage("label_Time"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Avg"));
        result.append(" (ns)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_Time"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Tot"));
        result.append(" (ns)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_Time"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Min"));
        result.append(" (ns)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_Time"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Max"));
        result.append(" (ns)");
        result.append(";");

        result.append(ApplicationUtilities.getMessage("label_Executions"));
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_Exceptions"));
        result.append(";");

        result.append(ApplicationUtilities.getMessage("label_DistinctUsers"));
        result.append(";");

        result.append("\n");

        DecimalFormat numberFormat = new DecimalFormat("0");

        for (TotalStatistics statistics : sessionBean.getActionStatisticsTotal()) {
            result.append(statistics.getContext());
            result.append(";");

            if (statistics.getCategory() == ActionObserver.CATEGORY_SERVLET) {
                result.append("SERVLET");
            } else if (statistics.getCategory() == ActionObserver.CATEGORY_JSF) {
                result.append("JSF");
            } else if (statistics.getCategory() == ActionObserver.CATEGORY_EJB) {
                result.append("EJB");
            } else if (statistics.getCategory() == ActionObserver.CATEGORY_JAXWS) {
                result.append("JAXWS");
            } else if (statistics.getCategory() == ActionObserver.CATEGORY_JDBC) {
                result.append("JDBC");
            } else if (statistics.getCategory() == ActionObserver.CATEGORY_CUSTOM) {
                result.append("CUSTOM");
            }

            result.append(";");
            if (sessionBean.getResearchGrouping() == RequestParameter.GROUP_BY_CONTEXT_PROJECT) {
                result.append(statistics.getProject());
            } else if (sessionBean.getResearchGrouping() == RequestParameter.GROUP_BY_CONTEXT_PROJECT_ELEMENT) {
                result.append(statistics.getElement());
            } else {
                result.append("");
            }
            result.append(";");

            result.append(numberFormat.format(statistics.getCpuNanosStatistics().getSum() / statistics.getExecutionsStatistics().getSum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getCpuNanosStatistics().getSum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getCpuNanosStatistics().getMinimum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getCpuNanosStatistics().getMaximum()));
            result.append(";");


            result.append(numberFormat.format(statistics.getTimeNanosStatistics().getSum() / statistics.getExecutionsStatistics().getSum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getTimeNanosStatistics().getSum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getTimeNanosStatistics().getMinimum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getTimeNanosStatistics().getMaximum()));
            result.append(";");

            result.append(numberFormat.format(statistics.getExecutionsStatistics().getSum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getExceptionsStatistics().getSum()));
            result.append(";");

            result.append(numberFormat.format(statistics.getUsersStatistics().getSum()));
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
        return "Action Servlet";
    }
}
