package br.edu.ifpr.pokestrategy.integracao.pokeapi;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Mapeia o JSON retornado por GET https://pokeapi.co/api/v2/pokemon/{nome}.
 * Contém apenas os campos usados pela aplicação.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record PokemonResposta(
        int id,
        String name,
        List<TipoSlot> types,
        List<StatSlot> stats,
        Sprites sprites) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TipoSlot(int slot, TipoInfo type) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record TipoInfo(String name, String url) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatSlot(@JsonProperty("base_stat") int baseStat, StatInfo stat) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record StatInfo(String name) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Sprites(@JsonProperty("front_default") String frontDefault) {
    }
}
