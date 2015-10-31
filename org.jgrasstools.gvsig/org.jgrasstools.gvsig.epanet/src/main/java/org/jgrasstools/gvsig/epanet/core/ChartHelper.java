/*
 * JGrass - Free Open Source Java GIS http://www.jgrass.org 
 * (C) HydroloGIS - www.hydrologis.com 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.jgrasstools.gvsig.epanet.core;

import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.gvsig.tools.swing.api.ToolsSwingLocator;
import org.gvsig.tools.swing.api.windowmanager.WindowManager;
import org.gvsig.tools.swing.api.windowmanager.WindowManager.MODE;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimePeriodAnchor;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.joda.time.DateTime;

/**
 * A Shell that can visualize a chart with multiple series.
 * 
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ChartHelper {

    private static SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    /**
     * Constructor.
     * 
     * @param valuesMapList the list of ordered maps containing x,y pairs.
     * @param title the title of the chart.
     * @param xLabel the x label.
     * @param yLabel the y label.
     */
    @SuppressWarnings("deprecation")
    public static void chart( List<LinkedHashMap<DateTime, float[]>> valuesMapList, String title, String xLabel, String yLabel ) {

        List<TimeSeries> seriesList = new ArrayList<TimeSeries>();
        for( LinkedHashMap<DateTime, float[]> valuesMap : valuesMapList ) {
            TimeSeries[] series = new TimeSeries[0];
            Set<Entry<DateTime, float[]>> entrySet = valuesMap.entrySet();
            for( Entry<DateTime, float[]> entry : entrySet ) {
                DateTime date = entry.getKey();
                float[] values = entry.getValue();

                if (series.length == 0) {
                    series = new TimeSeries[values.length];
                    for( int i = 0; i < values.length; i++ ) {
                        series[i] = new TimeSeries(i + 1);
                    }
                }

                for( int i = 0; i < values.length; i++ ) {
                    int sec = date.getSecondOfMinute();
                    int min = date.getMinuteOfHour();
                    int hour = date.getHourOfDay();
                    int day = date.getDayOfMonth();
                    int month = date.getMonthOfYear();
                    int year = date.getYear();
                    series[i].add(new Second(sec, min, hour, day, month, year), values[i]);
                }
            }
            for( TimeSeries timeSeries : series ) {
                seriesList.add(timeSeries);
            }
        }

        TimeSeriesCollection lineDataset = new TimeSeriesCollection();
        for( TimeSeries timeSeries : seriesList ) {
            lineDataset.addSeries(timeSeries);
        }

        lineDataset.setXPosition(TimePeriodAnchor.MIDDLE);
        lineDataset.setDomainIsPointsInTime(true);

        JFreeChart theChart = ChartFactory.createTimeSeriesChart(null, xLabel, yLabel, lineDataset, seriesList.size() > 1, true, true);
        XYPlot thePlot = theChart.getXYPlot();

        ((XYPlot) thePlot).setRenderer(new XYLineAndShapeRenderer());
        XYItemRenderer renderer = ((XYPlot) thePlot).getRenderer();

        DateAxis axis = (DateAxis) ((XYPlot) thePlot).getDomainAxis();
        axis.setDateFormatOverride(dateFormatter);
        // axis.setAutoRangeMinimumSize(5.0);

        ValueAxis rangeAxis = ((XYPlot) thePlot).getRangeAxis();
        rangeAxis.setAutoRangeMinimumSize(5.0);

        for( int i = 0; i < seriesList.size(); i++ ) {
            ((XYLineAndShapeRenderer) renderer).setSeriesLinesVisible(i, true);
            ((XYLineAndShapeRenderer) renderer).setSeriesShapesVisible(i, true);
        }
        
        ChartPanel chartPanel = new ChartPanel(theChart, false);
        chartPanel.setPreferredSize(new Dimension(600, 500));
        chartPanel.setHorizontalAxisTrace(false);
        chartPanel.setVerticalAxisTrace(false);
        
        
        WindowManager windowManager = ToolsSwingLocator.getWindowManager();
        windowManager.showWindow(chartPanel, title, MODE.WINDOW);
    }


}
