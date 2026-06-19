# Sistema de Gestão de Consultas e Exames (AEDS III)

Trabalho prático completo da disciplina de Algoritmos e Estruturas de Dados III. Este sistema gerencia atendimentos de uma clínica de saúde, possuindo uma API web e um front-end próprio.

**O grande diferencial técnico deste projeto é a ausência de um SGBD tradicional (como MySQL ou PostgreSQL).** Toda a camada de persistência, indexação (Hash, Árvore B+, Listas Invertidas), compressão (Huffman, LZW), criptografia e busca em texto foi **implementada manualmente em Java** operando diretamente sobre arquivos binários em disco.

## 👥 Integrantes

* Gabriel Teotônio de Castro Coelho Costa
* Thales Duque Câmara
* Tiago Delgado Rocha
* Henrique Amorim Soares
* Lucas Gontijo Riani

---

## 🏗️ Visão Geral da Arquitetura

O sistema foi arquitetado em camadas bem definidas para separar as responsabilidades:

* `model`: Interfaces e classes de entidade (POJOs) que representam as regras de negócio e sabem como se serializar em arrays de bytes.
* `dao` (Data Access Object): O coração do sistema. Contém a lógica de leitura/escrita em arquivos binários (`.db`), gerenciamento de índices complexos, algoritmos de compressão e backup.
* `busca` e `crypto`: Implementação de algoritmos clássicos de casamento de padrões em strings e criptografia.
* `principal`: Classes de inicialização da API HTTP (usando **Javalin**) e menus interativos de console.
* `resources/public`: Aplicação Front-end SPA (Single Page Application) construída com HTML, CSS e JavaScript modularizado.

---

## 💾 Camada de Persistência e Estruturas de Dados (`src/main/java/dao`)

Esta é a camada mais complexa do projeto, onde armazenamos e recuperamos os dados. Foram implementadas diversas estruturas de dados clássicas para garantir a eficiência das buscas:

* **`Arquivo.java`:** A classe genérica base do sistema de arquivos. Utiliza `RandomAccessFile` para gerenciar o CRUD básico de registros em arquivos binários. Controla as "lápides" (exclusão lógica) e o reaproveitamento de espaços fragmentados.
* **`HashExtensivelIntLong.java` / `HashExtensivelLongLong.java`:** Implementamos o algoritmo de **Hash Extensível** (com uso de diretórios `.dir` e buckets `.bkt`). Eles garantem acesso direto (O(1) na maioria dos casos) aos registros através das chaves primárias (IDs) ou chaves estrangeiras.
* **`ArvoreBMaisProcedimentoNome.java`:** Implementamos uma **Árvore B+** para indexar procedimentos pelo nome. A Árvore B+ permite buscas exatas e buscas por intervalos de forma extremamente eficiente no disco, minimizando os acessos a I/O.
* **`ListaInvertidaConsulta.java`:** Utilizamos o conceito de **Listas Invertidas** para relacionar e buscar entidades de forma flexível (ex: buscar todas as consultas de um determinado paciente ou em uma determinada data), armazenando os IDs dos registros que correspondem a um critério.
* **`IndicePacienteConsulta.java` / `IndiceConsultaProcedimento.java`:** Estruturas de relacionamento que utilizam o Hash/Listas Invertidas para resolver as relações 1:N e N:M entre as entidades.
* **DAOs Específicos (`UsuarioDAO`, `PacienteDAO`, `ConsultaDAO`, etc.):** Classes que herdam/usam a classe `Arquivo` e agrupam todas as operações de banco de dados específicas de cada entidade, aplicando as regras de negócio.

---

## 🧠 Algoritmos Implementados

Além das estruturas de armazenamento, implementamos algoritmos clássicos da computação para compressão, busca e segurança.

### 1. Compressão de Dados (Backup)

Os algoritmos de compressão varrem os arquivos binários (`.db`, `.dir`, `.bkt`) e os compactam em arquivos de backup (`.bak`).

* **`HuffmanCompressor.java`:** Implementa o algoritmo de **Huffman**, criando uma árvore binária baseada na frequência de repetição dos bytes para comprimir os dados.
* **`LZWCompressor.java`:** Implementa o algoritmo de **Lempel-Ziv-Welch (LZW)**, baseado em dicionário, excelente para padrões repetitivos nos nossos arquivos binários.
* **`GerenciadorBackup.java`:** Orquestra a criação e restauração dos backups zipados sob os formatos acima, adicionando cabeçalhos identificadores (`HUFF` ou `LZW_`).
* **`TesteCompressao.java`:** Classe dedicada a validar as taxas de compressão e a integridade dos dados comprimidos/descomprimidos.

### 2. Casamento de Padrões em Texto (`src/main/java/busca`)

Aplicados na funcionalidade de busca por nome de paciente de forma *case-insensitive*, evidenciando o número de comparações realizadas.

* **`KMP.java` (Knuth-Morris-Pratt):** Utiliza uma função de prefixo (tabela de falha) para garantir que a busca nunca retroceda no texto principal, sendo O(N).
* **`BoyerMoore.java`:** Implementa a heurística do mau caractere (*bad character*), comparando da direita para a esquerda e dando "saltos" ao encontrar letras que não pertencem ao padrão procurado.
* **`ResultadoBusca.java`:** Encapsula o retorno contendo os índices encontrados e a performance do algoritmo escolhido.

### 3. Criptografia Sensível (`src/main/java/crypto`)

* **`CriptografiaXOR.java`:** Visando a LGPD, o **CPF dos pacientes** é considerado um dado sensível. Implementamos a cifra de **XOR Simétrico com chave repetida**, combinada com codificação Base64.
* *Funcionamento:* O CPF é cifrado instantaneamente no método `toByteArray()` antes de ir para o disco, e decifrado no `fromByteArray()` ao ser carregado para a memória. Se alguém abrir o arquivo `pacientes.db`, verá apenas lixo digital em vez dos CPFs.



---

## 💻 Entidades (Modelos - `src/main/java/model`)

Todos implementam a interface `Registro` para garantir os métodos de serialização.

* **`Usuario.java`:** Profissionais e administradores.
* **`Paciente.java`:** Cadastro de pacientes.
* **`Procedimento.java`:** Catálogo de exames.
* **`Consulta.java`:** Agendamentos vinculando paciente e usuário.
* **`ConsultaProcedimento.java`:** Tabela associativa (N:M).

---

## 🎨 Front-end e Estilização (`resources/public`)

Abandonamos as interfaces cruas e construímos uma aplicação Web completa separada em módulos.

### Estilização (`css/`)

Utilizamos CSS puro, muito bem dividido para manutenção fácil:

* **`global.css`:** Contém variáveis CSS de cores, fontes, resets globais (margin/padding zero) aplicados no `body`.
* **`layout.css`:** Define a estrutura de grid/flexbox principal, navbar, sidebar, containers e responsividade.
* **`components.css`:** Estiliza os elementos reutilizáveis como botões (`.btn`), inputs, modais e tabelas de dados.
* **`login.css`:** Estilos específicos para centralizar a tela de autenticação e seus formulários.
* **`style.css`:** Ajustes finos gerais e utilitários.

### Javascript Modular (`js/`)

Dividimos o JS na arquitetura MVC/Services para comunicar com a API do Javalin:

* **`controllers/`** (`paciente-controller.js`, `consulta-controller.js`, etc.): Capturam os eventos dos botões no HTML, validam dados dos formulários e atualizam o DOM (tabelas e listas).
* **`services/`** (`paciente-api.js`, `consulta-api.js`, etc.): Isolam as chamadas de rede (Fetch API). Eles contêm os métodos `GET`, `POST`, `PUT`, `DELETE` para os respectivos endpoints Java.
* **`ui/`**:
* `toast.js`: Implementamos um sistema de notificações dinâmico na tela (sucesso/erro) sem usar bibliotecas externas.
* `layout.js`: Gerencia interações globais de UI (ex: abrir/fechar menus).



---

## 🖥️ Menus e Inicialização (`src/main/java/principal`)

* **`Main.java`:** Inicia o servidor Javalin, mapeia as rotas HTTP (REST) e serve a pasta `public`.
* **`Principal.java`:** Menu interativo principal rodando via Terminal/Console (ideal para quem quer testar sem o navegador).
* **`MenuPacientes.java` / `MenuPesquisa.java`:** Submenus de console modulares que permitem inserir dados e testar a busca via KMP/BoyerMoore diretamente do terminal.

---

## ⚙️ Como Compilar e Executar

**Pré-requisitos:** Java 21+ e Maven.

### 1. Compilar o projeto

```bash
mvn clean compile

```

### 2. Executar a Aplicação Web (Recomendado)

Inicia a API REST e o Front-end estático.

```bash
mvn exec:java -Dexec.mainClass=principal.Main

```

Após executar, acesse no seu navegador: **`http://localhost:8080`**

### 3. Executar o Menu de Console (Terminal)

Caso queira testar a Árvore B+, compressão ou buscas diretamente por linha de comando:

```bash
mvn exec:java -Dexec.mainClass=principal.Principal

```

*(No Windows, para exibir acentos no console, digite `chcp 65001` antes).*

---

## 📡 Principais Rotas da API (Javalin)

| Entidade | Rotas CRUD Disponíveis no `Main.java` |
| --- | --- |
| **Pacientes** | `GET /pacientes`, `GET /pacientes/{id}`, `POST /pacientes`, `PUT /pacientes/{id}`, `DELETE /pacientes/{id}` |
| **Busca Específica** | `GET /pacientes/buscar?padrao=NOME&algoritmo=kmp` |
| **Consultas** | `GET /consultas`, `POST /consultas`, `PUT /consultas`, `DELETE /consultas` |
| **Backups** | `GET /backup`, `POST /backup/huffman`, `POST /backup/lzw`, `POST /restaurar` |

*(A estrutura completa dos dados gerados fica salva na raiz do projeto na pasta `data/`).*