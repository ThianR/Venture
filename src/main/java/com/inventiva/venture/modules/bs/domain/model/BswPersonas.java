package com.inventiva.venture.modules.bs.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.time.LocalDate;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "BSW_PERSONAS", schema = "INV")
public class BswPersonas {

    private static final Set<String> TRUE_VALUES = Set.of("S", "A");

    @Id
    @SequenceGenerator(
            name = "BSW_PERSONAS_SEQ",
            sequenceName = "bsw_personas_id_seq",
            allocationSize = 1
    )
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BSW_PERSONAS_SEQ")
    @Column(name = "ID_PERSONA")
    private Long id;

    @Column(name = "COD_PERSONA", nullable = false, length = 30)
    private String codPersona;

    @Column(name = "NOMBRE", nullable = false, length = 150)
    private String nombre;

    @Column(name = "RUC", length = 20)
    private String ruc;

    @Column(name = "DIRECCION", length = 250)
    private String direccion;

    @Column(name = "TELEFONO", length = 50)
    private String telefono;

    @Column(name = "FEC_NACIMIENTO")
    private LocalDate fecNacimiento;

    @Column(name = "ES_FISICA", length = 1)
    private String esFisica;

    @Transient
    private boolean esFisicaAux;

    @Column(name = "ES_CLIENTE", length = 1)
    private String esCliente;

    @Transient
    private boolean esClienteAux;

    @Column(name = "ES_PROVEEDOR", length = 1)
    private String esProveedor;

    @Transient
    private boolean esProveedorAux;

    @Column(name = "ES_EMPLEADO", length = 1)
    private String esEmpleado;

    @Transient
    private boolean esEmpleadoAux;

    @Column(name = "ESTADO", length = 1)
    private String estado;

    @Transient
    private boolean estadoActivoAux;

    @PostLoad
    public void onPostLoad() {
        esFisicaAux = toBoolean(esFisica);
        esClienteAux = toBoolean(esCliente);
        esProveedorAux = toBoolean(esProveedor);
        esEmpleadoAux = toBoolean(esEmpleado);
        estadoActivoAux = toBoolean(estado);
    }

    @PrePersist
    @PreUpdate
    public void beforeSave() {
        esFisica = fromBoolean(esFisicaAux, esFisica);
        esCliente = fromBoolean(esClienteAux, esCliente);
        esProveedor = fromBoolean(esProveedorAux, esProveedor);
        esEmpleado = fromBoolean(esEmpleadoAux, esEmpleado);
        estado = fromBoolean(estadoActivoAux, estado);
    }

    private boolean toBoolean(String value) {
        if (value == null) {
            return false;
        }
        return TRUE_VALUES.contains(value.toUpperCase());
    }

    private String fromBoolean(boolean flag, String currentValue) {
        if (currentValue != null && (currentValue.equalsIgnoreCase("A") || currentValue.equalsIgnoreCase("I"))) {
            return flag ? "A" : "I";
        }
        return flag ? "S" : "N";
    }
}
