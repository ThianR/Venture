package com.inventiva.venture.modules.bs.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.inventiva.venture.modules.bs.domain.model.BswPersonas;
import com.inventiva.venture.modules.bs.infrastructure.repository.BswPersonasRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class BswPersonasServiceTest {

    @Mock
    private BswPersonasRepository repository;

    @InjectMocks
    private BswPersonasService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void listarDebeDelegarEnRepositorio() {
        BswPersonas persona = BswPersonas.builder().id(1L).codPersona("001").nombre("Juan Perez").build();
        Page<BswPersonas> page = new PageImpl<>(List.of(persona));
        when(repository.findByCodPersonaContainingIgnoreCaseOrNombreContainingIgnoreCase(eq("Juan"), eq("Juan"), any()))
                .thenReturn(page);

        Page<BswPersonas> result = service.listar("Juan", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(1);
        verify(repository).findByCodPersonaContainingIgnoreCaseOrNombreContainingIgnoreCase(eq("Juan"), eq("Juan"), any());
    }

    @Test
    void guardarDebePersistirEntidad() {
        BswPersonas persona = BswPersonas.builder().codPersona("001").nombre("Juan Perez").build();
        when(repository.save(persona)).thenReturn(persona);

        BswPersonas resultado = service.guardar(persona);

        assertThat(resultado).isEqualTo(persona);
        verify(repository).save(persona);
    }

    @Test
    void eliminarDebeDelegarEnRepositorio() {
        service.eliminar(1L);

        verify(repository).deleteById(1L);
    }
}
