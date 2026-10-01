package br.edu.ifpr.pokestrategy.model;

import jakarta.persistence.Embeddable;

@Embeddable
public record EstatisticasBase(
        int hp,
        int ataque,
        int defesa,
        int ataqueEspecial,
        int defesaEspecial,
        int velocidade) {
}
