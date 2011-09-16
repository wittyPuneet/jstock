/*
 * JStock - Free Stock Market Software
 * Copyright (C) 2010 Yan Cheng CHEOK <yccheok@yahoo.com>
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package org.yccheok.jstock.charting;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;
import org.yccheok.jstock.engine.Stock;
import org.yccheok.jstock.engine.StockHistoryServer;

/**
 *
 * @author yccheok
 */
public class Utils {

    /**
     * Returns daily chart data based on given stock history server.
     *
     * @param stockHistoryServer the stock history server
     * @return list of daily chart data
     */
    public static List<ChartData> getDailyChartData(StockHistoryServer stockHistoryServer) {
        final int days = stockHistoryServer.getNumOfCalendar();

        List<ChartData> chartDatas = new ArrayList<ChartData>();

        double prevPrice = 0;
        double openPrice = 0;
        double lastPrice = 0;
        double highPrice = Double.MIN_VALUE;
        double lowPrice = Double.MAX_VALUE;
        long volume = 0;
        long timestamp = 0;

        // Just perform simple one to one copy, without performing any
        // filtering.
        for (int i = 0; i < days; i++) {
            Calendar calendar = stockHistoryServer.getCalendar(i);
            Stock stock = stockHistoryServer.getStock(calendar);
            prevPrice = stock.getPrevPrice();
            openPrice = stock.getOpenPrice();
            lastPrice = stock.getLastPrice();
            highPrice = stock.getHighPrice();
            lowPrice = stock.getLowPrice();
            volume = stock.getVolume();
            timestamp = stock.getCalendar().getTimeInMillis();
            ChartData chartData = ChartData.newInstance(
                    prevPrice,
                    openPrice,
                    lastPrice,
                    highPrice,
                    lowPrice,
                    volume,
                    timestamp);
            chartDatas.add(chartData);
        }
        return chartDatas;
    }

    /**
     * Returns weekly chart data based on given stock history server.
     * 
     * @param stockHistoryServer the stock history server
     * @return list of weekly chart data
     */
    public static List<ChartData> getWeeklyChartData(StockHistoryServer stockHistoryServer) {
        final int days = stockHistoryServer.getNumOfCalendar();
        Calendar prevCalendar = null;

        List<ChartData> chartDatas = new ArrayList<ChartData>();

        double prevPrice = 0;
        double openPrice = 0;
        double lastPrice = 0;
        double highPrice = Double.MIN_VALUE;
        double lowPrice = Double.MAX_VALUE;
        long volume = 0;
        long timestamp = 0;
        int count = 0;

        for (int i = 0; i < days; i++) {
            // First, determine the current data is same week as the previous
            // data.
            boolean isSameWeek = false;
            Calendar calendar = stockHistoryServer.getCalendar(i);
            Stock stock = stockHistoryServer.getStock(calendar);

            if (prevCalendar != null) {
                int weekOfYear = calendar.get(Calendar.WEEK_OF_YEAR);
                int prevWeekOfYear = prevCalendar.get(Calendar.WEEK_OF_YEAR);
                // Is this the same week?
                isSameWeek = (weekOfYear == prevWeekOfYear);
            } else {
                // First time for us to enter this for loop.
                isSameWeek = true;
                openPrice = stock.getOpenPrice();
                prevPrice = stock.getPrevPrice();
            }
            
            if (isSameWeek == false) {
                // This is a new week. There must be data for previous week.
                assert(count > 0);
                ChartData chartData = ChartData.newInstance(
                        prevPrice,
                        openPrice,
                        lastPrice / count,  // Average last price.
                        highPrice,
                        lowPrice,
                        volume / count,     // Average volume.
                        timestamp);
                chartDatas.add(chartData);

                // First day of the week.
                prevPrice = stock.getPrevPrice();
                openPrice = stock.getOpenPrice();
                lastPrice = stock.getLastPrice();
                highPrice = stock.getHighPrice();
                lowPrice = stock.getLowPrice();
                volume = stock.getVolume();
                timestamp = stock.getCalendar().getTimeInMillis();
                count = 1;
            } else {
                // We will not update prevPrice and openPrice. They will remain
                // as the first day of the week's.
                lastPrice += stock.getLastPrice();
                highPrice = Math.max(highPrice, stock.getHighPrice());
                lowPrice = Math.min(lowPrice, stock.getLowPrice());
                volume += stock.getVolume();
                timestamp = stock.getCalendar().getTimeInMillis();
                count++;
            }

            prevCalendar = calendar;
        }

        // Is there any data which is not being inserted yet?
        if (count > 0) {
            ChartData chartData = ChartData.newInstance(
                    prevPrice,
                    openPrice,
                    lastPrice / count,
                    highPrice,
                    lowPrice,
                    volume / count,
                    timestamp);
            chartDatas.add(chartData);
        }

        return chartDatas;
    }

    /**
     * Returns monthly chart data based on given stock history server.
     *
     * @param stockHistoryServer the stock history server
     * @return list of monthly chart data
     */
    public static List<ChartData> getMonthlyChartData(StockHistoryServer stockHistoryServer) {
        final int days = stockHistoryServer.getNumOfCalendar();
        Calendar prevCalendar = null;

        List<ChartData> chartDatas = new ArrayList<ChartData>();

        double prevPrice = 0;
        double openPrice = 0;
        double lastPrice = 0;
        double highPrice = Double.MIN_VALUE;
        double lowPrice = Double.MAX_VALUE;
        long volume = 0;
        long timestamp = 0;
        int count = 0;

        for (int i = 0; i < days; i++) {
            // First, determine the current data is same month as the previous
            // data.
            boolean isSameMonth = false;
            Calendar calendar = stockHistoryServer.getCalendar(i);
            Stock stock = stockHistoryServer.getStock(calendar);

            if (prevCalendar != null) {
                int month = calendar.get(Calendar.MONTH);
                int prevMonth = prevCalendar.get(Calendar.MONTH);
                // Is this the same month?
                isSameMonth = (month == prevMonth);
            } else {
                // First time for us to enter this for loop.
                isSameMonth = true;
                openPrice = stock.getOpenPrice();
                prevPrice = stock.getPrevPrice();
            }

            if (isSameMonth == false) {
                // This is a new month. There must be data for previous month.
                assert(count > 0);
                ChartData chartData = ChartData.newInstance(
                        prevPrice,
                        openPrice,
                        lastPrice / count,  // Average last price.
                        highPrice,
                        lowPrice,
                        volume / count,     // Average volume.
                        timestamp);
                chartDatas.add(chartData);

                // First day of the month.
                prevPrice = stock.getPrevPrice();
                openPrice = stock.getOpenPrice();
                lastPrice = stock.getLastPrice();
                highPrice = stock.getHighPrice();
                lowPrice = stock.getLowPrice();
                volume = stock.getVolume();
                timestamp = stock.getCalendar().getTimeInMillis();
                count = 1;
            } else {
                // We will not update prevPrice and openPrice. They will remain
                // as the first day of the month's.
                lastPrice += stock.getLastPrice();
                highPrice = Math.max(highPrice, stock.getHighPrice());
                lowPrice = Math.min(lowPrice, stock.getLowPrice());
                volume += stock.getVolume();
                timestamp = stock.getCalendar().getTimeInMillis();
                count++;
            }

            prevCalendar = calendar;
        }

        // Is there any data which is not being inserted yet?
        if (count > 0) {
            ChartData chartData = ChartData.newInstance(
                    prevPrice,
                    openPrice,
                    lastPrice / count,
                    highPrice,
                    lowPrice,
                    volume / count,
                    timestamp);
            chartDatas.add(chartData);
        }

        return chartDatas;
    }

    /**
     * Applying chart theme based on given JFreeChart.
     *
     * @param chart the JFreeChart
     */
    public static void applyChartTheme(JFreeChart chart) {
        final StandardChartTheme chartTheme = (StandardChartTheme)org.jfree.chart.StandardChartTheme.createJFreeTheme();
        chartTheme.setXYBarPainter(barPainter);
        chartTheme.setShadowVisible(false);
        chartTheme.setPlotBackgroundPaint(Color.WHITE);
        chartTheme.setDomainGridlinePaint(Color.LIGHT_GRAY);
        chartTheme.setRangeGridlinePaint(Color.LIGHT_GRAY);
        chartTheme.setPlotOutlinePaint(Color.LIGHT_GRAY);
        
        // The default font used by JFreeChart unable to render Chinese properly.
        // We need to provide font which is able to support Chinese rendering.
        final Locale defaultLocale = Locale.getDefault();
        if (org.yccheok.jstock.gui.Utils.isSimplifiedChinese(defaultLocale) || org.yccheok.jstock.gui.Utils.isTraditionalChinese(defaultLocale)) {
            final Font oldExtraLargeFont = chartTheme.getExtraLargeFont();
            final Font oldLargeFont = chartTheme.getLargeFont();
            final Font oldRegularFont = chartTheme.getRegularFont();
            final Font oldSmallFont = chartTheme.getSmallFont();

            final Font extraLargeFont = new Font("Sans-serif", oldExtraLargeFont.getStyle(), oldExtraLargeFont.getSize());
            final Font largeFont = new Font("Sans-serif", oldLargeFont.getStyle(), oldLargeFont.getSize());
            final Font regularFont = new Font("Sans-serif", oldRegularFont.getStyle(), oldRegularFont.getSize());
            final Font smallFont = new Font("Sans-serif", oldSmallFont.getStyle(), oldSmallFont.getSize());

            chartTheme.setExtraLargeFont(extraLargeFont);
            chartTheme.setLargeFont(largeFont);
            chartTheme.setRegularFont(regularFont);
            chartTheme.setSmallFont(smallFont);
        }

        if (chart.getPlot() instanceof CombinedDomainXYPlot) {
            @SuppressWarnings("unchecked")
            List<Plot> plots = ((CombinedDomainXYPlot)chart.getPlot()).getSubplots();
            for (Plot plot : plots) {
                final int domainAxisCount = ((XYPlot)plot).getDomainAxisCount();
                final int rangeAxisCount = ((XYPlot)plot).getRangeAxisCount();
                for (int i = 0; i < domainAxisCount; i++) {
                    ((XYPlot)plot).getDomainAxis(i).setAxisLinePaint(Color.LIGHT_GRAY);
                    ((XYPlot)plot).getDomainAxis(i).setTickMarkPaint(Color.LIGHT_GRAY);
                }
                for (int i = 0; i < rangeAxisCount; i++) {
                    ((XYPlot)plot).getRangeAxis(i).setAxisLinePaint(Color.LIGHT_GRAY);
                    ((XYPlot)plot).getRangeAxis(i).setTickMarkPaint(Color.LIGHT_GRAY);
                }
            }
        }
        else {
            final Plot plot = chart.getPlot();
            if (plot instanceof XYPlot) {            
                final org.jfree.chart.plot.XYPlot xyPlot = (org.jfree.chart.plot.XYPlot)plot;
                final int domainAxisCount = xyPlot.getDomainAxisCount();
                final int rangeAxisCount = xyPlot.getRangeAxisCount();
                for (int i = 0; i < domainAxisCount; i++) {
                    xyPlot.getDomainAxis(i).setAxisLinePaint(Color.LIGHT_GRAY);
                    xyPlot.getDomainAxis(i).setTickMarkPaint(Color.LIGHT_GRAY);
                }
                for (int i = 0; i < rangeAxisCount; i++) {
                    xyPlot.getRangeAxis(i).setAxisLinePaint(Color.LIGHT_GRAY);
                    xyPlot.getRangeAxis(i).setTickMarkPaint(Color.LIGHT_GRAY);
                }                
            }
            //else if (plot instanceof org.jfree.chart.plot.PiePlot) {
            //    final org.jfree.chart.plot.PiePlot piePlot = (org.jfree.chart.plot.PiePlot)plot;
            //    
            //}
        }

        chartTheme.apply(chart);
    }

    private static final org.jfree.chart.renderer.xy.StandardXYBarPainter barPainter = new org.jfree.chart.renderer.xy.StandardXYBarPainter();
}
