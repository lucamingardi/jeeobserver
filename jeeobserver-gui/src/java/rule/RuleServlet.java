package rule;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jeeobserver.ActionObserver;
import jeeobserver.server.ActionRule;
import jeeobserver.server.Condition;
import jeeobserver.server.Condition.BooleanCondition;
import jeeobserver.server.Condition.NumberCondition;
import jeeobserver.server.Condition.SizeCondition;
import jeeobserver.server.Condition.TextCondition;
import jeeobserver.server.Condition.TimePeriodCondition;
import jeeobserver.server.HardDiskRule;
import jeeobserver.server.HttpSessionRule;
import jeeobserver.server.JeeObserverClient;
import jeeobserver.server.JeeObserverServerException;
import jeeobserver.server.JvmRule;
import jeeobserver.server.NotificationHandler.Notification;
import jeeobserver.server.Rule;
import jeeobserver.server.TimePeriod;
import jeeobserver.server.TimeSchedule;
import login.ApplicationSessionBean;
import utilities.ApplicationUtilities;
import utilities.html.HtmlDataColumn;
import utilities.html.HtmlDataTable;
import utilities.html.HtmlDataValue;
import utilities.html.HtmlTable;
import utilities.html.HtmlUtilities;

public class RuleServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String SERVLET = "rule";

    public static final int TYPE_ACTION = 1;

    public static final int TYPE_JVM = 2;

    public static final int TYPE_HTTP_SESSION = 3;

    public static final int TYPE_HARD_DISK = 4;

    public static final int CATEGORY_EXCLUSION = 1;

    public static final int CATEGORY_NOTIFICATION = 2;

    public static final int CATEGORY_REPORT = 3;

    public static final String COOKIE_RESULTS_EXPANDED = SERVLET + "ResultsExpanded";

    public static final String COOKIE_RESULTS_SORT_COLUMN = SERVLET + "ResultsSortColumn";

    public static final String COOKIE_RESULTS_SORT_ORDER = SERVLET + "ResultsSortOrder";

    private static final String DEFAULT_NOTIFICATION_HANDLER = "jeeobserver.server.EmailNotificationHandler";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action.equals("show")) {
            this.show(request, response);
        } else if (action.equals("ajaxRenderBodyMenu")) {
            this.ajaxRenderBodyMenu(request, response);
        } else if (action.equals("ajaxRenderBodyResultsTable")) {
            this.ajaxRenderBodyResultsTable(request, response);
        } else if (action.equals("ajaxRenderNotificationMessage")) {
            this.ajaxRenderNotificationMessage(request, response);
        } else if (action.equals("update")) {
            this.update(request, response);
        } else if (action.equals("modify")) {
            this.modify(request, response);
        } else if (action.equals("cancel")) {
            this.cancel(request, response);
        } else if (action.equals("insert")) {
            this.insert(request, response);
        } else if (action.equals("toggleStatus")) {
            this.toggleStatus(request, response);
        } else if (action.equals("delete")) {
            this.delete(request, response);
        } else if (action.equals("ajaxActionSelectType")) {
            this.ajaxActionSelectType(request, response);
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

    private void show(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        if (request.getUserPrincipal() != null && !request.getUserPrincipal().equals("") && !request.isUserInRole("admin")) {
            return;
        }

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        sessionBean.setCurrentServlet(RuleServlet.SERVLET);
        sessionBean.setRuleResultsExpanded(new HashSet<Integer>());

        sessionBean.setRuleCategory(ApplicationUtilities.getParameterInteger("category", request));

        if (sessionBean.getRuleCategory() == CATEGORY_EXCLUSION) {
            request.setAttribute("title", ApplicationUtilities.getMessage("page_rule_exclusion_WindowTitle"));
            request.setAttribute("subTitle", ApplicationUtilities.getMessage("page_rule_exclusion_WindowSubTitle"));
        } else if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
            request.setAttribute("title", ApplicationUtilities.getMessage("page_rule_notification_WindowTitle"));
            request.setAttribute("subTitle", ApplicationUtilities.getMessage("page_rule_notification_WindowSubTitle"));
        } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
            request.setAttribute("title", ApplicationUtilities.getMessage("page_rule_report_WindowTitle"));
            request.setAttribute("subTitle", ApplicationUtilities.getMessage("page_rule_report_WindowSubTitle"));
        }

        if (sessionBean.getRuleType() == 0) {
            sessionBean.setRuleType(TYPE_ACTION);
        }

        sessionBean.setRuleReadOnly(true);

        try {
            JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

            sessionBean.setRules(client.loadRules());

            //TODO remove after 4.1 release
            if (sessionBean.getRules().getActionReportRules() == null) {
                sessionBean.getRules().setActionReportRules(new ArrayList<ActionRule.ReportRule>());
            }
            if (sessionBean.getRules().getJvmReportRules() == null) {
                sessionBean.getRules().setJvmReportRules(new ArrayList<JvmRule.ReportRule>());
            }
            if (sessionBean.getRules().getHttpSessionReportRules() == null) {
                sessionBean.getRules().setHttpSessionReportRules(new ArrayList<HttpSessionRule.ReportRule>());
            }
            if (sessionBean.getRules().getHardDiskReportRules() == null) {
                sessionBean.getRules().setHardDiskReportRules(new ArrayList<HardDiskRule.ReportRule>());
            }

            //sessionBean.setRulesRules(null);

        } catch (JeeObserverServerException ex) {
            throw new ServletException(ex);
        }


        RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/secure/rule/rule.jsp");
        dispatcher.forward(request, response);
    }

    private void modify(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);
        sessionBean.setRuleReadOnly(false);

        sessionBean.setRuleSelectedId(ApplicationUtilities.getParameterInteger("selectedId", request));
    }

    private void cancel(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);
        try {
            JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

            sessionBean.setRules(client.loadRules());

        } catch (JeeObserverServerException ex) {
            throw new ServletException(ex);
        }
        sessionBean.setRuleReadOnly(true);
    }

    private void insert(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);


        String name = "";
        if (sessionBean.getRuleCategory() == CATEGORY_EXCLUSION) {
            if (sessionBean.getRuleType() == TYPE_ACTION) {
                name = getRuleName(sessionBean.getRules().getActionExclusionRules(), ApplicationUtilities.getMessage("label_NewExclusionRule"));
                sessionBean.getRules().getActionExclusionRules().add(new ActionRule.ExclusionRule(name));
            } else if (sessionBean.getRuleType() == TYPE_HTTP_SESSION) {
                name = getRuleName(sessionBean.getRules().getHttpSessionExclusionRules(), ApplicationUtilities.getMessage("label_NewExclusionRule"));
                sessionBean.getRules().getHttpSessionExclusionRules().add(new HttpSessionRule.ExclusionRule(name));
            } else if (sessionBean.getRuleType() == TYPE_JVM) {
                name = getRuleName(sessionBean.getRules().getJvmExclusionRules(), ApplicationUtilities.getMessage("label_NewExclusionRule"));
                sessionBean.getRules().getJvmExclusionRules().add(new JvmRule.ExclusionRule(name));
            } else if (sessionBean.getRuleType() == TYPE_HARD_DISK) {
                name = getRuleName(sessionBean.getRules().getHardDiskExclusionRules(), ApplicationUtilities.getMessage("label_NewExclusionRule"));
                sessionBean.getRules().getHardDiskExclusionRules().add(new HardDiskRule.ExclusionRule(name));
            }
        } else if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {


            String template = "<p>Dear user, <br>this is an automatic notification sent by <b>jeeObserver</b> monitoring tool.</p>\n"
                    + "\n"
                    + "<p><br></p>\n"
                    + "\n"
                    + "<h1>{context}</h1>\n"
                    + "\n"
                    + "{template_content}"
                    + "<p><br></p>\n"
                    + "\n"
                    + "<p><span style=\"color:rgb(204,204,204);\">Copyright 2009 - " + (new SimpleDateFormat("yyyy")).format(new Date()) + " Luca Mingardi. All rights reserved.</span></p>";

            if (sessionBean.getRuleType() == TYPE_ACTION) {

                name = getRuleName(sessionBean.getRules().getActionNotificationRules(), ApplicationUtilities.getMessage("label_NewNotification"));

                String templateContent = "<h3>{project}</h3>\n"
                        + "<p>Timestamp:<b>" + ActionRule.NotificationRule.TAG_TIMESTAMP + "</b></p>\n"
                        + "<p>User:<b>" + ActionRule.NotificationRule.TAG_USER + "</b></p>\n"
                        + "<p>Name:<b>" + ActionRule.NotificationRule.TAG_NAME + "</b></p>\n";

                template = template.replace("{template_content}", templateContent);


                sessionBean.getRules().getActionNotificationRules().add(new ActionRule.NotificationRule(name, new Notification(DEFAULT_NOTIFICATION_HANDLER, "", "", template)));
            } else if (sessionBean.getRuleType() == TYPE_HTTP_SESSION) {

                name = getRuleName(sessionBean.getRules().getHttpSessionNotificationRules(), ApplicationUtilities.getMessage("label_NewNotification"));

                String templateContent = "<h3>" + HttpSessionRule.NotificationRule.TAG_PROJECT + "</h3>\n"
                        + "<p>Timestamp:<b>" + HttpSessionRule.NotificationRule.TAG_TIMESTAMP + "</b></p>\n"
                        + "<p>Duration:<b>" + HttpSessionRule.NotificationRule.TAG_DURATION + "</b></p>\n";

                template = template.replace("{template_content}", templateContent);


                sessionBean.getRules().getHttpSessionNotificationRules().add(new HttpSessionRule.NotificationRule(name, new Notification(DEFAULT_NOTIFICATION_HANDLER, "", "", template)));
            } else if (sessionBean.getRuleType() == TYPE_JVM) {

                name = getRuleName(sessionBean.getRules().getJvmNotificationRules(), ApplicationUtilities.getMessage("label_NewNotification"));

                String templateContent = "<p><br></p>\n"
                        + "<p>Timestamp:<b>" + JvmRule.NotificationRule.TAG_TIMESTAMP + "</b></p>\n"
                        + "<p>CPU:<b>" + JvmRule.NotificationRule.TAG_CPU_USAGE + "</b></p>\n";

                template = template.replace("{template_content}", templateContent);

                sessionBean.getRules().getJvmNotificationRules().add(new JvmRule.NotificationRule(name, new Notification(DEFAULT_NOTIFICATION_HANDLER, "", "", template)));
            } else if (sessionBean.getRuleType() == TYPE_HARD_DISK) {

                name = getRuleName(sessionBean.getRules().getHardDiskNotificationRules(), ApplicationUtilities.getMessage("label_NewNotification"));

                String templateContent = "<h3>{path}</h3>\n"
                        + "<p>Timestamp:<b>" + HardDiskRule.NotificationRule.TAG_TIMESTAMP + "</b></p>\n"
                        + "<p>User:<b>" + HardDiskRule.NotificationRule.TAG_USAGE + "</b></p>\n";

                template = template.replace("{template_content}", templateContent);


                sessionBean.getRules().getHardDiskNotificationRules().add(new HardDiskRule.NotificationRule(name, new Notification(DEFAULT_NOTIFICATION_HANDLER, "", "", template)));
            }
        } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {

            name = ApplicationUtilities.getMessage("label_NewScheduledReport");

            String template = "<p>Dear user, <br>this is a scheduled report of statistics collected by <b>jeeObserver</b> monitoring tool yesterday.</p>\n"
                    + "<p><br></p>\n"
                    + "<h1>" + ActionRule.ReportRule.TAG_CONTEXT + "</h1>\n"
                    + "<p><br></p>\n"
                    + "{template_content}"
                    + "<p><br></p>\n"
                    + "<p><span style=\"color:rgb(204,204,204);\">Copyright 2009 - " + (new SimpleDateFormat("yyyy")).format(new Date()) + " Luca Mingardi. All rights reserved.</span></p>";


            if (sessionBean.getRuleType() == TYPE_ACTION) {

                name = getRuleName(sessionBean.getRules().getActionReportRules(), ApplicationUtilities.getMessage("label_NewScheduledReport"));

                String templateContent = ActionRule.ReportRule.TAG_REPEATED_CONTENT_BEGIN
                        + "<h3>" + ActionRule.ReportRule.TAG_PROJECT + "</h3>\n"
                        + "<p>From:<b>" + ActionRule.ReportRule.TAG_FIRST_DATE + "</b></p>\n"
                        + "<p>To:<b>" + ActionRule.ReportRule.TAG_LAST_DATE + "</b></p>\n"
                        + "<p>Executions:<b>" + ActionRule.ReportRule.TAG_EXECUTIONS_TOTAL + "</b></p>\n"
                        + "<p>CPU:<b>" + ActionRule.ReportRule.TAG_CPU_TOTAL + "</b></p>\n"
                        + "<p>Time:<b>" + ActionRule.ReportRule.TAG_TIME_TOTAL + "</b></p>\n"
                        + "<p>Users:<b>" + ActionRule.ReportRule.TAG_USERS_DIFFERENT + "</b></p>\n"
                        + ActionRule.ReportRule.TAG_REPEATED_CONTENT_END;

                template = template.replace("{template_content}", templateContent);

                sessionBean.getRules().getActionReportRules().add(new ActionRule.ReportRule(name, new Notification(DEFAULT_NOTIFICATION_HANDLER, "", "", template), new TimeSchedule.DailySchedule(0, 0)));
            } else if (sessionBean.getRuleType() == TYPE_HTTP_SESSION) {

                name = getRuleName(sessionBean.getRules().getHttpSessionReportRules(), ApplicationUtilities.getMessage("label_NewScheduledReport"));


                String templateContent = HttpSessionRule.ReportRule.TAG_REPEATED_CONTENT_BEGIN
                        + "<h3>" + HttpSessionRule.ReportRule.TAG_PROJECT + "</h3>\n"
                        + "<p>From:<b>" + HttpSessionRule.ReportRule.TAG_FIRST_DATE + "</b></p>\n"
                        + "<p>To:<b>" + HttpSessionRule.ReportRule.TAG_LAST_DATE + "</b></p>\n"
                        + "<p>Sessions:<b>" + HttpSessionRule.ReportRule.TAG_CREATED_TOTAL + "</b></p>\n"
                        + HttpSessionRule.ReportRule.TAG_REPEATED_CONTENT_END;

                template = template.replace("{template_content}", templateContent);

                sessionBean.getRules().getHttpSessionReportRules().add(new HttpSessionRule.ReportRule(name, new Notification(DEFAULT_NOTIFICATION_HANDLER, "", "", template), new TimeSchedule.DailySchedule(0, 0)));
            } else if (sessionBean.getRuleType() == TYPE_JVM) {

                name = getRuleName(sessionBean.getRules().getJvmReportRules(), ApplicationUtilities.getMessage("label_NewScheduledReport"));

                String templateContent = "<p>From:<b>" + JvmRule.ReportRule.TAG_FIRST_DATE + "</b></p>\n"
                        + "<p>To:<b>" + JvmRule.ReportRule.TAG_LAST_DATE + "</b></p>\n"
                        + "<p>CPU usage:<b>" + JvmRule.ReportRule.TAG_CPU_USAGE_AVERAGE + "</b></p>\n";

                template = template.replace("{template_content}", templateContent);

                sessionBean.getRules().getJvmReportRules().add(new JvmRule.ReportRule(name, new Notification(DEFAULT_NOTIFICATION_HANDLER, "", "", template), new TimeSchedule.DailySchedule(0, 0)));
            } else if (sessionBean.getRuleType() == TYPE_HARD_DISK) {

                name = getRuleName(sessionBean.getRules().getHardDiskReportRules(), ApplicationUtilities.getMessage("label_NewScheduledReport"));

                String templateContent = HardDiskRule.ReportRule.TAG_REPEATED_CONTENT_BEGIN
                        + "<h3>" + HardDiskRule.ReportRule.TAG_PATH + "</h3>\n"
                        + "<p>From:<b>" + HardDiskRule.ReportRule.TAG_FIRST_DATE + "</b></p>\n"
                        + "<p>To:<b>" + HardDiskRule.ReportRule.TAG_LAST_DATE + "</b></p>\n"
                        + "<p>Usage:<b>" + HardDiskRule.ReportRule.TAG_USAGE_AVERAGE + "</b></p>\n"
                        + HardDiskRule.ReportRule.TAG_REPEATED_CONTENT_END;

                template = template.replace("{template_content}", templateContent);

                sessionBean.getRules().getHardDiskReportRules().add(new HardDiskRule.ReportRule(name, new Notification(DEFAULT_NOTIFICATION_HANDLER, "", "", template), new TimeSchedule.DailySchedule(0, 0)));
            }
        }

        sessionBean.setRuleReadOnly(false);
        sessionBean.setRuleSelectedId(name.hashCode());
        sessionBean.getRuleResultsExpanded().add(name.hashCode());
    }

    private void update(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        if (sessionBean.getRuleType() == TYPE_ACTION) {

            if (sessionBean.getRuleCategory() == CATEGORY_EXCLUSION) {
                ActionRule.ExclusionRule rule = sessionBean.getRules().getActionExclusionRules().get(getRuleIndex(sessionBean.getRules().getActionExclusionRules(), sessionBean.getRuleSelectedId()));

                rule.setName(ApplicationUtilities.getParameterString("name:" + sessionBean.getRuleSelectedId(), request));
                rule.setContextCondition(getTextCondition(request, "context:" + sessionBean.getRuleSelectedId()));
                rule.setCategoryCondition(getNumberCondition(request, "category:" + sessionBean.getRuleSelectedId()));
                rule.setProjectCondition(getTextCondition(request, "project:" + sessionBean.getRuleSelectedId()));
                rule.setElementNameCondition(getTextCondition(request, "element:" + sessionBean.getRuleSelectedId()));
                rule.setUserCondition(getTextCondition(request, "user:" + sessionBean.getRuleSelectedId()));
                rule.setRemoteHostCondition(getTextCondition(request, "remoteHost:" + sessionBean.getRuleSelectedId()));
                rule.setCpuCondition(getTimePeriodCondition(request, "cpu:" + sessionBean.getRuleSelectedId()));
                rule.setTimeCondition(getTimePeriodCondition(request, "time:" + sessionBean.getRuleSelectedId()));
                rule.setExceptionThrowedCondition(getBooleanCondition(request, "exceptionThrowed:" + sessionBean.getRuleSelectedId()));

            } else if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {

                ActionRule.NotificationRule rule = sessionBean.getRules().getActionNotificationRules().get(getRuleIndex(sessionBean.getRules().getActionNotificationRules(), sessionBean.getRuleSelectedId()));

                rule.setName(ApplicationUtilities.getParameterString("name:" + sessionBean.getRuleSelectedId(), request));
                rule.setContextCondition(getTextCondition(request, "context:" + sessionBean.getRuleSelectedId()));
                rule.setCategoryCondition(getNumberCondition(request, "category:" + sessionBean.getRuleSelectedId()));
                rule.setProjectCondition(getTextCondition(request, "project:" + sessionBean.getRuleSelectedId()));
                rule.setElementNameCondition(getTextCondition(request, "element:" + sessionBean.getRuleSelectedId()));
                rule.setUserCondition(getTextCondition(request, "user:" + sessionBean.getRuleSelectedId()));
                rule.setRemoteHostCondition(getTextCondition(request, "remoteHost:" + sessionBean.getRuleSelectedId()));
                rule.setCpuCondition(getTimePeriodCondition(request, "cpu:" + sessionBean.getRuleSelectedId()));
                rule.setTimeCondition(getTimePeriodCondition(request, "time:" + sessionBean.getRuleSelectedId()));
                rule.setExceptionThrowedCondition(getBooleanCondition(request, "exceptionThrowed:" + sessionBean.getRuleSelectedId()));

                rule.setTimeLimitCondition(getTimePeriodCondition(request, "timeLimit:" + sessionBean.getRuleSelectedId()));

                rule.setNotification(new Notification(
                        ApplicationUtilities.getParameterString("handler:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("to:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("subject:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("message:" + sessionBean.getRuleSelectedId(), request)));

            } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {

                ActionRule.ReportRule rule = sessionBean.getRules().getActionReportRules().get(getRuleIndex(sessionBean.getRules().getActionReportRules(), sessionBean.getRuleSelectedId()));

                rule.setName(ApplicationUtilities.getParameterString("name:" + sessionBean.getRuleSelectedId(), request));
                rule.setContextCondition(getTextCondition(request, "context:" + sessionBean.getRuleSelectedId()));
                rule.setCategoryCondition(getNumberCondition(request, "category:" + sessionBean.getRuleSelectedId()));
                rule.setProjectCondition(getTextCondition(request, "project:" + sessionBean.getRuleSelectedId()));
                rule.setElementNameCondition(getTextCondition(request, "element:" + sessionBean.getRuleSelectedId()));

                rule.setSchedule(getTimeSchedule(request, "schedule:" + sessionBean.getRuleSelectedId()));


                rule.setNotification(new Notification(
                        ApplicationUtilities.getParameterString("handler:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("to:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("subject:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("message:" + sessionBean.getRuleSelectedId(), request)));

            }
        } else if (sessionBean.getRuleType() == TYPE_HTTP_SESSION) {

            if (sessionBean.getRuleCategory() == CATEGORY_EXCLUSION) {
                HttpSessionRule.ExclusionRule rule = sessionBean.getRules().getHttpSessionExclusionRules().get(getRuleIndex(sessionBean.getRules().getHttpSessionExclusionRules(), sessionBean.getRuleSelectedId()));

                rule.setName(ApplicationUtilities.getParameterString("name:" + sessionBean.getRuleSelectedId(), request));
                rule.setContextCondition(getTextCondition(request, "context:" + sessionBean.getRuleSelectedId()));
                rule.setProjectCondition(getTextCondition(request, "project:" + sessionBean.getRuleSelectedId()));
                rule.setSessionIdCondition(getTextCondition(request, "sessionId:" + sessionBean.getRuleSelectedId()));
                rule.setDurationCondition(getTimePeriodCondition(request, "duration:" + sessionBean.getRuleSelectedId()));

            } else if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                HttpSessionRule.NotificationRule rule = sessionBean.getRules().getHttpSessionNotificationRules().get(getRuleIndex(sessionBean.getRules().getHttpSessionNotificationRules(), sessionBean.getRuleSelectedId()));

                rule.setName(ApplicationUtilities.getParameterString("name:" + sessionBean.getRuleSelectedId(), request));
                rule.setContextCondition(getTextCondition(request, "context:" + sessionBean.getRuleSelectedId()));
                rule.setProjectCondition(getTextCondition(request, "project:" + sessionBean.getRuleSelectedId()));
                rule.setSessionIdCondition(getTextCondition(request, "sessionId:" + sessionBean.getRuleSelectedId()));
                rule.setDurationCondition(getTimePeriodCondition(request, "duration:" + sessionBean.getRuleSelectedId()));

                rule.setTimeLimitCondition(getTimePeriodCondition(request, "timeLimit:" + sessionBean.getRuleSelectedId()));

                rule.setNotification(new Notification(
                        ApplicationUtilities.getParameterString("handler:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("to:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("subject:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("message:" + sessionBean.getRuleSelectedId(), request)));

            } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
                HttpSessionRule.ReportRule rule = sessionBean.getRules().getHttpSessionReportRules().get(getRuleIndex(sessionBean.getRules().getHttpSessionReportRules(), sessionBean.getRuleSelectedId()));

                rule.setName(ApplicationUtilities.getParameterString("name:" + sessionBean.getRuleSelectedId(), request));
                rule.setContextCondition(getTextCondition(request, "context:" + sessionBean.getRuleSelectedId()));
                rule.setProjectCondition(getTextCondition(request, "project:" + sessionBean.getRuleSelectedId()));
                rule.setDurationCondition(getTimePeriodCondition(request, "duration:" + sessionBean.getRuleSelectedId()));

                rule.setSchedule(getTimeSchedule(request, "schedule:" + sessionBean.getRuleSelectedId()));

                rule.setNotification(new Notification(
                        ApplicationUtilities.getParameterString("handler:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("to:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("subject:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("message:" + sessionBean.getRuleSelectedId(), request)));

            }

        } else if (sessionBean.getRuleType() == TYPE_JVM) {

            if (sessionBean.getRuleCategory() == CATEGORY_EXCLUSION) {

                JvmRule.ExclusionRule rule = sessionBean.getRules().getJvmExclusionRules().get(getRuleIndex(sessionBean.getRules().getJvmExclusionRules(), sessionBean.getRuleSelectedId()));

                rule.setName(ApplicationUtilities.getParameterString("name:" + sessionBean.getRuleSelectedId(), request));
                rule.setContextCondition(getTextCondition(request, "context:" + sessionBean.getRuleSelectedId()));
                rule.setCpuUsagePercentageCondition(getNumberCondition(request, "cpu:" + sessionBean.getRuleSelectedId()));
                rule.setHeapMemorySizeCondition(getSizeCondition(request, "heap:" + sessionBean.getRuleSelectedId()));
                rule.setNonHeapMemorySizeCondition(getSizeCondition(request, "nonHeap:" + sessionBean.getRuleSelectedId()));

            } else if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {

                JvmRule.NotificationRule rule = sessionBean.getRules().getJvmNotificationRules().get(getRuleIndex(sessionBean.getRules().getJvmNotificationRules(), sessionBean.getRuleSelectedId()));

                rule.setName(ApplicationUtilities.getParameterString("name:" + sessionBean.getRuleSelectedId(), request));
                rule.setContextCondition(getTextCondition(request, "context:" + sessionBean.getRuleSelectedId()));
                rule.setCpuUsagePercentageCondition(getNumberCondition(request, "cpu:" + sessionBean.getRuleSelectedId()));
                rule.setHeapMemorySizeCondition(getSizeCondition(request, "heap:" + sessionBean.getRuleSelectedId()));
                rule.setNonHeapMemorySizeCondition(getSizeCondition(request, "nonHeap:" + sessionBean.getRuleSelectedId()));

                rule.setTimeLimitCondition(getTimePeriodCondition(request, "timeLimit:" + sessionBean.getRuleSelectedId()));

                rule.setNotification(new Notification(
                        ApplicationUtilities.getParameterString("handler:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("to:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("subject:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("message:" + sessionBean.getRuleSelectedId(), request)));

            } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {

                JvmRule.ReportRule rule = sessionBean.getRules().getJvmReportRules().get(getRuleIndex(sessionBean.getRules().getJvmReportRules(), sessionBean.getRuleSelectedId()));

                rule.setName(ApplicationUtilities.getParameterString("name:" + sessionBean.getRuleSelectedId(), request));
                rule.setContextCondition(getTextCondition(request, "context:" + sessionBean.getRuleSelectedId()));
                rule.setCpuUsagePercentageCondition(getNumberCondition(request, "cpu:" + sessionBean.getRuleSelectedId()));
                rule.setHeapMemorySizeCondition(getSizeCondition(request, "heap:" + sessionBean.getRuleSelectedId()));
                rule.setNonHeapMemorySizeCondition(getSizeCondition(request, "nonHeap:" + sessionBean.getRuleSelectedId()));

                rule.setSchedule(getTimeSchedule(request, "schedule:" + sessionBean.getRuleSelectedId()));

                rule.setNotification(new Notification(
                        ApplicationUtilities.getParameterString("handler:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("to:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("subject:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("message:" + sessionBean.getRuleSelectedId(), request)));

            }

        } else if (sessionBean.getRuleType() == TYPE_HARD_DISK) {

            if (sessionBean.getRuleCategory() == CATEGORY_EXCLUSION) {

                HardDiskRule.ExclusionRule rule = sessionBean.getRules().getHardDiskExclusionRules().get(getRuleIndex(sessionBean.getRules().getHardDiskExclusionRules(), sessionBean.getRuleSelectedId()));

                rule.setName(ApplicationUtilities.getParameterString("name:" + sessionBean.getRuleSelectedId(), request));

                rule.setContextCondition(getTextCondition(request, "context:" + sessionBean.getRuleSelectedId()));
                rule.setPathCondition(getTextCondition(request, "path:" + sessionBean.getRuleSelectedId()));
                //rule.setFreeSizeCondition(getSizeCondition(request, "free:" + sessionBean.getRuleSelectedId()));
                rule.setUsagePercentageCondition(getNumberCondition(request, "usage:" + sessionBean.getRuleSelectedId()));


            } else if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {

                HardDiskRule.NotificationRule rule = sessionBean.getRules().getHardDiskNotificationRules().get(getRuleIndex(sessionBean.getRules().getHardDiskNotificationRules(), sessionBean.getRuleSelectedId()));

                rule.setName(ApplicationUtilities.getParameterString("name:" + sessionBean.getRuleSelectedId(), request));
                rule.setContextCondition(getTextCondition(request, "context:" + sessionBean.getRuleSelectedId()));
                rule.setPathCondition(getTextCondition(request, "path:" + sessionBean.getRuleSelectedId()));
                //rule.setFreeSizeCondition(getSizeCondition(request, "free:" + sessionBean.getRuleSelectedId()));
                rule.setUsagePercentageCondition(getNumberCondition(request, "usage:" + sessionBean.getRuleSelectedId()));

                rule.setTimeLimitCondition(getTimePeriodCondition(request, "timeLimit:" + sessionBean.getRuleSelectedId()));

                rule.setNotification(new Notification(
                        ApplicationUtilities.getParameterString("handler:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("to:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("subject:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("message:" + sessionBean.getRuleSelectedId(), request)));
            } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {

                HardDiskRule.ReportRule rule = sessionBean.getRules().getHardDiskReportRules().get(getRuleIndex(sessionBean.getRules().getHardDiskReportRules(), sessionBean.getRuleSelectedId()));

                rule.setName(ApplicationUtilities.getParameterString("name:" + sessionBean.getRuleSelectedId(), request));
                rule.setContextCondition(getTextCondition(request, "context:" + sessionBean.getRuleSelectedId()));
                rule.setPathCondition(getTextCondition(request, "path:" + sessionBean.getRuleSelectedId()));
                //rule.setFreeSizeCondition(getSizeCondition(request, "free:" + sessionBean.getRuleSelectedId()));
                rule.setUsagePercentageCondition(getNumberCondition(request, "usage:" + sessionBean.getRuleSelectedId()));

                rule.setSchedule(getTimeSchedule(request, "schedule:" + sessionBean.getRuleSelectedId()));

                rule.setNotification(new Notification(
                        ApplicationUtilities.getParameterString("handler:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("to:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("subject:" + sessionBean.getRuleSelectedId(), request),
                        ApplicationUtilities.getParameterString("message:" + sessionBean.getRuleSelectedId(), request)));
            }
        }


        try {
            JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

            client.updateRules(sessionBean.getRules());

        } catch (JeeObserverServerException ex) {
            throw new ServletException(ex);
        }



        sessionBean.setRuleReadOnly(true);
    }

    private void toggleStatus(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        sessionBean.setRuleSelectedId(ApplicationUtilities.getParameterInteger("selectedId", request));

        if (sessionBean.getRuleCategory() == CATEGORY_EXCLUSION) {
            if (sessionBean.getRuleType() == TYPE_ACTION) {
                ActionRule.ExclusionRule rule = sessionBean.getRules().getActionExclusionRules().get(getRuleIndex(sessionBean.getRules().getActionExclusionRules(), sessionBean.getRuleSelectedId()));
                rule.setEnabled(!rule.isEnabled());
            } else if (sessionBean.getRuleType() == TYPE_HTTP_SESSION) {
                HttpSessionRule.ExclusionRule rule = sessionBean.getRules().getHttpSessionExclusionRules().get(getRuleIndex(sessionBean.getRules().getHttpSessionExclusionRules(), sessionBean.getRuleSelectedId()));
                rule.setEnabled(!rule.isEnabled());
            } else if (sessionBean.getRuleType() == TYPE_JVM) {
                JvmRule.ExclusionRule rule = sessionBean.getRules().getJvmExclusionRules().get(getRuleIndex(sessionBean.getRules().getJvmExclusionRules(), sessionBean.getRuleSelectedId()));
                rule.setEnabled(!rule.isEnabled());
            } else if (sessionBean.getRuleType() == TYPE_HARD_DISK) {
                HardDiskRule.ExclusionRule rule = sessionBean.getRules().getHardDiskExclusionRules().get(getRuleIndex(sessionBean.getRules().getHardDiskExclusionRules(), sessionBean.getRuleSelectedId()));
                rule.setEnabled(!rule.isEnabled());
            }
        } else if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
            if (sessionBean.getRuleType() == TYPE_ACTION) {
                ActionRule.NotificationRule rule = sessionBean.getRules().getActionNotificationRules().get(getRuleIndex(sessionBean.getRules().getActionNotificationRules(), sessionBean.getRuleSelectedId()));
                rule.setEnabled(!rule.isEnabled());
            } else if (sessionBean.getRuleType() == TYPE_HTTP_SESSION) {
                HttpSessionRule.NotificationRule rule = sessionBean.getRules().getHttpSessionNotificationRules().get(getRuleIndex(sessionBean.getRules().getHttpSessionNotificationRules(), sessionBean.getRuleSelectedId()));
                rule.setEnabled(!rule.isEnabled());
            } else if (sessionBean.getRuleType() == TYPE_JVM) {
                JvmRule.NotificationRule rule = sessionBean.getRules().getJvmNotificationRules().get(getRuleIndex(sessionBean.getRules().getJvmNotificationRules(), sessionBean.getRuleSelectedId()));
                rule.setEnabled(!rule.isEnabled());
            } else if (sessionBean.getRuleType() == TYPE_HARD_DISK) {
                HardDiskRule.NotificationRule rule = sessionBean.getRules().getHardDiskNotificationRules().get(getRuleIndex(sessionBean.getRules().getHardDiskNotificationRules(), sessionBean.getRuleSelectedId()));
                rule.setEnabled(!rule.isEnabled());
            }
        } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
            if (sessionBean.getRuleType() == TYPE_ACTION) {
                ActionRule.ReportRule rule = sessionBean.getRules().getActionReportRules().get(getRuleIndex(sessionBean.getRules().getActionReportRules(), sessionBean.getRuleSelectedId()));
                rule.setEnabled(!rule.isEnabled());
            } else if (sessionBean.getRuleType() == TYPE_HTTP_SESSION) {
                HttpSessionRule.ReportRule rule = sessionBean.getRules().getHttpSessionReportRules().get(getRuleIndex(sessionBean.getRules().getHttpSessionReportRules(), sessionBean.getRuleSelectedId()));
                rule.setEnabled(!rule.isEnabled());
            } else if (sessionBean.getRuleType() == TYPE_JVM) {
                JvmRule.ReportRule rule = sessionBean.getRules().getJvmReportRules().get(getRuleIndex(sessionBean.getRules().getJvmReportRules(), sessionBean.getRuleSelectedId()));
                rule.setEnabled(!rule.isEnabled());
            } else if (sessionBean.getRuleType() == TYPE_HARD_DISK) {
                HardDiskRule.ReportRule rule = sessionBean.getRules().getHardDiskReportRules().get(getRuleIndex(sessionBean.getRules().getHardDiskReportRules(), sessionBean.getRuleSelectedId()));
                rule.setEnabled(!rule.isEnabled());
            }
        }


        try {
            JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

            client.updateRules(sessionBean.getRules());

        } catch (JeeObserverServerException ex) {
            throw new ServletException(ex);
        }
    }

    private void delete(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        sessionBean.setRuleSelectedId(ApplicationUtilities.getParameterInteger("selectedId", request));

        if (sessionBean.getRuleCategory() == CATEGORY_EXCLUSION) {
            if (sessionBean.getRuleType() == TYPE_ACTION) {
                sessionBean.getRules().getActionExclusionRules().remove(getRuleIndex(sessionBean.getRules().getActionExclusionRules(), sessionBean.getRuleSelectedId()));
            } else if (sessionBean.getRuleType() == TYPE_HTTP_SESSION) {
                sessionBean.getRules().getHttpSessionExclusionRules().remove(getRuleIndex(sessionBean.getRules().getHttpSessionExclusionRules(), sessionBean.getRuleSelectedId()));
            } else if (sessionBean.getRuleType() == TYPE_JVM) {
                sessionBean.getRules().getJvmExclusionRules().remove(getRuleIndex(sessionBean.getRules().getJvmExclusionRules(), sessionBean.getRuleSelectedId()));
            } else if (sessionBean.getRuleType() == TYPE_HARD_DISK) {
                sessionBean.getRules().getHardDiskExclusionRules().remove(getRuleIndex(sessionBean.getRules().getHardDiskExclusionRules(), sessionBean.getRuleSelectedId()));
            }
        } else if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
            if (sessionBean.getRuleType() == TYPE_ACTION) {
                sessionBean.getRules().getActionNotificationRules().remove(getRuleIndex(sessionBean.getRules().getActionNotificationRules(), sessionBean.getRuleSelectedId()));
            } else if (sessionBean.getRuleType() == TYPE_HTTP_SESSION) {
                sessionBean.getRules().getHttpSessionNotificationRules().remove(getRuleIndex(sessionBean.getRules().getHttpSessionNotificationRules(), sessionBean.getRuleSelectedId()));
            } else if (sessionBean.getRuleType() == TYPE_JVM) {
                sessionBean.getRules().getJvmNotificationRules().remove(getRuleIndex(sessionBean.getRules().getJvmNotificationRules(), sessionBean.getRuleSelectedId()));
            } else if (sessionBean.getRuleType() == TYPE_HARD_DISK) {
                sessionBean.getRules().getHardDiskNotificationRules().remove(getRuleIndex(sessionBean.getRules().getHardDiskNotificationRules(), sessionBean.getRuleSelectedId()));
            }
        } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
            if (sessionBean.getRuleType() == TYPE_ACTION) {
                sessionBean.getRules().getActionReportRules().remove(getRuleIndex(sessionBean.getRules().getActionReportRules(), sessionBean.getRuleSelectedId()));
            } else if (sessionBean.getRuleType() == TYPE_HTTP_SESSION) {
                sessionBean.getRules().getHttpSessionReportRules().remove(getRuleIndex(sessionBean.getRules().getHttpSessionReportRules(), sessionBean.getRuleSelectedId()));
            } else if (sessionBean.getRuleType() == TYPE_JVM) {
                sessionBean.getRules().getJvmReportRules().remove(getRuleIndex(sessionBean.getRules().getJvmReportRules(), sessionBean.getRuleSelectedId()));
            } else if (sessionBean.getRuleType() == TYPE_HARD_DISK) {
                sessionBean.getRules().getHardDiskReportRules().remove(getRuleIndex(sessionBean.getRules().getHardDiskReportRules(), sessionBean.getRuleSelectedId()));
            }
        }

        try {
            JeeObserverClient client = new JeeObserverClient(sessionBean.getHost(), sessionBean.getPort());

            client.updateRules(sessionBean.getRules());

        } catch (JeeObserverServerException ex) {
            throw new ServletException(ex);
        }
    }

    //Ajax actions
    private void ajaxActionSelectType(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        sessionBean.setRuleType(ApplicationUtilities.getParameterInteger("selectedType", request));

        sessionBean.setRuleReadOnly(true);
    }

    //Ajax Renders
    private void ajaxRenderBodyMenu(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);


        int types[] = new int[]{TYPE_ACTION, TYPE_JVM, TYPE_HTTP_SESSION, TYPE_HARD_DISK};

        Map<Integer, String> names = new HashMap<Integer, String>();
        names.put(TYPE_ACTION, ApplicationUtilities.getMessage("label_ServletsJSFEJBJAXWSJDBCAndCustom"));
        names.put(TYPE_JVM, ApplicationUtilities.getMessage("label_JavaVirtualMachine"));
        names.put(TYPE_HTTP_SESSION, ApplicationUtilities.getMessage("label_HTTPSessions"));
        names.put(TYPE_HARD_DISK, ApplicationUtilities.getMessage("label_HardDisks"));


        HtmlTable htmlTableContainer = new HtmlTable(100, new int[]{100}, "");

        for (int type : types) {
            if (type == sessionBean.getRuleType()) {
                htmlTableContainer.addCell("<div class=\"ruleMenuElement\"><div class=\"bold selected\"\">" + names.get(type) + "</div></div>");
            } else {
                if (sessionBean.isRuleReadOnly()) {
                    htmlTableContainer.addCell("<div class=\"ruleMenuElement\"><a href=\"" + request.getContextPath() + "/secure/rule?action=show&readonly=true&type=" + type + "\">" + names.get(type) + "</a></div>");
                } else {
                    htmlTableContainer.addCell("<div class=\"ruleMenuElement\"><div class=\"disabled\"\">" + names.get(type) + "</div></div>");
                }
            }
        }

        PrintWriter out = response.getWriter();

        out.write(htmlTableContainer.getHtmlCode());

    }

    private void ajaxRenderNotificationMessage(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        PrintWriter out = response.getWriter();

        Integer id = ApplicationUtilities.getParameterInteger("id", request);

        //out.write("<body style=\"font-size: 11px; font-family: Arial, sans-serif;\">");

        if (sessionBean.getRuleType() == TYPE_ACTION) {
            if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                out.write(sessionBean.getRules().getActionNotificationRules().get(id).getNotification().getMessage());
            } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
                out.write(sessionBean.getRules().getActionReportRules().get(id).getNotification().getMessage());
            }
        }

        //out.write("</body>");

        //out.write("<h1>CIAO!!!!!!</h1>");
    }

    private void ajaxRenderBodyResultsTable(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);


        HtmlDataTable rulesDataTable = new HtmlDataTable("ruleRules", sessionBean.getRuleResultsSortColumn(), sessionBean.getRuleResultsSortOrder());

        rulesDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_Name"), 95, "left", true));
        rulesDataTable.addColumn(new HtmlDataColumn(ApplicationUtilities.getMessage("label_Status"), 5, "center", true));


        HtmlTable htmlTableContainer = new HtmlTable(100, new int[]{100}, "");

        String[][] menu = new String[][]{
            {String.valueOf(TYPE_ACTION), ApplicationUtilities.getMessage("label_ServletsJSFEJBJAXWSJDBCAndCustom")},
            {String.valueOf(TYPE_JVM), ApplicationUtilities.getMessage("label_JavaVirtualMachine")},
            {String.valueOf(TYPE_HTTP_SESSION), ApplicationUtilities.getMessage("label_HTTPSessions")},
            {String.valueOf(TYPE_HARD_DISK), ApplicationUtilities.getMessage("label_HardDisks")}
        };

        htmlTableContainer.addCell(HtmlUtilities.getCombo(menu, "#selectedType", String.valueOf(sessionBean.getRuleType())), "ruleMenuCombo", 1);

        List<Rule> rules = new ArrayList<Rule>();
        if (sessionBean.getRuleCategory() == CATEGORY_EXCLUSION) {
            if (sessionBean.getRuleType() == TYPE_ACTION) {
                rules.addAll(sessionBean.getRules().getActionExclusionRules());
            } else if (sessionBean.getRuleType() == TYPE_JVM) {
                rules.addAll(sessionBean.getRules().getJvmExclusionRules());
            } else if (sessionBean.getRuleType() == TYPE_HTTP_SESSION) {
                rules.addAll(sessionBean.getRules().getHttpSessionExclusionRules());
            } else if (sessionBean.getRuleType() == TYPE_HARD_DISK) {
                rules.addAll(sessionBean.getRules().getHardDiskExclusionRules());
            }
        } else if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
            if (sessionBean.getRuleType() == TYPE_ACTION) {
                rules.addAll(sessionBean.getRules().getActionNotificationRules());
            } else if (sessionBean.getRuleType() == TYPE_JVM) {
                rules.addAll(sessionBean.getRules().getJvmNotificationRules());
            } else if (sessionBean.getRuleType() == TYPE_HTTP_SESSION) {
                rules.addAll(sessionBean.getRules().getHttpSessionNotificationRules());
            } else if (sessionBean.getRuleType() == TYPE_HARD_DISK) {
                rules.addAll(sessionBean.getRules().getHardDiskNotificationRules());
            }
        } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
            if (sessionBean.getRuleType() == TYPE_ACTION) {
                rules.addAll(sessionBean.getRules().getActionReportRules());
            } else if (sessionBean.getRuleType() == TYPE_JVM) {
                rules.addAll(sessionBean.getRules().getJvmReportRules());
            } else if (sessionBean.getRuleType() == TYPE_HTTP_SESSION) {
                rules.addAll(sessionBean.getRules().getHttpSessionReportRules());
            } else if (sessionBean.getRuleType() == TYPE_HARD_DISK) {
                rules.addAll(sessionBean.getRules().getHardDiskReportRules());
            }
        }

        for (Rule rule : rules) {

            boolean expanded = Boolean.FALSE;

            if (sessionBean.getRuleResultsExpanded().contains(rule.getName().hashCode())) {
                expanded = Boolean.TRUE;
            }

            HtmlDataValue values[] = new HtmlDataValue[2];

            values[0] = new HtmlDataValue(rule.getName(), HtmlDataValue.TYPE_CHECK_LINK, "#expandedId", rule.getName().hashCode(), "expandDetailButton", ApplicationUtilities.getMessage("label_button_ShowDetail"));
            if (rule.isEnabled()) {
                values[1] = new HtmlDataValue(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/rule/enabled.png", 16, 13));
            } else {
                values[1] = new HtmlDataValue(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/rule/disabled.png", 14, 14));
            }

            if (expanded) {
                rulesDataTable.addValues(values, this.getRuleDetailPopup(rule.getName().hashCode(), rule, request), expanded);
            } else {
                rulesDataTable.addValues(values);
            }
        }

        htmlTableContainer.addCell("<div class=\"boxContentSepartor\"></div>", "top");
        //htmlTableContainer.addCell("<div class=\"bodySeparator\"></div>", "top rulesBodyRight");
        if (rules.isEmpty()) {
            if (sessionBean.getRuleCategory() == CATEGORY_EXCLUSION) {
                htmlTableContainer.addCell("<div class=\"informationPanel bold\">" + ApplicationUtilities.getMessage("label_NoExclusionRuleInserted") + "</div>", "top");
            } else if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                htmlTableContainer.addCell("<div class=\"informationPanel bold\">" + ApplicationUtilities.getMessage("label_NoNotificationRuleInserted") + "</div>", "top");
            } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
                htmlTableContainer.addCell("<div class=\"informationPanel bold\">" + ApplicationUtilities.getMessage("label_NoScheduledReportInserted") + "</div>", "top");
            }
        } else {
            htmlTableContainer.addCell(rulesDataTable.getHtmlCode(), "top");
        }
        htmlTableContainer.addCell("&nbsp;");
        String label = null;
        if (sessionBean.getRuleCategory() == CATEGORY_EXCLUSION) {
            label = ApplicationUtilities.getMessage("label_AddExclusionRule");
        } else if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
            label = ApplicationUtilities.getMessage("label_AddNotification");
        } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
            label = ApplicationUtilities.getMessage("label_AddScheduledReport");
        }


        htmlTableContainer.addCell(HtmlUtilities.getCheckLinkHtml(null, label, "#selectedId", null, "ruleInsertButton button", null), "right", 1);


        String result = htmlTableContainer.getHtmlCode();

        PrintWriter out = response.getWriter();

        out.print(result);

    }

    private String getRuleDetailPopup(int id, Rule rule, HttpServletRequest request) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        boolean readOnly = (sessionBean.isRuleReadOnly() || sessionBean.getRuleSelectedId() != id);

        String notificationHandlersString = ApplicationUtilities.getPropertyString(ApplicationUtilities.NOTIFICATION_HANDLERS_PARAMETER, ApplicationUtilities.DEFAULT_NOTIFICATION_HANDLERS);

        String[][] notificationHandlers = new String[notificationHandlersString.split(";").length][2];

        int index = 0;
        for (String handler : notificationHandlersString.split(";")) {
            notificationHandlers[index][0] = handler.trim().split(":")[1].trim();
            notificationHandlers[index][1] = handler.trim().split(":")[0].trim();
            index = index + 1;
        }



        HtmlTable htmlTableContainer = new HtmlTable(100, new int[]{25, 75}, "detailTableContainer");

        htmlTableContainer.addCell(HtmlUtilities.getInputTextHtml("name:" + id, rule.getName(), readOnly, null, 50, 100), "detailTitle", 2);
        htmlTableContainer.addCell("&nbsp;", "minSeparatorCell", 2);

        HtmlTable htmlTableName = new HtmlTable(100, new int[]{1, 99}, "detailTable");
        HtmlTable htmlTableValue = new HtmlTable(100, new int[]{100}, "detailTable");

        //ApplicationUtilities.getMessage("label_ResearchFilter")
        htmlTableName.addCell(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/rule/filter.png", 10, 16), "detailValue", 1);
        //htmlTableValue.addCell("&nbsp;", "", 1);


        htmlTableName.addCell(ApplicationUtilities.getMessage("label_Context") + ":", "detailValue");

        htmlTableValue.addCell(HtmlUtilities.getTextConditionHtml("context:" + id + ":", rule.getContextCondition(), readOnly, sessionBean.getRuleCategory() == CATEGORY_REPORT), "detailValue");

        //htmlTableName.addCell("", "minSeparatorCell", 2);

        if (rule instanceof ActionRule) {

            htmlTableName.addCell("", "detailValue");
            htmlTableName.addCell(ApplicationUtilities.getMessage("label_Category") + ":", "detailValue");
            String[][] categories = new String[][]{
                {"", ""},//
                {String.valueOf(ActionObserver.CATEGORY_SERVLET), "Servlet"},
                {String.valueOf(ActionObserver.CATEGORY_JSF), "JSF"},
                {String.valueOf(ActionObserver.CATEGORY_EJB), "EJB"},
                {String.valueOf(ActionObserver.CATEGORY_JDBC), "JDBC"},
                {String.valueOf(ActionObserver.CATEGORY_JAXWS), "JAX-WS"},
                {String.valueOf(ActionObserver.CATEGORY_CUSTOM), "Custom"}};

            htmlTableValue.addCell(HtmlUtilities.getNumberSelectConditionHtml("category:" + id + ":", ((ActionRule) rule).getCategoryCondition(), readOnly, categories), "detailValue");


            htmlTableName.addCell("", "detailValue");
            htmlTableName.addCell(ApplicationUtilities.getMessage("label_Project") + ":", "detailValue");
            htmlTableValue.addCell(HtmlUtilities.getTextConditionHtml("project:" + id + ":", ((ActionRule) rule).getProjectCondition(), readOnly, false), "detailValue");

            htmlTableName.addCell("", "detailValue");
            htmlTableName.addCell(ApplicationUtilities.getMessage("label_Element") + ":", "detailValue");
            htmlTableValue.addCell(HtmlUtilities.getTextConditionHtml("element:" + id + ":", ((ActionRule) rule).getElementNameCondition(), readOnly, false), "detailValue");

            if (sessionBean.getRuleCategory() == CATEGORY_EXCLUSION || sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_User") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getTextConditionHtml("user:" + id + ":", ((ActionRule) rule).getUserCondition(), readOnly, false), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_RemoteHost") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getTextConditionHtml("remoteHost:" + id + ":", ((ActionRule) rule).getRemoteHostCondition(), readOnly, false), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_Cpu") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getTimeConditionHtml("cpu:" + id + ":", ((ActionRule) rule).getCpuCondition(), false, readOnly), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_Time") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getTimeConditionHtml("time:" + id + ":", ((ActionRule) rule).getTimeCondition(), false, readOnly), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_ExceptionThrowed") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getBooleanConditionHtml("exceptionThrowed:" + id + ":", ((ActionRule) rule).getExceptionThrowedCondition(), readOnly), "detailValue");

            }
            if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION || sessionBean.getRuleCategory() == CATEGORY_REPORT) {

                htmlTableName.addCell("", "separatorCell", 2);
                htmlTableValue.addCell("", "separatorCell", 1);

                htmlTableName.addCell(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/rule/shedule.png", 10, 16), "detailValue", 1);
                //htmlTableValue.addCell("&nbsp;", "", 1);

                if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                    htmlTableName.addCell(ApplicationUtilities.getMessage("label_ConditionsMustBeFulfilled") + ":", "detailValue");
                    htmlTableValue.addCell(HtmlUtilities.getTimeConditionHtml("timeLimit:" + id + ":", ((ActionRule.NotificationRule) rule).getTimeLimitCondition(), true, readOnly), "detailValue");
                } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
                    htmlTableName.addCell(ApplicationUtilities.getMessage("label_ScheduledDailyAt") + ":", "detailValue");
                    htmlTableValue.addCell(HtmlUtilities.getTimeScheduleHtml("schedule:" + id + ":", ((ActionRule.ReportRule) rule).getSchedule(), readOnly), "detailValue");

                    htmlTableName.addCell("&nbsp;", null, 2);
                    htmlTableValue.addCell(ApplicationUtilities.getMessage("page_rule_report_statisticsPeriod"), "disabled");
                }


                htmlTableName.addCell("", "separatorCell", 2);
                htmlTableValue.addCell("", "separatorCell", 1);

                htmlTableName.addCell(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/rule/notification.png", 10, 16), "detailValue", 1);
                //htmlTableValue.addCell("&nbsp;", "", 1);

                Notification notification = null;
                if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                    notification = ((ActionRule.NotificationRule) rule).getNotification();
                } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
                    notification = ((ActionRule.ReportRule) rule).getNotification();
                }

                htmlTableName.addCell(ApplicationUtilities.getMessage("label_Type") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getInputSelectHtml("handler:" + id, notification.getHandlerClass(), notificationHandlers, readOnly, "ruleOperator bold", 40), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_To") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getInputTextHtml("to:" + id, notification.getAddress(), readOnly, "bold", 100, 500), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_Subject") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getInputTextHtml("subject:" + id, notification.getSubject(), readOnly, "bold", 100, 500), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_Message") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getInputTextareaHtml("message:" + id, notification.getMessage(), readOnly, "bold", 300, request.getContextPath() + "/secure/rules?action=ajaxRenderNotificationMessage&id=" + id), "");

                List<String> tags = new ArrayList<String>();
                if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                    tags.add(ActionRule.NotificationRule.TAG_CONTEXT);
                    tags.add(ActionRule.NotificationRule.TAG_CATEGORY);
                    tags.add(ActionRule.NotificationRule.TAG_PROJECT);
                    tags.add(ActionRule.NotificationRule.TAG_NAME);
                    tags.add(ActionRule.NotificationRule.TAG_TIMESTAMP);
                    tags.add(ActionRule.NotificationRule.TAG_TIMESTAMP_YEAR);
                    tags.add(ActionRule.NotificationRule.TAG_TIMESTAMP_MONTH);
                    tags.add(ActionRule.NotificationRule.TAG_TIMESTAMP_DAY_OF_MONTH);
                    tags.add(ActionRule.NotificationRule.TAG_USER);
                    tags.add(ActionRule.NotificationRule.TAG_REMOTE_HOST);
                    tags.add(ActionRule.NotificationRule.TAG_CPU);
                    tags.add(ActionRule.NotificationRule.TAG_TIME);
                    tags.add(ActionRule.NotificationRule.TAG_EXCEPTION_CLASS);
                    tags.add(ActionRule.NotificationRule.TAG_EXCEPTION_MESSAGE);
                    tags.add(ActionRule.NotificationRule.TAG_EXCEPTION_STACK_TRACE);
                } else {
                    tags.add(ActionRule.ReportRule.TAG_CONTEXT);
                    tags.add(ActionRule.ReportRule.TAG_CATEGORY);
                    tags.add(ActionRule.ReportRule.TAG_PROJECT);
                    tags.add(ActionRule.ReportRule.TAG_CPU_AVERAGE);
                    tags.add(ActionRule.ReportRule.TAG_CPU_TOTAL);
                    tags.add(ActionRule.ReportRule.TAG_TIME_AVERAGE);
                    tags.add(ActionRule.ReportRule.TAG_TIME_TOTAL);
                    tags.add(ActionRule.ReportRule.TAG_EXECUTIONS_TOTAL);
                    tags.add(ActionRule.ReportRule.TAG_EXCEPTIONS_TOTAL);
                    tags.add(ActionRule.ReportRule.TAG_USERS_DIFFERENT);

                    tags.add(ActionRule.ReportRule.TAG_FIRST_DATE);
                    tags.add(ActionRule.ReportRule.TAG_LAST_DATE);

                    tags.add(ActionRule.ReportRule.TAG_REPEATED_CONTENT_BEGIN);
                    tags.add(ActionRule.ReportRule.TAG_REPEATED_CONTENT_END);
                }

                if (!readOnly) {
                    htmlTableValue.addCell("<div class=\"link\" onclick=\"$('#message_keywords').toggle();\">" + ApplicationUtilities.getMessage("label_AvailableKeywords") + "</div><div id=\"message_keywords\" style=\"display: none;\">" + HtmlUtilities.getUlHtml(tags) + "</div>", "");
                }



                //htmlTableName.addCell();


                //htmlTableName.addCell(ApplicationUtilities.getMessage("page_rule_handler_actionNotificationMessageToolTip"), "");

            }

        } else if (rule instanceof HttpSessionRule) {

            htmlTableName.addCell("", "detailValue");
            htmlTableName.addCell(ApplicationUtilities.getMessage("label_Project") + ":", "detailValue");
            htmlTableValue.addCell(HtmlUtilities.getTextConditionHtml("project:" + id + ":", ((HttpSessionRule) rule).getProjectCondition(), readOnly, false), "detailValue");

            if (sessionBean.getRuleCategory() == CATEGORY_EXCLUSION || sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_Duration") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getTimeConditionHtml("duration:" + id + ":", ((HttpSessionRule) rule).getDurationCondition(), false, readOnly), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_SessionId") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getTextConditionHtml("sessionId:" + id + ":", ((HttpSessionRule) rule).getSessionIdCondition(), readOnly, false), "detailValue");
            }

            if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION || sessionBean.getRuleCategory() == CATEGORY_REPORT) {

                htmlTableName.addCell("", "separatorCell", 2);
                htmlTableValue.addCell("", "separatorCell", 1);

                htmlTableName.addCell(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/rule/shedule.png", 10, 16), "detailValue", 1);

                if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                    htmlTableName.addCell(ApplicationUtilities.getMessage("label_ConditionsMustBeFulfilled") + ":", "detailValue");
                    htmlTableValue.addCell(HtmlUtilities.getTimeConditionHtml("timeLimit:" + id + ":", ((HttpSessionRule.NotificationRule) rule).getTimeLimitCondition(), true, readOnly), "detailValue");
                } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
                    htmlTableName.addCell(ApplicationUtilities.getMessage("label_ScheduledDailyAt") + ":", "detailValue");
                    htmlTableValue.addCell(HtmlUtilities.getTimeScheduleHtml("schedule:" + id + ":", ((HttpSessionRule.ReportRule) rule).getSchedule(), readOnly), "detailValue");

                    htmlTableName.addCell("&nbsp;", null, 2);
                    htmlTableValue.addCell(ApplicationUtilities.getMessage("page_rule_report_statisticsPeriod"), "disabled");
                }

                htmlTableName.addCell("", "separatorCell", 2);
                htmlTableValue.addCell("", "separatorCell", 1);


                Notification notification = null;
                if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                    notification = ((HttpSessionRule.NotificationRule) rule).getNotification();
                } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
                    notification = ((HttpSessionRule.ReportRule) rule).getNotification();
                }

                htmlTableName.addCell(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/rule/notification.png", 10, 16), "detailValue", 1);

                htmlTableName.addCell(ApplicationUtilities.getMessage("label_Type") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getInputSelectHtml("handler:" + id, notification.getHandlerClass(), notificationHandlers, readOnly, "ruleOperator bold", 40), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_To") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getInputTextHtml("to:" + id, notification.getAddress(), readOnly, "bold", 100, 500), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_Subject") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getInputTextHtml("subject:" + id, notification.getSubject(), readOnly, "bold", 100, 500), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_Message") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getInputTextareaHtml("message:" + id, notification.getMessage(), readOnly, "bold", 300, request.getContextPath() + "/secure/rules?action=ajaxRenderNotificationMessage&id=" + id), "detailValue");

                List<String> tags = new ArrayList<String>();
                if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                    tags.add(HttpSessionRule.NotificationRule.TAG_CONTEXT);
                    tags.add(HttpSessionRule.NotificationRule.TAG_PROJECT);
                    tags.add(HttpSessionRule.NotificationRule.TAG_TIMESTAMP);
                    tags.add(HttpSessionRule.NotificationRule.TAG_TIMESTAMP_YEAR);
                    tags.add(HttpSessionRule.NotificationRule.TAG_TIMESTAMP_MONTH);
                    tags.add(HttpSessionRule.NotificationRule.TAG_TIMESTAMP_DAY_OF_MONTH);
                    tags.add(HttpSessionRule.NotificationRule.TAG_DURATION);
                    tags.add(HttpSessionRule.NotificationRule.TAG_SESSION_ID);
                    tags.add(HttpSessionRule.NotificationRule.TAG_CLOSED);
                } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
                    tags.add(HttpSessionRule.ReportRule.TAG_CONTEXT);
                    tags.add(HttpSessionRule.ReportRule.TAG_PROJECT);
                    tags.add(HttpSessionRule.ReportRule.TAG_DURATION_AVERAGE);
                    tags.add(HttpSessionRule.ReportRule.TAG_CREATED_TOTAL);
                    tags.add(HttpSessionRule.ReportRule.TAG_DESTROYED_TOTAL);

                    tags.add(HttpSessionRule.ReportRule.TAG_FIRST_DATE);
                    tags.add(HttpSessionRule.ReportRule.TAG_LAST_DATE);

                    tags.add(HttpSessionRule.ReportRule.TAG_REPEATED_CONTENT_BEGIN);
                    tags.add(HttpSessionRule.ReportRule.TAG_REPEATED_CONTENT_END);
                }
                if (!readOnly) {
                    htmlTableValue.addCell("<div class=\"link\" onclick=\"$('#message_keywords').toggle();\">" + ApplicationUtilities.getMessage("label_AvailableKeywords") + "</div><div id=\"message_keywords\" style=\"display: none;\">" + HtmlUtilities.getUlHtml(tags) + "</div>", "");
                }
            }

        } else if (rule instanceof JvmRule) {

            if (sessionBean.getRuleCategory() == CATEGORY_EXCLUSION || sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_CPUUsage") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getNumberPercentageConditionHtml("cpu:" + id + ":", ((JvmRule) rule).getCpuUsagePercentageCondition(), 3, readOnly), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_HeapMemory") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getSizeConditionHtml("heap:" + id + ":", ((JvmRule) rule).getHeapMemorySizeCondition(), readOnly), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_NonHeapMemory") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getSizeConditionHtml("nonHeap:" + id + ":", ((JvmRule) rule).getNonHeapMemorySizeCondition(), readOnly), "detailValue");
            }

            if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION || sessionBean.getRuleCategory() == CATEGORY_REPORT) {

                htmlTableName.addCell("", "separatorCell", 2);
                htmlTableValue.addCell("", "separatorCell", 1);


                htmlTableName.addCell(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/rule/shedule.png", 10, 16), "detailValue", 1);
                if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                    htmlTableName.addCell(ApplicationUtilities.getMessage("label_ConditionsMustBeFulfilled") + ":", "detailValue");
                    htmlTableValue.addCell(HtmlUtilities.getTimeConditionHtml("timeLimit:" + id + ":", ((JvmRule.NotificationRule) rule).getTimeLimitCondition(), true, readOnly), "detailValue");
                } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
                    htmlTableName.addCell(ApplicationUtilities.getMessage("label_ScheduledDailyAt") + ":", "detailValue");
                    htmlTableValue.addCell(HtmlUtilities.getTimeScheduleHtml("schedule:" + id + ":", ((JvmRule.ReportRule) rule).getSchedule(), readOnly), "detailValue");

                    htmlTableName.addCell("&nbsp;", null, 2);
                    htmlTableValue.addCell(ApplicationUtilities.getMessage("page_rule_report_statisticsPeriod"), "disabled");
                }

                htmlTableName.addCell("", "separatorCell", 2);
                htmlTableValue.addCell("", "separatorCell", 1);


                Notification notification = null;
                if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                    notification = ((JvmRule.NotificationRule) rule).getNotification();
                } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
                    notification = ((JvmRule.ReportRule) rule).getNotification();
                }


                htmlTableName.addCell(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/rule/notification.png", 10, 16), "detailValue", 1);

                htmlTableName.addCell(ApplicationUtilities.getMessage("label_Type") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getInputSelectHtml("handler:" + id, notification.getHandlerClass(), notificationHandlers, readOnly, "ruleOperator bold", 40), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_To") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getInputTextHtml("to:" + id, notification.getAddress(), readOnly, "bold", 100, 500), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_Subject") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getInputTextHtml("subject:" + id, notification.getSubject(), readOnly, "bold", 100, 500), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_Message") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getInputTextareaHtml("message:" + id, notification.getMessage(), readOnly, "bold", 300, request.getContextPath() + "/secure/rules?action=ajaxRenderNotificationMessage&id=" + id), "detailValue");

                List<String> tags = new ArrayList<String>();
                if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                    tags.add(JvmRule.NotificationRule.TAG_CONTEXT);
                    tags.add(JvmRule.NotificationRule.TAG_TIMESTAMP);
                    tags.add(JvmRule.NotificationRule.TAG_TIMESTAMP_YEAR);
                    tags.add(JvmRule.NotificationRule.TAG_TIMESTAMP_MONTH);
                    tags.add(JvmRule.NotificationRule.TAG_TIMESTAMP_DAY_OF_MONTH);
                    tags.add(JvmRule.NotificationRule.TAG_CPU_USAGE);
                    tags.add(JvmRule.NotificationRule.TAG_CPU_TIME);
                    tags.add(JvmRule.NotificationRule.TAG_HEAP_MEMORY_USED);
                    tags.add(JvmRule.NotificationRule.TAG_HEAP_MEMORY_COMMITTED);
                    tags.add(JvmRule.NotificationRule.TAG_HEAP_MEMORY_MAX);
                    tags.add(JvmRule.NotificationRule.TAG_NON_HEAP_MEMORY_USED);
                    tags.add(JvmRule.NotificationRule.TAG_NON_HEAP_MEMORY_COMMITTED);
                    tags.add(JvmRule.NotificationRule.TAG_NON_HEAP_MEMORY_MAX);
                    tags.add(JvmRule.NotificationRule.TAG_GARBAGE_COLLECTIONS);
                    tags.add(JvmRule.NotificationRule.TAG_GARBAGE_COLLECTIONS_TIME);
                    tags.add(JvmRule.NotificationRule.TAG_LOADED_CLASSES);
                    tags.add(JvmRule.NotificationRule.TAG_THREADS);
                } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
                    tags.add(JvmRule.ReportRule.TAG_CONTEXT);
                    tags.add(JvmRule.ReportRule.TAG_CPU_USAGE_AVERAGE);
                    tags.add(JvmRule.ReportRule.TAG_CPU_TIME_TOTAL);
                    tags.add(JvmRule.ReportRule.TAG_HEAP_MEMORY_USED_AVERAGE);
                    tags.add(JvmRule.ReportRule.TAG_HEAP_MEMORY_COMMITTED_AVERAGE);
                    tags.add(JvmRule.ReportRule.TAG_HEAP_MEMORY_MAX_AVERAGE);
                    tags.add(JvmRule.ReportRule.TAG_NON_HEAP_MEMORY_USED_AVERAGE);
                    tags.add(JvmRule.ReportRule.TAG_NON_HEAP_MEMORY_COMMITTED_AVERAGE);
                    tags.add(JvmRule.ReportRule.TAG_NON_HEAP_MEMORY_MAX_AVERAGE);
                    tags.add(JvmRule.ReportRule.TAG_GARBAGE_COLLECTIONS_TOTAL);
                    tags.add(JvmRule.ReportRule.TAG_GARBAGE_COLLECTIONS_TIME_TOTAL);
                    tags.add(JvmRule.ReportRule.TAG_LOADED_CLASSES_AVERAGE);
                    tags.add(JvmRule.ReportRule.TAG_THREADS_AVERAGE);

                    tags.add(JvmRule.ReportRule.TAG_FIRST_DATE);
                    tags.add(JvmRule.ReportRule.TAG_LAST_DATE);
                }
                if (!readOnly) {
                    htmlTableValue.addCell("<div class=\"link\" onclick=\"$('#message_keywords').toggle();\">" + ApplicationUtilities.getMessage("label_AvailableKeywords") + "</div><div id=\"message_keywords\" style=\"display: none;\">" + HtmlUtilities.getUlHtml(tags) + "</div>", "");
                }
            }

        } else if (rule instanceof HardDiskRule) {
            htmlTableName.addCell("", "detailValue");
            htmlTableName.addCell(ApplicationUtilities.getMessage("label_Path") + ":", "detailValue");
            htmlTableValue.addCell(HtmlUtilities.getTextConditionHtml("path:" + id + ":", ((HardDiskRule) rule).getPathCondition(), readOnly, false), "detailValue");

            if (sessionBean.getRuleCategory() == CATEGORY_EXCLUSION || sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_UsagePercentage") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getNumberPercentageConditionHtml("usage:" + id + ":", ((HardDiskRule) rule).getUsagePercentageCondition(), 3, readOnly), "detailValue");
            }

            if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION || sessionBean.getRuleCategory() == CATEGORY_REPORT) {

                htmlTableName.addCell("", "separatorCell", 2);
                htmlTableValue.addCell("", "separatorCell", 1);

                htmlTableName.addCell(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/rule/shedule.png", 10, 16), "detailValue", 1);
                if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                    htmlTableName.addCell(ApplicationUtilities.getMessage("label_ConditionsMustBeFulfilled") + ":", "detailValue");
                    htmlTableValue.addCell(HtmlUtilities.getTimeConditionHtml("timeLimit:" + id + ":", ((HardDiskRule.NotificationRule) rule).getTimeLimitCondition(), true, readOnly), "detailValue");
                } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
                    htmlTableName.addCell(ApplicationUtilities.getMessage("label_ScheduledDailyAt") + ":", "detailValue");
                    htmlTableValue.addCell(HtmlUtilities.getTimeScheduleHtml("schedule:" + id + ":", ((HardDiskRule.ReportRule) rule).getSchedule(), readOnly), "detailValue");

                    htmlTableName.addCell("&nbsp;", null, 2);
                    htmlTableValue.addCell(ApplicationUtilities.getMessage("page_rule_report_statisticsPeriod"), "disabled");
                }

                htmlTableName.addCell("", "separatorCell", 2);
                htmlTableValue.addCell("", "separatorCell", 1);

                Notification notification = null;
                if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                    notification = ((HardDiskRule.NotificationRule) rule).getNotification();
                } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
                    notification = ((HardDiskRule.ReportRule) rule).getNotification();
                }


                htmlTableName.addCell(HtmlUtilities.getImageHtml(request.getContextPath() + "/css/skin/default/images/rule/notification.png", 10, 16), "detailValue", 1);

                htmlTableName.addCell(ApplicationUtilities.getMessage("label_Type") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getInputSelectHtml("handler:" + id, notification.getHandlerClass(), notificationHandlers, readOnly, "ruleOperator bold", 40), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_To") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getInputTextHtml("to:" + id, notification.getAddress(), readOnly, "bold", 100, 500), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_Subject") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getInputTextHtml("subject:" + id, notification.getSubject(), readOnly, "bold", 100, 500), "detailValue");

                htmlTableName.addCell("", "detailValue");
                htmlTableName.addCell(ApplicationUtilities.getMessage("label_Message") + ":", "detailValue");
                htmlTableValue.addCell(HtmlUtilities.getInputTextareaHtml("message:" + id, notification.getMessage(), readOnly, "bold", 300, request.getContextPath() + "/secure/rules?action=ajaxRenderNotificationMessage&id=" + id), "detailValue");

                List<String> tags = new ArrayList<String>();
                if (sessionBean.getRuleCategory() == CATEGORY_NOTIFICATION) {
                    tags.add(HardDiskRule.NotificationRule.TAG_CONTEXT);
                    tags.add(HardDiskRule.NotificationRule.TAG_PATH);
                    tags.add(HardDiskRule.NotificationRule.TAG_TIMESTAMP);
                    tags.add(HardDiskRule.NotificationRule.TAG_TIMESTAMP_YEAR);
                    tags.add(HardDiskRule.NotificationRule.TAG_TIMESTAMP_MONTH);
                    tags.add(HardDiskRule.NotificationRule.TAG_TIMESTAMP_DAY_OF_MONTH);
                    tags.add(HardDiskRule.NotificationRule.TAG_USAGE);
                    tags.add(HardDiskRule.NotificationRule.TAG_FREE_SPACE);
                    tags.add(HardDiskRule.NotificationRule.TAG_USABLE_SPACE);
                    tags.add(HardDiskRule.NotificationRule.TAG_USED_SPACE);
                    tags.add(HardDiskRule.NotificationRule.TAG_TOTAL_SPACE);
                } else if (sessionBean.getRuleCategory() == CATEGORY_REPORT) {
                    tags.add(HardDiskRule.ReportRule.TAG_CONTEXT);
                    tags.add(HardDiskRule.ReportRule.TAG_USAGE_AVERAGE);
                    tags.add(HardDiskRule.ReportRule.TAG_FREE_SPACE_AVERAGE);
                    tags.add(HardDiskRule.ReportRule.TAG_USABLE_SPACE_AVERAGE);
                    tags.add(HardDiskRule.ReportRule.TAG_USED_SPACE_AVERAGE);
                    tags.add(HardDiskRule.ReportRule.TAG_TOTAL_SPACE_AVERAGE);

                    tags.add(HardDiskRule.ReportRule.TAG_FIRST_DATE);
                    tags.add(HardDiskRule.ReportRule.TAG_LAST_DATE);

                    tags.add(HardDiskRule.ReportRule.TAG_REPEATED_CONTENT_BEGIN);
                    tags.add(HardDiskRule.ReportRule.TAG_REPEATED_CONTENT_END);
                }

                if (!readOnly) {
                    htmlTableValue.addCell("<div class=\"link\" onclick=\"$('#message_keywords').toggle();\">" + ApplicationUtilities.getMessage("label_AvailableKeywords") + "</div><div id=\"message_keywords\" style=\"display: none;\">" + HtmlUtilities.getUlHtml(tags) + "</div>", "");
                }

            }
        }


        htmlTableContainer.addCell(htmlTableName.getHtmlCode(), "detailLeftTable top");
        htmlTableContainer.addCell(htmlTableValue.getHtmlCode(), "top");

        htmlTableContainer.addCell("&nbsp;", "", 2);


        String buttons = "";
        if (readOnly) {
            if (rule.isEnabled()) {
                buttons = buttons + HtmlUtilities.getCheckLinkHtml(null, ApplicationUtilities.getMessage("label_button_Disable"), "#selectedId", String.valueOf(id), "ruleEnableDisableButton button", null);
            } else {
                buttons = buttons + HtmlUtilities.getCheckLinkHtml(null, ApplicationUtilities.getMessage("label_button_Enable"), "#selectedId", String.valueOf(id), "ruleEnableDisableButton button", null);
            }
            buttons = buttons + "&nbsp;";
            buttons = buttons + HtmlUtilities.getCheckLinkHtml("prepareDeleteButton", ApplicationUtilities.getMessage("label_button_Delete"), "#selectedId", String.valueOf(id), "button", null);
            buttons = buttons + "&nbsp;";
            buttons = buttons + HtmlUtilities.getCheckLinkHtml(null, ApplicationUtilities.getMessage("label_button_Modify"), "#selectedId", String.valueOf(id), "ruleModifyButton button", null);
        } else {
            buttons = buttons + HtmlUtilities.getCheckLinkHtml(null, ApplicationUtilities.getMessage("label_button_Cancel"), "#selectedId", String.valueOf(id), "ruleCancelButton button", null);
            buttons = buttons + "&nbsp;";
            buttons = buttons + HtmlUtilities.getCheckLinkHtml(null, ApplicationUtilities.getMessage("label_button_Apply"), "#selectedId", String.valueOf(id), "ruleApplyButton button", null);

        }

        htmlTableContainer.addCell(buttons, "right", 2);


        return htmlTableContainer.getHtmlCode();
    }

    //Utilities methods
    private int getRuleIndex(List rules, int id) {
        int index = 0;
        for (Object rule : rules) {
            if (((Rule) rule).getName().hashCode() == id) {
                return index;
            }
            index = index + 1;
        }

        return -1;
    }

    private boolean checkRuleName(List rules, String name) {
        for (Object rule : rules) {
            if (((Rule) rule).getName().equals(name)) {
                return true;
            }
        }

        return false;
    }

    private String getRuleName(List rules, String name) {


        String secureName = name;

        int index = 0;
        while (checkRuleName(rules, secureName)) {
            index = index + 1;
            secureName = name + "_" + index;
        }

        return secureName;
    }

    private TextCondition getTextCondition(HttpServletRequest request, String id) {

        Integer operator = ApplicationUtilities.getParameterInteger(id + ":Operator", request);
        if (operator == null) {
            operator = Condition.OPERATOR_NO_CONDITION;
        }
        String value = ApplicationUtilities.getParameterString(id + ":Value", request);
        if (value == null) {
            operator = Condition.OPERATOR_NO_CONDITION;
        }

        return new TextCondition(operator, value);
    }

    private SizeCondition getSizeCondition(HttpServletRequest request, String id) {

        Integer operator = ApplicationUtilities.getParameterInteger(id + ":Operator", request);
        if (operator == null) {
            operator = Condition.OPERATOR_NO_CONDITION;
        }

        Double value = ApplicationUtilities.getParameterDouble(id + ":Value", request);
        if (value == null) {
            operator = Condition.OPERATOR_NO_CONDITION;
            value = new Double(0);
        }

        Integer unit = ApplicationUtilities.getParameterInteger(id + ":Unit", request);
        if (unit == null) {
            operator = Condition.OPERATOR_NO_CONDITION;
            value = new Double(0);
            unit = new Integer(0);
        }

        return new SizeCondition(operator, value, unit);
    }

    private TimePeriodCondition getTimePeriodCondition(HttpServletRequest request, String id) {

        Integer operator = ApplicationUtilities.getParameterInteger(id + ":Operator", request);
        if (operator == null) {
            operator = Condition.OPERATOR_NO_CONDITION;
        }

        Double value = ApplicationUtilities.getParameterDouble(id + ":Value", request);
        if (value == null) {
            operator = Condition.OPERATOR_NO_CONDITION;
            value = new Double(0);
        }

        Integer unit = ApplicationUtilities.getParameterInteger(id + ":Unit", request);
        if (unit == null) {
            operator = Condition.OPERATOR_NO_CONDITION;
            value = new Double(0);
            unit = new Integer(0);
        }

        return new TimePeriodCondition(operator, new TimePeriod(unit, value.intValue()));

    }

    private TimeSchedule getTimeSchedule(HttpServletRequest request, String id) {

        Integer hour = ApplicationUtilities.getParameterInteger(id + ":Hour", request);
        Integer minute = ApplicationUtilities.getParameterInteger(id + ":Minute", request);

        return new TimeSchedule.DailySchedule(hour, minute);
    }

    private NumberCondition getNumberCondition(HttpServletRequest request, String id) {

        Integer operator = ApplicationUtilities.getParameterInteger(id + ":Operator", request);
        if (operator == null) {
            operator = Condition.OPERATOR_NO_CONDITION;
        }

        Double value = ApplicationUtilities.getParameterDouble(id + ":Value", request);
        if (value == null) {
            operator = Condition.OPERATOR_NO_CONDITION;
            value = new Double(0);
        }

        return new NumberCondition(operator, value);
    }

    private BooleanCondition getBooleanCondition(HttpServletRequest request, String id) {

        Integer operator = ApplicationUtilities.getParameterInteger(id + ":Operator", request);
        if (operator == null) {
            operator = Condition.OPERATOR_NO_CONDITION;
        }

        Boolean value = ApplicationUtilities.getParameterBoolean(id + ":Value", request, true);
        if (value == null) {
            operator = Condition.OPERATOR_NO_CONDITION;
            value = Boolean.FALSE;
        }


        return new BooleanCondition(operator, value);
    }
}
