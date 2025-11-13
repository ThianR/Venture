package com.inventiva.venture.modules.bs.application.service;

import com.inventiva.venture.modules.bs.domain.model.BswPersonas;
import com.inventiva.venture.modules.bs.infrastructure.repository.BswPersonasRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BswPersonasService {

    private final BswPersonasRepository repository;

    public Page<BswPersonas> listar(String filtro, Pageable pageable) {
        String criterio = filtro == null ? "" : filtro.trim();
        return repository.findByCodPersonaContainingIgnoreCaseOrNombreContainingIgnoreCase(
                criterio,
                criterio,
                pageable
        );
    }

    @Transactional
    public BswPersonas guardar(BswPersonas persona) {
        return repository.save(persona);
    }

    @Transactional
    public void eliminar(Long id) {
        repository.deleteById(id);
    }
}
