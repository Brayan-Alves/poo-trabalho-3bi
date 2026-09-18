package br.edu.ifpr.pokestrategy.integracao;

/**
 * Quão ameaçador um tipo de ataque é para uma equipe inteira: quantos
 * membros ele atinge com dano ampliado e o multiplicador somado entre eles.
 */
public record AmeacaTipo(String tipo, int pokemonAfetados, double multiplicadorTotal) {
}
