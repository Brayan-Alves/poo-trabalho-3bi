package br.edu.ifpr.pokestrategy.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
public class Equipe {

    public static final int MAXIMO_DE_MEMBROS = 6;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "O nome da equipe é obrigatório.")
    @Size(max = 60, message = "O nome da equipe deve ter no máximo 60 caracteres.")
    @Setter
    private String nome;

    @OneToMany(mappedBy = "equipe", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<PokemonEquipe> pokemons = new ArrayList<>();

    public Equipe() {
    }

    public Equipe(String nome) {
        this.nome = nome;
    }

    public void adicionarPokemon(PokemonEquipe pokemon) {
        if (isCompleta()) {
            throw new IllegalStateException(
                    "A equipe já possui o máximo de " + MAXIMO_DE_MEMBROS + " Pokémon.");
        }
        pokemon.setEquipe(this);
        pokemons.add(pokemon);
    }

    public void removerPokemon(PokemonEquipe pokemon) {
        pokemons.remove(pokemon);
        pokemon.setEquipe(null);
    }

    public boolean isCompleta() {
        return pokemons.size() >= MAXIMO_DE_MEMBROS;
    }
}
