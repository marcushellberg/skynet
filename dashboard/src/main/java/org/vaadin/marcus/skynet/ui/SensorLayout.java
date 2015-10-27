package org.vaadin.marcus.skynet.ui;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.*;
import org.vaadin.marcus.skynet.entities.Sensor;
import org.vaadin.marcus.skynet.entities.Trigger;
import org.vaadin.marcus.skynet.service.MessageService;

import java.util.Date;
import java.util.Set;


public class SensorLayout extends VerticalLayout {

    public static final int MAX_MEASUREMENTS = 1000;

    private Grid grid;
    private Sensor sensor;
    private Label warningIcon;
    private DataSeries chartData;
    private Label onlineIndicator;
    private BeanItemContainer<Measurement> gridDataSource;


    public SensorLayout(Sensor sensor) {
        this.sensor = sensor;
        setSizeFull();

        TabSheet tabs = new TabSheet();

        tabs.setSizeFull();
        tabs.addComponents(createChart(), createGrid());

        addComponents(createHeader(sensor.getName()), tabs);
        setExpandRatio(tabs, 1);
    }

    private HorizontalLayout createHeader(String title) {
        HorizontalLayout headerLayout = new HorizontalLayout();
        headerLayout.setWidth("100%");
        headerLayout.addStyleName("sensor-header-layout");
        headerLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        headerLayout.setSpacing(true);

        // Online indicator
        onlineIndicator = new Label(FontAwesome.CIRCLE.getHtml(), ContentMode.HTML);
        onlineIndicator.addStyleName("status-indicator");
        onlineIndicator.setWidth("20px");

        // Caption
        Label caption = new Label(title);
        caption.setSizeUndefined();
        caption.addStyleName("sensor-name");
        Button alarmsButton = new Button();
        alarmsButton.setIcon(FontAwesome.BELL);
        alarmsButton.addClickListener(event -> getUI().addWindow(new AlarmsWindow(sensor)));

        // Warning icon
        warningIcon = new Label(FontAwesome.WARNING.getHtml(), ContentMode.HTML);
        warningIcon.addStyleName("warning-icon");
        warningIcon.setWidth("20px");
        warningIcon.setVisible(false);

        headerLayout.addComponents(onlineIndicator, caption, warningIcon, alarmsButton);
        headerLayout.setExpandRatio(alarmsButton, 1);
        headerLayout.setComponentAlignment(alarmsButton, Alignment.MIDDLE_RIGHT);

        return headerLayout;
    }

    private Chart createChart() {
        Chart chart = new Chart();
        chart.setCaption("Chart");
        chart.setSizeFull();

        // Hide data point marker
        PlotOptionsSpline plotOptions = new PlotOptionsSpline();
        plotOptions.setMarker(new Marker(false));

        // Empty data series for measurement data
        chartData = new DataSeries();
        chartData.setPlotOptions(plotOptions);

        // Configure chart type and set titles
        Configuration configuration = chart.getConfiguration();
        configuration.setTitle("");
        configuration.getChart().setType(ChartType.SPLINE);
        configuration.getyAxis().setTitle("Temperature (°C)");
        configuration.getxAxis().setType(AxisType.DATETIME);
        configuration.addSeries(chartData);

        chart.drawChart(configuration);
        return chart;
    }

    private Grid createGrid() {
        grid = new Grid("Table");
        grid.setSizeFull();
        gridDataSource = new BeanItemContainer<>(Measurement.class);
        grid.setContainerDataSource(gridDataSource);

        // Style any values that are above/below threshold values
        grid.setCellStyleGenerator(new WarningCellStyleGenerator());

        return grid;
    }

    public void addDataPoint(Double dataPoint, Date time) {
        gridDataSource.addItemAt(0, new Measurement(time, dataPoint));
        grid.setSortOrder(grid.getSortOrder());

        chartData.add(new DataSeriesItem(time, dataPoint));

        pruneMeasurements();
        updateStatusIcons();
    }

    public void setOffline() {
        onlineIndicator.addStyleName("offline");
    }

    public void triggered() {
        warningIcon.setVisible(true);
        Notification.show("Sensor '" + sensor.getName() + "' value crossed threshold.", Notification.Type.WARNING_MESSAGE);
    }

    private void pruneMeasurements() {
        if (gridDataSource.size() > MAX_MEASUREMENTS) {
            gridDataSource.removeItem(gridDataSource.getIdByIndex(MAX_MEASUREMENTS - 1));
        }

        if (chartData.size() > MAX_MEASUREMENTS) {
            chartData.remove(chartData.get(MAX_MEASUREMENTS - 1));
        }
    }

    private void updateStatusIcons() {
        onlineIndicator.removeStyleName("offline");
        warningIcon.setVisible(MessageService.getInstance().getTriggersForSensor(sensor).stream().anyMatch(Trigger::isTriggered));
    }

    private class WarningCellStyleGenerator implements Grid.CellStyleGenerator {
        @Override
        public String getStyle(Grid.CellReference cellReference) {
            if ("value".equals(cellReference.getPropertyId())) {
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

    protected class Measurement {
        private final Date timestamp;
        private final Double value;

        public Measurement(Date timestamp, Double value) {
            this.timestamp = timestamp;
            this.value = value;
        }

        public Date getTimestamp() {
            return timestamp;
        }

        public Double getValue() {
            return value;
        }

    }

}
