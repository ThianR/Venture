package com.inventiva.venture.modules.bs.infrastructure.repository;

import com.inventiva.venture.modules.bs.domain.model.BswPersonas;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BswPersonasRepository extends JpaRepository<BswPersonas, Long> {

        Page<BswPersonas> findByCodPersonaContainingIgnoreCaseOrNombreContainingIgnoreCase(
                        String cod,
                        String nombre,
                        Pageable pageable);

        long countByCodPersonaContainingIgnoreCaseOrNombreContainingIgnoreCase(
                        String cod,
                        String nombre);
}
