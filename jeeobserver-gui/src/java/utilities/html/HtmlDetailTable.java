package utilities.html;

import jeeobserver.server.Statistics.NumberRangeStatistics;
import jeeobserver.server.Statistics.NumberStatistics;
import jeeobserver.server.Statistics.NumberTrend;
import jeeobserver.server.TimePeriod;
import utilities.ApplicationUtilities;
import utilities.chart.ChartPlotPie;

public class HtmlDetailTable {

    public final static int TYPE_FULL = 1;

    public final static int TYPE_SIMPLE = 2;

    public final static int TYPE_SIMPLE_AVERAGE_COLUMNS = 2;

    private final int type;

    private HtmlTable containerTable;

    private HtmlTable nameTable;

    private HtmlTable averageTable;

    private HtmlTable sumTable;

    private HtmlTable minMaxTable;

    private boolean averageTableVisible;

    private int period;

    private boolean totalTableVisible;

    private boolean minMaxTableVisible;

    private HtmlTable chartTables[];

    public HtmlDetailTable(int type, String title, String subTitle, boolean averageVisible, boolean averageVarianceVisible, boolean totalVisible, boolean minMaxVisible, int period) {

        this.type = type;

        this.period = period;

        this.averageTableVisible = averageVisible;
        this.totalTableVisible = totalVisible;
        this.minMaxTableVisible = minMaxVisible;


        this.createTables();

        //Create container table

        if (this.type == TYPE_FULL) {
            this.containerTable = new HtmlTable(100, new int[]{20, 40, 25, 15});
        } else {
            //this.containerTable = new HtmlTable(100, new int[]{10, 20, 20, 50});
            //this.containerTable = new HtmlTable(100, new int[]{20, 40, 40});
            this.containerTable = new HtmlTable(100, new int[]{30, 70});
        }




        /*if (this.subTitle != null) {
         this.nameTable.addCell(this.subTitle, "detailValueTitle bold left");
         } else {
         this.nameTable.addCell("&nbsp;");
         }*/

        this.addHeader(title, subTitle, averageVisible, averageVarianceVisible, totalVisible, minMaxVisible);

        //this.containerTable.addCell(this.nameTable.getHtmlCode(), "detailLeftTable top");


    }

    public void addStatistics(int type, boolean goodParameter, boolean averageVisible, boolean averageVarianceVisible, boolean totalVisible, boolean minMaxVisible, String title, NumberStatistics statistics, NumberTrend trend, int totalType, NumberStatistics totalStatistics, NumberTrend totalTrend, int count) {
        addStatistics(type, goodParameter, averageVisible, averageVarianceVisible, totalVisible, minMaxVisible, title, statistics, trend, totalType, totalStatistics, totalTrend, count, null, null);
    }

    public void addStatistics(int type, boolean goodParameter, boolean averageVisible, boolean averageVarianceVisible, boolean totalVisible, boolean minMaxVisible, String title, NumberStatistics statistics, NumberTrend trend, int totalType, NumberStatistics totalStatistics, NumberTrend totalTrend, int count, Double averagePercentage, Double sumPercentage) {
        if (this.type == TYPE_FULL) {
            this.nameTable.addCell(title, "detailValue normal");
        } else {
            this.nameTable.addCell(title, "detailValue bold");
        }

        String highlightStyle = "";
        if (this.type == TYPE_FULL) {
            highlightStyle = " bold";
        }

        if (this.averageTable != null && averageVisible) {

            if (this.type == TYPE_SIMPLE) {
                if (averagePercentage != null) {
                    this.averageTable.addCell(HtmlUtilities.getMinimalProgressBarHtml(averagePercentage), "detailValue right");
                } else {
                    this.averageTable.addCell("", "detailValue right");
                }
            } else {
                //this.averageTable.addCell("", "detailValue right");
            }

            this.averageTable.addCell(type, statistics.getSum() / count, "detailValue right" + highlightStyle, 1);


            if (this.type == TYPE_FULL) {
                this.averageTable.addCell(HtmlTable.CELL_TYPE_INDEX, trend.getAverageTrendIndex(), "detailValue detailValueSeparator right", 1);
                this.averageTable.addCell(type, trend.getAverageLastDetected(), "detailValue disabled right", 1);
                this.averageTable.addCell(type, trend.getAveragePredictedAt(trend.getLastDate()), "detailValue disabled right", 1);
            } else {
                this.averageTable.addCell(HtmlTable.CELL_TYPE_INDEX, trend.getAverageTrendIndex(), "detailValue disabled right", 1);
            }
            this.averageTable.addCell(HtmlUtilities.getLogIndexImage(trend.getAverageTrendIndex(), goodParameter), "detailValue");

            if (this.type == TYPE_FULL) {

                //this.lastTable.addCell(type, trend.getAverageLastDetected(), "detailValue right", 1);

                if (averageVarianceVisible) {
                    this.averageTable.addCell(HtmlTable.CELL_TYPE_PERCENTAGE, trend.getAverageDeviationIndex(), "detailValue detailValueSeparator right", 1);
                    this.averageTable.addCell(type, trend.getAverageDeviation(), "detailValue disabled right", 1);
                    //this.averageTable.addCell(HtmlUtilities.getLogIndexImage(trend.getAverageDeviationIndex(), false), "detailValue");
                } else {
                    this.averageTable.addCell("", "detailValue disabled right", 2);
                }
            }

        } else {
            this.averageTable.addCell("&nbsp;", "detailValue right" + highlightStyle, this.averageTable.getColumns());
            //this.lastTable.addCell("&nbsp;", "detailValue right" + highlightStyle, this.lastTable.getColumns());
        }

        if (this.sumTable != null && totalVisible) {

            if (this.type == TYPE_SIMPLE) {
                if (sumPercentage != null) {
                    this.sumTable.addCell(HtmlUtilities.getMinimalProgressBarHtml(sumPercentage), "detailValue right");
                } else {
                    this.sumTable.addCell("", "detailValue right");
                }
            } else {
                //this.averageTable.addCell("", "detailValue right");
            }

            this.sumTable.addCell(totalType, totalStatistics.getSum(), "detailValue right" + highlightStyle, 1);


            if (this.type == TYPE_FULL) {
                this.sumTable.addCell(HtmlTable.CELL_TYPE_INDEX, totalTrend.getSumTrendIndex(), "detailValue detailValueSeparator right", 1);
                this.sumTable.addCell(totalType, totalTrend.getSumPredictedAt(trend.getLastDate()), "detailValue disabled right", 1);
            } else {
                this.sumTable.addCell(HtmlTable.CELL_TYPE_INDEX, totalTrend.getSumTrendIndex(), "detailValue disabled right", 1);
            }

            this.sumTable.addCell(HtmlUtilities.getLogIndexImage(totalTrend.getSumTrendIndex(), goodParameter), "detailValue");

        } else {
            this.sumTable.addCell("&nbsp;", "detailValue right" + highlightStyle, this.sumTable.getColumns());
        }


        if (this.type == TYPE_FULL && statistics instanceof NumberRangeStatistics && minMaxVisible) {
            this.minMaxTable.addCell(type, ((NumberRangeStatistics) statistics).getMinimum(), "detailValue right", 1);
            this.minMaxTable.addCell(type, ((NumberRangeStatistics) statistics).getMaximum(), "detailValue right", 1);
        } else {
            if (this.type == TYPE_FULL) {
                this.minMaxTable.addCell("&nbsp;", "detailValue right" + highlightStyle, this.minMaxTable.getColumns());
            }
        }
    }

    public void addStatistics(int type, boolean goodParameter, boolean averageVisible, boolean averageVarianceVisible, boolean totalVisible, boolean minMaxVisible, String title, NumberStatistics statistics, NumberTrend trend, int count) {
        this.addStatistics(type, goodParameter, averageVisible, averageVarianceVisible, totalVisible, minMaxVisible, title, statistics, trend, type, statistics, trend, count);
    }

    public void addStatistics(int type, boolean goodParameter, boolean averageVisible, boolean averageVarianceVisible, boolean totalVisible, boolean minMaxVisible, String title, NumberStatistics statistics, NumberTrend trend, int count, Double averagePercentage, Double sumPercentage) {
        this.addStatistics(type, goodParameter, averageVisible, averageVarianceVisible, totalVisible, minMaxVisible, title, statistics, trend, type, statistics, trend, count, averagePercentage, sumPercentage);
    }

    public void addStatisticsSeparator() {
        this.addStatisticsSeparator(null);
    }

    private void addHeader(String title, String subTitle, boolean averageVisible, boolean averageVarianceVisible, boolean totalVisible, boolean minMaxVisible) {

        if (title != null) {
            this.containerTable.addCell(title, "detailTitle top", this.containerTable.getColumns());
        }


        String highlightStyle = "";
        if (this.type == TYPE_FULL) {
            highlightStyle = " bold";
        }


        if (subTitle != null) {
            if (this.type == TYPE_FULL) {
                this.nameTable.addCell(subTitle, "detailValueTitle bold left");
            } else {
                this.nameTable.addCell(subTitle, "detailValueTitle disabled left");
            }
        } else {
            this.nameTable.addCell("&nbsp;");
        }

        String periodUnit = "";
        if (period > 0) {

            String unit = "?";
            if (period == TimePeriod.UNIT_MILLISECOND) {
                unit = "ms";
            } else if (period == TimePeriod.UNIT_SECOND) {
                unit = "s";
            } else if (period == TimePeriod.UNIT_MINUTE) {
                unit = "m";
            } else if (period == TimePeriod.UNIT_HOUR) {
                unit = "h";
            } else if (period == TimePeriod.UNIT_DAY) {
                unit = "d";
            } else if (period == TimePeriod.UNIT_WEEK) {
                unit = "w";
            } else if (period == TimePeriod.UNIT_MONTH) {
                unit = "M";
            }

            periodUnit = " (" + unit + ")";
        }

        if (averageVisible) {
            if (this.type == TYPE_SIMPLE) {
                this.averageTable.addCell("&nbsp;");
            }
            if (this.type == TYPE_FULL) {
                this.averageTable.addCell(ApplicationUtilities.getMessage("label_Average"), "detailValueTitle right " + highlightStyle, 1);
            } else {
                this.averageTable.addCell(ApplicationUtilities.getMessage("label_Average"), "detailValueTitle  right", 1);
            }


            if (this.type == TYPE_FULL) {
                this.averageTable.addCell(ApplicationUtilities.getMessage("label_Trend") + periodUnit, "detailValueTitle right", 1);
                this.averageTable.addCell(ApplicationUtilities.getMessage("label_Last"), "detailValueTitle disabled right", 1);
                this.averageTable.addCell(ApplicationUtilities.getMessage("label_Predicted"), "detailValueTitle disabled right", 1);
            } else {
                this.averageTable.addCell(ApplicationUtilities.getMessage("label_Trend") + periodUnit, "detailValueTitle disabled right", 1);
                //this.averageTable.addCell("", "detailValueTitle disabled right", 1);
            }
            this.averageTable.addCell("&nbsp;");

            if (this.type == TYPE_FULL) {
                if (averageVarianceVisible) {
                    this.averageTable.addCell(ApplicationUtilities.getMessage("label_Variance") + periodUnit, "detailValueTitle right", 1);
                    this.averageTable.addCell(ApplicationUtilities.getMessage("label_Average"), "detailValueTitle disabled right", 1);


                    //this.averageTable.addCell("&nbsp;");
                } else {
                    this.averageTable.addCell("&nbsp;", "detailValueTitle disabled", 2);
                }
            }

        } else {
            //if (this.type == TYPE_FULL) {
            this.averageTable.addCell("&nbsp;", "detailValueTitle" + highlightStyle, this.averageTable.getColumns());
            //this.lastTable.addCell("&nbsp;", "detailValueTitle bold", this.lastTable.getColumns());
            //}
        }


        if (totalVisible) {
            if (this.type == TYPE_SIMPLE) {
                this.sumTable.addCell("&nbsp;");
            }
            this.sumTable.addCell(ApplicationUtilities.getMessage("label_Total"), "detailValueTitle right " + highlightStyle, 1);

            if (this.type == TYPE_FULL) {
                this.sumTable.addCell(ApplicationUtilities.getMessage("label_Trend") + periodUnit, "detailValueTitle right", 1);
                this.sumTable.addCell(ApplicationUtilities.getMessage("label_Predicted"), "detailValueTitle disabled right", 1);
            } else {
                this.sumTable.addCell(ApplicationUtilities.getMessage("label_Trend") + periodUnit, "detailValueTitle disabled right", 1);
                //this.sumTable.addCell("", "detailValueTitle disabled right", 1);
            }
            this.sumTable.addCell("&nbsp;");
        } else {
            //if (this.type == TYPE_FULL) {
            this.sumTable.addCell("&nbsp;", "detailValueTitle " + highlightStyle, this.sumTable.getColumns());
            //}
        }

        if (this.type == TYPE_FULL && minMaxVisible) {
            this.minMaxTable.addCell(ApplicationUtilities.getMessage("label_Minimum"), "detailValueTitle right" + highlightStyle, 1);
            this.minMaxTable.addCell(ApplicationUtilities.getMessage("label_Maximum"), "detailValueTitle right" + highlightStyle, 1);


        } else {
            if (this.type == TYPE_FULL) {
                this.minMaxTable.addCell("&nbsp;", "detailValueTitle " + highlightStyle, this.minMaxTable.getColumns());
            }
        }




    }

    public void addStatisticsSeparator(String title, String subTitle, boolean averageVisible, boolean averageVarianceVisible, boolean totalVisible, boolean minMaxVisible) {
        this.terminateRow();

        this.containerTable.addCell("", "detailSeparator", this.containerTable.getColumns());

        this.createTables();

        if (subTitle != null || averageVisible || averageVarianceVisible || totalVisible || minMaxVisible) {
            this.addHeader(title, subTitle, averageVisible, averageVarianceVisible, totalVisible, minMaxVisible);
        }
    }

    public void addStatisticsSeparator(String subTitle) {

        this.addStatisticsSeparator(null, subTitle, false, false, false, false);
    }

    public void addTotalChart(String id, String title, ChartPlotPie plot, int width, int height) {

        for (int i = 0; i < chartTables.length; i++) {
            HtmlTable htmlTable = chartTables[i];

            if (htmlTable == null) {
                htmlTable = new HtmlTable(100, new int[]{100}, "detailTable");

                htmlTable.addCell(title, "detailValueTitle bold", 1);

                htmlTable.addCell(HtmlUtilities.getTotalChart(id, plot), "detailValue");

                chartTables[i] = htmlTable;

                break;
            }
        }
    }

    public void addTotalChart(String id, String title, ChartPlotPie plot, boolean average, boolean total) {


        HtmlTable htmlTable = new HtmlTable(100, new int[]{100}, "detailTable");

        htmlTable.addCell(title, "detailValueTitle bold", 1);

        //htmlTable.addCell(HtmlUtilities.getPieChart(id, plot, width, height), "detailValue");

        if (this.nameTable != null) {
            //this.nameTable.addCell("", "detailSeparator", this.nameTable.getColumns());
        }
        if (this.averageTable != null && average) {
            this.averageTable.addCell(HtmlUtilities.getTotalChart(id, plot), "detailValue", this.averageTable.getColumns());
        } else {
            //	 this.averageTable.addCell("", "detailSeparator", this.sumTable.getColumns());
        }
        if (this.sumTable != null && total) {
            this.sumTable.addCell(HtmlUtilities.getTotalChart(id, plot), "detailValue", this.sumTable.getColumns());
        } else {
            // this.sumTable.addCell("", "detailSeparator", this.sumTable.getColumns());
        }
        if (this.minMaxTable != null) {
            //this.minMaxTable.addCell("", "detailSeparator", this.minMaxTable.getColumns());
        }

    }

    private void createTables() {
        this.nameTable = new HtmlTable(100, new int[]{100}, "detailTable");

        if (this.type == TYPE_FULL) {
            this.averageTable = new HtmlTable(100, new int[]{20, 15, 15, 15, 5, 15, 15}, "detailTable");
        } else if (this.type == TYPE_SIMPLE) {
            //this.averageTable = new HtmlTable(100, new int[]{30, 25, 5, 25, 5}, "detailTable");
            this.averageTable = new HtmlTable(100, new int[]{20, 45, 30, 5}, "detailTable");
        }

        if (this.type == TYPE_FULL) {
            this.sumTable = new HtmlTable(100, new int[]{35, 30, 30, 5}, "detailTable");
        } else if (this.type == TYPE_SIMPLE) {
            this.sumTable = new HtmlTable(100, new int[]{20, 45, 30, 5}, "detailTable");
        }

        if (this.type == TYPE_FULL) {
            this.minMaxTable = new HtmlTable(100, new int[]{50, 50}, "detailTable");
        } else {
            this.chartTables = new HtmlTable[2];
        }

        //this.lastTable = new HtmlTable(100, new int[]{100}, "detailTable");


    }

    private void terminateRow() {

        if (this.nameTable != null) {
            if (this.type == TYPE_FULL) {
                this.containerTable.addCell(this.nameTable.getHtmlCode(), "detailLeftTable top");
            } else {
                this.containerTable.addCell(this.nameTable.getHtmlCode(), "top");
            }
        }

        if (this.averageTable != null && (this.type == TYPE_FULL || this.averageTableVisible)) {
            if (this.type == TYPE_FULL) {
                this.containerTable.addCell(this.averageTable.getHtmlCode(), "detailLeftTable top");
            } else {
                this.containerTable.addCell(this.averageTable.getHtmlCode(), "top");
            }
        }



        if (this.sumTable != null && (this.type == TYPE_FULL || this.totalTableVisible)) {
            if (this.type == TYPE_FULL) {
                this.containerTable.addCell(this.sumTable.getHtmlCode(), "detailLeftTable top");
            } else {
                this.containerTable.addCell(this.sumTable.getHtmlCode(), "top");
            }
        }

        if (this.minMaxTable != null && (this.type == TYPE_FULL || this.minMaxTableVisible)) {
            this.containerTable.addCell(this.minMaxTable.getHtmlCode(), "top");
        }

        /*if (this.lastTable != null && (this.type == TYPE_FULL || this.averageTableVisible)) {
         this.containerTable.addCell(this.lastTable.getHtmlCode(), "top");
         }*/

        if (this.type == TYPE_SIMPLE) {

            HtmlTable chartTable = new HtmlTable(100, new int[]{50, 50}, "detailTable");

            for (HtmlTable htmlTable : chartTables) {
                if (htmlTable == null) {
                    chartTable.addCell("&nbsp;");
                } else {
                    chartTable.addCell(htmlTable.getHtmlCode(), "detailLeftTable top");
                }
            }

            //this.containerTable.addCell(chartTable.getHtmlCode(), "detailLeftTable top");
        }

        this.containerTable.terminateRow();
    }

    public String getHtmlCode() {

        this.terminateRow();

        return containerTable.getHtmlCode();
    }
}
