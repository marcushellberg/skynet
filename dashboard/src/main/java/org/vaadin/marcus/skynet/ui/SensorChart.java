package org.vaadin.marcus.skynet.ui;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.ListSeries;


public class SensorChart extends Chart {

    private final ListSeries data;

    public SensorChart(String title) {
        setWidth("100%");
        setHeight("300px");
        Configuration configuration = getConfiguration();
        configuration.setTitle(title);
        configuration.getChart().setType(ChartType.SPLINE);
        configuration.getyAxis().setTitle("Temperature (°C)");
        data = new ListSeries();
        configuration.addSeries(data);
        drawChart(configuration);
    }


    public void addDataPoint(float dataPoint) {
        data.addData(dataPoint);
    }
}
