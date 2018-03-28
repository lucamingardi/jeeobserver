package menu;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jeeobserver.server.RequestParameter;
import jeeobserver.server.TimePeriod;
import jeeobserver.utilities.TimeUtilities;
import login.ApplicationSessionBean;
import utilities.ApplicationUtilities;
import utilities.html.HtmlUtilities;

public class ResearchFilterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    public static final String SERVLET = "researchFilter";

    public static final String COOKIE_FILTER_CONTEXT = SERVLET + "Context";

    public static final String COOKIE_FILTER_PATH = SERVLET + "Path";

    public static final String COOKIE_FILTER_PROJECT = SERVLET + "Project";

    public static final String COOKIE_FILTER_ELEMENT = SERVLET + "Element";

    public static final String COOKIE_FILTER_ELEMENT_REGEX = SERVLET + "ElementRegex";

    public static final String COOKIE_FILTER_SAMPLING_PERIOD = SERVLET + "SamplingPeriod";

    public static final String COOKIE_FILTER_GROUPING = SERVLET + "Grouping";

    protected void processRequest(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");

        if (action == null || action.equals("")) {
            throw new ServletException("Action is a mandatory parameter");
        } else if (action.equals("ajaxActionToggleContext")) {
            this.ajaxActionToggleContext(request, response);
        } else if (action.equals("ajaxActionToggleProject")) {
            this.ajaxActionToggleProject(request, response);
        } else if (action.equals("ajaxActionToggleGrouping")) {
            this.ajaxActionToggleGrouping(request, response);
        } else if (action.equals("ajaxActionTogglePath")) {
            this.ajaxActionTogglePath(request, response);
        } else if (action.equals("ajaxActionToggleElement")) {
            this.ajaxActionToggleElement(request, response);
        } else if (action.equals("ajaxActionToggleElementRegex")) {
            this.ajaxActionToggleElementRegex(request, response);
        } else if (action.equals("ajaxRenderResearchFilterContext")) {
            this.ajaxRenderResearchFilterContext(request, response);
        } else if (action.equals("ajaxRenderResearchFilterPath")) {
            this.ajaxRenderResearchFilterPath(request, response);
        } else if (action.equals("ajaxRenderResearchFilterElement")) {
            this.ajaxRenderResearchFilterElement(request, response);
        } else if (action.equals("ajaxActionSelectDateFrom")) {
            this.ajaxActionSelectDateFrom(request, response);
        } else if (action.equals("ajaxActionSelectDateTo")) {
            this.ajaxActionSelectDateTo(request, response);
        } else if (action.equals("ajaxRenderResearchFilterProject")) {
            this.ajaxRenderResearchFilterProject(request, response);
        } else if (action.equals("ajaxRenderResearchFilterGrouping")) {
            this.ajaxRenderResearchFilterGrouping(request, response);
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

    //Ajax actions
    private void ajaxActionToggleContext(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        String researchContext = ApplicationUtilities.getParameterString("researchContext", request);

        sessionBean.setResearchContext(researchContext);
        sessionBean.setResearchProject(null);
        sessionBean.setResearchPath(null);

        ApplicationUtilities.addCookieString(ResearchFilterServlet.COOKIE_FILTER_CONTEXT, sessionBean.getResearchContext(), request, response);
        ApplicationUtilities.addCookieString(ResearchFilterServlet.COOKIE_FILTER_PROJECT, sessionBean.getResearchProject(), request, response);
        ApplicationUtilities.addCookieString(ResearchFilterServlet.COOKIE_FILTER_PATH, sessionBean.getResearchPath(), request, response);
    }

    private void ajaxActionToggleProject(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        String researchProject = ApplicationUtilities.getParameterString("researchProject", request);

        sessionBean.setResearchProject(researchProject);

        ApplicationUtilities.addCookieString(ResearchFilterServlet.COOKIE_FILTER_PROJECT, sessionBean.getResearchProject(), request, response);
    }

    private void ajaxActionToggleGrouping(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        Integer researchGrouping = ApplicationUtilities.getParameterInteger("researchGrouping", request);

        sessionBean.setResearchGrouping(researchGrouping);

        ApplicationUtilities.addCookieInteger(ResearchFilterServlet.COOKIE_FILTER_GROUPING, sessionBean.getResearchGrouping(), request, response);
    }

    private void ajaxActionTogglePath(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        String researchPath = ApplicationUtilities.getParameterString("researchPath", request);

        sessionBean.setResearchPath(researchPath);

        ApplicationUtilities.addCookieString(ResearchFilterServlet.COOKIE_FILTER_PATH, sessionBean.getResearchPath(), request, response);
    }

    private void ajaxActionToggleElementRegex(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        Boolean researchElementRegex = ApplicationUtilities.getParameterBoolean("researchElementRegex", request, false);

        sessionBean.setResearchElementRegex(!researchElementRegex);

        ApplicationUtilities.addCookieBoolean(ResearchFilterServlet.COOKIE_FILTER_ELEMENT_REGEX, sessionBean.isResearchElementRegex(), request, response);
    }

    private void ajaxActionToggleElement(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        String researchElement = ApplicationUtilities.getParameterString("researchElement", request);

        if (researchElement == null || researchElement.length() == 0) {
            sessionBean.setResearchElement(null);
        } else {
            sessionBean.setResearchElement(researchElement);
        }

        ApplicationUtilities.addCookieString(ResearchFilterServlet.COOKIE_FILTER_ELEMENT, sessionBean.getResearchElement(), request, response);
    }

    private void ajaxActionSelectDateFrom(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        Date researchDateFrom = ApplicationUtilities.getParameterDate("researchDateFrom", request);

        if (researchDateFrom == null) {
            researchDateFrom = new Date();
        }
        
        sessionBean.setResearchDateFrom(researchDateFrom);

        //Automatic selection of time period
        double nanos = (sessionBean.getResearchDateTo().getTime() - sessionBean.getResearchDateFrom().getTime()) * TimeUtilities.MILLIS_NANOS;

        if (nanos < 10 * TimeUtilities.HOUR_NANOS) {
            sessionBean.setResearchSamplingPeriod(TimePeriod.UNIT_MINUTE);
        } else if (nanos < 10 * TimeUtilities.DAY_NANOS) {
            sessionBean.setResearchSamplingPeriod(TimePeriod.UNIT_HOUR);
        } else {
            sessionBean.setResearchSamplingPeriod(TimePeriod.UNIT_DAY);
        }
    }

    private void ajaxActionSelectDateTo(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        Date researchDateTo = ApplicationUtilities.getParameterDate("researchDateTo", request);

        if (researchDateTo == null) {
            researchDateTo = new Date();
        }

        sessionBean.setResearchDateTo(researchDateTo);

        //Automatic selection of time period
        double nanos = (sessionBean.getResearchDateTo().getTime() - sessionBean.getResearchDateFrom().getTime()) * TimeUtilities.MILLIS_NANOS;

        if (nanos < 10 * TimeUtilities.HOUR_NANOS) {
            sessionBean.setResearchSamplingPeriod(TimePeriod.UNIT_MINUTE);
        } else if (nanos < 10 * TimeUtilities.DAY_NANOS) {
            sessionBean.setResearchSamplingPeriod(TimePeriod.UNIT_HOUR);
        } else {
            sessionBean.setResearchSamplingPeriod(TimePeriod.UNIT_DAY);
        }
    }

    //Ajax Renders
    private void ajaxRenderResearchFilterContext(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        PrintWriter out = response.getWriter();
        try {
            for (String element : sessionBean.getResearchContexts()) {
                String elementValue;
                String elementName;

                if (element == null) {
                    elementValue = ApplicationUtilities.PARAMETER_NULL_VALUE;
                    elementName = ApplicationUtilities.getMessage("lebel_AllContexts");
                } else {
                    elementValue = element;
                    elementName = element;
                    if (elementName.length() > 50) {
                        elementName = elementName.substring(0, 50) + "...";
                    }
                }

                boolean selected = false;

                if ((element == null && sessionBean.getResearchContext() == null) || (element != null && element.equals(sessionBean.getResearchContext()))) {
                    selected = true;
                }

                out.write("<div class=\"checkBoxListItem\">");

                out.write(HtmlUtilities.getCheckBoxHtml(elementName, "#researchContext", elementValue, "menuResearchFilterContext", null, selected, true));

                out.write("</div>");
            }
        } finally {
            //out.close();
        }
    }

    private void ajaxRenderResearchFilterPath(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        PrintWriter out = response.getWriter();
        try {
            if (sessionBean.getResearchContext() != null && sessionBean.getResearchContextPaths().get(sessionBean.getResearchContext()) != null) {

                List<String> elementsList = new ArrayList(sessionBean.getResearchContextPaths().get(sessionBean.getResearchContext()));

                //if (!sessionBean.getCurrentServlet().equals(HomeServlet.SERVLET)) {
                elementsList.add(0, null);
                //}

                for (String element : elementsList) {

                    String elementValue;
                    String elementName;

                    if (element == null) {
                        elementValue = ApplicationUtilities.PARAMETER_NULL_VALUE;
                        elementName = ApplicationUtilities.getMessage("label_AllHardDisks");
                    } else {
                        elementValue = element.replace("\\", "\\\\");
                        elementName = element;
                        if (elementName.length() > 50) {
                            elementName = elementName.substring(0, 50) + "...";
                        }
                    }

                    boolean selected = false;

                    if ((element == null && sessionBean.getResearchPath() == null) || (element != null && element.equals(sessionBean.getResearchPath()))) {
                        selected = true;
                    }

                    out.write("<div class=\"checkBoxListItem\">");

                    out.write(HtmlUtilities.getCheckBoxHtml(elementName, "#researchPath", elementValue, "menuResearchFilterPath", null, selected, true));

                    out.write("</div>");
                }
            }
        } finally {
            //out.close();
        }
    }

    private void ajaxRenderResearchFilterProject(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        PrintWriter out = response.getWriter();
        try {
            if (sessionBean.getResearchContext() != null && sessionBean.getResearchContextProjects().get(sessionBean.getResearchContext()) != null) {

                List<String> elementsList = new ArrayList(sessionBean.getResearchContextProjects().get(sessionBean.getResearchContext()));

                //if (!sessionBean.getCurrentServlet().equals(HomeServlet.SERVLET)) {
                elementsList.add(0, null);
                //}

                for (String element : elementsList) {

                    String elementValue;
                    String elementName;

                    if (element == null) {
                        elementValue = ApplicationUtilities.PARAMETER_NULL_VALUE;
                        elementName = ApplicationUtilities.getMessage("label_AllProjects");
                    } else {
                        elementValue = element.replace("\\", "\\\\");
                        elementName = element;
                        if (elementName.length() > 50) {
                            elementName = elementName.substring(0, 50) + "...";
                        }
                    }

                    boolean selected = false;

                    if ((element == null && sessionBean.getResearchProject() == null) || (element != null && element.equals(sessionBean.getResearchProject()))) {
                        selected = true;
                    }

                    out.write("<div class=\"checkBoxListItem\">");

                    out.write(HtmlUtilities.getCheckBoxHtml(elementName, "#researchProject", elementValue, "menuResearchFilterProject", null, selected, true));

                    out.write("</div>");
                }
            }
        } finally {
            //out.close();
        }
    }

    private void ajaxRenderResearchFilterElement(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        PrintWriter out = response.getWriter();
        try {
            String researchElement = "";
            if (sessionBean.getResearchElement() != null) {
                researchElement = sessionBean.getResearchElement();
            }
            out.write("<input type=\"text\" id=\"menuResearchElement\" name=\"menuResearchElement\" value=\"" + researchElement + "\" style=\"width: 99%;\"/>");
            out.write("<div style=\"padding-top: 5px;\">");
            out.write(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_RegularExpression"), "#researchElementRegex", String.valueOf(sessionBean.isResearchElementRegex()), "menuResearchFilterElementRegex", null, sessionBean.isResearchElementRegex(), true));
            out.write("</div>");

        } finally {
            //out.close();
        }
    }

    private void ajaxRenderResearchFilterGrouping(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/html; charset=UTF-8");
        response.setHeader("Cache-Control", "no-cache");

        ApplicationSessionBean sessionBean = ApplicationUtilities.getApplicationSessionBean(request);

        PrintWriter out = response.getWriter();
        try {

            //If project not selected then grouping forced to project
            if (sessionBean.getResearchProject() == null) {
                sessionBean.setResearchGrouping(RequestParameter.GROUP_BY_CONTEXT_PROJECT);
                ApplicationUtilities.addCookieInteger(ResearchFilterServlet.COOKIE_FILTER_GROUPING, sessionBean.getResearchGrouping(), request, response);
            }

            boolean selected = false;

            if (sessionBean.getResearchGrouping() == RequestParameter.GROUP_BY_CONTEXT_PROJECT) {
                selected = true;
            }

            out.write("<div class=\"checkBoxListItem\">");

            out.write(HtmlUtilities.getCheckBoxHtml(ApplicationUtilities.getMessage("label_GroupByProject"), "#researchGrouping", String.valueOf(RequestParameter.GROUP_BY_CONTEXT_PROJECT), "menuResearchFilterGrouping", null, selected, true));

            out.write("</div>");

            //if (sessionBean.getResearchProject() != null) {
            if (sessionBean.getResearchGrouping() == RequestParameter.GROUP_BY_CONTEXT_PROJECT_ELEMENT) {
                selected = true;
            } else {
                selected = false;
            }

            out.write("<div class=\"checkBoxListItem\">");

            boolean enabled = sessionBean.getResearchProject() != null;

            String label = ApplicationUtilities.getMessage("label_GroupByProjectElement");
            if (!enabled) {
                label = label + " (" + ApplicationUtilities.getMessage("label_SelectAProjectFirst") + ")";
            }

            out.write(HtmlUtilities.getCheckBoxHtml(label, "#researchGrouping", String.valueOf(RequestParameter.GROUP_BY_CONTEXT_PROJECT_ELEMENT), "menuResearchFilterGrouping", null, selected, enabled));

            out.write("</div>");
            //}
        } finally {
            //out.close();
        }
    }

    @Override
    public String getServletInfo() {
        return "Menu Research Filter Servlet";
    }
}
