package br.edu.ifpr.pokestrategy.model;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
public class PokemonEquipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer pokedexId;

    private String nome;

    @Setter
    private String apelido;

    private String tipoPrimario;

    private String tipoSecundario;

    private String spriteUrl;

    @Embedded
    private EstatisticasBase estatisticas;

    @ManyToOne
    @JoinColumn(name = "equipe_id")
    @Setter
    private Equipe equipe;

    public PokemonEquipe() {
    }

    public PokemonEquipe(Integer pokedexId, String nome, String tipoPrimario, String tipoSecundario,
            String spriteUrl, EstatisticasBase estatisticas) {
        this.pokedexId = pokedexId;
        this.nome = nome;
        this.tipoPrimario = tipoPrimario;
        this.tipoSecundario = tipoSecundario;
        this.spriteUrl = spriteUrl;
        this.estatisticas = estatisticas;
    }

    public String getNomeExibicao() {
        if (apelido != null && !apelido.isBlank()) {
            return apelido;
        }
        return capitalizar(nome);
    }

    private static String capitalizar(String texto) {
        if (texto == null || texto.isEmpty()) {
            return texto;
        }
        return Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
