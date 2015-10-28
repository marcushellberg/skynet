package org.vaadin.marcus.skynet.ui;

import com.google.common.eventbus.Subscribe;
import com.vaadin.annotations.Push;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.Title;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.UI;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.marcus.skynet.entities.Sensor;
import org.vaadin.marcus.skynet.events.SensorOfflineEvent;
import org.vaadin.marcus.skynet.events.SensorTriggeredEvent;
import org.vaadin.marcus.skynet.events.SensorUpdatedEvent;
import org.vaadin.marcus.skynet.service.MessageService;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

import javax.servlet.annotation.WebServlet;
import java.util.HashMap;
import java.util.Map;

@Push
@Theme("dashboard")
@Title("Skynet dashboard")
public class DashboardUI extends UI {

    private MessageService messageService = MessageService.getInstance();
    private Map<Sensor, SensorLayout> sensors = new HashMap<>();
    private MHorizontalLayout sensorsLayout;

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        Label heading = new Label("Sensor Dashboard");
        heading.addStyleName(ValoTheme.LABEL_H1);

        sensorsLayout = new MHorizontalLayout().withFullWidth();
        setContent(new MVerticalLayout(heading).expand(sensorsLayout));

        messageService.registerListener(this);
        addDetachListener(detach -> messageService.unregisterListener(this));
    }

    @Subscribe
    public void sensorUpdateListener(SensorUpdatedEvent evt) {
        Sensor sensor = evt.getSensor();

        if (sensors.containsKey(sensor)) {
            access(() -> sensors.get(sensor).addDataPoint(sensor.getValue(), sensor.getTime()));
        } else {
            SensorLayout sensorLayout = new SensorLayout(sensor);
            sensorLayout.addDataPoint(sensor.getValue(), sensor.getTime());

            access(() -> {
                this.sensorsLayout.addComponent(sensorLayout);
                sensors.put(sensor, sensorLayout);
            });
        }
    }

    @Subscribe
    public void sensorOfflineListener(SensorOfflineEvent evt) {
        Sensor sensor = evt.getSensor();
        access(() -> {
            Notification.show(evt.getSensor().getName() + " went offline.", Notification.Type.TRAY_NOTIFICATION);
            if (sensors.containsKey(sensor)) {
                sensors.get(sensor).setOffline();
            }
        });

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
