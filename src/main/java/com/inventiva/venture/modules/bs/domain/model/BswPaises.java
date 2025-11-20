package com.inventiva.venture.modules.bs.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
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
@Table(name = "BSW_PAISES", schema = "INV")
public class BswPaises {
    @SequenceGenerator(name = "BSW_PAISES_SEQ", sequenceName = "BSW_PAISES_ID_SEQ", allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "BSW_PAISES_SEQ")
    @Id
    @Column(name = "ID")
    private Long id;
    @Column(name = "COD_PAIS", nullable = false)
    private String codPais;
    @Column(name = "DESCRIPCION", nullable = false)
    private String descripcion;
    @Column(name = "NACIONALIDAD")
    private String nacionalidad;
    @Column(name = "CODIGO_AREA")
    private String codigoArea;
    @Column(name = "ABREVIATURA")
    private String abreviatura;
    @Column(name = "SIGLAS")
    private String siglas;
    @Column(name = "COD_USUARIO_AUD")
    private String codUsuarioAud;
}
