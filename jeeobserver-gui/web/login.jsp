<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@include file="/WEB-INF/jspf/script.jspf" %>

<%

         ApplicationUtilities.removeApplicationSessionBean(request);

         String host = ApplicationUtilities.getCookieString("host", request);

         if (host == null) {
                        host = "localhost";
         }

         Integer port = ApplicationUtilities.getCookieInteger("port", request);

         if (port == null) {
                        port = 5688;
         }

         request.setAttribute("title", ApplicationUtilities.getMessage("label_ConnectToJeeObserverServerInstance"));
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
                    <form action="/jeeobserver-gui/login" method="get">
                        <input name="action" id="action" type="hidden" value="login">
                            <table width="100%" cellspacing="0" cellpadding="0" border="0">
                                <tr>
                                    <td style="vertical-align: top; padding-top: 50px;">
                                        <table width="100%" cellspacing="0" cellpadding="0" style="left: 50%;">
                                            <tr>
                                                <td class="filterCell right" style="width: 50%;">
                                                    <b><%= ApplicationUtilities.getMessage("label_Host")%></b>:
                                                </td>
                                                <td class="filterCell" style="width: 50%;">
                                                    <input id="host" name="host" type="text" size="30" value="<%=host%>">
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="minSeparatorCell"></td>
                                            </tr>
                                            <tr>
                                                <td class="filterCell right">
                                                    <b><%= ApplicationUtilities.getMessage("label_Port")%></b>:
                                                </td>
                                                <td class="filterCell">
                                                    <input id="port" name="port" type="text" size="10" value="<%=port%>">
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="separatorCell"></td>
                                            </tr>
                                            <tr>
                                                <td class="filterCell"></td>
                                                <td class="filterCell">
                                                    <input type="submit" value="<%= ApplicationUtilities.getMessage("label_button_ConnectToServer")%>"/>
                                                </td>
                                            </tr>
                                            <tr>
                                                <td class="separatorCell"></td>
                                            </tr>
                                        </table>
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
