package com.inventiva.venture.modules.bs.infrastructure.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.inventiva.venture.modules.bs.domain.model.BswPersonas;
import java.time.LocalDate;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.OracleContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@Testcontainers
@ExtendWith(SpringExtension.class)
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class BswPersonasRepositoryIT {

    @Container
    private static final OracleContainer ORACLE = new OracleContainer("gvenzl/oracle-xe:21-slim")
            .withDatabaseName("INV")
            .withUsername("INV")
            .withPassword("INV");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", ORACLE::getJdbcUrl);
        registry.add("spring.datasource.username", ORACLE::getUsername);
        registry.add("spring.datasource.password", ORACLE::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "oracle.jdbc.OracleDriver");
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.flyway.locations", () -> "classpath:db/migration");
    }

    @Autowired
    private BswPersonasRepository repository;

    @Autowired
    private Flyway flyway;

    @BeforeEach
    void setUp() {
        flyway.clean();
        flyway.migrate();
    }

    @Test
    void debeGuardarYRecuperarPersonas() {
        BswPersonas persona = BswPersonas.builder()
                .codPersona("001")
                .nombre("Ana Gomez")
                .ruc("1234567-8")
                .direccion("Asunción")
                .telefono("021123456")
                .fecNacimiento(LocalDate.of(1990, 5, 10))
                .esFisicaAux(true)
                .esClienteAux(true)
                .esProveedorAux(false)
                .esEmpleadoAux(false)
                .estadoActivoAux(true)
                .build();

        repository.save(persona);

        Page<BswPersonas> result = repository
                .findByCodPersonaContainingIgnoreCaseOrNombreContainingIgnoreCase("001", "ana", PageRequest.of(0, 10));

        assertThat(result.getTotalElements()).isEqualTo(1);
        BswPersonas recuperada = result.getContent().get(0);
        assertThat(recuperada.isEsFisicaAux()).isTrue();
        assertThat(recuperada.isEsClienteAux()).isTrue();
        assertThat(recuperada.getCodPersona()).isEqualTo("001");
    }
}
