package br.edu.ifpr.pokestrategy.integracao;

import java.util.List;

/**
 * Resultado já processado de uma pesquisa na PokeAPI: dados do Pokémon mais
 * o resumo de fraquezas, resistências e imunidades de tipo, pronto para ser
 * exibido na tela.
 */
public record PokemonPesquisado(
        int pokedexId,
        String nome,
        String spriteUrl,
        List<String> tipos,
        List<TipoMultiplicador> fraquezas,
        List<TipoMultiplicador> resistencias,
        List<String> imunidades) {
}
