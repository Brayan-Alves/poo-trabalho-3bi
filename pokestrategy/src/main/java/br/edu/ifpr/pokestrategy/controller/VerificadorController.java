package br.edu.ifpr.pokestrategy.controller;

import br.edu.ifpr.pokestrategy.integracao.PokeApiClient;
import br.edu.ifpr.pokestrategy.repository.EquipeRepository;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/** Verificador de fraquezas avulso: pesquisa qualquer Pokémon sem precisar abrir uma equipe primeiro. */
@Controller
public class VerificadorController {

    private final PokeApiClient pokeApiClient;
    private final EquipeRepository equipeRepository;

    public VerificadorController(PokeApiClient pokeApiClient, EquipeRepository equipeRepository) {
        this.pokeApiClient = pokeApiClient;
        this.equipeRepository = equipeRepository;
    }

    @GetMapping("/verificador")
    public String verificar(@RequestParam(required = false) String busca, Model model) {
        model.addAttribute("equipes", equipeRepository.findAll());

        if (busca != null && !busca.isBlank()) {
            model.addAttribute("termoBusca", busca);
            pokeApiClient.buscarPokemon(busca).ifPresentOrElse(
                    resultado -> model.addAttribute("resultado", resultado),
                    () -> model.addAttribute("erroBusca", "Nenhum Pokémon encontrado para \"" + busca + "\"."));
        }
        return "verificador";
    }
}
