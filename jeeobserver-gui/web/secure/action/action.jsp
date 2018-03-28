<%@page import="utilities.CommonServlet"%>
<%@page import="utilities.ApplicationUtilities"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">


<%@include file="/WEB-INF/jspf/script.jspf" %>

<%
         String chartDisplay = "";

         if (applicationBean.isChartCollapsed()) {
                        chartDisplay = "none";
         } else {
                        chartDisplay = "";
         }

         String resultsDisplay = "";

         if (applicationBean.isResultsCollapsed()) {
                        resultsDisplay = "none";
         } else {
                        resultsDisplay = "";
         }
%>

<html xmlns="http://www.w3.org/1999/xhtml">

    <head>
        <%@include file="/WEB-INF/jspf/head.jspf" %>
    </head>


    <body>
        <div id="container">
            <div id="page" style="width: <%= ApplicationUtilities.getPropertyInteger(ApplicationUtilities.PAGE_WIDTH_PARAMETER, ApplicationUtilities.DEFAULT_PAGE_WIDTH)%>px;">
                <%@include file="/WEB-INF/jspf/header.jspf" %>
                <%@include file="/WEB-INF/jspf/menu.jspf" %>
                <%@include file="/WEB-INF/jspf/title.jspf" %>
                <div id="content">

                    <% if (!applicationBean.isStatisticsExtracted()) {%>
                    <%@include file="/WEB-INF/jspf/noStatisticsExtracted.jspf" %>
                    <% } else {%>

                    <div id="bodyChartSeparator" class="bodySeparator" title="Show / hide chart">
                        <div class="bodySeparatorButton">&#160;</div>
                    </div>
                    <div id="bodyChartPanel" style="display: <%= chartDisplay%>; padding-bottom: 10px; padding-top: 10px;">
                        <div id="bodyChartLegend">
                            <jsp:include page="/secure/action?action=ajaxRenderBodyChartLegend" />
                        </div>
                        <div id="bodyChartImage" style="height: <%= ApplicationUtilities.getPropertyInteger(ApplicationUtilities.TIME_CHART_HEIGHT_PARAMETER, ApplicationUtilities.DEFAULT_TIME_CHART_HEIGHT)%>px;">
                            <jsp:include page="/secure/action?action=ajaxRenderBodyChartImage" />
                        </div>
                    </div>

                    <div id="bodyResultsSeparator" class="bodySeparator" title="Show / hide results table">
                        <div class="bodySeparatorButton">&#160;</div>
                    </div>
                    <div id="bodyResultsTable" style="display: <%= resultsDisplay%>;">
                        <jsp:include page="/secure/action?action=ajaxRenderBodyResultsTable" />
                    </div>

                    <div id="buttons">
                        <a class="button" href="javascript:window.print();">
                            <span class="buttonPrint">
                                <%= ApplicationUtilities.getMessage("label_button_PrintPage")%>
                            </span>
                        </a>
                        &#160;
                        <a class="button" href="<%= request.getContextPath()%>/secure/action?action=export">
                            <span class="buttonExport">
                                <%= ApplicationUtilities.getMessage("label_button_ExportStatistics")%>
                            </span>
                        </a>
                        <% if (request.getUserPrincipal() == null || request.getUserPrincipal().equals("") || request.isUserInRole("admin")) {%>
                        &#160;
                        <a class="button" id="prepareDeleteButton" title="<%= ApplicationUtilities.getMessage("label_button_DeleteStatistics")%>">
                            <span class="buttonDelete">
                                <%= ApplicationUtilities.getMessage("label_button_DeleteStatistics")%>
                            </span>
                        </a>
                        <% }%>
                    </div>
                    <% }%>
                </div>

                <%@include file="/WEB-INF/jspf/footer.jspf" %>
            </div>
        </div>

    </body>
</html>
