package br.edu.ifpr.pokestrategy.integracao.pokeapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Mapeia o JSON retornado por GET https://pokeapi.co/api/v2/type/{nome}.
 * O campo damage_relations é o que permite calcular fraquezas/resistências.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TipoResposta(@JsonProperty("damage_relations") RelacoesDeDano damageRelations) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record RelacoesDeDano(
            @JsonProperty("double_damage_from") List<PokemonResposta.TipoInfo> doubleDamageFrom,
            @JsonProperty("half_damage_from") List<PokemonResposta.TipoInfo> halfDamageFrom,
            @JsonProperty("no_damage_from") List<PokemonResposta.TipoInfo> noDamageFrom) {
    }
}
