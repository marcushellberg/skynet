package org.vaadin.marcus.skynet.ui;

import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.marcus.skynet.entities.Sensor;
import org.vaadin.marcus.skynet.entities.Trigger;
import org.vaadin.marcus.skynet.service.MessageService;
import org.vaadin.viritin.BeanBinder;
import org.vaadin.viritin.button.PrimaryButton;
import org.vaadin.viritin.layouts.MHorizontalLayout;
import org.vaadin.viritin.layouts.MVerticalLayout;

public class AlarmsWindow extends Window {

    private MessageService service = MessageService.getInstance();
    private Grid triggersGrid;
    private Trigger trigger;
    private Sensor sensor;
    private TriggerForm form;

    public AlarmsWindow(Sensor sensor) {
        this.sensor = sensor;

        setCaption("Alarms");
        center();
        setModal(true);
        setWidth("80%");

        setContent(new MHorizontalLayout(createForm(), createGrid()).withFullWidth().withMargin(true));
    }

    private VerticalLayout createForm() {
        Label caption = new Label("Add new alarm");
        caption.addStyleName(ValoTheme.LABEL_H3);

        form = new TriggerForm();
        resetForm();

        Button addButton = new PrimaryButton("Add", event -> {
            service.addTrigger(trigger);
            resetForm();
            refreshGrid();
            Notification.show("Added successfully.");
        });

        Button clearButton = new Button("Clear", event -> resetForm());
        clearButton.addStyleName(ValoTheme.BUTTON_LINK);

        MHorizontalLayout buttonsLayout = new MHorizontalLayout(addButton, clearButton).withMargin(false);

        return new MVerticalLayout(caption, form, buttonsLayout).withMargin(false);
    }

    private VerticalLayout createGrid() {

        Label caption = new Label("Existing");
        caption.addStyleName(ValoTheme.LABEL_H3);

        triggersGrid = new Grid();
        triggersGrid.setEditorEnabled(true);
        triggersGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        triggersGrid.setColumns("severity", "condition", "triggerValue", "triggered");
        triggersGrid.setSizeFull();
        refreshGrid();

        Button removeButton = new Button("Remove selected", event -> {
            triggersGrid.getSelectedRows().forEach(t -> {
                service.removeTrigger((Trigger) t);
            });
            refreshGrid();
        });
        removeButton.addStyleName(ValoTheme.BUTTON_DANGER);

        Button resetButton = new Button("Reset all", event -> {
            MessageService.getInstance().getTriggersForSensor(sensor).forEach(t -> t.setTriggered(false));
            refreshGrid();
        });

        MHorizontalLayout buttonsLayout = new MHorizontalLayout(resetButton, removeButton);

        return new MVerticalLayout(caption, triggersGrid, buttonsLayout).expand(triggersGrid).withMargin(false);
    }

    private void refreshGrid() {
        triggersGrid.setContainerDataSource(new BeanItemContainer<>(Trigger.class, service.getTriggersForSensor(sensor)));
    }

    private void resetForm() {
        trigger = new Trigger();
        trigger.setSensor(sensor);
        BeanBinder.bind(trigger, form);
    }
}
