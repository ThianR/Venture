package com.inventiva.venture.modules.bs.presentation.components;

import com.inventiva.venture.modules.bs.domain.model.BswPersonas;
import com.vaadin.flow.component.ComponentEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.BinderValidationStatus;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.binder.ValidationResult;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.shared.Registration;
import java.util.Locale;
import java.util.Objects;

public class FormBswPersonas extends FormLayout {

    private final TextField codPersona = new TextField("Código");
    private final TextField nombre = new TextField("Nombre");
    private final TextField ruc = new TextField("RUC");
    private final TextField direccion = new TextField("Dirección");
    private final TextField telefono = new TextField("Teléfono");
    private final DatePicker fecNacimiento = new DatePicker("Fecha de nacimiento");
    private final Checkbox esFisicaAux = new Checkbox("Persona física");
    private final Checkbox esClienteAux = new Checkbox("Cliente");
    private final Checkbox esProveedorAux = new Checkbox("Proveedor");
    private final Checkbox esEmpleadoAux = new Checkbox("Empleado");
    private final Checkbox estadoActivoAux = new Checkbox("Activo");

    private final Button save = new Button("Guardar");
    private final Button delete = new Button("Eliminar");
    private final Button cancel = new Button("Cancelar");

    private final BeanValidationBinder<BswPersonas> binder = new BeanValidationBinder<>(BswPersonas.class);

    private BswPersonas persona;

    public FormBswPersonas() {
        configureFields();
        configureBinder();
        add(codPersona, nombre, ruc, direccion, telefono, fecNacimiento,
                esFisicaAux, esClienteAux, esProveedorAux, esEmpleadoAux, estadoActivoAux,
                createButtonsLayout());
    }

    private void configureFields() {
        codPersona.setId("codPersona");
        nombre.setId("nombre");
        ruc.setId("ruc");
        direccion.setId("direccion");
        telefono.setId("telefono");
        fecNacimiento.setId("fecNacimiento");
        esFisicaAux.setId("esFisicaAux");
        esClienteAux.setId("esClienteAux");
        esProveedorAux.setId("esProveedorAux");
        esEmpleadoAux.setId("esEmpleadoAux");
        estadoActivoAux.setId("estadoActivoAux");

        codPersona.setRequiredIndicatorVisible(true);
        nombre.setRequiredIndicatorVisible(true);
        fecNacimiento.setLocale(new Locale("es", "PY"));
        telefono.setValueChangeMode(ValueChangeMode.EAGER);
    }

    private void configureBinder() {
        binder.forField(codPersona)
                .asRequired("El código es obligatorio")
                .withValidator(value -> value != null && value.length() <= 30,
                        "Máximo 30 caracteres")
                .bind(BswPersonas::getCodPersona, BswPersonas::setCodPersona);

        binder.forField(nombre)
                .asRequired("El nombre es obligatorio")
                .withValidator(value -> value != null && value.length() <= 150,
                        "Máximo 150 caracteres")
                .bind(BswPersonas::getNombre, BswPersonas::setNombre);

        binder.forField(ruc)
                .withValidator(value -> value == null || value.length() <= 20,
                        "Máximo 20 caracteres")
                .bind(BswPersonas::getRuc, BswPersonas::setRuc);

        binder.forField(direccion)
                .withValidator(value -> value == null || value.length() <= 250,
                        "Máximo 250 caracteres")
                .bind(BswPersonas::getDireccion, BswPersonas::setDireccion);

        binder.forField(telefono)
                .withValidator(value -> value == null || value.length() <= 50,
                        "Máximo 50 caracteres")
                .bind(BswPersonas::getTelefono, BswPersonas::setTelefono);

        binder.bind(fecNacimiento, BswPersonas::getFecNacimiento, BswPersonas::setFecNacimiento);
        binder.bind(esFisicaAux, BswPersonas::isEsFisicaAux, BswPersonas::setEsFisicaAux);        
    }

    private HorizontalLayout createButtonsLayout() {
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        delete.addThemeVariants(ButtonVariant.LUMO_ERROR);
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);

        save.addClickListener(event -> validateAndSave());
        delete.addClickListener(event -> fireEvent(new DeleteEvent(this, persona)));
        cancel.addClickListener(event -> fireEvent(new CloseEvent(this)));

        return new HorizontalLayout(save, delete, cancel);
    }

    public void setPersona(BswPersonas persona) {
        this.persona = persona;
        if (persona != null) {
            binder.readBean(persona);
        } else {
            binder.readBean(new BswPersonas());
        }
        delete.setEnabled(persona != null && persona.getId() != null);
    }

    private void validateAndSave() {
        if (persona == null) {
            persona = new BswPersonas();
        }
        try {
            binder.writeBean(persona);
            fireEvent(new SaveEvent(this, persona));
        } catch (ValidationException validationException) {
            BinderValidationStatus<BswPersonas> status = binder.validate();
            status.getValidationErrors().stream()
                    .map(ValidationResult::getErrorMessage)
                    .filter(Objects::nonNull)
                    .findFirst()
                    .ifPresent(message -> {
                        // Notification handled by parent view
                    });
        }
    }

    public Button getSave() {
        return save;
    }

    public Button getDelete() {
        return delete;
    }

    public Button getCancel() {
        return cancel;
    }

    public TextField getCodPersonaField() {
        return codPersona;
    }

    public TextField getNombreField() {
        return nombre;
    }

    public DatePicker getFecNacimientoField() {
        return fecNacimiento;
    }

    public Checkbox getEstadoActivoCheckbox() {
        return estadoActivoAux;
    }

    public void submit() {
        validateAndSave();
    }

    // Events
    public abstract static class PersonaFormEvent extends ComponentEvent<FormBswPersonas> {
        private final BswPersonas persona;

        protected PersonaFormEvent(FormBswPersonas source, BswPersonas persona) {
            super(source, false);
            this.persona = persona;
        }

        public BswPersonas getPersona() {
            return persona;
        }
    }

    public static class SaveEvent extends PersonaFormEvent {
        SaveEvent(FormBswPersonas source, BswPersonas persona) {
            super(source, persona);
        }
    }

    public static class DeleteEvent extends PersonaFormEvent {
        DeleteEvent(FormBswPersonas source, BswPersonas persona) {
            super(source, persona);
        }
    }

    public static class CloseEvent extends PersonaFormEvent {
        CloseEvent(FormBswPersonas source) {
            super(source, null);
        }
    }

    public <T extends ComponentEvent<?>> Registration addListener(Class<T> eventType,
            ComponentEventListener<T> listener) {
        return getEventBus().addListener(eventType, listener);
    }
}
