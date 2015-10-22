package org.vaadin.marcus.skynet.ui;

import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.marcus.skynet.entities.Sensor;
import org.vaadin.marcus.skynet.service.MessageService;
import org.vaadin.marcus.skynet.events.SensorOfflineEvent;
import org.vaadin.marcus.skynet.events.SensorTriggeredEvent;
import org.vaadin.marcus.skynet.events.SensorUpdatedEvent;

import javax.servlet.annotation.WebServlet;
import java.util.HashMap;
import java.util.Map;

@Push
@Title("Skynet dashboard")
@Theme("dashboard")
public class DashboardUI extends UI {

    private Map<Sensor, SensorLayout> sensors = new HashMap<>();
    private MessageService messageService = MessageService.getInstance();
    private GridLayout sensorGrid;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setMargin(true);
        rootLayout.setSpacing(true);
        rootLayout.setSizeFull();

        Label heading = new Label("Sensor Dashboard");
        heading.addStyleName(ValoTheme.LABEL_H1);

        sensorGrid = new GridLayout(2, 2);
        sensorGrid.setSpacing(true);
        sensorGrid.setMargin(true);
        sensorGrid.setSizeFull();

        rootLayout.addComponents(heading, sensorGrid);
        rootLayout.setExpandRatio(sensorGrid, 1);
        setContent(rootLayout);

        messageService.registerListener(this);
        addDetachListener(detach -> messageService.unregisterListener(this));
    }

    @Subscribe
    public void sensorUpdateListener(SensorUpdatedEvent evt) {
        try {
            Sensor sensor = evt.getSensor();
            if (sensors.containsKey(sensor)) {
                access(() -> sensors.get(sensor).addDataPoint(sensor.getValue(), sensor.getTime()));
            } else {
                SensorLayout chart = new SensorLayout(sensor);
                access(() -> {
                    sensorGrid.addComponent(chart);
                    chart.addDataPoint(sensor.getValue(), sensor.getTime());
                    sensors.put(sensor, chart);
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
                if (sensors.containsKey(sensor)) {
                    sensors.get(sensor).setOffline();
                }
            });
        } catch (Exception ex) {
            // Don't kill messaging service
        }
    }

    @Subscribe
    public void sensorTriggeredListener(SensorTriggeredEvent evt) {
        access(() -> sensors.get(evt.getSensor()).triggered());
    }

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = DashboardUI.class, widgetset = "org.vaadin.marcus.DashboardWidgetset")
    public static class Servlet extends VaadinServlet {
    }
}
