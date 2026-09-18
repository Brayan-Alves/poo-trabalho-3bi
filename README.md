# PokéStrategy

Trabalho do 3º Bimestre — Programação Orientada a Objetos (IFPR Campus Cascavel, 3º Informática).

## Integrantes

- Brayan Alves

## Tema

**Formador de equipes Pokémon e verificador de fraquezas de tipos.**

O usuário cria equipes (até 6 Pokémon cada), pesquisa Pokémon reais pelo nome ou número da Pokédex, visualiza automaticamente contra quais tipos aquele Pokémon é **fraco**, **resistente** ou **imune**, e pode adicioná-lo a uma de suas equipes. Também é possível editar apelidos, remover Pokémon e gerenciar (criar/editar/excluir) as equipes.

## Descrição do sistema

A aplicação é um Spring Boot MVC com as seguintes camadas:

- **Model/Entity**: `Equipe` (nome, lista de Pokémon, regra de no máximo 6 membros) e `PokemonEquipe` (dados do Pokémon: número da Pokédex, nome, apelido, tipos e sprite).
- **Repository**: `EquipeRepository` e `PokemonEquipeRepository`, ambos `JpaRepository`, persistindo em MySQL.
- **Controller**: `EquipeController` (CRUD de equipes), `PokemonController` (adicionar/editar apelido/remover Pokémon de uma equipe) e `VerificadorController` (busca avulsa de fraquezas).
- **Views**: Thymeleaf, com fragmentos reutilizáveis para a navegação e para o card de resultado de pesquisa.

## API externa utilizada

**[PokeAPI](https://pokeapi.co/)** — API pública e gratuita (sem chave de autenticação) com dados de todos os jogos Pokémon.

- Documentação: https://pokeapi.co/docs/v2
- Endpoints consumidos:
  - `GET /api/v2/pokemon/{nome-ou-numero}` — nome, número da Pokédex, tipos e sprite do Pokémon pesquisado.
  - `GET /api/v2/type/{tipo}` — `damage_relations` de cada tipo, usado para calcular o multiplicador de dano recebido.

### Como os dados são usados (funcionalidade implementada)

O JSON bruto da PokeAPI **não é exibido** ao usuário. A aplicação:

1. Recebe o termo de busca digitado pelo usuário (nome ou número).
2. Consulta `/pokemon/{termo}` na PokeAPI e extrai os tipos do Pokémon.
3. Para cada tipo do Pokémon, consulta `/type/{tipo}` e combina os `damage_relations` (dobra, reduz pela metade ou anula o dano de cada tipo de ataque), somando o efeito quando o Pokémon tem dois tipos — a mesma lógica de efetividade usada no jogo.
4. Apresenta o resultado processado: sprite, tipos, lista de **fraquezas** (×2 ou ×4), **resistências** (×0.5 ou ×0.25) e **imunidades** (×0), com a opção de adicionar aquele Pokémon a uma equipe salva no banco de dados.

## Como executar

Pré-requisitos: Java 17+, Maven (ou o `mvnw` incluso) e um servidor MySQL rodando em `localhost:3306`.

1. Crie o banco e um usuário de aplicação (rode uma vez, com um cliente MySQL conectado como root):

   ```sql
   CREATE DATABASE IF NOT EXISTS pokestrategy CHARACTER SET utf8mb4;
   CREATE USER IF NOT EXISTS 'pokestrategy'@'localhost' IDENTIFIED BY 'pokestrategy123';
   GRANT ALL PRIVILEGES ON pokestrategy.* TO 'pokestrategy'@'localhost';
   FLUSH PRIVILEGES;
   ```

2. Rode a aplicação:

   ```bash
   cd pokestrategy
   ./mvnw spring-boot:run
   ```

As tabelas são criadas automaticamente pelo Hibernate na primeira execução. A aplicação sobe em http://localhost:8080.

## Tecnologias

Java 17, Spring Boot, Spring Web, Spring Data JPA, MySQL, Thymeleaf, Bean Validation.
