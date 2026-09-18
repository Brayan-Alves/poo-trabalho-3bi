package br.edu.ifpr.pokestrategy.controller;

import br.edu.ifpr.pokestrategy.integracao.PokeApiClient;
import br.edu.ifpr.pokestrategy.model.Equipe;
import br.edu.ifpr.pokestrategy.repository.EquipeRepository;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/** CRUD de equipes (cadastrar, listar, editar, excluir) e a página de detalhe onde os Pokémon são adicionados. */
@Controller
@RequestMapping("/equipes")
public class EquipeController {

    private final EquipeRepository equipeRepository;
    private final PokeApiClient pokeApiClient;

    public EquipeController(EquipeRepository equipeRepository, PokeApiClient pokeApiClient) {
        this.equipeRepository = equipeRepository;
        this.pokeApiClient = pokeApiClient;
    }

    @GetMapping
    public String listar(Model model) {
        model.addAttribute("equipes", equipeRepository.findAll());
        return "equipes/lista";
    }

    @GetMapping("/novo")
    public String novoFormulario(Model model) {
        model.addAttribute("equipe", new Equipe());
        return "equipes/form";
    }

    @PostMapping
    public String salvar(@Valid @ModelAttribute("equipe") Equipe equipe, BindingResult resultado,
            RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return "equipes/form";
        }
        equipeRepository.save(equipe);
        redirect.addFlashAttribute("sucesso", "Equipe \"" + equipe.getNome() + "\" criada com sucesso.");
        return "redirect:/equipes";
    }

    @GetMapping("/{id}/editar")
    public String editarFormulario(@PathVariable Long id, Model model) {
        model.addAttribute("equipe", buscarOuFalhar(id));
        return "equipes/form";
    }

    @PostMapping("/{id}/editar")
    public String atualizar(@PathVariable Long id, @Valid @ModelAttribute("equipe") Equipe equipeDoFormulario,
            BindingResult resultado, RedirectAttributes redirect) {
        if (resultado.hasErrors()) {
            return "equipes/form";
        }
        Equipe equipe = buscarOuFalhar(id);
        equipe.setNome(equipeDoFormulario.getNome());
        equipeRepository.save(equipe);
        redirect.addFlashAttribute("sucesso", "Equipe atualizada com sucesso.");
        return "redirect:/equipes";
    }

    @PostMapping("/{id}/excluir")
    public String excluir(@PathVariable Long id, RedirectAttributes redirect) {
        equipeRepository.deleteById(id);
        redirect.addFlashAttribute("sucesso", "Equipe removida.");
        return "redirect:/equipes";
    }

    @GetMapping("/{id}")
    public String detalhe(@PathVariable Long id, @RequestParam(required = false) String busca, Model model) {
        Equipe equipe = buscarOuFalhar(id);
        model.addAttribute("equipe", equipe);

        if (!equipe.getPokemons().isEmpty()) {
            model.addAttribute("ameacas", pokeApiClient.calcularAmeacasEquipe(equipe.getPokemons()));
        }

        if (busca != null && !busca.isBlank()) {
            model.addAttribute("termoBusca", busca);
            pokeApiClient.buscarPokemon(busca).ifPresentOrElse(
                    resultado -> model.addAttribute("resultado", resultado),
                    () -> model.addAttribute("erroBusca", "Nenhum Pokémon encontrado para \"" + busca + "\"."));
        }
        return "equipes/detalhe";
    }

    private Equipe buscarOuFalhar(Long id) {
        return equipeRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Equipe não encontrada."));
    }
}
