package utilities;

import java.awt.Color;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import login.ApplicationSessionBean;

public class ApplicationUtilities {

    public static final String PARAMETER_NULL_VALUE = "NULL_VALUE";

    public static final Color CHART_PLOT_BACKGROUND_COLOR = Color.decode("#FFFFFF");

    public static final Color CHART_OUTLINE_COLOR = Color.decode("#FFFFFF");

    public static final Color CHART_AXIS_LINE_COLOR = Color.decode("#9CABC1");

    public static final Color BORDER_COLOR = Color.decode("#9CABC1");

    public static final Color BACKGROUND_COLOR = Color.decode("#9CABC1");

    public static final Color FOREGROUND_COLOR = Color.decode("#C7CFDB");

    public static final String TIME_CHART_TYPE_PARAMETER = "JOG_TIME_CHART_TYPE";

    public static final String TIME_CHART_HEIGHT_PARAMETER = "JOG_TIME_CHART_HEIGHT";

    public static final String TIME_CHART_LINE_WIDTH_PARAMETER = "JOG_TIME_CHART_LINE_WIDTH";

    public static final String TIME_CHART_POINT_SIZE_PARAMETER = "JOG_TIME_CHART_POINT_SIZE";

    public static final String TOTAL_CHART_TYPE_PARAMETER = "JOG_TOTAL_CHART_TYPE";

    public static final String TOTAL_CHART_HEIGHT_PARAMETER = "JOG_TOTAL_CHART_HEIGHT";

    public static final String TOTAL_CHART_WIDTH_PARAMETER = "JOG_TOTAL_CHART_WIDTH";

    public static final String NOTIFICATION_HANDLERS_PARAMETER = "JOG_NOTIFICATION_HANDLERS";

    public static final String PAGE_WIDTH_PARAMETER = "JOG_PAGE_WIDTH";

    public static final int DEFAULT_TIME_CHART_HEIGHT = 400;

    public static final int DEFAULT_TIME_CHART_LINE_WIDTH = 1;

    public static final int DEFAULT_TIME_CHART_POINT_SIZE = 1;

    public static final int DEFAULT_PAGE_WIDTH = 1200;

    public static final String DEFAULT_TIME_CHART_TYPE = "AreaChart";

    public static final String DEFAULT_TOTAL_CHART_TYPE = "PieChart";

    public static final int DEFAULT_TOTAL_CHART_HEIGHT = 120;

    public static final int DEFAULT_TOTAL_CHART_WIDTH = 220;

    public static final String DEFAULT_NOTIFICATION_HANDLERS = "Email:jeeobserver.server.EmailNotificationHandler; File:jeeobserver.server.FileNotificationHandler";

    private static final ResourceBundle properties = loadResourceBundle("jeeobserver-gui");

    private static final ResourceBundle messageBoundle = loadResourceBundle("locale.MessageResources");

    public static final SimpleDateFormat dateFullFormatter = new SimpleDateFormat("MMMM dd, yyyy - HH.mm");

    public static final SimpleDateFormat dateFileNameFormatter = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");

    public static final DecimalFormat percentageFormatter = new DecimalFormat("##0.00 %");

    public static final DecimalFormat decimalFormatter = new DecimalFormat("#,###,###,###,##0.00");

    public static final DecimalFormat integerFormatter = new DecimalFormat("#,###,###,###,##0");

    public static final DecimalFormat indexFormatter = getIndexFormat();

    private static ResourceBundle loadResourceBundle(String name) {

        ResourceBundle resourceBundle;
        try {
            resourceBundle = ResourceBundle.getBundle(name, Locale.ENGLISH);
        } catch (MissingResourceException e) {
            resourceBundle = null;
        }

        return resourceBundle;
    }

    private static DecimalFormat getIndexFormat() {
        DecimalFormat result = new DecimalFormat("##0.00 %");
        result.setPositivePrefix("+ ");
        result.setNegativePrefix("- ");

        result.setPositiveSuffix("%");
        result.setNegativeSuffix("%");

        return result;
    }

    public static String getRegexPattern(String value, boolean regex) {

        String result = null;
        if (value != null && !value.equals("")) {
            result = "";

            //Check regex
            if (regex) {
                try {
                    Pattern.compile(value);
                } catch (PatternSyntaxException e) {
                    e.printStackTrace(System.out);
                    regex = false;
                }
            }

            if (regex) {
                result = value;
            } else {
                for (String current : value.split(Pattern.quote("*"))) {
                    if (!current.equals("") && !result.equals("")) {
                        result = result + "(.*)" + Pattern.quote(current);
                    } else if (result.equals("")) {
                        result = Pattern.quote(current);
                    }
                }
                if (value.startsWith("*")) {
                    result = "(.*)" + result;
                }
                if (value.endsWith("*")) {
                    result = result + "(.*)";
                }
            }
        }

        return result;
    }

    public static String getMessage(String key) {
        String result;
        try {
            result = messageBoundle.getString(key);
        } catch (MissingResourceException ex) {

            result = "???" + key + "???";
        }
        return result;
    }

    public static String formatDate(Date date) {
        if (date == null) {
            return "";
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        return dateFormat.format(date);
    }

    public static Date parseDate(String date) throws ParseException {
        if (date == null || date.equals("")) {
            return null;
        }

        if (date.length() == 16) {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm").parse(date);
        } else if (date.length() == 10) {
            return new SimpleDateFormat("yyyy-MM-dd").parse(date);
        }

        return null;
    }

    public static ApplicationSessionBean getApplicationSessionBean(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            Object result = request.getSession(false).getAttribute("applicationSessionBean");

            if (result != null) {
                return (ApplicationSessionBean) result;
            }
        }

        return null;
    }

    public static void setApplicationSessionBean(HttpServletRequest request, ApplicationSessionBean applicationSessionBean) {
        request.getSession(true).setAttribute("applicationSessionBean", applicationSessionBean);
    }

    public static void removeApplicationSessionBean(HttpServletRequest request) {
        if (request.getSession(false) != null) {
            request.getSession(false).removeAttribute("applicationSessionBean");
        }
    }

    public static void addError(HttpServletRequest request, String message) {
        request.setAttribute("error", message);
    }

    public static String getFormattedString(Object value) {
        if (value != null) {
            return value.toString();
        } else {
            return PARAMETER_NULL_VALUE;
        }
    }

    public static String getParameterString(String parameter, HttpServletRequest request) {

        String result = request.getParameter(parameter);

        if (result != null && !result.equals(PARAMETER_NULL_VALUE)) {
            return result;
        } else {
            return null;
        }
    }

    public static Boolean getParameterBoolean(String parameter, HttpServletRequest request, boolean nullable) {
        String result = getParameterString(parameter, request);

        if (result != null && !result.equals("")) {
            return Boolean.parseBoolean(result);
        }

        if (nullable) {
            return null;
        } else {
            return Boolean.FALSE;
        }

    }

    public static Date getParameterDate(String parameter, HttpServletRequest request) {
        String result = getParameterString(parameter, request);

        if (result != null && !result.equals("")) {
            try {
                return parseDate(result);
            } catch (ParseException ex) {
                ex.printStackTrace(System.out);
            }
        }

        return null;

    }

    public static synchronized Integer getParameterInteger(String parameter, HttpServletRequest request) {
        String value = getParameterString(parameter, request);

        Integer result = null;
        if (value != null && !value.trim().equals("")) {
            try {
                result = ApplicationUtilities.integerFormatter.parse(value).intValue();
            } catch (ParseException ex) {
                ex.printStackTrace(System.out);
            }
        }

        return result;
    }

    public static int getPropertyInteger(String key, int defaultValue) {
        try {
            if (properties != null) {
                return Integer.parseInt(properties.getString(key));
            }
        } catch (Exception ex) {
            //return defaultValue;
        }

        return defaultValue;

    }

    public static String getPropertyString(String key, String defaultValue) {
        try {
            if (properties != null) {
                return properties.getString(key);
            }
        } catch (Exception ex) {
            //return defaultValue;
        }

        return defaultValue;

    }

    public static Double getParameterDouble(String parameter, HttpServletRequest request) {
        String value = getParameterString(parameter, request);

        Double result = null;
        if (value != null && !value.trim().equals("")) {
            try {
                result = ApplicationUtilities.decimalFormatter.parse(value).doubleValue();
            } catch (ParseException ex) {
                ex.printStackTrace(System.out);
            }
        }

        return result;
    }

    public static void addCookieString(String parameter, String value, HttpServletRequest request, HttpServletResponse response) {

        if (value == null) {
            value = PARAMETER_NULL_VALUE;
        }

        try {
            Cookie cookie = new Cookie(parameter, URLEncoder.encode(value, "UTF-8"));
            cookie.setMaxAge(365 * 24 * 60 * 60);

            //Cookies visible by all pages
            cookie.setPath(request.getContextPath());

            response.addCookie(cookie);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(System.out);
        }
    }

    public static void addCookieInteger(String parameter, Integer value, HttpServletRequest request, HttpServletResponse response) {
        addCookieString(parameter, value.toString(), request, response);
    }

    public static void addCookieBoolean(String parameter, Boolean value, HttpServletRequest request, HttpServletResponse response) {
        addCookieString(parameter, value.toString(), request, response);
    }

    public static void addCookieStringArray(String parameter, String values[], HttpServletRequest request, HttpServletResponse response) {

        String result = "";

        for (int i = 0; i < values.length; i++) {
            if (i == 0) {
                result = values[i];
            } else {
                result = result + "_" + values[i];
            }

        }

        addCookieString(parameter, result, request, response);
    }

    public static String[] getCookieStringArray(String parameter, HttpServletRequest request) {

        String value = getCookieString(parameter, request);
        if (value != null && !value.equals("")) {
            return value.split("_");
        }

        return null;
    }

    public static void addCookieIntegerArray(String parameter, Integer values[], HttpServletRequest request, HttpServletResponse response) {

        String valuesString[] = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            Integer value = values[i];
            if (value != null) {
                valuesString[i] = value.toString();
            }
        }

        addCookieStringArray(parameter, valuesString, request, response);
    }

    public static Integer[] getCookieIntegerArray(String parameter, HttpServletRequest request) {

        String valuesString[] = getCookieStringArray(parameter, request);

        if (valuesString != null) {
            Integer values[] = new Integer[valuesString.length];
            for (int i = 0; i < valuesString.length; i++) {
                String integerString = valuesString[i];

                if (integerString != null && !integerString.equals("")) {
                    values[i] = Integer.parseInt(integerString);
                }
            }

            return values;
        } else {
            return null;
        }
    }

    public static String getCookieString(String parameter, HttpServletRequest request) {

        Cookie cookies[] = request.getCookies();
        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(parameter)) {
                    try {
                        String result = URLDecoder.decode(cookies[i].getValue(), "UTF-8");
                        if (result.equals(PARAMETER_NULL_VALUE)) {
                            return null;
                        }

                        return result;
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace(System.out);
                    }
                }
            }
        }

        return null;
    }

    public static Integer getCookieInteger(String parameter, HttpServletRequest request) {

        String value = getCookieString(parameter, request);

        Integer result = null;
        if (value != null && !value.trim().equals("")) {
            result = Integer.parseInt(value);
        }

        return result;

    }

    public static Boolean getCookieBoolean(String parameter, HttpServletRequest request) {

        String value = getCookieString(parameter, request);

        Boolean result = Boolean.FALSE;
        if (value != null && !value.trim().equals("")) {
            result = Boolean.parseBoolean(value);
        }

        return result;

    }

    public static Color getTrendColor(Color color) {

        int alpha = 130;
        int red, green, blue;
        red = color.getRed() * alpha + CHART_PLOT_BACKGROUND_COLOR.getRed() * (255 - alpha);
        green = color.getGreen() * alpha + CHART_PLOT_BACKGROUND_COLOR.getGreen() * (255 - alpha);
        blue = color.getBlue() * alpha + CHART_PLOT_BACKGROUND_COLOR.getBlue() * (255 - alpha);
        return new Color(red / 255, green / 255, blue / 255);

        //return new Color((color.getRed() + (color.getRed() / 5)), (color.getGreen()+ (color.getGreen() / 5)), (color.getBlue()+ (color.getBlue() / 5)));

    }
}
