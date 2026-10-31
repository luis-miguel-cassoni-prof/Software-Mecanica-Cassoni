package com.mecanicacassoni.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Servico {

    private Integer idServico;
    private String nome;
    private Integer idCliente;
}