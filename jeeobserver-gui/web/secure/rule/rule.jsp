<%@page import="utilities.ApplicationUtilities"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">

<%@include file="/WEB-INF/jspf/script.jspf" %>

<%
         applicationBean.setMenuSearchEnabled(false);
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
                    <form id="formRules" name="formRules" method="post">
                        <table cellpadding="0" cellspacing="0" style="width: 100%;">
                            <tr>
                                <td style="width: 100%; vertical-align: top; padding-left: 0px;">
                                    <div id="bodyResultsTable">
                                        <jsp:include page="/secure/rule?action=ajaxRenderBodyResultsTable" />
                                    </div>
                                </td>
                            </tr>
                        </table>
                    </form>
                </div>
                <%@include file="/WEB-INF/jspf/footer.jspf" %>
            </div>
        </div>
    </body>
</html>
