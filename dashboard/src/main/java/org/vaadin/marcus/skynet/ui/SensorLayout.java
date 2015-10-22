package org.vaadin.marcus.skynet.ui;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.marcus.skynet.entities.Sensor;
import org.vaadin.marcus.skynet.entities.Trigger;
import org.vaadin.marcus.skynet.service.MessageService;

import java.util.Date;
import java.util.Set;


public class SensorLayout extends VerticalLayout {

    public static final String VALUE_PROPERTY_ID = "Value";
    public static final String TIME_PROPERTY_ID = "Time";
    public static final int MAX_MEASUREMENTS = 1000;

    private Grid grid;
    private Sensor sensor;
    private Label warningIcon;
    private DataSeries chartData;
    private Label statusIndicator;
    private IndexedContainer container;


    public SensorLayout(Sensor sensor) {
        this.sensor = sensor;
        setWidth("500px");
        setHeight("500px");

        setupContainer();

        TabSheet tabs = new TabSheet();

        tabs.setSizeFull();
        tabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabs.addComponents(createChart(), createGrid());

        addComponents(createHeader(sensor.getName()), tabs);
        setExpandRatio(tabs, 1);
    }

    public void setOffline() {
        statusIndicator.addStyleName("offline");
    }

    public void triggered() {
        warningIcon.setVisible(true);
        Notification.show("Sensor '" + sensor.getName() + "' value crossed threshold.", Notification.Type.WARNING_MESSAGE);
    }

    public void addDataPoint(Double dataPoint, Date time) {
        // Add to Grid
        Object itemId = container.addItemAt(0);
        container.getContainerProperty(itemId, VALUE_PROPERTY_ID).setValue(dataPoint);
        container.getContainerProperty(itemId, TIME_PROPERTY_ID).setValue(time);
        grid.setSortOrder(grid.getSortOrder());

        // Round to nearest half degree for chart
        dataPoint = 0.5 * Math.round(dataPoint / 0.5);

        // Add to chart
        chartData.add(new DataSeriesItem(time, dataPoint));

        pruneMeasurements();
        updateStatusIcons();
    }


    private void setupContainer() {
        container = new IndexedContainer();
        container.addContainerProperty(TIME_PROPERTY_ID, Date.class, null);
        container.addContainerProperty(VALUE_PROPERTY_ID, Double.class, null);
    }

    private HorizontalLayout createHeader(String title) {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidth("100%");
        headerLayout.addStyleName("sensor-header-layout");
        headerLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        headerLayout.setSpacing(true);

        statusIndicator = new Label(FontAwesome.CIRCLE.getHtml(), ContentMode.HTML);
        statusIndicator.addStyleName("status-indicator");
        statusIndicator.setWidth("20px");

        Label caption = new Label(title);
        caption.setSizeUndefined();
        caption.addStyleName("sensor-name");
        Button alarmsButton = new Button();
        alarmsButton.setIcon(FontAwesome.BELL);
        alarmsButton.addClickListener(event -> getUI().addWindow(new AlarmsWindow(sensor)));

        warningIcon = new Label(FontAwesome.WARNING.getHtml(), ContentMode.HTML);
        warningIcon.addStyleName("warning-icon");
        warningIcon.setWidth("20px");
        warningIcon.setVisible(false);

        headerLayout.addComponents(statusIndicator, caption, warningIcon, alarmsButton);
        headerLayout.setExpandRatio(alarmsButton, 1);
        headerLayout.setComponentAlignment(alarmsButton, Alignment.MIDDLE_RIGHT);

        return headerLayout;
    }

    private Chart createChart() {
        Chart chart = new Chart();
        chart.setCaption("Chart");

        chart.setSizeFull();

        // Configure chart type and options
        Configuration configuration = chart.getConfiguration();
        configuration.setTitle("");
        configuration.getChart().setType(ChartType.SPLINE);
        configuration.getyAxis().setTitle("Temperature (°C)");
        configuration.getxAxis().setType(AxisType.DATETIME);
        chartData = new DataSeries();
        PlotOptionsSpline optionsSpline = new PlotOptionsSpline();
        optionsSpline.setMarker(new Marker(false));
        chartData.setPlotOptions(optionsSpline);
        configuration.addSeries(chartData);
        chart.drawChart(configuration);

        return chart;
    }

    private Grid createGrid() {
        grid = new Grid("Table");
        grid.setSizeFull();
        grid.setContainerDataSource(container);

        // Style any values that are above/below threshold values  
        grid.setCellStyleGenerator(new WarningCellStyleGenerator());

        return grid;
    }

    private void pruneMeasurements() {
        if (container.size() > MAX_MEASUREMENTS) {
            container.removeItem(container.getIdByIndex(MAX_MEASUREMENTS - 1));
        }

        if (chartData.size() > MAX_MEASUREMENTS) {
            chartData.remove(chartData.get(MAX_MEASUREMENTS - 1));
        }
    }

    private void updateStatusIcons() {
        statusIndicator.removeStyleName("offline");
        warningIcon.setVisible(MessageService.getInstance().getTriggersForSensor(sensor).stream().anyMatch(Trigger::isTriggered));
    }

    private class WarningCellStyleGenerator implements Grid.CellStyleGenerator {
        @Override
        public String getStyle(Grid.CellReference cellReference) {
            if (VALUE_PROPERTY_ID.equals(cellReference.getPropertyId())) {
                Double cellValue = (Double) cellReference.getValue();

                Set<Trigger> triggers = MessageService.getInstance().getTriggersForSensor(sensor);

                for (Trigger trigger : triggers) {
                    Double triggerValue = trigger.getTriggerValue();
                    if ((trigger.getCondition() == Trigger.Condition.GREATER_THAN && cellValue.compareTo(triggerValue) > 0) ||
                            (trigger.getCondition() == Trigger.Condition.LESS_THAN && cellValue.compareTo(triggerValue) < 0)) {
                        return "warning";
                    }
                }
            }
            return null;
        }
    }
}