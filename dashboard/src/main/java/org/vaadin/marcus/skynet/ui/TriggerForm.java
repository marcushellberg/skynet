package org.vaadin.marcus.skynet.ui;

import com.vaadin.ui.*;
import org.vaadin.marcus.skynet.entities.Alarm;
import org.vaadin.marcus.skynet.entities.Trigger;
import org.vaadin.marcus.skynet.service.MessageService;
import org.vaadin.marcus.skynet.shared.Severity;
import org.vaadin.viritin.fields.MTextField;

import java.util.Set;

public class TriggerForm extends FormLayout {
    MessageService service = MessageService.getInstance();

    // Note that the attributes have the same names as in the Trigger class
    CheckBox triggerAll = new CheckBox("Trigger all alarms");
    OptionGroup alarms = new OptionGroup("Trigger selected alarms");
    ComboBox condition = new ComboBox("Condition");
    MTextField triggerValue = new MTextField("Trigger value");
    ComboBox severity = new ComboBox("Severity");

    public TriggerForm() {
        setSpacing(true);

        // Alarm selector
        Set<Alarm> foundAlarms = service.getAlarms();
        if (foundAlarms.isEmpty()) {
            triggerAll.setValue(true);
            triggerAll.setEnabled(false);
            alarms.setVisible(false);
        } else {
            foundAlarms.forEach(alarm -> this.alarms.addItem(alarm));
            alarms.setMultiSelect(true);
            alarms.setEnabled(false);
            triggerAll.addValueChangeListener(event -> this.alarms.setEnabled(!triggerAll.getValue()));
        }

        // Populate trigger conditions
        for (Trigger.Condition c : Trigger.Condition.values()) {
            condition.addItem(c);
        }
        condition.setNullSelectionAllowed(false);

        // Populate severity values
        for (Severity s : Severity.values()) {
            severity.addItem(s);
        }
        severity.setNullSelectionAllowed(false);

        triggerValue.setRequired(true);

        addComponents(triggerAll, alarms, condition, triggerValue, severity);
    }
}
