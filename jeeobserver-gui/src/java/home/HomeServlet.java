package home;

import action.ActionServlet;
import hardDisk.HardDiskServlet;
import httpSession.HttpSessionServlet;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
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
import jeeobserver.server.ActionParameters.SearchStatisticsParameter;
import jeeobserver.server.ActionStatistics;
import jeeobserver.server.HardDiskParameters;
import jeeobserver.server.HardDiskStatistics;
import jeeobserver.server.HttpSessionParameters;
import jeeobserver.server.HttpSessionStatistics;
import jeeobserver.server.JeeObserverClient;
import jeeobserver.server.JeeObserverServerException;
import jeeobserver.server.JvmParameters;
import jeeobserver.server.JvmStatistics;
import jeeobserver.server.RequestParameter;
import jeeobserver.server.Statistics.NumberRangeStatistics;
import jeeobserver.server.Statistics.NumberStatistics;
import jeeobserver.server.Statistics.NumberTrend;
import jeeobserver.server.TimePeriod;
import jvm.JvmServlet;
import login.ApplicationSessionBean;
import utilities.ApplicationUtilities;
import utilities.html.HtmlDetailTable;
import utilities.html.HtmlTable;
import utilities.html.HtmlUtilities;

public class HomeServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String SERVLET = "home";

    public static final String COOKIE_CHART_ACTION_COLLAPSED = SERVLET + "ActionChartCollapsed";

    public static final String COOKIE_CHART_JVM_COLLAPSED = SERVLET + "JvmChartCollapsed";

    public static final String COOKIE_CHART_HTTP_SESSION_COLLAPSED = SERVLET + "HttpSessionChartCollapsed";

    public static final String COOKIE_CHART_HARD_DISK_COLLAPSED = SERVLET + "HardDiskChartCollapsed";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null || action.equals("")) {
            throw new ServletException("Action is a mandatory parameter");
        } else if (action.equals("show")) {
            this.actionShow(request, response);
        } else if (action.equals("ajaxRenderBodyAction")) {
            this.ajaxRenderBodyAction(request, response);
        } else if (action.equals("ajaxRenderBodyJvm")) {
            this.ajaxRenderBodyJvm(request, response);
        } else if (action.equals("ajaxRenderBodyHardDisk")) {
            this.ajaxRenderBodyHardDisk(request, response);
        } else if (action.equals("ajaxRenderBodyHttpSession")) {
            this.ajaxRenderBodyHttpSession(request, response);
        } else {
            throw new ServletException(String.format("Action parameter '%s' unknown.", action));
        }
    }

    private void actionShow(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        sessionBean.setCurrentServlet(HomeServlet.SERVLET);

        request.setAttribute("title", ApplicationUtilities.getMessage("page_home_WindowTitle"));
        request.setAttribute("subTitle", ApplicationUtilities.getMessage("page_home_WindowSubTitle"));

        try {

            JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

            //Load all elements for research filter menu
            sessionBean.setResearchContexts(new TreeSet<String>());
            sessionBean.setResearchContextProjects(new TreeMap<String, Set<String>>());
            sessionBean.setResearchContextPaths(new TreeMap<String, Set<String>>());


            //Action elements
            Collection<ActionStatistics.Element> actionElements = client.searchActionElements(new ActionParameters.SearchElementsParameter(null, null));

            for (ActionStatistics.Element element : actionElements) {
                sessionBean.getResearchContexts().add(element.getContext());

                if (sessionBean.getResearchContextProjects().get(element.getContext()) == null) {
                    sessionBean.getResearchContextProjects().put(element.getContext(), new TreeSet<String>());
                }

                sessionBean.getResearchContextProjects().get(element.getContext()).add(element.getProject());
            }

            //Jvm elements
            Collection<JvmStatistics.Element> jvmElements = client.searchJvmElements(new JvmParameters.SearchElementsParameter(null, null));

            for (JvmStatistics.Element element : jvmElements) {
                sessionBean.getResearchContexts().add(element.getContext());
            }


            //HttpSession elements
            Collection<HttpSessionStatistics.Element> httpSessionElements = client.searchHttpSessionElements(new HttpSessionParameters.SearchElementsParameter(null, null));

            for (HttpSessionStatistics.Element element : httpSessionElements) {
                sessionBean.getResearchContexts().add(element.getContext());

                if (sessionBean.getResearchContextProjects().get(element.getContext()) == null) {
                    sessionBean.getResearchContextProjects().put(element.getContext(), new TreeSet<String>());
                }

                sessionBean.getResearchContextProjects().get(element.getContext()).add(element.getProject());
            }

            //HardDisk elements
            Collection<HardDiskStatistics.Element> hardDiskElements = client.searchHardDiskElements(new HardDiskParameters.SearchElementsParameter(null, null));

            for (HardDiskStatistics.Element element : hardDiskElements) {
                sessionBean.getResearchContexts().add(element.getContext());

                if (sessionBean.getResearchContextPaths().get(element.getContext()) == null) {
                    sessionBean.getResearchContextPaths().put(element.getContext(), new TreeSet<String>());
                }

                sessionBean.getResearchContextPaths().get(element.getContext()).add(element.getPath());
            }


            //Default Context
            if (!sessionBean.getResearchContexts().isEmpty() && (sessionBean.getResearchContext() == null || !sessionBean.getResearchContexts().contains(sessionBean.getResearchContext()))) {
                sessionBean.setResearchContext(sessionBean.getResearchContexts().iterator().next());
            }

            /*sessionBean.setHomeResearchProject(sessionBean.getResearchProject());
            if (sessionBean.getHomeResearchProject() == null) {
                if (!sessionBean.getResearchContextProjects().isEmpty() && (sessionBean.getResearchContextProjects().get(sessionBean.getResearchContext()) != null && !sessionBean.getResearchContextProjects().get(sessionBean.getResearchContext()).isEmpty())) {
                    sessionBean.setHomeResearchProject(sessionBean.getResearchContextProjects().get(sessionBean.getResearchContext()).iterator().next());
                }
            }

            sessionBean.setHomeResearchPath(sessionBean.getResearchPath());
            if (sessionBean.getHomeResearchPath() == null) {
                if (!sessionBean.getResearchContextPaths().isEmpty() && (sessionBean.getResearchContextPaths().get(sessionBean.getResearchContext()) != null && !sessionBean.getResearchContextPaths().get(sessionBean.getResearchContext()).isEmpty())) {
                    sessionBean.setHomeResearchPath(sessionBean.getResearchContextPaths().get(sessionBean.getResearchContext()).iterator().next());
                }
            }*/


        } catch (JeeObserverServerException ex) {
            ApplicationUtilities.addError(request, ex.getMessage());
            throw new ServletException(ex.getMessage());
        }

        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/secure/home/home.jsp");
        dispatcher.forward(request, response);
    }

    //Ajax Renders
    private void ajaxRenderBodyAction(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        int categories[] = new int[]{ActionObserver.CATEGORY_SERVLET, ActionObserver.CATEGORY_JSF, ActionObserver.CATEGORY_EJB, ActionObserver.CATEGORY_JAXWS, ActionObserver.CATEGORY_JDBC, ActionObserver.CATEGORY_CUSTOM};

        String result = "";

        boolean statisticsFound = false;

        for (int category : categories) {

            try {
                JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

                sessionBean.setActionStatisticsTotal(client.searchActionTotalStatistics(new SearchStatisticsParameter(category, ApplicationUtilities.getRegexPattern(sessionBean.getResearchContext(), false), ApplicationUtilities.getRegexPattern(sessionBean.getResearchProject(), false), null, sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo(), RequestParameter.GROUP_BY_CONTEXT, new TimePeriod(sessionBean.getResearchSamplingPeriod()), true)));

            } catch (JeeObserverServerException ex) {
                ApplicationUtilities.addError(request, ex.getMessage());
                throw new ServletException(ex.getMessage());
            }

            if (!sessionBean.getActionStatisticsTotal().isEmpty()) {

                statisticsFound = true;
                ActionStatistics.TotalStatistics statistics = sessionBean.getActionStatisticsTotal().iterator().next();

                String title = "";
                if (category == ActionObserver.CATEGORY_SERVLET) {
                    //title = HtmlUtilities.getLinkHtml(ApplicationUtilities.getMessage("label_Servlets"), request.getContextPath() + "/secure/action?action=show&category=" + ActionObserver.CATEGORY_SERVLET + "&chart=" + ActionServlet.CHART_EXECUTIONS + "_" + ActionServlet.CHART_EXCEPTIONS + "&selected=" + statistics.getId(), "");
                    title = ApplicationUtilities.getMessage("label_Servlets");
                } else if (category == ActionObserver.CATEGORY_JSF) {
                    //title = HtmlUtilities.getLinkHtml(ApplicationUtilities.getMessage("label_JSF"), request.getContextPath() + "/secure/action?action=show&category=" + ActionObserver.CATEGORY_JSF + "&chart=" + ActionServlet.CHART_EXECUTIONS + "_" + ActionServlet.CHART_EXCEPTIONS + "&selected=" + statistics.getId(), "");
                    title = ApplicationUtilities.getMessage("label_JSF");
                } else if (category == ActionObserver.CATEGORY_EJB) {
                    //title = HtmlUtilities.getLinkHtml(ApplicationUtilities.getMessage("label_EJB"), request.getContextPath() + "/secure/action?action=show&category=" + ActionObserver.CATEGORY_EJB + "&chart=" + ActionServlet.CHART_EXECUTIONS + "_" + ActionServlet.CHART_EXCEPTIONS + "&selected=" + statistics.getId(), "");
                    title = ApplicationUtilities.getMessage("label_EJB");
                } else if (category == ActionObserver.CATEGORY_JAXWS) {
                    //title = HtmlUtilities.getLinkHtml(ApplicationUtilities.getMessage("label_JAX-WS"), request.getContextPath() + "/secure/action?action=show&category=" + ActionObserver.CATEGORY_JAXWS + "&chart=" + ActionServlet.CHART_EXECUTIONS + "_" + ActionServlet.CHART_EXCEPTIONS + "&selected=" + statistics.getId(), "");
                    title = ApplicationUtilities.getMessage("label_JAX-WS");
                } else if (category == ActionObserver.CATEGORY_JDBC) {
                    //title = HtmlUtilities.getLinkHtml(ApplicationUtilities.getMessage("label_JDBC"), request.getContextPath() + "/secure/action?action=show&category=" + ActionObserver.CATEGORY_JDBC + "&chart=" + ActionServlet.CHART_EXECUTIONS + "_" + ActionServlet.CHART_EXCEPTIONS + "&selected=" + statistics.getId(), "");
                    title = ApplicationUtilities.getMessage("label_JDBC");
                } else if (category == ActionObserver.CATEGORY_CUSTOM) {
                    //title = HtmlUtilities.getLinkHtml(ApplicationUtilities.getMessage("label_Custom"), request.getContextPath() + "/secure/action?action=show&category=" + ActionObserver.CATEGORY_CUSTOM + "&chart=" + ActionServlet.CHART_EXECUTIONS + "_" + ActionServlet.CHART_EXCEPTIONS + "&selected=" + statistics.getId(), "");
                    title = ApplicationUtilities.getMessage("label_Custom");
                }

                HtmlTable containerTable = new HtmlTable(100, new int[]{33, 34, 33});

                HtmlDetailTable htmlTable = new HtmlDetailTable(HtmlDetailTable.TYPE_SIMPLE, null, title, false, false, true, false, sessionBean.getResearchSamplingPeriod());

                htmlTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, false, false, false, true, false, ApplicationUtilities.getMessage("label_Invocations") + ":", statistics.getExecutionsStatistics(), statistics.getExecutionsTrend(), statistics.getCount(), null, new Double(statistics.getExecutionsStatistics().getSum() / (statistics.getExecutionsStatistics().getSum() + statistics.getExceptionsStatistics().getSum())));
                htmlTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, false, false, false, true, false, ApplicationUtilities.getMessage("label_Exceptions") + ":", statistics.getExceptionsStatistics(), statistics.getExceptionsTrend(), statistics.getCount(), null, new Double(statistics.getExceptionsStatistics().getSum() / (statistics.getExecutionsStatistics().getSum() + statistics.getExceptionsStatistics().getSum())));


                containerTable.addCell(htmlTable.getHtmlCode(), "detailSimpleLeftTable bodyPanel");


                htmlTable = new HtmlDetailTable(HtmlDetailTable.TYPE_SIMPLE, null, null, true, false, false, false, sessionBean.getResearchSamplingPeriod());

                htmlTable.addStatistics(HtmlTable.CELL_TYPE_TIME, false, true, false, false, false, ApplicationUtilities.getMessage("label_Cpu") + ":", statistics.getCpuNanosStatistics(), statistics.getCpuNanosTrend(), statistics.getCount(), new Double((statistics.getCpuNanosStatistics().getSum()/statistics.getCount()) / ((statistics.getCpuNanosStatistics().getSum() + statistics.getTimeNanosStatistics().getSum())/statistics.getCount())), null);
                htmlTable.addStatistics(HtmlTable.CELL_TYPE_TIME, false, true, false, false, false, ApplicationUtilities.getMessage("label_Time") + ":", statistics.getTimeNanosStatistics(), statistics.getTimeNanosTrend(), statistics.getCount(), new Double(statistics.getTimeNanosStatistics().getSum() / (statistics.getCpuNanosStatistics().getSum() + statistics.getTimeNanosStatistics().getSum())), null);


                containerTable.addCell(htmlTable.getHtmlCode(), "detailSimpleLeftTable bodyPanel");
                //htmlTable.addStatisticsSeparator();

                htmlTable = new HtmlDetailTable(HtmlDetailTable.TYPE_SIMPLE, null, null, false, false, true, false, sessionBean.getResearchSamplingPeriod());

                htmlTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, false, false, false, true, false, ApplicationUtilities.getMessage("label_Users") + ":", statistics.getUsersStatistics(), statistics.getUsersTrend(), statistics.getCount());

                containerTable.addCell(htmlTable.getHtmlCode(), "bodyPanel");


                result = result  + containerTable.getHtmlCode();
            }

            //htmlTableContainer.addCell(HtmlUtilities.getPieChart("servletChart", plot, 350, 140), "detailValue");
        }

        if (!statisticsFound) {
            result = "";
        } else {

            String header = "";

            header = header + "<div class=\"bodySeparator\" title=\"Show / hide statistics\">";
            header = header + "<div class=\"bodySeparatorButton\">&#160;</div>";
            header = header + "</div>";

            header = header + "<div id=\"" + HomeServlet.COOKIE_CHART_ACTION_COLLAPSED + "\" class=\"boxContent detailTableContainer\" style=\"display: " + (sessionBean.isHomeActionCollapsed() ? "none" : "") + ";\">";
            header = header + "<h1 class=\"homeTitle action\">" + ApplicationUtilities.getMessage("label_ServletsJSFEJBJAXWSJDBCAndCustom") + "</h1>";
            //header = header + "<h2>" + sessionBean.getHomeResearchProject() + "</h2>";
            //header = header + "<br/>";

            result = header + result + "</div>";
        }


        PrintWriter out = response.getWriter();

        out.write(result);

    }

    private void ajaxRenderBodyJvm(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        try {
            JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

            sessionBean.setJvmStatisticsTotal(client.searchJvmTotalStatistics(new JvmParameters.SearchStatisticsParameter(ApplicationUtilities.getRegexPattern(sessionBean.getResearchContext(), false), sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo(), RequestParameter.GROUP_BY_CONTEXT, new TimePeriod(sessionBean.getResearchSamplingPeriod()), true)));

        } catch (JeeObserverServerException ex) {
            ApplicationUtilities.addError(request, ex.getMessage());
            throw new ServletException(ex.getMessage());
        }

        String result;
        if (!sessionBean.getJvmStatisticsTotal().isEmpty()) {

            JvmStatistics.TotalStatistics statistics = sessionBean.getJvmStatisticsTotal().iterator().next();

            HtmlTable containerTable = new HtmlTable(100, new int[]{33, 34, 33});

            //HtmlDetailTable heapMemoryTable = new HtmlDetailTable(HtmlDetailTable.TYPE_SIMPLE, null, HtmlUtilities.getLinkHtml(ApplicationUtilities.getMessage("label_HeapMemory"), request.getContextPath() + "/secure/jvm?action=show&chart=" + JvmServlet.CHART_HEAP_MEMORY_USED + "_" + JvmServlet.CHART_HEAP_MEMORY_COMMITTED + "&selected=" + statistics.getId(), ""), true, true, false, false, sessionBean.getResearchSamplingPeriod());
            HtmlDetailTable heapMemoryTable = new HtmlDetailTable(HtmlDetailTable.TYPE_SIMPLE, null, ApplicationUtilities.getMessage("label_HeapMemory"), true, true, false, false, sessionBean.getResearchSamplingPeriod());

            heapMemoryTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, false, true, true, false, true, ApplicationUtilities.getMessage("label_Used") + ":", statistics.getHeapMemoryUsedBytesStatistics(), statistics.getHeapMemoryUsedBytesTrend(), statistics.getCount(), new Double((statistics.getHeapMemoryUsedBytesStatistics().getSum() / statistics.getCount()) / (statistics.getHeapMemoryMaxBytesStatistics().getSum() / statistics.getCount())), null);
            heapMemoryTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, false, true, true, false, true, ApplicationUtilities.getMessage("label_Committed") + ":", statistics.getHeapMemoryCommittedBytesStatistics(), statistics.getHeapMemoryCommittedBytesTrend(), statistics.getCount(), new Double((statistics.getHeapMemoryCommittedBytesStatistics().getSum() / statistics.getCount()) / (statistics.getHeapMemoryMaxBytesStatistics().getSum() / statistics.getCount())), null);
            heapMemoryTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, false, true, true, false, true, ApplicationUtilities.getMessage("label_Total") + ":", statistics.getHeapMemoryMaxBytesStatistics(), statistics.getHeapMemoryMaxBytesTrend(), statistics.getCount());

            containerTable.addCell(heapMemoryTable.getHtmlCode(), "detailSimpleLeftTable bodyPanel");

            //HtmlDetailTable nonHeapMemoryTable = new HtmlDetailTable(HtmlDetailTable.TYPE_SIMPLE, null, HtmlUtilities.getLinkHtml(ApplicationUtilities.getMessage("label_NonHeapMemory"), request.getContextPath() + "/secure/jvm?action=show&chart=" + JvmServlet.CHART_NON_HEAP_MEMORY_USED + "_" + JvmServlet.CHART_NON_HEAP_MEMORY_COMMITTED + "&selected=" + statistics.getId(), ""), true, true, false, false, sessionBean.getResearchSamplingPeriod());
            HtmlDetailTable nonHeapMemoryTable = new HtmlDetailTable(HtmlDetailTable.TYPE_SIMPLE, null, ApplicationUtilities.getMessage("label_NonHeapMemory"), true, true, false, false, sessionBean.getResearchSamplingPeriod());

            nonHeapMemoryTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, false, true, true, false, true, ApplicationUtilities.getMessage("label_Used") + ":", statistics.getNonHeapMemoryUsedBytesStatistics(), statistics.getNonHeapMemoryUsedBytesTrend(), statistics.getCount(), new Double((statistics.getNonHeapMemoryUsedBytesStatistics().getSum() / statistics.getCount()) / (statistics.getNonHeapMemoryMaxBytesStatistics().getSum() / statistics.getCount())), null);
            nonHeapMemoryTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, false, true, true, false, true, ApplicationUtilities.getMessage("label_Committed") + ":", statistics.getNonHeapMemoryCommittedBytesStatistics(), statistics.getNonHeapMemoryCommittedBytesTrend(), statistics.getCount(), new Double((statistics.getNonHeapMemoryCommittedBytesStatistics().getSum() / statistics.getCount()) / (statistics.getNonHeapMemoryMaxBytesStatistics().getSum() / statistics.getCount())), null);
            nonHeapMemoryTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, false, true, true, false, true, ApplicationUtilities.getMessage("label_Total") + ":", statistics.getNonHeapMemoryMaxBytesStatistics(), statistics.getNonHeapMemoryMaxBytesTrend(), statistics.getCount());

            containerTable.addCell(nonHeapMemoryTable.getHtmlCode(), "detailSimpleLeftTable bodyPanel");

            //HtmlDetailTable cpuTable = new HtmlDetailTable(HtmlDetailTable.TYPE_SIMPLE, null, HtmlUtilities.getLinkHtml(ApplicationUtilities.getMessage("label_Cpu"), request.getContextPath() + "/secure/jvm?action=show&chart=" + JvmServlet.CHART_CPU_PERCENTAGE + "_" + JvmServlet.CHART_THREADS + "&selected=" + statistics.getId(), ""), true, true, false, false, sessionBean.getResearchSamplingPeriod());
            HtmlDetailTable cpuTable = new HtmlDetailTable(HtmlDetailTable.TYPE_SIMPLE, null, ApplicationUtilities.getMessage("label_Cpu"), true, true, false, false, sessionBean.getResearchSamplingPeriod());

            cpuTable.addStatistics(HtmlTable.CELL_TYPE_PERCENTAGE, false, true, true, false, true, ApplicationUtilities.getMessage("label_CPUUsage") + ":", statistics.getCpuPercentageStatistics(), statistics.getCpuPercentageTrend(), HtmlTable.CELL_TYPE_TIME, statistics.getCpuNanosStatistics(), statistics.getCpuNanosTrend(), statistics.getCount(), new Double(statistics.getCpuPercentageStatistics().getSum() / statistics.getCount()), null);

            cpuTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, false, true, true, false, true, ApplicationUtilities.getMessage("label_Threads") + ":", statistics.getThreadsStatistics(), statistics.getThreadsTrend(), statistics.getCount());
            cpuTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, false, true, true, false, true, ApplicationUtilities.getMessage("label_LoadedClasses") + ":", statistics.getLoadedClassesStatistics(), statistics.getLoadedClassesTrend(), statistics.getCount());

            containerTable.addCell(cpuTable.getHtmlCode(), "bodyPanel");

            HtmlDetailTable garbageCollectionsTable = new HtmlDetailTable(HtmlDetailTable.TYPE_SIMPLE, null, ApplicationUtilities.getMessage("label_GarbageCollections"), true, true, true, true, sessionBean.getResearchSamplingPeriod());

            garbageCollectionsTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, false, false, true, true, false, ApplicationUtilities.getMessage("label_GarbageCollections") + ":", statistics.getGarbageCollectionsStatistics(), statistics.getGarbageCollectionsTrend(), statistics.getCount());
            garbageCollectionsTable.addStatistics(HtmlTable.CELL_TYPE_TIME, false, false, true, true, false, ApplicationUtilities.getMessage("label_GarbageCollectionsTime") + ":", statistics.getGarbageCollectionNanosStatistics(), statistics.getGarbageCollectionNanosTrend(), statistics.getCount());

            //containerTable.addCell(garbageCollectionsTable.getHtmlCode(), "bodyPanel");
            result = containerTable.getHtmlCode();



            String header = "";

            header = header + "<div class=\"bodySeparator\" title=\"Show / hide statistics\">";
            header = header + "<div class=\"bodySeparatorButton\">&#160;</div>";
            header = header + "</div>";

            header = header + "<div id=\"" + HomeServlet.COOKIE_CHART_JVM_COLLAPSED + "\" class=\"boxContent detailTableContainer\" style=\"display: " + (sessionBean.isHomeJvmCollapsed() ? "none" : "") + ";\">";
            header = header + "<h1 class=\"homeTitle jvm\">" + ApplicationUtilities.getMessage("label_JavaVirtualMachine") + "</h1>";
            //header = header + "<h2>" + sessionBean.getResearchContext() + "</h2>";
            //header = header + "<br/>";

            result = header + result + "</div>";

        } else {
            result = "";
        }



        PrintWriter out = response.getWriter();

        out.write(result);

    }

    private void ajaxRenderBodyHardDisk(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        try {
            JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

            sessionBean.setHardDiskStatisticsTotal(client.searchHardDiskTotalStatistics(new HardDiskParameters.SearchStatisticsParameter(ApplicationUtilities.getRegexPattern(sessionBean.getResearchContext(), false), ApplicationUtilities.getRegexPattern(sessionBean.getResearchPath(), false), sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo(), RequestParameter.GROUP_BY_CONTEXT, new TimePeriod(sessionBean.getResearchSamplingPeriod()), true)));

        } catch (JeeObserverServerException ex) {
            ApplicationUtilities.addError(request, ex.getMessage());
            throw new ServletException(ex.getMessage());
        }

        String result;
        if (!sessionBean.getHardDiskStatisticsTotal().isEmpty()) {


            HardDiskStatistics.TotalStatistics statistics = sessionBean.getHardDiskStatisticsTotal().iterator().next();

            HtmlTable containerTable = new HtmlTable(100, new int[]{33, 34, 33});

            //HtmlDetailTable usageTable = new HtmlDetailTable(HtmlDetailTable.TYPE_SIMPLE, null, HtmlUtilities.getLinkHtml(ApplicationUtilities.getMessage("label_Usage"), request.getContextPath() + "/secure/hardDisk?action=show&chart=" + HardDiskServlet.CHART_USED + "_" + HardDiskServlet.CHART_TOTAL + "&selected=" + statistics.getId(), ""), true, true, false, false, sessionBean.getResearchSamplingPeriod());
            HtmlDetailTable usageTable = new HtmlDetailTable(HtmlDetailTable.TYPE_SIMPLE, null, ApplicationUtilities.getMessage("label_Usage"), true, true, false, false, sessionBean.getResearchSamplingPeriod());

            //usageTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, true, true, true, false, false, ApplicationUtilities.getMessage("label_Usable") + ":", usableBytesStatistics, usableBytesTrend, count);
            //usageTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, true, true, true, false, false, ApplicationUtilities.getMessage("label_Free") + ":", freeBytesStatistics, freeBytesTrend, count, new Double((freeBytesStatistics.getSum() / count) / (totalBytesStatistics.getSum() / count)), null);
            usageTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, false, true, true, false, false, ApplicationUtilities.getMessage("label_Used") + ":", statistics.getUsedBytesStatistics(), statistics.getUsedBytesTrend(), statistics.getCount(), new Double((statistics.getUsedBytesStatistics().getSum() / statistics.getCount()) / (statistics.getTotalBytesStatistics().getSum() / statistics.getCount())), null);
            usageTable.addStatistics(HtmlTable.CELL_TYPE_SIZE, false, true, true, false, false, ApplicationUtilities.getMessage("label_Total") + ":", statistics.getTotalBytesStatistics(), statistics.getTotalBytesTrend(), statistics.getCount());

            containerTable.addCell(usageTable.getHtmlCode(), "detailSimpleLeftTable bodyPanel");
            containerTable.addCell("&nbsp;", "bodyPanel");
            containerTable.addCell("&nbsp;", "bodyPanel");

            result = containerTable.getHtmlCode();


            String header = "";

            header = header + "<div class=\"bodySeparator\" title=\"Show / hide statistics\">";
            header = header + "<div class=\"bodySeparatorButton\">&#160;</div>";
            header = header + "</div>";

            header = header + "<div id=\"" + HomeServlet.COOKIE_CHART_HARD_DISK_COLLAPSED + "\" class=\"boxContent detailTableContainer\" style=\"display: " + (sessionBean.isHomeHardDiskCollapsed() ? "none" : "") + ";\">";
            header = header + "<h1 class=\"homeTitle hardDisk\">" + ApplicationUtilities.getMessage("label_HardDisks") + "</h1>";
            //header = header + "<h2>" + sessionBean.getHomeResearchPath() + "</h2>";
            //header = header + "<br/>";

            result = header + result + "</div>";

        } else {
            result = "";

        }

        PrintWriter out = response.getWriter();

        out.write(result);

    }

    private void ajaxRenderBodyHttpSession(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);


        try {
            JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

            sessionBean.setHttpSessionStatisticsTotal(client.searchHttpSessionTotalStatistics(new HttpSessionParameters.SearchStatisticsParameter(ApplicationUtilities.getRegexPattern(sessionBean.getResearchContext(), false), ApplicationUtilities.getRegexPattern(sessionBean.getResearchProject(), false), sessionBean.getResearchDateFrom(), sessionBean.getResearchDateTo(), RequestParameter.GROUP_BY_CONTEXT, new TimePeriod(sessionBean.getResearchSamplingPeriod()), true)));

        } catch (JeeObserverServerException ex) {
            ApplicationUtilities.addError(request, ex.getMessage());
            throw new ServletException(ex.getMessage());
        }

        String result;
        if (!sessionBean.getHttpSessionStatisticsTotal().isEmpty()) {

            HttpSessionStatistics.TotalStatistics statistics = sessionBean.getHttpSessionStatisticsTotal().iterator().next();

            HtmlTable containerTable = new HtmlTable(100, new int[]{33, 34, 33});

            //HtmlDetailTable sessionsTable = new HtmlDetailTable(HtmlDetailTable.TYPE_SIMPLE, null, HtmlUtilities.getLinkHtml(ApplicationUtilities.getMessage("label_Sessions"), request.getContextPath() + "/secure/httpSession?action=show&chart=" + HttpSessionServlet.CHART_SESSIONS_CREATED + "_" + HttpSessionServlet.CHART_SESSIONS_DESTROYED + "&selected=" + statistics.getId(), ""), false, false, true, false, sessionBean.getResearchSamplingPeriod());
            HtmlDetailTable sessionsTable = new HtmlDetailTable(HtmlDetailTable.TYPE_SIMPLE, null, ApplicationUtilities.getMessage("label_Sessions"), false, false, true, false, sessionBean.getResearchSamplingPeriod());

            sessionsTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, false, false, false, true, true, ApplicationUtilities.getMessage("label_Created") + ":", statistics.getSessionsCreatedStatistics(), statistics.getSessionsCreatedTrend(), statistics.getCount());
            sessionsTable.addStatistics(HtmlTable.CELL_TYPE_INTEGER, false, false, false, true, true, ApplicationUtilities.getMessage("label_Closed") + ":", statistics.getSessionsDestroyedStatistics(), statistics.getSessionsDestroyedTrend(), statistics.getCount(), new Double((statistics.getSessionsDestroyedStatistics().getSum() / statistics.getCount()) / (statistics.getSessionsCreatedStatistics().getSum() / statistics.getCount())), null);

            containerTable.addCell(sessionsTable.getHtmlCode(), "detailSimpleLeftTable bodyPanel");

            //HtmlDetailTable durationTable = new HtmlDetailTable(HtmlDetailTable.TYPE_SIMPLE, null, HtmlUtilities.getLinkHtml(ApplicationUtilities.getMessage("label_Duration"), request.getContextPath() + "/secure/httpSession?action=show&chart_statistics=" + HttpSessionServlet.CHART_SESSIONS_DESTROYED_TIME_AVERAGE, ""), true, true, false, false, sessionBean.getResearchSamplingPeriod());
            HtmlDetailTable durationTable = new HtmlDetailTable(HtmlDetailTable.TYPE_SIMPLE, null, ApplicationUtilities.getMessage("label_Duration"), true, true, false, false, sessionBean.getResearchSamplingPeriod());

            durationTable.addStatistics(HtmlTable.CELL_TYPE_TIME, false, true, false, false, false, ApplicationUtilities.getMessage("label_Time") + ":", statistics.getSessionsDestroyedTimeNanosStatistics(), statistics.getSessionsDestroyedTimeNanosTrend(), statistics.getCount());

            containerTable.addCell(durationTable.getHtmlCode(), "detailSimpleLeftTable bodyPanel");
            containerTable.addCell("&nbsp;", "bodyPanel");

            result = containerTable.getHtmlCode();


            String header = "";

            header = header + "<div class=\"bodySeparator\" title=\"Show / hide statistics\">";
            header = header + "<div class=\"bodySeparatorButton\">&#160;</div>";
            header = header + "</div>";

            header = header + "<div id=\"" + HomeServlet.COOKIE_CHART_HTTP_SESSION_COLLAPSED + "\" class=\"boxContent detailTableContainer\" style=\"display: " + (sessionBean.isHomeHttpSessionCollapsed() ? "none" : "") + ";\">";
            header = header + "<h1 class=\"homeTitle httpSession\">" + ApplicationUtilities.getMessage("label_HTTPSessions") + "</h1>";
            //header = header + "<h2>" + sessionBean.getHomeResearchProject() + "</h2>";
            //header = header + "<br/>";

            result = header + result + "</div>";

        } else {
            result = "";
        }

        PrintWriter out = response.getWriter();

        out.write(result);

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
