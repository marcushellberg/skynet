package org.vaadin.marcus.skynet.ui;

import com.google.common.eventbus.Subscribe;
import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.Configuration;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import org.vaadin.marcus.skynet.entities.Sensor;
import org.vaadin.marcus.skynet.service.MessageService;
import org.vaadin.marcus.skynet.service.SensorOfflineEvent;
import org.vaadin.marcus.skynet.service.SensorTriggeredEvent;
import org.vaadin.marcus.skynet.service.SensorUpdatedEvent;

import javax.servlet.annotation.WebServlet;
import java.util.HashMap;
import java.util.Map;

@Theme("valo")
@Title("Skynet dashboard")
@Push
public class DashboardUI extends UI {

    private Map<Sensor, SensorDataBox> sensorData = new HashMap<>();
    private Configuration chartConfiguration;
    private Chart tempChart;
    private MessageService messageService = new MessageService();
    private GridLayout layout;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        layout = new GridLayout(2, 2);
        layout.setSpacing(true);
        layout.setMargin(true);
        setContent(layout);
        messageService.registerListener(this);

        addDetachListener(detach -> messageService.unregisterListener(this));
    }

    @Subscribe
    public void sensorUpdateListener(SensorUpdatedEvent evt) {
        try {
            Sensor sensor = evt.getSensor();
            if (sensorData.containsKey(sensor)) {
                access(() -> sensorData.get(sensor).addDataPoint(sensor.getValue(), sensor.getTime()));
            } else {
                SensorDataBox chart = new SensorDataBox(sensor.getName());
                access(() -> {
                    layout.addComponent(chart);
                    chart.addDataPoint(sensor.getValue(), sensor.getTime());
                    sensorData.put(sensor, chart);
                });
            }
        } catch (Exception ex) {
            // Don't kill the messenger
        }
    }

    @Subscribe
    public void sensorOfflineListener(SensorOfflineEvent evt) {
        try {
            Sensor sensor = evt.getSensor();
            access(() -> {
                Notification.show(evt.getSensor().getName() + " went offline.", Notification.Type.TRAY_NOTIFICATION);
                if (sensorData.containsKey(sensor)) {
                    layout.removeComponent(sensorData.get(sensor));
                    sensorData.remove(sensor);
                }
            });
        } catch (Exception ex) {
            // Don't kill messaging service
        }
    }

    @Subscribe
    public void sensorTriggeredListener(SensorTriggeredEvent evt) {
        access(() -> Notification.show("Sensor alert triggered", Notification.Type.WARNING_MESSAGE));
    }

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DashboardUI.class, widgetset = "org.vaadin.marcus.DashboardWidgetset")
    public static class Servlet extends VaadinServlet {
    }
}
