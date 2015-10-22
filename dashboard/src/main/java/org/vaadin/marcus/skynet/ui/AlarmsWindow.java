package org.vaadin.marcus.skynet.ui;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.ShortcutAction;
import com.vaadin.ui.*;
import com.vaadin.ui.themes.ValoTheme;
import org.vaadin.marcus.skynet.entities.Sensor;
import org.vaadin.marcus.skynet.entities.Trigger;
import org.vaadin.marcus.skynet.service.MessageService;

public class AlarmsWindow extends Window {

    private MessageService service = MessageService.getInstance();
    private Grid triggersGrid;
    private FieldGroup fieldGroup;
    private Sensor sensor;
    private Trigger trigger;

    public AlarmsWindow(Sensor sensor) {
        this.sensor = sensor;

        setCaption("Alarms");
        setModal(true);
        center();
        setWidth("90%");
        setHeight("500px");

        createRootLayout();
    }

    private void createRootLayout() {
        HorizontalLayout rootLayout = new HorizontalLayout();
        rootLayout.setSizeFull();
        rootLayout.setSpacing(true);
        rootLayout.setMargin(true);

        VerticalLayout form = createForm();
        VerticalLayout grid = createGrid();

        rootLayout.addComponents(form, grid);
        rootLayout.setExpandRatio(form, 2);
        rootLayout.setExpandRatio(grid, 3);

        setContent(rootLayout);
    }

    private VerticalLayout createForm() {
        VerticalLayout formLayout = new VerticalLayout();
        formLayout.setSizeFull();
        formLayout.setSpacing(true);

        Label addNewLabel = new Label("Add new alarm");
        addNewLabel.addStyleName(ValoTheme.LABEL_H3);

        TriggerForm form = new TriggerForm();
        fieldGroup = new FieldGroup();
        resetForm();
        fieldGroup.bindMemberFields(form);

        formLayout.addComponent(addNewLabel);
        formLayout.addComponent(form);
        formLayout.addComponent(createButtonsLayout());
        formLayout.setExpandRatio(form, 1);

        return formLayout;
    }

    private VerticalLayout createGrid() {
        VerticalLayout gridLayout = new VerticalLayout();
        gridLayout.setSpacing(true);
        gridLayout.setSizeFull();

        Label existingLabel = new Label("Existing");
        existingLabel.addStyleName(ValoTheme.LABEL_H3);

        triggersGrid = new Grid();
        triggersGrid.setEditorEnabled(true);
        triggersGrid.setSelectionMode(Grid.SelectionMode.MULTI);
        triggersGrid.setColumns("severity", "condition", "triggerValue", "triggered");
        triggersGrid.setSizeFull();
        refreshGrid();

        Button removeButton = new Button("Remove selected", event -> {
            triggersGrid.getSelectedRows().forEach(t -> {
                service.removeTrigger((Trigger) t);
                refreshGrid();
            });
        });
        removeButton.addStyleName(ValoTheme.BUTTON_DANGER);

        Button resetButton = new Button("Reset all", event -> {
            MessageService.getInstance().getTriggersForSensor(sensor).forEach(t -> t.setTriggered(false));
            refreshGrid();
        });

        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);
        buttonsLayout.setWidth("100%");
        buttonsLayout.addComponents(resetButton, removeButton);
        buttonsLayout.setExpandRatio(resetButton, 1);
        buttonsLayout.setComponentAlignment(resetButton, Alignment.MIDDLE_RIGHT);

        gridLayout.addComponents(existingLabel, triggersGrid, buttonsLayout);
        gridLayout.setExpandRatio(triggersGrid, 1);
        return gridLayout;
    }

    private void refreshGrid() {
        triggersGrid.setContainerDataSource(new BeanItemContainer<>(Trigger.class, service.getTriggersForSensor(sensor)));
    }

    private void resetForm() {
        trigger = new Trigger();
        trigger.setSensor(sensor);
        fieldGroup.setItemDataSource(new BeanItem<>(trigger));
    }

    private HorizontalLayout createButtonsLayout() {
        HorizontalLayout buttonsLayout = new HorizontalLayout();
        buttonsLayout.setSpacing(true);

        Button addButton = new Button("Add", event -> {
            try {
                fieldGroup.commit();
                service.addTrigger(trigger);
                resetForm();
                refreshGrid();
                Notification.show("Added successfully.");
            } catch (FieldGroup.CommitException e) {
                Notification.show("Adding failed.", Notification.Type.WARNING_MESSAGE);
                e.printStackTrace();
            }
        });
        addButton.addStyleName(ValoTheme.BUTTON_PRIMARY);
        addButton.setClickShortcut(ShortcutAction.KeyCode.ENTER);

        Button clearButton = new Button("Clear", event -> resetForm());
        clearButton.addStyleName(ValoTheme.BUTTON_LINK);
        buttonsLayout.addComponents(addButton, clearButton);
        return buttonsLayout;
    }
}
