package br.edu.ifpr.pokestrategy.controller;

import br.edu.ifpr.pokestrategy.integracao.PokeApiClient;
import br.edu.ifpr.pokestrategy.integracao.PokemonPesquisado;
import br.edu.ifpr.pokestrategy.model.Equipe;
import br.edu.ifpr.pokestrategy.model.PokemonEquipe;
import br.edu.ifpr.pokestrategy.repository.EquipeRepository;
import br.edu.ifpr.pokestrategy.repository.PokemonEquipeRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

/**
 * CRUD dos Pokémon dentro de uma equipe: adicionar (via PokeAPI), editar
 * apelido, remover. O ID da equipe chega como parâmetro para que o mesmo
 * formulário de "adicionar" sirva tanto na página da equipe quanto no
 * verificador de fraquezas avulso.
 */
@Controller
@RequestMapping("/pokemons")
public class PokemonController {

    private final EquipeRepository equipeRepository;
    private final PokemonEquipeRepository pokemonEquipeRepository;
    private final PokeApiClient pokeApiClient;

    public PokemonController(EquipeRepository equipeRepository, PokemonEquipeRepository pokemonEquipeRepository,
            PokeApiClient pokeApiClient) {
        this.equipeRepository = equipeRepository;
        this.pokemonEquipeRepository = pokemonEquipeRepository;
        this.pokeApiClient = pokeApiClient;
    }

    @PostMapping
    public String adicionar(@RequestParam Long equipeId, @RequestParam String pokedexId,
            RedirectAttributes redirect) {
        Equipe equipe = equipeRepository.findById(equipeId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipe não encontrada."));

        if (equipe.isCompleta()) {
            redirect.addFlashAttribute("erroBusca",
                    "A equipe \"" + equipe.getNome() + "\" já está completa (máx. 6 Pokémon).");
            return "redirect:/equipes/" + equipeId;
        }

        Optional<PokemonPesquisado> resultado = pokeApiClient.buscarPokemon(pokedexId);
        if (resultado.isEmpty()) {
            redirect.addFlashAttribute("erroBusca", "Não foi possível adicionar: Pokémon não encontrado.");
            return "redirect:/equipes/" + equipeId;
        }

        PokemonPesquisado dados = resultado.get();
        PokemonEquipe pokemon = new PokemonEquipe(
                dados.pokedexId(),
                dados.nome(),
                dados.tipos().get(0),
                dados.tipos().size() > 1 ? dados.tipos().get(1) : null,
                dados.spriteUrl(),
                dados.estatisticas());

        equipe.adicionarPokemon(pokemon);
        pokemonEquipeRepository.save(pokemon);
        redirect.addFlashAttribute("sucesso", pokemon.getNomeExibicao() + " foi adicionado à equipe!");
        return "redirect:/equipes/" + equipeId;
    }

    @GetMapping("/{id}/editar")
    public String editarFormulario(@PathVariable Long id, Model model) {
        model.addAttribute("pokemon", buscarOuFalhar(id));
        return "pokemons/form";
    }

    @PostMapping("/{id}/editar")
    public String atualizarApelido(@PathVariable Long id, @RequestParam(required = false) String apelido,
            RedirectAttributes redirect) {
        PokemonEquipe pokemon = buscarOuFalhar(id);
        pokemon.setApelido(apelido != null && !apelido.isBlank() ? apelido.trim() : null);
        pokemonEquipeRepository.save(pokemon);
        redirect.addFlashAttribute("sucesso", "Apelido atualizado.");
        return "redirect:/equipes/" + pokemon.getEquipe().getId();
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        PokemonEquipe pokemon = buscarOuFalhar(id);
        Long equipeId = pokemon.getEquipe().getId();
        pokemonEquipeRepository.deleteById(id);
        redirect.addFlashAttribute("sucesso", "Pokémon removido da equipe.");
        return "redirect:/equipes/" + equipeId;
    }

    private PokemonEquipe buscarOuFalhar(Long id) {
        return pokemonEquipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pokémon não encontrado."));
    }
}
