package br.edu.ifpr.pokestrategy.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.edu.ifpr.pokestrategy.model.Equipe;

public interface EquipeRepository extends JpaRepository<Equipe, Long> {
}
