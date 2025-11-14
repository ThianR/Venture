package com.inventiva.venture.modules.bs.presentation.views;

import com.inventiva.venture.modules.bs.application.service.BswPersonasService;
import com.inventiva.venture.modules.bs.domain.model.BswPersonas;
import com.inventiva.venture.modules.bs.presentation.components.FormBswPersonas;
import com.inventiva.venture.modules.bs.presentation.components.FormBswPersonas.CloseEvent;
import com.inventiva.venture.modules.bs.presentation.components.FormBswPersonas.DeleteEvent;
import com.inventiva.venture.modules.bs.presentation.components.FormBswPersonas.SaveEvent;
import com.inventiva.venture.ui.MainLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.orderedlayout.FlexComponent.Alignment;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@PageTitle("Personas")
@Route(value = "BswPersonas", layout = MainLayout.class)
public class BswPersonasView extends VerticalLayout {

    private final BswPersonasService service;
    private final Grid<BswPersonas> grid = new Grid<>(BswPersonas.class, false);
    private final TextField filtro = new TextField("Filtrar por código o nombre");
    private final FormBswPersonas form = new FormBswPersonas();

    private String currentFilter = "";

    public BswPersonasView(BswPersonasService service) {
        this.service = service;
        setSizeFull();
        setSpacing(true);
        setPadding(true);

        configureGrid();
        configureForm();
        add(createToolbar(), createContent());
    }

    private HorizontalLayout createToolbar() {
        filtro.setPlaceholder("Buscar...");
        filtro.setClearButtonVisible(true);
        filtro.setValueChangeMode(ValueChangeMode.LAZY);
        filtro.addValueChangeListener(event -> {
            currentFilter = event.getValue();
            refreshGrid();
        });

        Button newPersona = new Button("Nueva persona");
        newPersona.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        newPersona.addClickListener(event -> newPersona());

        HorizontalLayout toolbar = new HorizontalLayout(filtro, newPersona);
        toolbar.setWidthFull();
        toolbar.setAlignItems(Alignment.END);
        return toolbar;
    }

    private FlexLayout createContent() {
        FlexLayout content = new FlexLayout(grid, form);
        content.setFlexGrow(2, grid);
        content.setFlexGrow(1, form);
        content.setSizeFull();
        content.addClassName("content");
        form.setVisible(false);
        return content;
    }

    private void configureGrid() {
        grid.addColumn(BswPersonas::getCodPersona).setHeader("Código").setAutoWidth(true);
        grid.addColumn(BswPersonas::getNombre).setHeader("Nombre").setAutoWidth(true);
        grid.addColumn(BswPersonas::getRuc).setHeader("RUC").setAutoWidth(true);
        grid.addColumn(BswPersonas::getDireccion).setHeader("Dirección").setAutoWidth(true);
        grid.addColumn(BswPersonas::getTelefono).setHeader("Teléfono").setAutoWidth(true);
        grid.addColumn(persona -> persona.getFecNacimiento() != null ? persona.getFecNacimiento().toString() : "")
                .setHeader("Fecha de nacimiento");       
        grid.addThemeVariants(GridVariant.LUMO_ROW_STRIPES);
        grid.setSizeFull();

        CallbackDataProvider<BswPersonas, Void> dataProvider = new CallbackDataProvider<>(
                query -> fetch(query, currentFilter),
                query -> count(currentFilter));
        grid.setDataProvider(dataProvider);

        grid.asSingleSelect().addValueChangeListener(event -> editPersona(event.getValue()));
    }

    private Stream<BswPersonas> fetch(Query<BswPersonas, Void> query, String filter) {
        String criterio = filter == null ? "" : filter.trim();
        Pageable pageable = createPageable(query);
        Page<BswPersonas> page = service.listar(criterio, pageable);
        return page.stream();
    }

    private int count(String filter) {
        String criterio = filter == null ? "" : filter.trim();
        return (int) service.contar(criterio);
    }

    private Pageable createPageable(Query<BswPersonas, Void> query) {
        int page = query.getOffset() / query.getLimit();

        List<Sort.Order> orders = query.getSortOrders().stream()
                .map(order -> new Sort.Order(
                        order.getDirection() == SortDirection.ASCENDING ? Sort.Direction.ASC : Sort.Direction.DESC,
                        order.getSorted()))
                .toList();

        // Si el usuario no ordenó desde la UI, usamos id DESC por defecto
        Sort sort = orders.isEmpty()
                ? Sort.by(Sort.Direction.DESC, "id") // nombre del campo en la entidad (tienes getId()n)
                : Sort.by(orders);

        return PageRequest.of(page, query.getLimit(), sort);
    }

    private void configureForm() {
        form.setWidth("25em");
        form.addListener(SaveEvent.class, this::savePersona);
        form.addListener(DeleteEvent.class, this::deletePersona);
        form.addListener(CloseEvent.class, event -> closeEditor());
    }

    private void savePersona(SaveEvent event) {
        BswPersonas persona = event.getPersona();
        if (persona == null) {
            Notification.show("Datos incompletos", 3000, Notification.Position.MIDDLE);
            return;
        }
        service.guardar(persona);
        Notification.show("Persona guardada", 3000, Notification.Position.BOTTOM_START);
        refreshGrid();
        closeEditor();
    }

    private void deletePersona(DeleteEvent event) {
        BswPersonas persona = event.getPersona();
        if (persona != null && persona.getId() != null) {
            service.eliminar(persona.getId());
            Notification.show("Persona eliminada", 3000, Notification.Position.BOTTOM_START);
            refreshGrid();
            closeEditor();
        }
    }

    private void editPersona(BswPersonas persona) {
        if (persona == null) {
            closeEditor();
        } else {
            form.setPersona(persona);
            form.setVisible(true);
            addClassName("editing");
        }
    }

    private void newPersona() {
        grid.asSingleSelect().clear();
        form.setPersona(new BswPersonas());
        form.setVisible(true);
        addClassName("editing");
    }

    private void refreshGrid() {
        grid.getDataProvider().refreshAll();
    }

    private void closeEditor() {
        form.setPersona(null);
        form.setVisible(false);
        removeClassName("editing");
    }
}
