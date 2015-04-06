package org.vaadin.marcus.skynet.ui;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.ChartType;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.addon.charts.model.ListSeries;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.Widgetset;
import com.vaadin.cdi.CDIUI;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.ValoTheme;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.vaadin.marcus.skynet.entities.Sensor;
import org.vaadin.marcus.skynet.service.MessageService;
import org.vaadin.marcus.skynet.service.SensorOfflineEvent;
import org.vaadin.marcus.skynet.service.SensorUpdatedEvent;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@Theme("valo")
@Widgetset("org.vaadin.marcus.DashboardWidgetset")
@Title("Skynet dashboard")
@CDIUI("")
public class DashboardUI extends UI {

    private VerticalLayout layout;
    private Map<Sensor, ListSeries> sensorData = new HashMap<>();
    private Configuration chartConfiguration;
    private Chart tempChart;

    @Inject
    MessageService messageService;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        setPollInterval(500);
        layout = new VerticalLayout();
        layout.setMargin(true);
        layout.setSpacing(true);
        setContent(layout);

        Label title = new Label("IoT! Cloud! Hypeword! Enterprise Edition &trade;", ContentMode.HTML);
        title.addStyleName(ValoTheme.LABEL_H1);
        layout.addComponent(title);
        createSensorChart();

        try {
            messageService.init();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    private void createSensorChart() {
        tempChart = new Chart();
        tempChart.setWidth("100%");
        tempChart.setHeight("300px");
        chartConfiguration = tempChart.getConfiguration();
        chartConfiguration.setTitle("Sensor Temperature");
        chartConfiguration.getChart().setType(ChartType.SPLINE);
        chartConfiguration.getyAxis().setTitle("Temperature (°C)");
        layout.addComponent(tempChart);
    }

    public void sensorUpdateListener(@Observes SensorUpdatedEvent evt) {
        Sensor sensor = evt.getSensor();

        if (sensorData.containsKey(sensor)) {
            access(() -> sensorData.get(sensor).addData(sensor.getValue()));
        } else {
            ListSeries listSeries = new ListSeries();
            listSeries.setName(sensor.getName());
            listSeries.addData(sensor.getValue());
            sensorData.put(sensor, listSeries);
            access(() -> {
                chartConfiguration.addSeries(listSeries);
                tempChart.drawChart(chartConfiguration);
            });
        }
    }

    public void sensorOfflineListener(@Observes SensorOfflineEvent evt) {
        Sensor sensor = evt.getSensor();
        if (sensorData.containsKey(sensor)) {
            sensorData.remove(sensor);
            chartConfiguration.setSeries(new ArrayList<>(sensorData.values()));
        }
    }
}
