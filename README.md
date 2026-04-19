# 🏥 Sistema de Gestão de Consultas e Exames - TP AEDS III

**Pontifícia Universidade Católica de Minas Gerais (PUC Minas)** **Instituto de Ciências Exatas e Informática** **Disciplina:** Algoritmos e Estruturas de Dados III (AED III)

---

## 👥 Integrantes do Grupo
* Gabriel Teotônio de Castro Coelho Costa
* Thales Duque Câmara
* Tiago Delgado Rocha
* Henrique Amorim Soares
* Lucas Gontijo Riani

---

## 📌 Sobre o Projeto
Este projeto é um aplicativo minimalista para o registro e gerenciamento de atendimentos em uma pequena clínica de saúde. O objetivo principal é desenvolver um sistema robusto de backend que realize operações de CRUD aplicando conceitos avançados de estruturas de dados e armazenamento direto em memória secundária (arquivos binários), **sem o uso de Sistemas Gerenciadores de Bancos de Dados (SGBDs)**.

[cite_start]O sistema segue a arquitetura **MVC + DAO**[cite: 35], garantindo a separação de responsabilidades entre as regras de negócio, a representação em memória e a persistência em disco.

---

## ⚙️ Modelagem de Dados
O domínio do problema exige relacionamentos complexos e campos específicos, modelados da seguinte forma:

* **`Usuario`**: Responsável pela autenticação do sistema (Recepcionistas/Administradores).
* **`Paciente`**: Entidade principal contendo dados básicos e um **campo string multivalorado** (Lista de Alergias/Telefones).
* **`Consulta`**: Entidade que registra o atendimento. Possui relacionamento **1:N** com Paciente (um paciente tem várias consultas).
* **`Exame`**: Catálogo de exames disponíveis.
* **`Consulta_Exame`**: Tabela intermediária para modelar o relacionamento **N:N** (uma consulta tem vários exames, e um exame pode pertencer a várias consultas).

---

## 🚀 Requisitos e Funcionalidades Técnicas
O desenvolvimento está sendo dividido em fases, contemplando a implementação 100% manual (via código Java) das seguintes estruturas:

### Fase 1: Persistência Básica (Atual)
- [x] [cite_start]CRUD completo de registros[cite: 12].
- [x] [cite_start]Armazenamento em arquivos binários (`.db`)[cite: 13, 27].
- [x] [cite_start]Gerenciamento de Cabeçalho (controle do último ID inserido)[cite: 10].
- [x] [cite_start]Controle de exclusão lógica de registros utilizando **Lápide**[cite: 10, 13, 21].
- [x] Interface temporária via Console para validação lógica.

### Fases Futuras (A Implementar)
- [ ] **Indexação Externa:** Implementação de **Árvore B+** e **Hash Extensível** para buscas eficientes (ex: busca por CPF ou ID do Paciente).
- [ ] **Pesquisa Textual:** Algoritmos de casamento de padrões (**Boyer-Moore** ou **KMP**) no campo de diagnóstico da Consulta.
- [ ] **Segurança:** Autenticação de usuários com senhas protegidas via **Criptografia XOR**.
- [ ] **Otimização de Espaço:** Algoritmos de compactação e descompactação de dados (**Huffman** e **LZW**).
- [ ] [cite_start]**Interface Gráfica:** Substituição do Console por uma interface mínima web em **HTML/CSS**[cite: 25, 26, 39].

---

## 📁 Estrutura do Repositório
Para facilitar a navegação e respeitar o padrão MVC, o projeto está organizado da seguinte maneira:

```text
/
├── src/
│   ├── model/       # Classes de entidade (Paciente, Consulta, etc.)
│   ├── dao/         # Classes de persistência em arquivos binários (File Access)
│   ├── controller/  # Regras de negócio e mediação
│   └── view/        # Interface de interação com o usuário
├── data/            # Diretório ignorado pelo Git (armazena os arquivos .db localmente)
└── docs/            # Documentação técnica (DCU, DER, Arquitetura)
