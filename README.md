# Sistema de Gestao de Consultas e Exames

Trabalho pratico da disciplina de Algoritmos e Estruturas de Dados III, com persistencia em arquivos binarios e implementacao manual de estruturas de indice, sem uso de SGBD.

## Integrantes

- Gabriel Teotonio de Castro Coelho Costa
- Thales Duque Camara
- Tiago Delgado Rocha
- Henrique Amorim Soares
- Lucas Gontijo Riani

## Visao Geral

O sistema foi desenvolvido para gerenciar atendimentos de uma clinica de saude. A aplicacao realiza operacoes de cadastro, busca, alteracao e exclusao logica das entidades do dominio, mantendo os dados persistidos em disco entre execucoes.

O projeto foi organizado com foco em separacao de responsabilidades:

- `model`: classes de entidade e serializacao dos registros
- `dao`: acesso aos arquivos, indices e regras de persistencia
- `principal`: inicializacao da API e execucao da aplicacao
- `src/main/resources/public`: front-end web estatico

## Entidades do Projeto

- `Usuario`: profissionais e administradores do sistema
- `Paciente`: cadastro do paciente com atributo multivalorado de alergias
- `Consulta`: atendimento vinculado a paciente e usuario
- `Procedimento`: catalogo de procedimentos e exames
- `ConsultaProcedimento`: entidade associativa entre consulta e procedimento

## Funcionalidades Implementadas

- CRUD completo de `Usuario`, `Paciente`, `Consulta`, `Procedimento` e `ConsultaProcedimento`
- Persistencia em arquivos binarios `.db`
- Exclusao logica com lapide
- Reutilizacao de espaco de registros removidos logicamente
- Indice primario por chave primaria com Hash Extensivel
- Relacionamento `Paciente 1:N Consulta` com Hash Extensivel
- Validacao de chaves inexistentes e conflitos basicos de integridade
- API HTTP em Java com Javalin
- Front-end web para operacao do sistema

## Tecnologias Utilizadas

- Java 21
- Maven
- Javalin
- Jackson
- HTML, CSS e JavaScript

## Estrutura de Persistencia

Os dados sao gravados em disco dentro da pasta `data/`.

- `data/usuarios/usuarios.db`
- `data/pacientes/pacientes.db`
- `data/consultas/consultas.db`
- `data/procedimentos/procedimentos.db`
- `data/consulta_procedimentos/consulta_procedimentos.db`
- `data/indices/` para os arquivos de indice

## Compilacao e Execucao

### Pre-requisitos

- Java 21
- Maven 3.9 ou superior

### Compilar o projeto

```bash
mvn clean compile
```

### Executar a aplicacao

```bash
mvn exec:java -Dexec.mainClass=principal.Main
```

Tambem e possivel executar diretamente a classe `principal.Main` pela IDE.

## Acesso

Com a aplicacao em execucao:

- API e front-end: `http://localhost:8080`

## Testes Basicos

Os CRUDs podem ser testados por meio das rotas HTTP usando Postman, Insomnia ou ferramenta equivalente.

Principais recursos expostos:

- `/usuarios`
- `/pacientes`
- `/consultas`
- `/procedimentos`
- `/consulta_procedimentos`

## Documentacao Tecnica

O detalhamento da persistencia, dos indices e das decisoes de projeto esta em:

- [docs/documentacao-tecnica.md](docs/documentacao-tecnica.md)

## Estrutura do Repositorio

```text
/
|-- src/
|   |-- main/java/model/
|   |-- main/java/dao/
|   |-- main/java/principal/
|   `-- main/resources/public/
|-- data/
`-- docs/
```

## Observacoes Finais

O projeto foi desenvolvido sem banco de dados relacional, utilizando exclusivamente arquivos binarios, indices persistidos em disco e estruturas de dados implementadas manualmente para atender aos requisitos da disciplina.
