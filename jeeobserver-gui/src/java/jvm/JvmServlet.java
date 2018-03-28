package jvm;

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
import jeeobserver.server.JeeObserverClient;
import jeeobserver.server.JeeObserverServerException;
import jeeobserver.server.JvmParameters;
import jeeobserver.server.JvmParameters.DeleteStatisticsParameter;
import jeeobserver.server.JvmParameters.SearchElementsParameter;
import jeeobserver.server.JvmParameters.SearchStatisticsParameter;
import jeeobserver.server.JvmStatistics;
import jeeobserver.server.JvmStatistics.Element;
import jeeobserver.server.JvmStatistics.TotalStatistics;
import jeeobserver.server.RequestParameter;
import jeeobserver.server.TimePeriod;
import jeeobserver.utilities.MathUtilities;
import jeeobserver.utilities.SizeUtilities;
import jeeobserver.utilities.TimeUtilities;
import login.ApplicationSessionBean;
import utilities.ApplicationUtilities;
import utilities.CommonServlet;
import utilities.chart.ChartPlotArea;
import utilities.chart.ChartPlotPie;
import utilities.html.HtmlDetailTable;
import utilities.html.HtmlTable;
import utilities.html.HtmlUtilities;

public class JvmServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String SERVLET = "jvm";

    public static final String COOKIE_RESULT_SELECTED = SERVLET + "ResultSelected";

    public static final String COOKIE_RESULTS_EXPANDED = SERVLET + "ResultsExpanded";

    public static final String COOKIE_STATISTICS_SELECTED = SERVLET + "StatisticsSelected";

    public static final String COOKIE_RESULTS_SELECTED = SERVLET + "ResultsSelected";

    public static final String COOKIE_RESULTS_SORT_COLUMN = SERVLET + "ResultsSortColumn";

    public static final String COOKIE_RESULTS_SORT_ORDER = SERVLET + "ResultsSortOrder";

    
    public static final String CHART_HEAP_MEMORY_USED = "heapMemoryUsed";

    public static final String CHART_HEAP_MEMORY_COMMITTED = "heapMemoryCommitted";

    public static final String CHART_HEAP_MEMORY_MAX = "heapMemoryMax";

    public static final String CHART_NON_HEAP_MEMORY_USED = "nonHeapMemoryUsed";

    public static final String CHART_NON_HEAP_MEMORY_COMMITTED = "nonHeapMemoryCommitted";

    public static final String CHART_NON_HEAP_MEMORY_MAX = "nonHeapMemoryMax";

    public static final String CHART_CPU_PERCENTAGE = "cpuPercentage";

    public static final String CHART_THREADS = "threads";

    public static final String CHART_GARBAGE_COLLECTIONS = "garbageCollections";

    public static final String CHART_GARBAGE_COLLECTIONS_TIME = "garbageCollectionsTime";

    public static final String CHART_LOADED_CLASSES = "loadedClasses";
    
    
    public static final Color CHART_COLOR_HEAP_MEMORY_USED = CommonServlet.CHART_COLOR_ORANGE_1;

    public static final Color CHART_COLOR_HEAP_MEMORY_COMMITTED = CommonServlet.CHART_COLOR_ORANGE_2;

    public static final Color CHART_COLOR_HEAP_MEMORY_MAX = CommonServlet.CHART_COLOR_ORANGE_3;

    public static final Color CHART_COLOR_NON_HEAP_MEMORY_USED = CommonServlet.CHART_COLOR_GREEN_1;

    public static final Color CHART_COLOR_NON_HEAP_MEMORY_COMMITTED = CommonServlet.CHART_COLOR_GREEN_2;

    public static final Color CHART_COLOR_NON_HEAP_MEMORY_MAX = CommonServlet.CHART_COLOR_GREEN_3;

    public static final Color CHART_COLOR_CPU_PERCENTAGE = CommonServlet.CHART_COLOR_BLUE_1;

    public static final Color CHART_COLOR_THREADS = CommonServlet.CHART_COLOR_RED_1;

    public static final Color CHART_COLOR_GARBAGE_COLLECTIONS = CommonServlet.CHART_COLOR_PURPLE_1;

    public static final Color CHART_COLOR_GARBAGE_COLLECTIONS_TIME = CommonServlet.CHART_COLOR_PURPLE_2;

    public static final Color CHART_COLOR_LOADED_CLASSES = CommonServlet.CHART_COLOR_RED_2;

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

        sessionBean.setCurrentServlet(JvmServlet.SERVLET);

        request.setAttribute("title", ApplicationUtilities.getMessage("page_jvm_WindowTitle"));
        request.setAttribute("subTitle", ApplicationUtilities.getMessage("page_jvm_WindowSubTitle"));

        //Statistics colors

        sessionBean.setJvmStatisticsColors(new HashMap<String, Color>());

        sessionBean.getJvmStatisticsColors().put(CHART_HEAP_MEMORY_USED, CHART_COLOR_HEAP_MEMORY_USED);
        sessionBean.getJvmStatisticsColors().put(CHART_HEAP_MEMORY_COMMITTED, CHART_COLOR_HEAP_MEMORY_COMMITTED);
        sessionBean.getJvmStatisticsColors().put(CHART_HEAP_MEMORY_MAX, CHART_COLOR_HEAP_MEMORY_MAX);

        sessionBean.getJvmStatisticsColors().put(CHART_NON_HEAP_MEMORY_USED, CHART_COLOR_NON_HEAP_MEMORY_USED);
        sessionBean.getJvmStatisticsColors().put(CHART_NON_HEAP_MEMORY_COMMITTED, CHART_COLOR_NON_HEAP_MEMORY_COMMITTED);
        sessionBean.getJvmStatisticsColors().put(CHART_NON_HEAP_MEMORY_MAX, CHART_COLOR_NON_HEAP_MEMORY_MAX);

        sessionBean.getJvmStatisticsColors().put(CHART_CPU_PERCENTAGE, CHART_COLOR_CPU_PERCENTAGE);
        sessionBean.getJvmStatisticsColors().put(CHART_THREADS, CHART_COLOR_THREADS);

        sessionBean.getJvmStatisticsColors().put(CHART_GARBAGE_COLLECTIONS, CHART_COLOR_GARBAGE_COLLECTIONS);
        sessionBean.getJvmStatisticsColors().put(CHART_GARBAGE_COLLECTIONS_TIME, CHART_COLOR_GARBAGE_COLLECTIONS_TIME);

        sessionBean.getJvmStatisticsColors().put(CHART_LOADED_CLASSES, CHART_COLOR_LOADED_CLASSES);

        String chartStatistics = ApplicationUtilities.getParameterString("chart", request);
        if (chartStatistics != null) {
            sessionBean.setJvmStatisticsSelected(new HashSet(Arrays.asList(chartStatistics.split("_"))));
        }
        
        if (sessionBean.getJvmStatisticsSelected().isEmpty()) {
            sessionBean.getJvmStatisticsSelected().add(CHART_HEAP_MEMORY_USED);
            sessionBean.getJvmStatisticsSelected().add(CHART_HEAP_MEMORY_COMMITTED);
        }

        searchTotalStatistics(request);
        searchTimeStatistics(request);


        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/secure/jvm/jvm.jsp");
        dispatcher.forward(request, response);
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (request.getUserPrincipal() != null && !request.isUserInRole("admin")) {
            return;
        }

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        try {
            JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

            client.deleteJvmStatistics(new DeleteStatisticsParameter(ApplicationUtilities.getRegexPattern(sessionBean.getResearchContext(), false), sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()));

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

            Collection<Element> elements = client.searchJvmElements(new SearchElementsParameter());

            boolean contextFound = false;

            for (Element element : elements) {

                if (element.getContext().equals(sessionBean.getResearchContext())) {
                    contextFound = true;
                }

                sessionBean.getResearchContexts().add(element.getContext());
            }

            if (!elements.isEmpty()) {
                if (!contextFound) {
                    sessionBean.setResearchContext(sessionBean.getResearchContexts().iterator().next());
                }
                sessionBean.setMenuSearchEnabled(true);
            } else {
                sessionBean.setMenuSearchEnabled(false);
            }

            //Total statistics
            sessionBean.setJvmStatisticsTotal(client.searchJvmTotalStatistics(new SearchStatisticsParameter(ApplicationUtilities.getRegexPattern(sessionBean.getResearchContext(), false), sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo(), RequestParameter.GROUP_BY_CONTEXT, new TimePeriod(sessionBean.getResearchSamplingPeriod()), true)));

            boolean selectedFound = false;
            for (TotalStatistics statistics : sessionBean.getJvmStatisticsTotal()) {
                if (sessionBean.getJvmResultSelected() != null && statistics.getId() == sessionBean.getJvmResultSelected()) {
                    selectedFound = true;
                }
            }

            if ((!selectedFound || sessionBean.getJvmResultSelected() == null) && !sessionBean.getJvmStatisticsTotal().isEmpty()) {
                sessionBean.setJvmResultSelected(sessionBean.getJvmStatisticsTotal().iterator().next().getId());
            }

            if (sessionBean.getJvmStatisticsTotal().isEmpty()) {
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

            for (JvmStatistics.TotalStatistics statistics : sessionBean.getJvmStatisticsTotal()) {

                if (statistics.getId() == sessionBean.getJvmResultSelected().intValue()) {
                    context = statistics.getContext();
                }
            }

            sessionBean.setJvmStatisticsTime(client.searchJvmTimeStatistics(new JvmParameters.SearchStatisticsParameter(ApplicationUtilities.getRegexPattern(context, false), sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo(), RequestParameter.GROUP_BY_CONTEXT, new TimePeriod(sessionBean.getResearchSamplingPeriod()), false)));

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
        double heapMemoryMax = 0;
        double nonHeapMemoryMax = 0;

        double garbageCollectionTimeMax = 0;

        for (JvmStatistics statistics : sessionBean.getJvmStatisticsTime()) {


            if (sessionBean.getJvmStatisticsSelected().contains(CHART_HEAP_MEMORY_USED)) {
                heapMemoryMax = Math.max(heapMemoryMax, MathUtilities.avg(statistics.getCount(), statistics.getHeapMemoryUsedBytesStatistics().getSum()));
            }
            if (sessionBean.getJvmStatisticsSelected().contains(CHART_HEAP_MEMORY_COMMITTED)) {
                heapMemoryMax = Math.max(heapMemoryMax, MathUtilities.avg(statistics.getCount(), statistics.getHeapMemoryCommittedBytesStatistics().getSum()));
            }
            if (sessionBean.getJvmStatisticsSelected().contains(CHART_HEAP_MEMORY_MAX)) {
                heapMemoryMax = Math.max(heapMemoryMax, MathUtilities.avg(statistics.getCount(), statistics.getHeapMemoryMaxBytesStatistics().getSum()));
            }

            if (sessionBean.getJvmStatisticsSelected().contains(CHART_NON_HEAP_MEMORY_USED)) {
                nonHeapMemoryMax = Math.max(nonHeapMemoryMax, MathUtilities.avg(statistics.getCount(), statistics.getNonHeapMemoryUsedBytesStatistics().getSum()));
            }
            if (sessionBean.getJvmStatisticsSelected().contains(CHART_NON_HEAP_MEMORY_COMMITTED)) {
                nonHeapMemoryMax = Math.max(nonHeapMemoryMax, MathUtilities.avg(statistics.getCount(), statistics.getNonHeapMemoryCommittedBytesStatistics().getSum()));
            }
            if (sessionBean.getJvmStatisticsSelected().contains(CHART_NON_HEAP_MEMORY_MAX)) {
                nonHeapMemoryMax = Math.max(nonHeapMemoryMax, MathUtilities.avg(statistics.getCount(), statistics.getNonHeapMemoryMaxBytesStatistics().getSum()));
            }

            if (sessionBean.getJvmStatisticsSelected().contains(CHART_GARBAGE_COLLECTIONS_TIME)) {
                garbageCollectionTimeMax = Math.max(garbageCollectionTimeMax, statistics.getGarbageCollectionNanosStatistics().getSum());
            }
        }

        String heapMemoryUnit = SizeUtilities.fromBytesToUnit(heapMemoryMax);
        String nonHeapMemoryUnit = SizeUtilities.fromBytesToUnit(nonHeapMemoryMax);

        String garbageCollectionTimeUnit = TimeUtilities.fromNanosToUnit(garbageCollectionTimeMax);


        //Count how many statistics to show
        Map<String, ChartPlotArea> plots = new HashMap();


        plots.put("heapMemory", new ChartPlotArea(ApplicationUtilities.integerFormatter.toPattern() + " " + heapMemoryUnit, null, new Date[]{sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()}));
        plots.put("nonHeapMemory", new ChartPlotArea(ApplicationUtilities.integerFormatter.toPattern() + " " + nonHeapMemoryUnit, null, new Date[]{sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()}));
        plots.put(CHART_CPU_PERCENTAGE, new ChartPlotArea(ApplicationUtilities.percentageFormatter.toPattern(), new double[]{0, 1}, new Date[]{sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()}));
        plots.put(CHART_THREADS, new ChartPlotArea(ApplicationUtilities.integerFormatter.toPattern(), null, new Date[]{sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()}));

        plots.put(CHART_GARBAGE_COLLECTIONS, new ChartPlotArea(ApplicationUtilities.integerFormatter.toPattern(), null, new Date[]{sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()}));
        plots.put(CHART_GARBAGE_COLLECTIONS_TIME, new ChartPlotArea(ApplicationUtilities.decimalFormatter.toPattern() + " " + garbageCollectionTimeUnit, null, new Date[]{sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()}));

        plots.put(CHART_LOADED_CLASSES, new ChartPlotArea(ApplicationUtilities.integerFormatter.toPattern(), null, new Date[]{sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()}));

        plots.put("heapMemory_trend", new ChartPlotArea(ApplicationUtilities.integerFormatter.toPattern(), null, new Date[]{sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo()}));

        if (sessionBean.getJvmStatisticsSelected().contains(CHART_HEAP_MEMORY_USED)) {
            plots.get("heapMemory").addColum(ApplicationUtilities.getMessage("label_HeapMemoryUsed"), sessionBean.getJvmStatisticsColors().get(CHART_HEAP_MEMORY_USED));
        }
        if (sessionBean.getJvmStatisticsSelected().contains(CHART_HEAP_MEMORY_COMMITTED)) {
            plots.get("heapMemory").addColum(ApplicationUtilities.getMessage("label_HeapMemoryCommitted"), sessionBean.getJvmStatisticsColors().get(CHART_HEAP_MEMORY_COMMITTED));
        }
        if (sessionBean.getJvmStatisticsSelected().contains(CHART_HEAP_MEMORY_MAX)) {
            plots.get("heapMemory").addColum(ApplicationUtilities.getMessage("label_HeapMemoryMax"), sessionBean.getJvmStatisticsColors().get(CHART_HEAP_MEMORY_MAX));
        }

        if (sessionBean.getJvmStatisticsSelected().contains(CHART_NON_HEAP_MEMORY_USED)) {
            plots.get("nonHeapMemory").addColum(ApplicationUtilities.getMessage("label_NonHeapMemoryUsed"), sessionBean.getJvmStatisticsColors().get(CHART_NON_HEAP_MEMORY_USED));
        }
        if (sessionBean.getJvmStatisticsSelected().contains(CHART_NON_HEAP_MEMORY_COMMITTED)) {
            plots.get("nonHeapMemory").addColum(ApplicationUtilities.getMessage("label_NonHeapMemoryCommitted"), sessionBean.getJvmStatisticsColors().get(CHART_NON_HEAP_MEMORY_COMMITTED));
        }
        if (sessionBean.getJvmStatisticsSelected().contains(CHART_NON_HEAP_MEMORY_MAX)) {
            plots.get("nonHeapMemory").addColum(ApplicationUtilities.getMessage("label_NonHeapMemoryMax"), sessionBean.getJvmStatisticsColors().get(CHART_NON_HEAP_MEMORY_MAX));
        }

        if (sessionBean.getJvmStatisticsSelected().contains(CHART_THREADS)) {
            plots.get(CHART_THREADS).addColum(ApplicationUtilities.getMessage("label_Threads"), sessionBean.getJvmStatisticsColors().get(CHART_THREADS));
        }

        if (sessionBean.getJvmStatisticsSelected().contains(CHART_CPU_PERCENTAGE)) {
            plots.get(CHART_CPU_PERCENTAGE).addColum(ApplicationUtilities.getMessage("label_CPUUsage"), sessionBean.getJvmStatisticsColors().get(CHART_CPU_PERCENTAGE));
        }

        if (sessionBean.getJvmStatisticsSelected().contains(CHART_GARBAGE_COLLECTIONS)) {
            plots.get(CHART_GARBAGE_COLLECTIONS).addColum(ApplicationUtilities.getMessage("label_GarbageCollections"), sessionBean.getJvmStatisticsColors().get(CHART_GARBAGE_COLLECTIONS));
        }

        if (sessionBean.getJvmStatisticsSelected().contains(CHART_GARBAGE_COLLECTIONS_TIME)) {
            plots.get(CHART_GARBAGE_COLLECTIONS_TIME).addColum(ApplicationUtilities.getMessage("label_GarbageCollectionsTime"), sessionBean.getJvmStatisticsColors().get(CHART_GARBAGE_COLLECTIONS_TIME));
        }

        if (sessionBean.getJvmStatisticsSelected().contains(CHART_LOADED_CLASSES)) {
            plots.get(CHART_LOADED_CLASSES).addColum(ApplicationUtilities.getMessage("label_LoadedClasses"), sessionBean.getJvmStatisticsColors().get(CHART_LOADED_CLASSES));
        }

        for (JvmStatistics statistics : sessionBean.getJvmStatisticsTime()) {

            if (sessionBean.getJvmStatisticsSelected().contains(CHART_HEAP_MEMORY_USED)) {
                plots.get("heapMemory").addValue(statistics.getDate(), SizeUtilities.fromBytesToValue(MathUtilities.avg(statistics.getCount(), statistics.getHeapMemoryUsedBytesStatistics().getSum()), SizeUtilities.MB_UNIT));
                //TODO....
                //plots.get("heapMemory").addValue(statistics.getDate(), SizeUtilities.fromBytesToValue(statisticsTotal.getHeapMemoryUsedBytesTrend().getAveragePredictedAt(statistics.getDate()), SizeUtilities.MB_UNIT));
            }
            if (sessionBean.getJvmStatisticsSelected().contains(CHART_HEAP_MEMORY_COMMITTED)) {
                plots.get("heapMemory").addValue(statistics.getDate(), SizeUtilities.fromBytesToValue(MathUtilities.avg(statistics.getCount(), statistics.getHeapMemoryCommittedBytesStatistics().getSum()), SizeUtilities.MB_UNIT));
            }
            if (sessionBean.getJvmStatisticsSelected().contains(CHART_HEAP_MEMORY_MAX)) {
                plots.get("heapMemory").addValue(statistics.getDate(), SizeUtilities.fromBytesToValue(MathUtilities.avg(statistics.getCount(), statistics.getHeapMemoryMaxBytesStatistics().getSum()), SizeUtilities.MB_UNIT));
            }

            if (sessionBean.getJvmStatisticsSelected().contains(CHART_NON_HEAP_MEMORY_USED)) {
                plots.get("nonHeapMemory").addValue(statistics.getDate(), SizeUtilities.fromBytesToValue(MathUtilities.avg(statistics.getCount(), statistics.getNonHeapMemoryUsedBytesStatistics().getSum()), SizeUtilities.MB_UNIT));
            }
            if (sessionBean.getJvmStatisticsSelected().contains(CHART_NON_HEAP_MEMORY_COMMITTED)) {
                plots.get("nonHeapMemory").addValue(statistics.getDate(), SizeUtilities.fromBytesToValue(MathUtilities.avg(statistics.getCount(), statistics.getNonHeapMemoryCommittedBytesStatistics().getSum()), SizeUtilities.MB_UNIT));
            }
            if (sessionBean.getJvmStatisticsSelected().contains(CHART_NON_HEAP_MEMORY_MAX)) {
                plots.get("nonHeapMemory").addValue(statistics.getDate(), SizeUtilities.fromBytesToValue(MathUtilities.avg(statistics.getCount(), statistics.getNonHeapMemoryMaxBytesStatistics().getSum()), SizeUtilities.MB_UNIT));
            }

            if (sessionBean.getJvmStatisticsSelected().contains(CHART_THREADS)) {
                plots.get(CHART_THREADS).addValue(statistics.getDate(), MathUtilities.avg(statistics.getCount(), statistics.getThreadsStatistics().getSum()));
            }

            if (sessionBean.getJvmStatisticsSelected().contains(CHART_CPU_PERCENTAGE)) {
                plots.get(CHART_CPU_PERCENTAGE).addValue(statistics.getDate(), (MathUtilities.avg(statistics.getCount(), statistics.getCpuPercentageStatistics().getSum())));
            }

            if (sessionBean.getJvmStatisticsSelected().contains(CHART_GARBAGE_COLLECTIONS)) {
                plots.get(CHART_GARBAGE_COLLECTIONS).addValue(statistics.getDate(), statistics.getGarbageCollectionsStatistics().getSum());
            }
            if (sessionBean.getJvmStatisticsSelected().contains(CHART_GARBAGE_COLLECTIONS_TIME)) {
                plots.get(CHART_GARBAGE_COLLECTIONS_TIME).addValue(statistics.getDate(), TimeUtilities.fromNanosToValue(statistics.getGarbageCollectionNanosStatistics().getSum(), garbageCollectionTimeUnit));
            }

            if (sessionBean.getJvmStatisticsSelected().contains(CHART_LOADED_CLASSES)) {
                plots.get(CHART_LOADED_CLASSES).addValue(statistics.getDate(), MathUtilities.avg(statistics.getCount(), statistics.getLoadedClassesStatistics().getSum()));
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

        String detail = null;

        for (TotalStatistics statistics : sessionBean.getJvmStatisticsTotal()) {
            detail = this.getDetailPopup(statistics, sessionBean.getResearchSamplingPeriod());
            detail = detail + "<div class=\"boxContentSepartor\"></div>";
        }
        PrintWriter out = response.getWriter();
        try {
            out.write(detail);
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

            HtmlTable htmlTableContainer = new HtmlTable(100, new int[]{50, 50});

            //Statistics legend check boxes
            HtmlTable htmlTableLegend = new HtmlTable(new int[]{33, 34, 33});

            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_HeapMemoryUsed"), "#selectedStatistics", CHART_HEAP_MEMORY_USED, "selectStatistics", sessionBean.getJvmStatisticsColors().get(CHART_HEAP_MEMORY_USED), sessionBean.getJvmStatisticsSelected().contains(CHART_HEAP_MEMORY_USED), true));
            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_HeapMemoryCommitted"), "#selectedStatistics", CHART_HEAP_MEMORY_COMMITTED, "selectStatistics", sessionBean.getJvmStatisticsColors().get(CHART_HEAP_MEMORY_COMMITTED), sessionBean.getJvmStatisticsSelected().contains(CHART_HEAP_MEMORY_COMMITTED), true));
            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_HeapMemoryMax"), "#selectedStatistics", CHART_HEAP_MEMORY_MAX, "selectStatistics", sessionBean.getJvmStatisticsColors().get(CHART_HEAP_MEMORY_MAX), sessionBean.getJvmStatisticsSelected().contains(CHART_HEAP_MEMORY_MAX), true));

            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_NonHeapMemoryUsed"), "#selectedStatistics", CHART_NON_HEAP_MEMORY_USED, "selectStatistics", sessionBean.getJvmStatisticsColors().get(CHART_NON_HEAP_MEMORY_USED), sessionBean.getJvmStatisticsSelected().contains(CHART_NON_HEAP_MEMORY_USED), true));
            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_NonHeapMemoryCommitted"), "#selectedStatistics", CHART_NON_HEAP_MEMORY_COMMITTED, "selectStatistics", sessionBean.getJvmStatisticsColors().get(CHART_NON_HEAP_MEMORY_COMMITTED), sessionBean.getJvmStatisticsSelected().contains(CHART_NON_HEAP_MEMORY_COMMITTED), true));
            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_NonHeapMemoryMax"), "#selectedStatistics", CHART_NON_HEAP_MEMORY_MAX, "selectStatistics", sessionBean.getJvmStatisticsColors().get(CHART_NON_HEAP_MEMORY_MAX), sessionBean.getJvmStatisticsSelected().contains(CHART_NON_HEAP_MEMORY_MAX), true));

            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_CPUUsage"), "#selectedStatistics", CHART_CPU_PERCENTAGE, "selectStatistics", sessionBean.getJvmStatisticsColors().get(CHART_CPU_PERCENTAGE), sessionBean.getJvmStatisticsSelected().contains(CHART_CPU_PERCENTAGE), true));
            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_Threads"), "#selectedStatistics", CHART_THREADS, "selectStatistics", sessionBean.getJvmStatisticsColors().get(CHART_THREADS), sessionBean.getJvmStatisticsSelected().contains(CHART_THREADS), true));
            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_LoadedClasses"), "#selectedStatistics", CHART_LOADED_CLASSES, "selectStatistics", sessionBean.getJvmStatisticsColors().get(CHART_LOADED_CLASSES), sessionBean.getJvmStatisticsSelected().contains(CHART_LOADED_CLASSES), true));

            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_GarbageCollections"), "#selectedStatistics", CHART_GARBAGE_COLLECTIONS, "selectStatistics", sessionBean.getJvmStatisticsColors().get(CHART_GARBAGE_COLLECTIONS), sessionBean.getJvmStatisticsSelected().contains(CHART_GARBAGE_COLLECTIONS), true));
            htmlTableLegend.addCell(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_GarbageCollectionsTime"), "#selectedStatistics", CHART_GARBAGE_COLLECTIONS_TIME, "selectStatistics", sessionBean.getJvmStatisticsColors().get(CHART_GARBAGE_COLLECTIONS_TIME), sessionBean.getJvmStatisticsSelected().contains(CHART_GARBAGE_COLLECTIONS_TIME), true));
            htmlTableLegend.addCell("");

            htmlTableContainer.addCell(htmlTableLegend.getHtmlCode(), "top");

            htmlTableContainer.addCell(HtmlUtilities.getChartOptions(sessionBean.getResearchSamplingPeriod(), sessionBean.getSamplingPeriod().getUnit(), sessionBean.getSamplingPeriod().getUnit(), sessionBean.isResearchLogScale(), sessionBean.isResearchTrendLines()), "top right");

            out.write(htmlTableContainer.getHtmlCode());

        } finally {
            //out.close();
        }

    }

    private String getDetailPopup(TotalStatistics statistics, int period) throws ServletException, IOException {

        //Find last statistics for all elements

        HtmlTable htmlTableContainer = new HtmlTable(100, new int[]{100}, "boxContent");


        //Heap memory table

        HtmlDetailTable htmlTable = new HtmlDetailTable(HtmlDetailTable.TYPE_FULL, null, ApplicationUtilities.getMessage("label_HeapMemory"), true, true, false, true, period);

        htmlTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, false, true, true, false, true, ApplicationUtilities.getMessage("label_Used") + ":", statistics.getHeapMemoryUsedBytesStatistics(), statistics.getHeapMemoryUsedBytesTrend(), statistics.getCount());
        htmlTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, false, true, true, false, true, ApplicationUtilities.getMessage("label_Committed") + ":", statistics.getHeapMemoryCommittedBytesStatistics(), statistics.getHeapMemoryCommittedBytesTrend(), statistics.getCount());
        htmlTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, false, true, true, false, true, ApplicationUtilities.getMessage("label_Total") + ":", statistics.getHeapMemoryMaxBytesStatistics(), statistics.getHeapMemoryMaxBytesTrend(), statistics.getCount());

        ChartPlotPie heapMemoryPlot = new ChartPlotPie();

        heapMemoryPlot.addValue(ApplicationUtilities.getMessage("label_Used"), statistics.getHeapMemoryUsedBytesStatistics().getSum() / statistics.getCount(), JvmServlet.CHART_COLOR_HEAP_MEMORY_USED);
        heapMemoryPlot.addValue(ApplicationUtilities.getMessage("label_Committed"), (statistics.getHeapMemoryCommittedBytesStatistics().getSum() - statistics.getHeapMemoryUsedBytesStatistics().getSum()) / statistics.getCount(), JvmServlet.CHART_COLOR_HEAP_MEMORY_COMMITTED);
        heapMemoryPlot.addValue(ApplicationUtilities.getMessage("label_Maximum"), (statistics.getHeapMemoryMaxBytesStatistics().getSum() - statistics.getHeapMemoryCommittedBytesStatistics().getSum()) / statistics.getCount(), JvmServlet.CHART_COLOR_HEAP_MEMORY_MAX);

        htmlTable.addTotalChart("jvmChartHeapMemory" + Math.abs(statistics.getId()), ApplicationUtilities.getMessage("label_HeapMemory"), heapMemoryPlot, true, false);


        //htmlTable.addStatisticsSeparator(ApplicationUtilities.getMessage("label_NonHeapMemory"));
        htmlTable.addStatisticsSeparator(null, ApplicationUtilities.getMessage("label_NonHeapMemory"), true, true, false, true);

        htmlTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, false, true, true, false, true, ApplicationUtilities.getMessage("label_Used") + ":", statistics.getNonHeapMemoryUsedBytesStatistics(), statistics.getNonHeapMemoryUsedBytesTrend(), statistics.getCount());
        htmlTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, false, true, true, false, true, ApplicationUtilities.getMessage("label_Committed") + ":", statistics.getNonHeapMemoryCommittedBytesStatistics(), statistics.getNonHeapMemoryCommittedBytesTrend(), statistics.getCount());
        htmlTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, false, true, true, false, true, ApplicationUtilities.getMessage("label_Total") + ":", statistics.getNonHeapMemoryMaxBytesStatistics(), statistics.getNonHeapMemoryMaxBytesTrend(), statistics.getCount());


        ChartPlotPie nonHeapMemoryPlot = new ChartPlotPie();

        nonHeapMemoryPlot.addValue(ApplicationUtilities.getMessage("label_Used"), statistics.getNonHeapMemoryUsedBytesStatistics().getSum() / statistics.getCount(), JvmServlet.CHART_COLOR_NON_HEAP_MEMORY_USED);
        nonHeapMemoryPlot.addValue(ApplicationUtilities.getMessage("label_Committed"), (statistics.getNonHeapMemoryCommittedBytesStatistics().getSum() - statistics.getNonHeapMemoryUsedBytesStatistics().getSum()) / statistics.getCount(), JvmServlet.CHART_COLOR_NON_HEAP_MEMORY_COMMITTED);
        nonHeapMemoryPlot.addValue(ApplicationUtilities.getMessage("label_Maximum"), (statistics.getNonHeapMemoryMaxBytesStatistics().getSum() - statistics.getNonHeapMemoryCommittedBytesStatistics().getSum()) / statistics.getCount(), JvmServlet.CHART_COLOR_NON_HEAP_MEMORY_MAX);

        htmlTable.addTotalChart("jvmChartNonHeapMemory" + Math.abs(statistics.getId()), ApplicationUtilities.getMessage("label_NonHeapMemory"), nonHeapMemoryPlot, true, false);



        htmlTable.addStatisticsSeparator();
        htmlTable.addStatisticsSeparator(null, ApplicationUtilities.getMessage("label_CPU"), true, true, true, true);

        htmlTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, true, true, true, false, true, ApplicationUtilities.getMessage("label_Processors") + ":", statistics.getProcessorsStatistics(), statistics.getProcessorsTrend(), statistics.getCount());
        htmlTable.addStatistics(HtmlTable.CELL_TYPE_PERCENTAGE, false, true, true, true, true, ApplicationUtilities.getMessage("label_CPUUsage") + ":", statistics.getCpuPercentageStatistics(), statistics.getCpuPercentageTrend(), HtmlTable.CELL_TYPE_TIME, statistics.getCpuNanosStatistics(), statistics.getCpuNanosTrend(), statistics.getCount());

        ChartPlotPie cpuPlot = new ChartPlotPie();
        cpuPlot.addValue(ApplicationUtilities.getMessage("label_Used"), statistics.getCpuPercentageStatistics().getSum() / statistics.getCount(), JvmServlet.CHART_COLOR_CPU_PERCENTAGE);
        cpuPlot.addValue(ApplicationUtilities.getMessage("label_Free"), 1 - (statistics.getCpuPercentageStatistics().getSum()) / statistics.getCount(), CommonServlet.CHART_COLOR_BLUE_3);


        //cpuTable.addStatisticsSeparator();

        //cpuTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, true, true, true, false, true, ApplicationUtilities.getMessage("label_Processors") + ":", processorsStatistics, processorsTrend, count);

        htmlTable.addTotalChart("jvmChartCpu" + Math.abs(statistics.getId()), ApplicationUtilities.getMessage("label_Cpu"), cpuPlot, true, false);



        htmlTable.addStatisticsSeparator();
        htmlTable.addStatisticsSeparator(null, ApplicationUtilities.getMessage("label_ThreadsAndClasses"), true, true, false, true);
        //htmlTable.addStatisticsSeparator(ApplicationUtilities.getMessage("label_Threads"),null, true, true, false, true);

        htmlTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, false, true, true, false, true, ApplicationUtilities.getMessage("label_Threads") + ":", statistics.getThreadsStatistics(), statistics.getThreadsTrend(), statistics.getCount());
        htmlTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, false, true, true, false, true, ApplicationUtilities.getMessage("label_LoadedClasses") + ":", statistics.getLoadedClassesStatistics(), statistics.getLoadedClassesTrend(), statistics.getCount());


        htmlTable.addStatisticsSeparator();
        htmlTable.addStatisticsSeparator(null, ApplicationUtilities.getMessage("label_GarbageCollections"), false, false, true, false);

        htmlTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, false, false, true, true, false, ApplicationUtilities.getMessage("label_Occurrences") + ":", statistics.getGarbageCollectionsStatistics(), statistics.getGarbageCollectionsTrend(), statistics.getCount());
        htmlTable.addStatistics(HtmlTable.CELL_TYPE_TIME, false, false, true, true, false, ApplicationUtilities.getMessage("label_TimeSpent") + ":", statistics.getGarbageCollectionNanosStatistics(), statistics.getGarbageCollectionNanosTrend(), statistics.getCount());




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


        result.append(ApplicationUtilities.getMessage("label_HeapMemoryUsed"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Avg"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_HeapMemoryUsed"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Min"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_HeapMemoryUsed"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Max"));
        result.append(" (b)");
        result.append(";");

        result.append(ApplicationUtilities.getMessage("label_HeapMemoryCommitted"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Avg"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_HeapMemoryCommitted"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Min"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_HeapMemoryCommitted"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Max"));
        result.append(" (b)");
        result.append(";");


        result.append(ApplicationUtilities.getMessage("label_HeapMemoryMax"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Avg"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_HeapMemoryMax"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Min"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_HeapMemoryMax"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Max"));
        result.append(" (b)");
        result.append(";");



        result.append(ApplicationUtilities.getMessage("label_NonHeapMemoryUsed"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Avg"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_NonHeapMemoryUsed"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Min"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_NonHeapMemoryUsed"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Max"));
        result.append(" (b)");
        result.append(";");

        result.append(ApplicationUtilities.getMessage("label_NonHeapMemoryCommitted"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Avg"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_NonHeapMemoryCommitted"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Min"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_NonHeapMemoryCommitted"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Max"));
        result.append(" (b)");
        result.append(";");


        result.append(ApplicationUtilities.getMessage("label_NonHeapMemoryMax"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Avg"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_NonHeapMemoryMax"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Min"));
        result.append(" (b)");
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_NonHeapMemoryMax"));
        result.append(" - ");
        result.append(ApplicationUtilities.getMessage("label_Max"));
        result.append(" (b)");
        result.append(";");

        result.append(ApplicationUtilities.getMessage("label_Processors"));
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_TotalCPU"));
        result.append(" (ns)");
        result.append(";");

        result.append(ApplicationUtilities.getMessage("label_Threads"));
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_LoadedClasses"));
        result.append(";");

        result.append(ApplicationUtilities.getMessage("label_GarbageCollections"));
        result.append(";");
        result.append(ApplicationUtilities.getMessage("label_GarbageCollectionsTime"));
        result.append(" (ns)");
        result.append(";");


        result.append("\n");

        DecimalFormat numberFormat = new DecimalFormat("0");

        for (JvmStatistics.TotalStatistics statistics : sessionBean.getJvmStatisticsTotal()) {
            result.append(statistics.getContext());
            result.append(";");

            result.append(numberFormat.format(statistics.getHeapMemoryUsedBytesStatistics().getSum() / statistics.getCount()));
            result.append(";");
            result.append(numberFormat.format(statistics.getHeapMemoryUsedBytesStatistics().getMinimum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getHeapMemoryUsedBytesStatistics().getMaximum()));
            result.append(";");

            result.append(numberFormat.format(statistics.getHeapMemoryCommittedBytesStatistics().getSum() / statistics.getCount()));
            result.append(";");
            result.append(numberFormat.format(statistics.getHeapMemoryCommittedBytesStatistics().getMinimum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getHeapMemoryCommittedBytesStatistics().getMaximum()));
            result.append(";");

            result.append(numberFormat.format(statistics.getHeapMemoryMaxBytesStatistics().getSum() / statistics.getCount()));
            result.append(";");
            result.append(numberFormat.format(statistics.getHeapMemoryMaxBytesStatistics().getMinimum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getHeapMemoryMaxBytesStatistics().getMaximum()));
            result.append(";");


            result.append(numberFormat.format(statistics.getNonHeapMemoryUsedBytesStatistics().getSum() / statistics.getCount()));
            result.append(";");
            result.append(numberFormat.format(statistics.getNonHeapMemoryUsedBytesStatistics().getMinimum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getNonHeapMemoryUsedBytesStatistics().getMaximum()));
            result.append(";");

            result.append(numberFormat.format(statistics.getNonHeapMemoryCommittedBytesStatistics().getSum() / statistics.getCount()));
            result.append(";");
            result.append(numberFormat.format(statistics.getNonHeapMemoryCommittedBytesStatistics().getMinimum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getNonHeapMemoryCommittedBytesStatistics().getMaximum()));
            result.append(";");

            result.append(numberFormat.format(statistics.getNonHeapMemoryMaxBytesStatistics().getSum() / statistics.getCount()));
            result.append(";");
            result.append(numberFormat.format(statistics.getNonHeapMemoryMaxBytesStatistics().getMinimum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getNonHeapMemoryMaxBytesStatistics().getMaximum()));
            result.append(";");

            result.append(numberFormat.format(statistics.getProcessorsStatistics().getSum() / statistics.getCount()));
            result.append(";");

            result.append(numberFormat.format(statistics.getCpuNanosStatistics().getSum()));
            result.append(";");


            result.append(numberFormat.format(statistics.getThreadsStatistics().getSum() / statistics.getCount()));
            result.append(";");

            result.append(numberFormat.format(statistics.getLoadedClassesStatistics().getSum() / statistics.getCount()));
            result.append(";");

            result.append(numberFormat.format(statistics.getGarbageCollectionsStatistics().getSum()));
            result.append(";");
            result.append(numberFormat.format(statistics.getGarbageCollectionNanosStatistics().getSum()));
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
        return "Jvm Servlet";
    }
}
