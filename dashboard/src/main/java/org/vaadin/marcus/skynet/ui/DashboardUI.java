package org.vaadin.marcus.skynet.ui;

import com.vaadin.annotations.*;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Button;
import com.vaadin.ui.Label;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import javax.servlet.annotation.WebServlet;

@Push
@Theme("valo")
@Widgetset("org.vaadin.marcus.DashboardWidgetset")
@Title("Skynet dashboard")
public class DashboardUI extends UI {

    @Override
    protected void init(VaadinRequest vaadinRequest) {
        final VerticalLayout layout = new VerticalLayout();
        layout.setMargin(true);
        setContent(layout);

        Button button = new Button("Click Me");
        button.addClickListener(event -> layout.addComponent(new Label("Thank you for clicking")));
        layout.addComponent(button);

    }

    @WebServlet(urlPatterns = "/*", name = "Dashboard", asyncSupported = true)
    @VaadinServletConfiguration(ui = DashboardUI.class, productionMode = false)
    public static class MyUIServlet extends VaadinServlet {
    }
}
