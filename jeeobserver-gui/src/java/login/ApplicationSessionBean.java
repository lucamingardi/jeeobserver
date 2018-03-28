package login;

import action.ActionServlet;
import hardDisk.HardDiskServlet;
import home.HomeServlet;
import httpSession.HttpSessionServlet;
import java.awt.Color;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import jeeobserver.server.ActionStatistics;
import jeeobserver.server.ActionStatistics.TimeStatistics;
import jeeobserver.server.ActionStatistics.TotalStatistics;
import jeeobserver.server.HardDiskStatistics;
import jeeobserver.server.HttpSessionStatistics;
import jeeobserver.server.JvmStatistics;
import jeeobserver.server.Rule;
import jeeobserver.server.Rules;
import jeeobserver.server.TimePeriod;
import utilities.ApplicationUtilities;

public class ApplicationSessionBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private String host;

    private Integer port;

    private Locale locale;

    private TimePeriod samplingPeriod;

    //Menu
    private boolean menuHomeEnabled;

    private boolean menuSearchEnabled;

    private boolean menuStatisticsEnabled;

    private boolean menuRuleEnabled;

    private boolean menuExitEnabled;

    //Research
    private String researchContext;

    private String researchProject;

    private int researchCategory;

    private String researchPath;

    private String researchElement;

    private boolean researchElementRegex;

    private Date researchDateFrom;

    private Date researchDateTo;

    private Set<String> researchContexts;

    private Map<String, Set<String>> researchContextProjects;

    private Map<String, Set<String>> researchContextPaths;

    private boolean researchLogScale;

    private boolean researchTrendLines;

    private int researchSamplingPeriod;

    private int researchGrouping;

    //current servlet
    private String currentServlet;

    //Hard Disk
    private Collection<HardDiskStatistics.TimeStatistics> hardDiskStatisticsTime;

    private Collection<HardDiskStatistics.TotalStatistics> hardDiskStatisticsTotal;

    private Integer hardDiskResultSelected;

    private Integer hardDiskResultsSortColumn;

    private Integer hardDiskResultsSortOrder;

    private Set<String> hardDiskStatisticsSelected;

    private Set<Integer> hardDiskResultsExpanded;

    private Map<String, Color> hardDiskStatisticsColors;

    //Http Session
    private Collection<HttpSessionStatistics.TimeStatistics> httpSessionStatisticsTime;

    private Collection<HttpSessionStatistics.TotalStatistics> httpSessionStatisticsTotal;

    private Integer httpSessionResultSelected;

    private Integer httpSessionResultsSortColumn;

    private Integer httpSessionResultsSortOrder;

    private Set<String> httpSessionStatisticsSelected;

    private Set<Integer> httpSessionResultsExpanded;

    private Map<String, Color> httpSessionStatisticsColors;

    //JVM
    private Collection<JvmStatistics.TimeStatistics> jvmStatisticsTime;

    private Collection<JvmStatistics.TotalStatistics> jvmStatisticsTotal;

    private Integer jvmResultSelected;

    private Integer jvmResultsSortColumn;

    private Integer jvmResultsSortOrder;

    private Set<String> jvmStatisticsSelected;

    private Set<Integer> jvmResultsExpanded;

    private Map<String, Color> jvmStatisticsColors;

    //Action
    private Collection<ActionStatistics.TimeStatistics> actionStatisticsTime;

    private Collection<ActionStatistics.TotalStatistics> actionStatisticsTotal;

    private Integer actionServletResultSelected;

    private Integer actionJsfResultSelected;

    private Integer actionEjbResultSelected;

    private Integer actionJaxWsResultSelected;

    private Integer actionJdbcResultSelected;

    private Integer actionCustomResultSelected;

    private Integer actionResultsSortColumn;

    private Integer actionResultsSortOrder;

    private Set<String> actionStatisticsSelected;

    private Set<Integer> actionResultsExpanded;

    private Map<String, Color> actionStatisticsColors;

    private boolean statisticsExtracted;

    private boolean chartCollapsed;

    private boolean resultsCollapsed;

    private boolean homeActionCollapsed;

    private boolean homeJvmCollapsed;

    private boolean homeHardDiskCollapsed;

    private boolean homeHttpSessionCollapsed;

    //Rules
    private Rules rules;

    private Set<Integer> ruleResultsExpanded;

    private Collection<Rule> ruleRules;

    private int ruleType;

    private int ruleCategory;

    private boolean ruleReadOnly;

    private int ruleSelectedId;

    private Integer ruleResultsSortColumn;

    private Integer ruleResultsSortOrder;

    public String getResearchFilter() {

        if (this.currentServlet == null || !this.menuSearchEnabled) {
            return "";
        }

        String result = "<table cellspacing=\"0\" cellpadding=\"0\" style=\"width: 100%;\">\n";
        result = result + "<tr>";
        result = result + "<td>";

        if (this.researchContext != null) {
            String researchContextShort = this.researchContext;
            if (researchContextShort.length() > 30) {
                researchContextShort = researchContextShort.substring(0, 30) + "...";
            }
            result = result + "<b>" + ApplicationUtilities.getMessage("label_Context") + ":</b> " + researchContextShort;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy HH:mm");


        if (currentServlet.equals(HttpSessionServlet.SERVLET) || currentServlet.equals(ActionServlet.SERVLET) || currentServlet.equals(HomeServlet.SERVLET)) {
            if (this.researchProject != null) {
                String researchProjectShort = this.researchProject;
                if (researchProjectShort.length() > 30) {
                    researchProjectShort = researchProjectShort.substring(0, 30) + "...";
                }
                result = result + ",  " + "<b>" + ApplicationUtilities.getMessage("label_Project") + ":</b> " + researchProjectShort;
            }
            if (currentServlet.equals(ActionServlet.SERVLET)) {
                /*if (this.researchCategory != null) {
                 result = result + ",  " + "<b>" + ApplicationUtilities.getMessage("label_Category") + ":</b> " + this.researchCategory;
                 }*/
                if (this.researchElement != null && !this.researchElement.equals("")) {
                    String researchElementShort = this.researchElement;
                    if (researchElementShort.length() > 50) {
                        researchElementShort = researchElementShort.substring(0, 50) + "...";
                    }
                    result = result + ",  " + "<b>" + ApplicationUtilities.getMessage("label_Element") + ":</b> " + "<span title=\"" + this.researchElement + "\">" + researchElementShort + "</span>";
                }
            }
        }
        if (currentServlet.equals(HardDiskServlet.SERVLET) || currentServlet.equals(HomeServlet.SERVLET)) {
            if (this.researchPath != null) {
                result = result + ",  " + "<b>" + ApplicationUtilities.getMessage("label_Path") + ":</b> " + this.researchPath;
            }
        }


        result = result + "</td>";

        result = result + "<td style=\"text-align: right;\">";

        if (this.researchDateFrom != null) {
            result = result + "<b>" + ApplicationUtilities.getMessage("label_From") + ":</b> " + dateFormat.format(this.researchDateFrom);
        }

        if (this.researchDateTo != null) {
            result = result + "&#160;&#160;&#160;" + "<b>" + ApplicationUtilities.getMessage("label_To") + ":</b> " + dateFormat.format(this.researchDateTo);
        }

        result = result + "</td>";
        result = result + "</tr>";
        result = result + "</table>";

        return result;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public boolean isMenuHomeEnabled() {
        return menuHomeEnabled;
    }

    public void setMenuHomeEnabled(boolean menuHomeEnabled) {
        this.menuHomeEnabled = menuHomeEnabled;
    }

    public boolean isMenuSearchEnabled() {
        return menuSearchEnabled;
    }

    public void setMenuSearchEnabled(boolean menuSearchEnabled) {
        this.menuSearchEnabled = menuSearchEnabled;
    }

    public boolean isMenuStatisticsEnabled() {
        return menuStatisticsEnabled;
    }

    public void setMenuStatisticsEnabled(boolean menuStatisticsEnabled) {
        this.menuStatisticsEnabled = menuStatisticsEnabled;
    }

    public boolean isMenuRuleEnabled() {
        return menuRuleEnabled;
    }

    public void setMenuRuleEnabled(boolean menuRuleEnabled) {
        this.menuRuleEnabled = menuRuleEnabled;
    }

    public boolean isMenuExitEnabled() {
        return menuExitEnabled;
    }

    public void setMenuExitEnabled(boolean menuExitEnabled) {
        this.menuExitEnabled = menuExitEnabled;
    }

    public String getResearchContext() {
        return researchContext;
    }

    public void setResearchContext(String researchContext) {
        this.researchContext = researchContext;
    }

    public String getResearchProject() {
        return researchProject;
    }

    public void setResearchProject(String researchProject) {
        this.researchProject = researchProject;
    }

    public int getResearchCategory() {
        return researchCategory;
    }

    public void setResearchCategory(int researchCategory) {
        this.researchCategory = researchCategory;
    }

    public String getResearchPath() {
        return researchPath;
    }

    public void setResearchPath(String researchPath) {
        this.researchPath = researchPath;
    }

    public String getResearchElement() {
        return researchElement;
    }

    public void setResearchElement(String researchElement) {
        this.researchElement = researchElement;
    }

    public boolean isResearchElementRegex() {
        return researchElementRegex;
    }

    public void setResearchElementRegex(boolean researchElementRegex) {
        this.researchElementRegex = researchElementRegex;
    }

    public Date getResearchDateFrom() {
        return researchDateFrom;
    }

    public void setResearchDateFrom(Date researchDateFrom) {
        this.researchDateFrom = researchDateFrom;
    }

    public Date getResearchDateTo() {
        return researchDateTo;
    }

    public void setResearchDateTo(Date researchDateTo) {
        this.researchDateTo = researchDateTo;
    }

    public Set<String> getResearchContexts() {
        return researchContexts;
    }

    public void setResearchContexts(Set<String> researchContexts) {
        this.researchContexts = researchContexts;
    }

    public Map<String, Set<String>> getResearchContextProjects() {
        return researchContextProjects;
    }

    public void setResearchContextProjects(Map<String, Set<String>> researchContextProjects) {
        this.researchContextProjects = researchContextProjects;
    }

    public Map<String, Set<String>> getResearchContextPaths() {
        return researchContextPaths;
    }

    public void setResearchContextPaths(Map<String, Set<String>> researchContextPaths) {
        this.researchContextPaths = researchContextPaths;
    }

    public boolean isResearchLogScale() {
        return researchLogScale;
    }

    public void setResearchLogScale(boolean researchLogScale) {
        this.researchLogScale = researchLogScale;
    }

    public int getResearchSamplingPeriod() {
        return researchSamplingPeriod;
    }

    public void setResearchSamplingPeriod(int researchSamplingPeriod) {
        this.researchSamplingPeriod = researchSamplingPeriod;
    }

    public int getResearchGrouping() {
        return researchGrouping;
    }

    public void setResearchGrouping(int researchGrouping) {
        this.researchGrouping = researchGrouping;
    }

    public String getCurrentServlet() {
        return currentServlet;
    }

    public void setCurrentServlet(String currentServlet) {
        this.currentServlet = currentServlet;
    }

    public Collection<HardDiskStatistics.TimeStatistics> getHardDiskStatisticsTime() {
        return hardDiskStatisticsTime;
    }

    public void setHardDiskStatisticsTime(Collection<HardDiskStatistics.TimeStatistics> hardDiskStatisticsTime) {
        this.hardDiskStatisticsTime = hardDiskStatisticsTime;
    }

    public Collection<HardDiskStatistics.TotalStatistics> getHardDiskStatisticsTotal() {
        return hardDiskStatisticsTotal;
    }

    public void setHardDiskStatisticsTotal(Collection<HardDiskStatistics.TotalStatistics> hardDiskStatisticsTotal) {
        this.hardDiskStatisticsTotal = hardDiskStatisticsTotal;
    }

    public Integer getHardDiskResultSelected() {
        return hardDiskResultSelected;
    }

    public void setHardDiskResultSelected(Integer hardDiskResultSelected) {
        this.hardDiskResultSelected = hardDiskResultSelected;
    }

    public Integer getHardDiskResultsSortColumn() {
        return hardDiskResultsSortColumn;
    }

    public void setHardDiskResultsSortColumn(Integer hardDiskResultsSortColumn) {
        this.hardDiskResultsSortColumn = hardDiskResultsSortColumn;
    }

    public Integer getHardDiskResultsSortOrder() {
        return hardDiskResultsSortOrder;
    }

    public void setHardDiskResultsSortOrder(Integer hardDiskResultsSortOrder) {
        this.hardDiskResultsSortOrder = hardDiskResultsSortOrder;
    }

    public Set<String> getHardDiskStatisticsSelected() {
        return hardDiskStatisticsSelected;
    }

    public void setHardDiskStatisticsSelected(Set<String> hardDiskStatisticsSelected) {
        this.hardDiskStatisticsSelected = hardDiskStatisticsSelected;
    }

    public Set<Integer> getHardDiskResultsExpanded() {
        return hardDiskResultsExpanded;
    }

    public void setHardDiskResultsExpanded(Set<Integer> hardDiskResultsExpanded) {
        this.hardDiskResultsExpanded = hardDiskResultsExpanded;
    }

    public Map<String, Color> getHardDiskStatisticsColors() {
        return hardDiskStatisticsColors;
    }

    public void setHardDiskStatisticsColors(Map<String, Color> hardDiskStatisticsColors) {
        this.hardDiskStatisticsColors = hardDiskStatisticsColors;
    }

    public Collection<HttpSessionStatistics.TimeStatistics> getHttpSessionStatisticsTime() {
        return httpSessionStatisticsTime;
    }

    public void setHttpSessionStatisticsTime(Collection<HttpSessionStatistics.TimeStatistics> httpSessionStatisticsTime) {
        this.httpSessionStatisticsTime = httpSessionStatisticsTime;
    }

    public Collection<HttpSessionStatistics.TotalStatistics> getHttpSessionStatisticsTotal() {
        return httpSessionStatisticsTotal;
    }

    public void setHttpSessionStatisticsTotal(Collection<HttpSessionStatistics.TotalStatistics> httpSessionStatisticsTotal) {
        this.httpSessionStatisticsTotal = httpSessionStatisticsTotal;
    }

    public Integer getHttpSessionResultSelected() {
        return httpSessionResultSelected;
    }

    public void setHttpSessionResultSelected(Integer httpSessionResultSelected) {
        this.httpSessionResultSelected = httpSessionResultSelected;
    }

    public Integer getHttpSessionResultsSortColumn() {
        return httpSessionResultsSortColumn;
    }

    public void setHttpSessionResultsSortColumn(Integer httpSessionResultsSortColumn) {
        this.httpSessionResultsSortColumn = httpSessionResultsSortColumn;
    }

    public Integer getHttpSessionResultsSortOrder() {
        return httpSessionResultsSortOrder;
    }

    public void setHttpSessionResultsSortOrder(Integer httpSessionResultsSortOrder) {
        this.httpSessionResultsSortOrder = httpSessionResultsSortOrder;
    }

    public Set<String> getHttpSessionStatisticsSelected() {
        return httpSessionStatisticsSelected;
    }

    public void setHttpSessionStatisticsSelected(Set<String> httpSessionStatisticsSelected) {
        this.httpSessionStatisticsSelected = httpSessionStatisticsSelected;
    }

    public Set<Integer> getHttpSessionResultsExpanded() {
        return httpSessionResultsExpanded;
    }

    public void setHttpSessionResultsExpanded(Set<Integer> httpSessionResultsExpanded) {
        this.httpSessionResultsExpanded = httpSessionResultsExpanded;
    }

    public Map<String, Color> getHttpSessionStatisticsColors() {
        return httpSessionStatisticsColors;
    }

    public void setHttpSessionStatisticsColors(Map<String, Color> httpSessionStatisticsColors) {
        this.httpSessionStatisticsColors = httpSessionStatisticsColors;
    }

    public Collection<JvmStatistics.TimeStatistics> getJvmStatisticsTime() {
        return jvmStatisticsTime;
    }

    public void setJvmStatisticsTime(Collection<JvmStatistics.TimeStatistics> jvmStatisticsTime) {
        this.jvmStatisticsTime = jvmStatisticsTime;
    }

    public Collection<JvmStatistics.TotalStatistics> getJvmStatisticsTotal() {
        return jvmStatisticsTotal;
    }

    public void setJvmStatisticsTotal(Collection<JvmStatistics.TotalStatistics> jvmStatisticsTotal) {
        this.jvmStatisticsTotal = jvmStatisticsTotal;
    }

    public Integer getJvmResultSelected() {
        return jvmResultSelected;
    }

    public void setJvmResultSelected(Integer jvmResultSelected) {
        this.jvmResultSelected = jvmResultSelected;
    }

    public Integer getJvmResultsSortColumn() {
        return jvmResultsSortColumn;
    }

    public void setJvmResultsSortColumn(Integer jvmResultsSortColumn) {
        this.jvmResultsSortColumn = jvmResultsSortColumn;
    }

    public Integer getJvmResultsSortOrder() {
        return jvmResultsSortOrder;
    }

    public void setJvmResultsSortOrder(Integer jvmResultsSortOrder) {
        this.jvmResultsSortOrder = jvmResultsSortOrder;
    }

    public Set<String> getJvmStatisticsSelected() {
        return jvmStatisticsSelected;
    }

    public void setJvmStatisticsSelected(Set<String> jvmStatisticsSelected) {
        this.jvmStatisticsSelected = jvmStatisticsSelected;
    }

    public Set<Integer> getJvmResultsExpanded() {
        return jvmResultsExpanded;
    }

    public void setJvmResultsExpanded(Set<Integer> jvmResultsExpanded) {
        this.jvmResultsExpanded = jvmResultsExpanded;
    }

    public Map<String, Color> getJvmStatisticsColors() {
        return jvmStatisticsColors;
    }

    public void setJvmStatisticsColors(Map<String, Color> jvmStatisticsColors) {
        this.jvmStatisticsColors = jvmStatisticsColors;
    }

    public Collection<TimeStatistics> getActionStatisticsTime() {
        return actionStatisticsTime;
    }

    public void setActionStatisticsTime(Collection<TimeStatistics> actionStatisticsTime) {
        this.actionStatisticsTime = actionStatisticsTime;
    }

    public Collection<TotalStatistics> getActionStatisticsTotal() {
        return actionStatisticsTotal;
    }

    public void setActionStatisticsTotal(Collection<TotalStatistics> actionStatisticsTotal) {
        this.actionStatisticsTotal = actionStatisticsTotal;
    }

    public Integer getActionServletResultSelected() {
        return actionServletResultSelected;
    }

    public void setActionServletResultSelected(Integer actionServletResultSelected) {
        this.actionServletResultSelected = actionServletResultSelected;
    }

    public Integer getActionJsfResultSelected() {
        return actionJsfResultSelected;
    }

    public void setActionJsfResultSelected(Integer actionJsfResultSelected) {
        this.actionJsfResultSelected = actionJsfResultSelected;
    }

    public Integer getActionEjbResultSelected() {
        return actionEjbResultSelected;
    }

    public void setActionEjbResultSelected(Integer actionEjbResultSelected) {
        this.actionEjbResultSelected = actionEjbResultSelected;
    }

    public Integer getActionJdbcResultSelected() {
        return actionJdbcResultSelected;
    }

    public void setActionJdbcResultSelected(Integer actionJdbcResultSelected) {
        this.actionJdbcResultSelected = actionJdbcResultSelected;
    }

    public Integer getActionCustomResultSelected() {
        return actionCustomResultSelected;
    }

    public void setActionCustomResultSelected(Integer actionCustomResultSelected) {
        this.actionCustomResultSelected = actionCustomResultSelected;
    }

    public Integer getActionResultsSortColumn() {
        return actionResultsSortColumn;
    }

    public void setActionResultsSortColumn(Integer actionResultsSortColumn) {
        this.actionResultsSortColumn = actionResultsSortColumn;
    }

    public Integer getActionResultsSortOrder() {
        return actionResultsSortOrder;
    }

    public void setActionResultsSortOrder(Integer actionResultsSortOrder) {
        this.actionResultsSortOrder = actionResultsSortOrder;
    }

    public Set<String> getActionStatisticsSelected() {
        return actionStatisticsSelected;
    }

    public void setActionStatisticsSelected(Set<String> actionStatisticsSelected) {
        this.actionStatisticsSelected = actionStatisticsSelected;
    }

    public Set<Integer> getActionResultsExpanded() {
        return actionResultsExpanded;
    }

    public void setActionResultsExpanded(Set<Integer> actionResultsExpanded) {
        this.actionResultsExpanded = actionResultsExpanded;
    }

    public Map<String, Color> getActionStatisticsColors() {
        return actionStatisticsColors;
    }

    public void setActionStatisticsColors(Map<String, Color> actionStatisticsColors) {
        this.actionStatisticsColors = actionStatisticsColors;
    }

    public boolean isStatisticsExtracted() {
        return statisticsExtracted;
    }

    public void setStatisticsExtracted(boolean statisticsExtracted) {
        this.statisticsExtracted = statisticsExtracted;
    }

    public boolean isChartCollapsed() {
        return chartCollapsed;
    }

    public void setChartCollapsed(boolean chartCollapsed) {
        this.chartCollapsed = chartCollapsed;
    }

    public boolean isResultsCollapsed() {
        return resultsCollapsed;
    }

    public void setResultsCollapsed(boolean resultsCollapsed) {
        this.resultsCollapsed = resultsCollapsed;
    }

    public boolean isHomeActionCollapsed() {
        return homeActionCollapsed;
    }

    public void setHomeActionCollapsed(boolean homeActionCollapsed) {
        this.homeActionCollapsed = homeActionCollapsed;
    }

    public boolean isHomeJvmCollapsed() {
        return homeJvmCollapsed;
    }

    public void setHomeJvmCollapsed(boolean homeJvmCollapsed) {
        this.homeJvmCollapsed = homeJvmCollapsed;
    }

    public boolean isHomeHardDiskCollapsed() {
        return homeHardDiskCollapsed;
    }

    public void setHomeHardDiskCollapsed(boolean homeHardDiskCollapsed) {
        this.homeHardDiskCollapsed = homeHardDiskCollapsed;
    }

    public boolean isHomeHttpSessionCollapsed() {
        return homeHttpSessionCollapsed;
    }

    public void setHomeHttpSessionCollapsed(boolean homeHttpSessionCollapsed) {
        this.homeHttpSessionCollapsed = homeHttpSessionCollapsed;
    }

    public Rules getRules() {
        return rules;
    }

    public void setRules(Rules rules) {
        this.rules = rules;
    }

    public Set<Integer> getRuleResultsExpanded() {
        return ruleResultsExpanded;
    }

    public void setRuleResultsExpanded(Set<Integer> ruleResultsExpanded) {
        this.ruleResultsExpanded = ruleResultsExpanded;
    }

    public Collection<Rule> getRuleRules() {
        return ruleRules;
    }

    public void setRuleRules(Collection<Rule> ruleRules) {
        this.ruleRules = ruleRules;
    }

    public int getRuleType() {
        return ruleType;
    }

    public void setRuleType(int ruleType) {
        this.ruleType = ruleType;
    }

    public int getRuleCategory() {
        return ruleCategory;
    }

    public void setRuleCategory(int ruleCategory) {
        this.ruleCategory = ruleCategory;
    }

    public boolean isRuleReadOnly() {
        return ruleReadOnly;
    }

    public void setRuleReadOnly(boolean ruleReadOnly) {
        this.ruleReadOnly = ruleReadOnly;
    }

    public int getRuleSelectedId() {
        return ruleSelectedId;
    }

    public void setRuleSelectedId(int ruleSelectedId) {
        this.ruleSelectedId = ruleSelectedId;
    }

    public Integer getRuleResultsSortColumn() {
        return ruleResultsSortColumn;
    }

    public void setRuleResultsSortColumn(Integer ruleResultsSortColumn) {
        this.ruleResultsSortColumn = ruleResultsSortColumn;
    }

    public Integer getRuleResultsSortOrder() {
        return ruleResultsSortOrder;
    }

    public void setRuleResultsSortOrder(Integer ruleResultsSortOrder) {
        this.ruleResultsSortOrder = ruleResultsSortOrder;
    }

    public Integer getActionJaxWsResultSelected() {
        return actionJaxWsResultSelected;
    }

    public void setActionJaxWsResultSelected(Integer actionJaxWsResultSelected) {
        this.actionJaxWsResultSelected = actionJaxWsResultSelected;
    }

    public TimePeriod getSamplingPeriod() {
        return samplingPeriod;
    }

    public void setSamplingPeriod(TimePeriod samplingPeriod) {
        this.samplingPeriod = samplingPeriod;
    }

    public boolean isResearchTrendLines() {
        return researchTrendLines;
    }

    public void setResearchTrendLines(boolean researchTrendLines) {
        this.researchTrendLines = researchTrendLines;
    }
}
