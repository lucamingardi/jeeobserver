package utilities;

import action.ActionServlet;
import hardDisk.HardDiskServlet;
import home.HomeServlet;
import httpSession.HttpSessionServlet;
import java.awt.Color;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jeeobserver.ActionObserver;
import jvm.JvmServlet;
import login.ApplicationSessionBean;
import menu.ResearchFilterServlet;
import rule.RuleServlet;

public class CommonServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String SERVLET = "common";

    public static final String COOKIE_RESULTS_COLLAPSED = SERVLET + "ResultsCollapsed";

    public static final String COOKIE_CHART_COLLAPSED = SERVLET + "ChartCollapsed";

    public static final Color CHART_COLOR_RED_1 = Color.decode("#D90B0B");

    public static final Color CHART_COLOR_RED_2 = Color.decode("#FF4C4C");

    public static final Color CHART_COLOR_RED_3 = Color.decode("#FFD1D1");

    public static final Color CHART_COLOR_ORANGE_1 = Color.decode("#FF7D26");

    public static final Color CHART_COLOR_ORANGE_2 = Color.decode("#FFC46B");

    public static final Color CHART_COLOR_ORANGE_3 = Color.decode("#FFECCF");

    public static final Color CHART_COLOR_GREEN_1 = Color.decode("#9BE800");

    public static final Color CHART_COLOR_GREEN_2 = Color.decode("#C7FF57");

    public static final Color CHART_COLOR_GREEN_3 = Color.decode("#E0FFA3");

    public static final Color CHART_COLOR_BLUE_1 = Color.decode("#0000FF");

    public static final Color CHART_COLOR_BLUE_2 = Color.decode("#6E81FF");

    public static final Color CHART_COLOR_BLUE_3 = Color.decode("#E5E8FF");

    public static final Color CHART_COLOR_PURPLE_1 = Color.decode("#660066");

    public static final Color CHART_COLOR_PURPLE_2 = Color.decode("#990066");

    public static final Color CHART_COLOR_PURPLE_3 = Color.decode("#FF0066");

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        String action = request.getParameter("action");

        if (action == null || action.equals("")) {
            throw new ServletException("Action is a mandatory parameter");
        } else if (action.equals("ajaxActionSelectResultsRow")) {
            this.ajaxActionSelectResultsRow(request, response);
        } else if (action.equals("ajaxActionExpandResultsRow")) {
            this.ajaxActionExpandResultsRow(request, response);
        } else if (action.equals("ajaxActionSelectStatistics")) {
            this.ajaxActionSelectStatistics(request, response);
        } else if (action.equals("ajaxActionTogglePanel")) {
            this.ajaxActionTogglePanel(request, response);
        } else if (action.equals("ajaxActionSortResults")) {
            this.ajaxActionSortResults(request, response);
        } else if (action.equals("ajaxActionSelectSamplingPeriod")) {
            this.ajaxActionSelectSamplingPeriod(request, response);
        } else if (action.equals("ajaxActionSelectLogScale")) {
            this.ajaxActionSelectLogScale(request, response);
        } else if (action.equals("ajaxActionSelectTrendLines")) {
            this.ajaxActionSelectTrendLines(request, response);
        } else {
            throw new ServletException("Action parameter unknown.");
        }
    }

    //Ajax Actions
    private void ajaxActionSortResults(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);
        if (sessionBean.getCurrentServlet().equals(HardDiskServlet.SERVLET)) {
            //Set column and order
            sessionBean.setHardDiskResultsSortColumn(ApplicationUtilities.getParameterInteger("resultsSortColumn", request));
            sessionBean.setHardDiskResultsSortOrder(ApplicationUtilities.getParameterInteger("resultsSortOrder", request));
            //Save to cookie
            ApplicationUtilities.addCookieInteger(HardDiskServlet.COOKIE_RESULTS_SORT_COLUMN, sessionBean.getHardDiskResultsSortColumn(), request, response);
            ApplicationUtilities.addCookieInteger(HardDiskServlet.COOKIE_RESULTS_SORT_ORDER, sessionBean.getHardDiskResultsSortOrder(), request, response);
        } else if (sessionBean.getCurrentServlet().equals(HttpSessionServlet.SERVLET)) {
            //Set column and order
            sessionBean.setHttpSessionResultsSortColumn(ApplicationUtilities.getParameterInteger("resultsSortColumn", request));
            sessionBean.setHttpSessionResultsSortOrder(ApplicationUtilities.getParameterInteger("resultsSortOrder", request));
            //Save to cookie
            ApplicationUtilities.addCookieInteger(HttpSessionServlet.COOKIE_RESULTS_SORT_COLUMN, sessionBean.getHttpSessionResultsSortColumn(), request, response);
            ApplicationUtilities.addCookieInteger(HttpSessionServlet.COOKIE_RESULTS_SORT_ORDER, sessionBean.getHttpSessionResultsSortOrder(), request, response);
        } else if (sessionBean.getCurrentServlet().equals(JvmServlet.SERVLET)) {
            //Set column and order
            sessionBean.setJvmResultsSortColumn(ApplicationUtilities.getParameterInteger("resultsSortColumn", request));
            sessionBean.setJvmResultsSortOrder(ApplicationUtilities.getParameterInteger("resultsSortOrder", request));
            //Save to cookie
            ApplicationUtilities.addCookieInteger(JvmServlet.COOKIE_RESULTS_SORT_COLUMN, sessionBean.getJvmResultsSortColumn(), request, response);
            ApplicationUtilities.addCookieInteger(JvmServlet.COOKIE_RESULTS_SORT_ORDER, sessionBean.getJvmResultsSortOrder(), request, response);
        } else if (sessionBean.getCurrentServlet().equals(ActionServlet.SERVLET)) {
            //Set column and order
            sessionBean.setActionResultsSortColumn(ApplicationUtilities.getParameterInteger("resultsSortColumn", request));
            sessionBean.setActionResultsSortOrder(ApplicationUtilities.getParameterInteger("resultsSortOrder", request));
            //Save to cookie
            ApplicationUtilities.addCookieInteger(ActionServlet.COOKIE_RESULTS_SORT_COLUMN, sessionBean.getActionResultsSortColumn(), request, response);
            ApplicationUtilities.addCookieInteger(ActionServlet.COOKIE_RESULTS_SORT_ORDER, sessionBean.getActionResultsSortOrder(), request, response);
        } else if (sessionBean.getCurrentServlet().equals(RuleServlet.SERVLET)) {
            //Set column and order
            sessionBean.setRuleResultsSortColumn(ApplicationUtilities.getParameterInteger("resultsSortColumn", request));
            sessionBean.setRuleResultsSortOrder(ApplicationUtilities.getParameterInteger("resultsSortOrder", request));
            //Save to cookie
            ApplicationUtilities.addCookieInteger(RuleServlet.COOKIE_RESULTS_SORT_COLUMN, sessionBean.getRuleResultsSortColumn(), request, response);
            ApplicationUtilities.addCookieInteger(RuleServlet.COOKIE_RESULTS_SORT_ORDER, sessionBean.getRuleResultsSortOrder(), request, response);
        }
    }

    private void ajaxActionSelectResultsRow(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        if (sessionBean.getCurrentServlet().equals(HardDiskServlet.SERVLET)) {
            sessionBean.setHardDiskResultSelected(ApplicationUtilities.getParameterInteger("selectedId", request));
            HardDiskServlet.searchTimeStatistics(request);
            //Save to cookie
            ApplicationUtilities.addCookieInteger(HardDiskServlet.COOKIE_RESULT_SELECTED, sessionBean.getHardDiskResultSelected(), request, response);
        } else if (sessionBean.getCurrentServlet().equals(HttpSessionServlet.SERVLET)) {
            sessionBean.setHttpSessionResultSelected(ApplicationUtilities.getParameterInteger("selectedId", request));
            HttpSessionServlet.searchTimeStatistics(request);
            //Save to cookie
            ApplicationUtilities.addCookieInteger(HttpSessionServlet.COOKIE_RESULT_SELECTED, sessionBean.getHttpSessionResultSelected(), request, response);
        } else if (sessionBean.getCurrentServlet().equals(JvmServlet.SERVLET)) {
            sessionBean.setJvmResultSelected(ApplicationUtilities.getParameterInteger("selectedId", request));
            JvmServlet.searchTimeStatistics(request);
            //Save to cookie
            ApplicationUtilities.addCookieInteger(JvmServlet.COOKIE_RESULT_SELECTED, sessionBean.getJvmResultSelected(), request, response);
        } else if (sessionBean.getCurrentServlet().equals(ActionServlet.SERVLET)) {

            if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_SERVLET) {
                sessionBean.setActionServletResultSelected(ApplicationUtilities.getParameterInteger("selectedId", request));
                ApplicationUtilities.addCookieInteger(ActionServlet.COOKIE_RESULT_SELECTED_SERVLET, sessionBean.getActionServletResultSelected(), request, response);
            } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JSF) {
                sessionBean.setActionJsfResultSelected(ApplicationUtilities.getParameterInteger("selectedId", request));
                ApplicationUtilities.addCookieInteger(ActionServlet.COOKIE_RESULT_SELECTED_JSF, sessionBean.getActionJsfResultSelected(), request, response);
            } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_EJB) {
                sessionBean.setActionEjbResultSelected(ApplicationUtilities.getParameterInteger("selectedId", request));
                ApplicationUtilities.addCookieInteger(ActionServlet.COOKIE_RESULT_SELECTED_EJB, sessionBean.getActionEjbResultSelected(), request, response);
            } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JAXWS) {
                sessionBean.setActionJaxWsResultSelected(ApplicationUtilities.getParameterInteger("selectedId", request));
                ApplicationUtilities.addCookieInteger(ActionServlet.COOKIE_RESULT_SELECTED_JAXWS, sessionBean.getActionJaxWsResultSelected(), request, response);
            } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_JDBC) {
                sessionBean.setActionJdbcResultSelected(ApplicationUtilities.getParameterInteger("selectedId", request));
                ApplicationUtilities.addCookieInteger(ActionServlet.COOKIE_RESULT_SELECTED_JDBC, sessionBean.getActionJdbcResultSelected(), request, response);
            } else if (sessionBean.getResearchCategory() == ActionObserver.CATEGORY_CUSTOM) {
                sessionBean.setActionCustomResultSelected(ApplicationUtilities.getParameterInteger("selectedId", request));
                ApplicationUtilities.addCookieInteger(ActionServlet.COOKIE_RESULT_SELECTED_CUSTOM, sessionBean.getActionCustomResultSelected(), request, response);
            }

            ActionServlet.searchTimeStatistics(request);

        }

    }

    private void ajaxActionExpandResultsRow(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        Integer expandedId = ApplicationUtilities.getParameterInteger("expandedId", request);
        if (sessionBean.getCurrentServlet().equals(HardDiskServlet.SERVLET)) {
            if (sessionBean.getHardDiskResultsExpanded().contains(expandedId)) {
                sessionBean.getHardDiskResultsExpanded().remove(expandedId);
            } else {
                sessionBean.getHardDiskResultsExpanded().add(expandedId);
            }
            //Save to cookie
            ApplicationUtilities.addCookieIntegerArray(HardDiskServlet.COOKIE_RESULTS_EXPANDED, sessionBean.getHardDiskResultsExpanded().toArray(new Integer[0]), request, response);
        } else if (sessionBean.getCurrentServlet().equals(HttpSessionServlet.SERVLET)) {
            if (sessionBean.getHttpSessionResultsExpanded().contains(expandedId)) {
                sessionBean.getHttpSessionResultsExpanded().remove(expandedId);
            } else {
                sessionBean.getHttpSessionResultsExpanded().add(expandedId);
            }
            //Save to cookie
            ApplicationUtilities.addCookieIntegerArray(HttpSessionServlet.COOKIE_RESULTS_EXPANDED, sessionBean.getHttpSessionResultsExpanded().toArray(new Integer[0]), request, response);
        } else if (sessionBean.getCurrentServlet().equals(JvmServlet.SERVLET)) {
            if (sessionBean.getJvmResultsExpanded().contains(expandedId)) {
                sessionBean.getJvmResultsExpanded().remove(expandedId);
            } else {
                sessionBean.getJvmResultsExpanded().add(expandedId);
            }
            //Save to cookie
            ApplicationUtilities.addCookieIntegerArray(JvmServlet.COOKIE_RESULTS_EXPANDED, sessionBean.getActionResultsExpanded().toArray(new Integer[0]), request, response);
        } else if (sessionBean.getCurrentServlet().equals(ActionServlet.SERVLET)) {
            if (sessionBean.getActionResultsExpanded().contains(expandedId)) {
                sessionBean.getActionResultsExpanded().remove(expandedId);
            } else {
                sessionBean.getActionResultsExpanded().add(expandedId);
            }
            //Save to cookie
            ApplicationUtilities.addCookieIntegerArray(ActionServlet.COOKIE_RESULTS_EXPANDED, sessionBean.getActionResultsExpanded().toArray(new Integer[0]), request, response);
        } else if (sessionBean.getCurrentServlet().equals(RuleServlet.SERVLET)) {
            if (sessionBean.getRuleResultsExpanded().contains(expandedId)) {
                sessionBean.getRuleResultsExpanded().remove(expandedId);
            } else {
                sessionBean.getRuleResultsExpanded().add(expandedId);
            }
            //Save to cookie
            ApplicationUtilities.addCookieIntegerArray(RuleServlet.COOKIE_RESULTS_EXPANDED, sessionBean.getRuleResultsExpanded().toArray(new Integer[0]), request, response);
        }

    }

    private void ajaxActionSelectSamplingPeriod(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        if (ApplicationUtilities.getParameterInteger("selectedSamplingPeriod", request) != null) {
            sessionBean.setResearchSamplingPeriod(ApplicationUtilities.getParameterInteger("selectedSamplingPeriod", request));
        }

        if (sessionBean.getCurrentServlet().equals(HardDiskServlet.SERVLET)) {
            HardDiskServlet.searchTimeStatistics(request);
            HardDiskServlet.searchTotalStatistics(request);
        } else if (sessionBean.getCurrentServlet().equals(HttpSessionServlet.SERVLET)) {
            HttpSessionServlet.searchTimeStatistics(request);
            HttpSessionServlet.searchTotalStatistics(request);
        } else if (sessionBean.getCurrentServlet().equals(JvmServlet.SERVLET)) {
            JvmServlet.searchTimeStatistics(request);
            JvmServlet.searchTotalStatistics(request);
        } else if (sessionBean.getCurrentServlet().equals(ActionServlet.SERVLET)) {
            ActionServlet.searchTimeStatistics(request);
            ActionServlet.searchTotalStatistics(request);
        }

        //Save to cookie
        ApplicationUtilities.addCookieInteger(ResearchFilterServlet.COOKIE_FILTER_SAMPLING_PERIOD, sessionBean.getResearchSamplingPeriod(), request, response);
    }

    private void ajaxActionSelectLogScale(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        sessionBean.setResearchLogScale(ApplicationUtilities.getParameterBoolean("selectedLogScale", request, false));

        //Save to cookie
        //ApplicationUtilities.addCookieInteger(ResearchFilterServlet.COOKIE_FILTER_LOG_SCALE, sessionBean.getResearchSamplingPeriod(), request, response);
    }

    private void ajaxActionSelectTrendLines(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        sessionBean.setResearchTrendLines(ApplicationUtilities.getParameterBoolean("selectedTrendLines", request, false));

        //Save to cookie
        //ApplicationUtilities.addCookieInteger(ResearchFilterServlet.COOKIE_FILTER_LOG_SCALE, sessionBean.getResearchSamplingPeriod(), request, response);
    }

    private void ajaxActionSelectStatistics(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        String selectedStatistics = ApplicationUtilities.getParameterString("selectedStatistics", request);
        if (sessionBean.getCurrentServlet().equals(HardDiskServlet.SERVLET)) {
            if (sessionBean.getHardDiskStatisticsSelected().contains(selectedStatistics)) {
                if (sessionBean.getHardDiskStatisticsSelected().size() > 1) {
                    sessionBean.getHardDiskStatisticsSelected().remove(selectedStatistics);
                }
            } else {
                sessionBean.getHardDiskStatisticsSelected().add(selectedStatistics);
            }
            //Save to cookie
            ApplicationUtilities.addCookieStringArray(HardDiskServlet.COOKIE_STATISTICS_SELECTED, sessionBean.getHardDiskStatisticsSelected().toArray(new String[0]), request, response);
        } else if (sessionBean.getCurrentServlet().equals(HttpSessionServlet.SERVLET)) {
            if (sessionBean.getHttpSessionStatisticsSelected().contains(selectedStatistics)) {
                if (sessionBean.getHttpSessionStatisticsSelected().size() > 1) {
                    sessionBean.getHttpSessionStatisticsSelected().remove(selectedStatistics);
                }
            } else {
                sessionBean.getHttpSessionStatisticsSelected().add(selectedStatistics);
            }
            //Save to cookie
            ApplicationUtilities.addCookieStringArray(HttpSessionServlet.COOKIE_STATISTICS_SELECTED, sessionBean.getHttpSessionStatisticsSelected().toArray(new String[0]), request, response);
        } else if (sessionBean.getCurrentServlet().equals(JvmServlet.SERVLET)) {
            if (sessionBean.getJvmStatisticsSelected().contains(selectedStatistics)) {

                if (sessionBean.getJvmStatisticsSelected().size() > 1) {
                    sessionBean.getJvmStatisticsSelected().remove(selectedStatistics);
                }
            } else {
                sessionBean.getJvmStatisticsSelected().add(selectedStatistics);
            }
            //Save to cookie
            ApplicationUtilities.addCookieStringArray(JvmServlet.COOKIE_STATISTICS_SELECTED, sessionBean.getJvmStatisticsSelected().toArray(new String[0]), request, response);
        } else if (sessionBean.getCurrentServlet().equals(ActionServlet.SERVLET)) {
            if (sessionBean.getActionStatisticsSelected().contains(selectedStatistics)) {
                if (sessionBean.getActionStatisticsSelected().size() > 1) {
                    sessionBean.getActionStatisticsSelected().remove(selectedStatistics);
                }
            } else {
                sessionBean.getActionStatisticsSelected().add(selectedStatistics);
            }
            //Save to cookie
            ApplicationUtilities.addCookieStringArray(ActionServlet.COOKIE_STATISTICS_SELECTED, sessionBean.getActionStatisticsSelected().toArray(new String[0]), request, response);
        }

    }

    private void ajaxActionTogglePanel(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        String panel = ApplicationUtilities.getParameterString("panel", request);
        Boolean value = ApplicationUtilities.getParameterBoolean("value", request, false);
        //Save to cookie
        if (panel.equals("bodyResultsTable")) {
            sessionBean.setResultsCollapsed(Boolean.valueOf(value));
            ApplicationUtilities.addCookieBoolean(CommonServlet.COOKIE_RESULTS_COLLAPSED, value, request, response);
        } else if (panel.equals("bodyChartPanel")) {
            sessionBean.setChartCollapsed(Boolean.valueOf(value));
            ApplicationUtilities.addCookieBoolean(CommonServlet.COOKIE_CHART_COLLAPSED, value, request, response);
        } else if (panel.equals(HomeServlet.COOKIE_CHART_ACTION_COLLAPSED)) {
            sessionBean.setHomeActionCollapsed(Boolean.valueOf(value));
            ApplicationUtilities.addCookieBoolean(HomeServlet.COOKIE_CHART_ACTION_COLLAPSED, value, request, response);
        } else if (panel.equals(HomeServlet.COOKIE_CHART_JVM_COLLAPSED)) {
            sessionBean.setHomeJvmCollapsed(Boolean.valueOf(value));
            ApplicationUtilities.addCookieBoolean(HomeServlet.COOKIE_CHART_JVM_COLLAPSED, value, request, response);
        } else if (panel.equals(HomeServlet.COOKIE_CHART_HTTP_SESSION_COLLAPSED)) {
            sessionBean.setHomeHttpSessionCollapsed(Boolean.valueOf(value));
            ApplicationUtilities.addCookieBoolean(HomeServlet.COOKIE_CHART_HTTP_SESSION_COLLAPSED, value, request, response);
        } else if (panel.equals(HomeServlet.COOKIE_CHART_HARD_DISK_COLLAPSED)) {
            sessionBean.setHomeHardDiskCollapsed(Boolean.valueOf(value));
            ApplicationUtilities.addCookieBoolean(HomeServlet.COOKIE_CHART_HARD_DISK_COLLAPSED, value, request, response);
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
        return "Disk Servlet";
    }
}
