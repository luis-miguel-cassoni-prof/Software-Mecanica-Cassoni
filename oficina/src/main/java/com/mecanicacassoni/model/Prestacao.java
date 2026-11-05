package com.mecanicacassoni.model;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.math.BigDecimal;

@Getter
@Setter
public class Prestacao {

    private Integer idPrestacao;
    private Integer idServico;
    private Integer idFuncionario;
    private LocalDate data;
    private LocalTime horaInicio;
    private LocalTime horaFim;
    private BigDecimal vlrMaterial;
    private BigDecimal valorHora;
}