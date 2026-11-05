package com.mecanicacassoni.model;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Servico {

    private Integer idServico;
    private String nome;
    private Integer idCliente;
    private BigDecimal valorFinal;
}