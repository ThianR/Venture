package com.inventiva.venture.modules.bs.presentation.components;

import static org.assertj.core.api.Assertions.assertThat;

import com.inventiva.venture.modules.bs.domain.model.BswPersonas;
import com.vaadin.flow.component.textfield.TextField;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class FormBswPersonasTest {

    private FormBswPersonas form;

    @BeforeEach
    void setUp() {
        form = new FormBswPersonas();
    }

    @Test
    void noDebeEmitirEventoCuandoCamposObligatoriosEstanVacios() {
        AtomicReference<BswPersonas> guardada = new AtomicReference<>();
        form.addListener(FormBswPersonas.SaveEvent.class, event -> guardada.set(event.getPersona()));
        form.setPersona(new BswPersonas());

        form.submit();

        assertThat(guardada.get()).isNull();
    }

    @Test
    void debeSincronizarCamposBooleanosConEntidad() {
        AtomicReference<BswPersonas> guardada = new AtomicReference<>();
        form.addListener(FormBswPersonas.SaveEvent.class, event -> guardada.set(event.getPersona()));
        form.setPersona(new BswPersonas());

        TextField codField = form.getCodPersonaField();
        TextField nombreField = form.getNombreField();

        codField.setValue("001");
        nombreField.setValue("Juan Perez");

        form.submit();

        assertThat(guardada.get()).isNotNull();        
        assertThat(guardada.get().getCodPersona()).isEqualTo("001");
    }
}
