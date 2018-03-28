package login;

import action.ActionServlet;
import hardDisk.HardDiskServlet;
import home.HomeServlet;
import httpSession.HttpSessionServlet;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Locale;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jeeobserver.server.JeeObserverClient;
import jeeobserver.server.JeeObserverServerContext;
import jeeobserver.server.JeeObserverServerException;
import jeeobserver.server.RequestParameter;
import jeeobserver.server.TimePeriod;
import jvm.JvmServlet;
import menu.ResearchFilterServlet;
import rule.RuleServlet;
import utilities.ApplicationUtilities;
import utilities.CommonServlet;

public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String COOKIE_HOST = "host";

    public static final String COOKIE_PORT = "port";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action.equals("login")) {
            this.actionLogin(request, response);
        } else if (action.equals("logout")) {
            this.actionLogout(request, response);
        } else {
            throw new ServletException(String.format("Action parameter '%s' unknown.", action));
        }
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
        return "Login Servlet";
    }

    private void actionLogin(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        try {
            String host = request.getParameter("host");

            if (host == null || host.equals("")) {
                throw new Exception("Host is a mandatory field");
            }

            String port = request.getParameter("port");

            if (port == null || port.equals("")) {
                throw new Exception("Port is a mandatory field");
            }


            ApplicationSessionBean sessionBean = new ApplicationSessionBean();

            sessionBean.setHost(host);
            sessionBean.setPort(Integer.parseInt(port));

            sessionBean.setLocale(Locale.getDefault());

            sessionBean.setMenuHomeEnabled(true);
            sessionBean.setMenuStatisticsEnabled(true);
            sessionBean.setMenuSearchEnabled(false);
            sessionBean.setMenuRuleEnabled(true);
            sessionBean.setMenuExitEnabled(true);


            ApplicationUtilities.addCookieInteger("port", sessionBean.getPort(), request, response);
            ApplicationUtilities.addCookieString("host", sessionBean.getHost(), request, response);

            //Research filter
            Calendar calendar = new GregorianCalendar();

            calendar.set(Calendar.HOUR_OF_DAY, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            sessionBean.setResearchDateFrom(calendar.getTime());

            calendar.add(Calendar.DAY_OF_YEAR, 1);

            sessionBean.setResearchDateTo(calendar.getTime());

            sessionBean.setResearchContext(ApplicationUtilities.getCookieString(ResearchFilterServlet.COOKIE_FILTER_CONTEXT, request));
            sessionBean.setResearchProject(ApplicationUtilities.getCookieString(ResearchFilterServlet.COOKIE_FILTER_PROJECT, request));

            sessionBean.setResearchPath(ApplicationUtilities.getCookieString(ResearchFilterServlet.COOKIE_FILTER_PATH, request));

            sessionBean.setResearchElement(ApplicationUtilities.getCookieString(ResearchFilterServlet.COOKIE_FILTER_ELEMENT, request));
            if (sessionBean.getResearchElement() == null) {
                sessionBean.setResearchElement("");
            }


            Boolean regex = ApplicationUtilities.getCookieBoolean(ResearchFilterServlet.COOKIE_FILTER_ELEMENT_REGEX, request);
            if (regex != null) {
                sessionBean.setResearchElementRegex(regex);
            } else {
                sessionBean.setResearchElementRegex(false);
            }

            Integer samplingPeriod = ApplicationUtilities.getCookieInteger(ResearchFilterServlet.COOKIE_FILTER_SAMPLING_PERIOD, request);
            if (samplingPeriod == null) {
                sessionBean.setResearchSamplingPeriod(TimePeriod.UNIT_HOUR);
            } else {
                sessionBean.setResearchSamplingPeriod(samplingPeriod.intValue());
            }

            Integer grouping = ApplicationUtilities.getCookieInteger(ResearchFilterServlet.COOKIE_FILTER_GROUPING, request);
            if (grouping == null) {
                sessionBean.setResearchGrouping(RequestParameter.GROUP_BY_CONTEXT_PROJECT);
            } else {
                sessionBean.setResearchGrouping(grouping.intValue());
            }

            //Load all cookies

            //Hard Disk
            sessionBean.setHardDiskResultSelected(ApplicationUtilities.getCookieInteger(HardDiskServlet.COOKIE_RESULT_SELECTED, request));

            String hardDiskStatisticsSelected[] = ApplicationUtilities.getCookieStringArray(HardDiskServlet.COOKIE_STATISTICS_SELECTED, request);
            if (hardDiskStatisticsSelected != null) {
                sessionBean.setHardDiskStatisticsSelected(new HashSet(Arrays.asList(hardDiskStatisticsSelected)));
            } else {
                sessionBean.setHardDiskStatisticsSelected(new HashSet<String>());
            }

            Integer hardDiskResultsExpanded[] = ApplicationUtilities.getCookieIntegerArray(HardDiskServlet.COOKIE_RESULTS_EXPANDED, request);
            if (hardDiskResultsExpanded != null) {
                sessionBean.setHardDiskResultsExpanded(new HashSet(Arrays.asList(hardDiskResultsExpanded)));
            } else {
                sessionBean.setHardDiskResultsExpanded(new HashSet<Integer>());
            }

            sessionBean.setHardDiskResultsSortColumn(ApplicationUtilities.getCookieInteger(HardDiskServlet.COOKIE_RESULTS_SORT_COLUMN, request));
            sessionBean.setHardDiskResultsSortOrder(ApplicationUtilities.getCookieInteger(HardDiskServlet.COOKIE_RESULTS_SORT_ORDER, request));


            //Http Session
            sessionBean.setHttpSessionResultSelected(ApplicationUtilities.getCookieInteger(HttpSessionServlet.COOKIE_RESULT_SELECTED, request));

            String httpSessionStatisticsSelected[] = ApplicationUtilities.getCookieStringArray(HttpSessionServlet.COOKIE_STATISTICS_SELECTED, request);
            if (httpSessionStatisticsSelected != null) {
                sessionBean.setHttpSessionStatisticsSelected(new HashSet(Arrays.asList(httpSessionStatisticsSelected)));
            } else {
                sessionBean.setHttpSessionStatisticsSelected(new HashSet<String>());
            }

            Integer httpSessionResultsExpanded[] = ApplicationUtilities.getCookieIntegerArray(HttpSessionServlet.COOKIE_RESULTS_EXPANDED, request);
            if (httpSessionResultsExpanded != null) {
                sessionBean.setHttpSessionResultsExpanded(new HashSet(Arrays.asList(httpSessionResultsExpanded)));
            } else {
                sessionBean.setHttpSessionResultsExpanded(new HashSet<Integer>());
            }

            sessionBean.setHttpSessionResultsSortColumn(ApplicationUtilities.getCookieInteger(HttpSessionServlet.COOKIE_RESULTS_SORT_COLUMN, request));
            sessionBean.setHttpSessionResultsSortOrder(ApplicationUtilities.getCookieInteger(HttpSessionServlet.COOKIE_RESULTS_SORT_ORDER, request));


            //JVM
            sessionBean.setJvmResultSelected(ApplicationUtilities.getCookieInteger(JvmServlet.COOKIE_RESULT_SELECTED, request));

            String jvmStatisticsSelected[] = ApplicationUtilities.getCookieStringArray(JvmServlet.COOKIE_STATISTICS_SELECTED, request);
            if (jvmStatisticsSelected != null) {
                sessionBean.setJvmStatisticsSelected(new HashSet(Arrays.asList(jvmStatisticsSelected)));
            } else {
                sessionBean.setJvmStatisticsSelected(new HashSet<String>());
            }

            sessionBean.setJvmResultsSortColumn(ApplicationUtilities.getCookieInteger(JvmServlet.COOKIE_RESULTS_SORT_COLUMN, request));
            sessionBean.setJvmResultsSortOrder(ApplicationUtilities.getCookieInteger(JvmServlet.COOKIE_RESULTS_SORT_ORDER, request));


            //Action
            sessionBean.setActionServletResultSelected(ApplicationUtilities.getCookieInteger(ActionServlet.COOKIE_RESULT_SELECTED_SERVLET, request));
            sessionBean.setActionEjbResultSelected(ApplicationUtilities.getCookieInteger(ActionServlet.COOKIE_RESULT_SELECTED_EJB, request));
            sessionBean.setActionJsfResultSelected(ApplicationUtilities.getCookieInteger(ActionServlet.COOKIE_RESULT_SELECTED_JSF, request));
            sessionBean.setActionJdbcResultSelected(ApplicationUtilities.getCookieInteger(ActionServlet.COOKIE_RESULT_SELECTED_JDBC, request));
            sessionBean.setActionJaxWsResultSelected(ApplicationUtilities.getCookieInteger(ActionServlet.COOKIE_RESULT_SELECTED_JAXWS, request));
            sessionBean.setActionCustomResultSelected(ApplicationUtilities.getCookieInteger(ActionServlet.COOKIE_RESULT_SELECTED_CUSTOM, request));

            String actionStatisticsSelected[] = ApplicationUtilities.getCookieStringArray(ActionServlet.COOKIE_STATISTICS_SELECTED, request);
            if (actionStatisticsSelected != null) {
                sessionBean.setActionStatisticsSelected(new HashSet(Arrays.asList(actionStatisticsSelected)));
            } else {
                sessionBean.setActionStatisticsSelected(new HashSet<String>());
            }

            Integer actionResultsExpanded[] = ApplicationUtilities.getCookieIntegerArray(ActionServlet.COOKIE_RESULTS_EXPANDED, request);
            if (actionResultsExpanded != null) {
                sessionBean.setActionResultsExpanded(new HashSet(Arrays.asList(actionResultsExpanded)));
            } else {
                sessionBean.setActionResultsExpanded(new HashSet<Integer>());
            }

            sessionBean.setActionResultsSortColumn(ApplicationUtilities.getCookieInteger(ActionServlet.COOKIE_RESULTS_SORT_COLUMN, request));
            sessionBean.setActionResultsSortOrder(ApplicationUtilities.getCookieInteger(ActionServlet.COOKIE_RESULTS_SORT_ORDER, request));



            //Rules
            Integer rulesResultsExpanded[] = ApplicationUtilities.getCookieIntegerArray(RuleServlet.COOKIE_RESULTS_EXPANDED, request);
            if (rulesResultsExpanded != null) {
                sessionBean.setRuleResultsExpanded(new HashSet(Arrays.asList(rulesResultsExpanded)));
            } else {
                sessionBean.setRuleResultsExpanded(new HashSet<Integer>());
            }

            sessionBean.setRuleResultsSortColumn(ApplicationUtilities.getCookieInteger(RuleServlet.COOKIE_RESULTS_SORT_COLUMN, request));
            sessionBean.setRuleResultsSortOrder(ApplicationUtilities.getCookieInteger(RuleServlet.COOKIE_RESULTS_SORT_ORDER, request));



            //Home
            sessionBean.setHomeActionCollapsed(ApplicationUtilities.getCookieBoolean(HomeServlet.COOKIE_CHART_ACTION_COLLAPSED, request));
            sessionBean.setHomeJvmCollapsed(ApplicationUtilities.getCookieBoolean(HomeServlet.COOKIE_CHART_JVM_COLLAPSED, request));
            sessionBean.setHomeHttpSessionCollapsed(ApplicationUtilities.getCookieBoolean(HomeServlet.COOKIE_CHART_HTTP_SESSION_COLLAPSED, request));
            sessionBean.setHomeHardDiskCollapsed(ApplicationUtilities.getCookieBoolean(HomeServlet.COOKIE_CHART_HARD_DISK_COLLAPSED, request));

            //Common
            sessionBean.setChartCollapsed(ApplicationUtilities.getCookieBoolean(CommonServlet.COOKIE_CHART_COLLAPSED, request));
            sessionBean.setResultsCollapsed(ApplicationUtilities.getCookieBoolean(CommonServlet.COOKIE_RESULTS_COLLAPSED, request));


            ApplicationUtilities.setApplicationSessionBean(request, sessionBean);


            //Try server connection
            try {
                JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

                JeeObserverServerContext context = client.getServerContext();
                sessionBean.setSamplingPeriod(context.getSamplingPeriod());

            } catch (JeeObserverServerException ex) {
                ApplicationUtilities.addError(request, ex.getMessage());
                throw new ServletException(ex.getMessage());
            }


            response.sendRedirect(request.getContextPath() + "/secure/home?action=show");

            //RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/secure/home?action=show");
            //dispatcher.forward(request, response);


        } catch (Exception e) {
            ApplicationUtilities.removeApplicationSessionBean(request);

            ApplicationUtilities.addError(request, e.getMessage());
            e.printStackTrace(System.out);

            RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/login.jsp");
            dispatcher.forward(request, response);
        }

    }

    private void actionLogout(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.getSession().invalidate();
        ApplicationUtilities.removeApplicationSessionBean(request);
        response.sendRedirect(request.getContextPath() + "/");
    }
}
