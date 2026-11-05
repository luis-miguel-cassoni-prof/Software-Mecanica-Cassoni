
package com.mecanicacassoni.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.util.List;

import com.mecanicacassoni.model.Prestacao;

public class CalculadoraValorServico {

    public BigDecimal calcularMaoDeObra(Prestacao prestacao) {
        long minutos = Duration.between(
                prestacao.getHoraInicio(),
                prestacao.getHoraFim()
        ).toMinutes();

        if (minutos <= 0) {
            throw new IllegalArgumentException(
                    "O horário final deve ser posterior ao inicial."
            );
        }

        if (prestacao.getValorHora() == null
                || prestacao.getValorHora().signum() < 0) {
            throw new IllegalArgumentException(
                    "O valor-hora deve ser válido."
            );
        }

        return prestacao.getValorHora()
                .multiply(BigDecimal.valueOf(minutos))
                .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP);
    }

    public BigDecimal calcularTotalPrestacao(Prestacao prestacao) {
        BigDecimal material = prestacao.getVlrMaterial();

        if (material == null || material.signum() < 0) {
            throw new IllegalArgumentException(
                    "O valor dos materiais deve ser válido."
            );
        }

        return calcularMaoDeObra(prestacao).add(material);
    }

    public BigDecimal calcularTotalServico(List<Prestacao> prestacoes) {
        BigDecimal total = BigDecimal.ZERO;

        for (Prestacao prestacao : prestacoes) {
            total = total.add(calcularTotalPrestacao(prestacao));
        }

        return total.setScale(2, RoundingMode.HALF_UP);
    }
}
