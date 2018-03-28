<%@page import="home.HomeServlet"%>
<%@page import="utilities.ApplicationUtilities"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@include file="/WEB-INF/jspf/script.jspf" %>

<%
    applicationBean.setMenuSearchEnabled(true);
    applicationBean.setMenuStatisticsEnabled(true);
    applicationBean.setMenuHomeEnabled(true);
    applicationBean.setMenuRuleEnabled(true);
    applicationBean.setMenuExitEnabled(true);
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


                    <jsp:include page="/secure/home?action=ajaxRenderBodyAction" />

                    <jsp:include page="/secure/home?action=ajaxRenderBodyJvm" />

                    <jsp:include page="/secure/home?action=ajaxRenderBodyHttpSession" />

                    <jsp:include page="/secure/home?action=ajaxRenderBodyHardDisk" />
                </div>

                <%@include file="/WEB-INF/jspf/footer.jspf" %>
            </div>
        </div>
    </body>
</html>
