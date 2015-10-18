package org.vaadin.marcus.skynet.ui;

import com.vaadin.addon.charts.Chart;
import com.vaadin.addon.charts.model.*;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.server.FontAwesome;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;

import java.util.Date;


public class SensorDataBox extends VerticalLayout {

    private IndexedContainer container;
    private Grid grid;
    private Chart chart;
    private Configuration configuration;


    public SensorDataBox(String title) {

        setWidth("500px");
        setHeight("500px");

        setupContainer();

        HorizontalLayout headerLayout = createHeader(title);
        headerLayout.setWidth("100%");
        headerLayout.setDefaultComponentAlignment(Alignment.MIDDLE_CENTER);
        TabSheet tabs = new TabSheet();
        tabs.setSizeFull();
        tabs.addStyleName(ValoTheme.TABSHEET_FRAMED);
        tabs.addComponents(createChart(), createGrid());

        addComponents(headerLayout, tabs);
        setExpandRatio(tabs, 1);
    }

    private HorizontalLayout createHeader(String title) {
        HorizontalLayout headerLayout = new HorizontalLayout();
        Label caption = new Label(title);
        caption.addStyleName(ValoTheme.LABEL_H1);
        Button triggersButton = new Button();
        triggersButton.setIcon(FontAwesome.BELL);

        headerLayout.setSpacing(true);
        headerLayout.addComponents(caption, triggersButton);
        headerLayout.setExpandRatio(caption, 1);
        return headerLayout;
    }

    private void setupContainer() {
        container = new IndexedContainer();
        container.addContainerProperty("Time", Date.class, null);
        container.addContainerProperty("Value", Float.class, null);
    }

    private Chart createChart() {
        chart = new Chart();
        chart.setCaption("Chart");

        chart.setSizeFull();
        configuration = chart.getConfiguration();
        configuration.setTitle("");
        configuration.getChart().setType(ChartType.SPLINE);
        configuration.getyAxis().setTitle("Temperature (°C)");
        configuration.getxAxis().setType(AxisType.DATETIME);
        ContainerDataSeries data = new ContainerDataSeries(container);
        data.setYPropertyId("Value");
        data.setXPropertyId("Time");
        PlotOptionsSpline optionsSpline = new PlotOptionsSpline();
        optionsSpline.setMarker(new Marker(false));
        optionsSpline.setAnimation(false);
        data.setPlotOptions(optionsSpline);
        configuration.addSeries(data);
        chart.drawChart(configuration);
        return chart;
    }

    private Grid createGrid() {
        grid = new Grid("Table");
        grid.setSizeFull();
        grid.setContainerDataSource(container);

        return grid;
    }


    public void addDataPoint(float dataPoint, Date time) {

        Object itemId = container.addItemAt(0);
        container.getContainerProperty(itemId, "Value").setValue(dataPoint);
        container.getContainerProperty(itemId, "Time").setValue(time);

        grid.setSortOrder(grid.getSortOrder());
        chart.drawChart(configuration);

        if (container.size() > 1000) {
            container.removeItem(container.getIdByIndex(999));
        }


    }
}
