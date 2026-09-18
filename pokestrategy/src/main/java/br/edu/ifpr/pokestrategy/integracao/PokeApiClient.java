package br.edu.ifpr.pokestrategy.integracao;

import br.edu.ifpr.pokestrategy.integracao.pokeapi.PokemonResposta;
import br.edu.ifpr.pokestrategy.integracao.pokeapi.TipoResposta;
import br.edu.ifpr.pokestrategy.model.PokemonEquipe;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Consome a PokeAPI (https://pokeapi.co/) para buscar um Pokémon e transformar
 * os tipos dele em uma tabela de efetividade (fraquezas, resistências e
 * imunidades), usando o campo "damage_relations" de cada tipo.
 */
@Component
public class PokeApiClient {

    private static final List<String> TODOS_OS_TIPOS = List.of(
            "normal", "fire", "water", "electric", "grass", "ice", "fighting", "poison", "ground",
            "flying", "psychic", "bug", "rock", "ghost", "dragon", "dark", "steel", "fairy");

    private final RestClient restClient;
    private final Map<String, TipoResposta> cacheDeTipos = new ConcurrentHashMap<>();

    public PokeApiClient() {
        this.restClient = RestClient.builder().baseUrl("https://pokeapi.co/api/v2").build();
    }

    /**
     * Busca um Pokémon por nome ou número da Pokédex e devolve seus dados já
     * combinados com o cálculo de fraquezas/resistências/imunidades.
     * Retorna vazio se o Pokémon não existir na PokeAPI.
     */
    public Optional<PokemonPesquisado> buscarPokemon(String nomeOuId) {
        String termo = nomeOuId.trim().toLowerCase();
        if (termo.isEmpty()) {
            return Optional.empty();
        }

        PokemonResposta resposta = buscarPokemonNaApi(termo);
        if (resposta == null) {
            return Optional.empty();
        }

        List<String> tipos = resposta.types().stream()
                .sorted(Comparator.comparingInt(PokemonResposta.TipoSlot::slot))
                .map(slot -> slot.type().name())
                .toList();

        Map<String, Double> multiplicadores = calcularMultiplicadores(tipos);

        List<TipoMultiplicador> fraquezas = multiplicadores.entrySet().stream()
                .filter(entrada -> entrada.getValue() > 1.0)
                .map(entrada -> new TipoMultiplicador(entrada.getKey(), entrada.getValue()))
                .sorted(Comparator.comparingDouble(TipoMultiplicador::multiplicador).reversed())
                .toList();

        List<TipoMultiplicador> resistencias = multiplicadores.entrySet().stream()
                .filter(entrada -> entrada.getValue() > 0.0 && entrada.getValue() < 1.0)
                .map(entrada -> new TipoMultiplicador(entrada.getKey(), entrada.getValue()))
                .sorted(Comparator.comparingDouble(TipoMultiplicador::multiplicador))
                .toList();

        List<String> imunidades = multiplicadores.entrySet().stream()
                .filter(entrada -> entrada.getValue() == 0.0)
                .map(Map.Entry::getKey)
                .toList();

        String sprite = resposta.sprites() != null ? resposta.sprites().frontDefault() : null;

        return Optional.of(new PokemonPesquisado(
                resposta.id(), resposta.name(), sprite, tipos, fraquezas, resistencias, imunidades));
    }

    /**
     * Analisa a equipe inteira e aponta quais tipos de ataque são mais
     * ameaçadores para ela — ou seja, os tipos que um time adversário usaria
     * para "se dar bem" contra essa equipe, por atingirem vários membros ao
     * mesmo tempo com dano ampliado.
     */
    public List<AmeacaTipo> calcularAmeacasEquipe(List<PokemonEquipe> pokemons) {
        Map<String, Double> multiplicadorTotalPorTipo = new LinkedHashMap<>();
        Map<String, Integer> afetadosPorTipo = new LinkedHashMap<>();
        TODOS_OS_TIPOS.forEach(tipo -> {
            multiplicadorTotalPorTipo.put(tipo, 0.0);
            afetadosPorTipo.put(tipo, 0);
        });

        for (PokemonEquipe pokemon : pokemons) {
            List<String> tipos = pokemon.getTipoSecundario() != null
                    ? List.of(pokemon.getTipoPrimario(), pokemon.getTipoSecundario())
                    : List.of(pokemon.getTipoPrimario());

            calcularMultiplicadores(tipos).forEach((tipo, multiplicador) -> {
                multiplicadorTotalPorTipo.merge(tipo, multiplicador, Double::sum);
                if (multiplicador > 1.0) {
                    afetadosPorTipo.merge(tipo, 1, Integer::sum);
                }
            });
        }

        return TODOS_OS_TIPOS.stream()
                .map(tipo -> new AmeacaTipo(tipo, afetadosPorTipo.get(tipo), multiplicadorTotalPorTipo.get(tipo)))
                .filter(ameaca -> ameaca.pokemonAfetados() > 0)
                .sorted(Comparator.comparingInt(AmeacaTipo::pokemonAfetados).reversed()
                        .thenComparing(Comparator.comparingDouble(AmeacaTipo::multiplicadorTotal).reversed()))
                .limit(6)
                .toList();
    }

    private PokemonResposta buscarPokemonNaApi(String termo) {
        try {
            return restClient.get()
                    .uri("/pokemon/{termo}", termo)
                    .retrieve()
                    .body(PokemonResposta.class);
        } catch (RestClientResponseException erro) {
            return null;
        }
    }

    /**
     * Combina o damage_relations de cada tipo do Pokémon (que pode ter 1 ou 2
     * tipos) num único mapa tipo-de-ataque -> multiplicador de dano recebido,
     * multiplicando os efeitos quando os dois tipos concordam.
     */
    private Map<String, Double> calcularMultiplicadores(List<String> tiposDoPokemon) {
        Map<String, Double> multiplicadores = new LinkedHashMap<>();
        TODOS_OS_TIPOS.forEach(tipo -> multiplicadores.put(tipo, 1.0));

        for (String tipoDefensor : tiposDoPokemon) {
            TipoResposta info = buscarInfoDoTipo(tipoDefensor);
            if (info == null || info.damageRelations() == null) {
                continue;
            }
            var relacoes = info.damageRelations();
            relacoes.doubleDamageFrom().forEach(t -> multiplicadores.merge(t.name(), 2.0, (a, b) -> a * b));
            relacoes.halfDamageFrom().forEach(t -> multiplicadores.merge(t.name(), 0.5, (a, b) -> a * b));
            relacoes.noDamageFrom().forEach(t -> multiplicadores.merge(t.name(), 0.0, (a, b) -> a * b));
        }
        return multiplicadores;
    }

    private TipoResposta buscarInfoDoTipo(String tipo) {
        TipoResposta emCache = cacheDeTipos.get(tipo);
        if (emCache != null) {
            return emCache;
        }
        try {
            TipoResposta resposta = restClient.get()
                    .uri("/type/{tipo}", tipo)
                    .retrieve()
                    .body(TipoResposta.class);
            if (resposta != null) {
                cacheDeTipos.put(tipo, resposta);
            }
            return resposta;
        } catch (RestClientResponseException erro) {
            return null;
        }
    }
}
