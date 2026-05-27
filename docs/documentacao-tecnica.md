# Documentacao Tecnica

## 1. Introducao

Este documento descreve as principais decisoes de projeto adotadas na implementacao do sistema, com foco em armazenamento dos dados, indices em disco, relacionamento `1:N`, relacionamento `N:N`, consulta ordenada com Arvore B+, exclusao logica e organizacao geral do repositorio.

O sistema foi construindo sem uso de SGBD. Toda a persistencia ocorre por meio de arquivos binarios acessados diretamente pela aplicacao Java.

## 2. Estrutura Geral da Persistencia

Cada entidade do sistema possui um arquivo de dados proprio, armazenado em disco na pasta `data/`. Os registros sao serializados manualmente com `DataOutputStream` e desserializados com `DataInputStream`.

Arquivos principais:

- `data/usuarios/usuarios.db`
- `data/pacientes/pacientes.db`
- `data/consultas/consultas.db`
- `data/procedimentos/procedimentos.db`
- `data/consulta_procedimentos/consulta_procedimentos.db`

Os indices ficam em arquivos separados, dentro da pasta `data/indices/`.

## 3. Representacao dos Registros

Cada registro e armazenado com:

- lapide
- tamanho do registro
- bloco de bytes contendo os dados serializados

No cabecalho do arquivo principal sao mantidos:

- ultimo ID utilizado
- ponteiro para a lista de espacos livres

Esse formato permite:

- insercao de novos registros
- leitura de registros ativos
- exclusao logica
- reaproveitamento de espaco

## 4. Exclusao Logica

A exclusao logica foi implementada por meio de lapide.

Quando um registro e excluido:

- sua lapide passa de ativo para removido
- ele deixa de ser retornado nas operacoes de leitura
- seu espaco entra em uma lista de espacos livres

Essa lista permite que novos registros reaproveitem areas previamente liberadas, reduzindo desperdicio de espaco em disco.

## 5. Indices Primarios Baseados na PK

Todas as tabelas possuem indice primario baseado na chave primaria `id`.

Esse indice foi implementado com Hash Extensivel. A funcao do indice primario e mapear:

- chave: `id` do registro
- valor: endereco do registro dentro do arquivo `.db`

Com isso, as operacoes de busca por ID deixam de depender de varredura sequencial do arquivo de dados.

## 6. Como os Indices Sao Armazenados em Disco

O Hash Extensivel foi persistido em disco usando dois tipos de arquivo:

### 6.1 Diretorio

O diretorio armazena:

- profundidade global
- vetor de ponteiros para buckets

### 6.2 Buckets

Cada bucket armazena:

- profundidade local
- quantidade de entradas ocupadas
- pares `chave -> valor`

No indice primario:

- chave: `id`
- valor: endereco do registro no arquivo de dados

No relacionamento `1:N`:

- chave: `idPaciente`
- valor: ponteiro para o inicio da lista de consultas do paciente

No relacionamento `N:N` entre `Consulta` e `Procedimento`:

- chave primaria composta: `idConsulta + idProcedimento`
- indice composto: Hash Extensivel com chave `long`
- indices auxiliares: Hash Extensivel por `idConsulta` e por `idProcedimento`, ambos apontando para listas invertidas em disco

Na consulta ordenada de procedimentos:

- foi utilizada uma Arvore B+ persistida em `data/indices/procedimentos_nome.bplus`
- as folhas mantem as chaves de nome em ordem e sao percorridas sequencialmente na listagem ordenada

## 7. Atualizacao e Sincronizacao dos Indices

### Insercao

Na insercao:

- o registro e gravado no arquivo de dados
- o indice primario recebe o par `id -> endereco`
- se o registro for `Consulta`, o relacionamento `Paciente -> Consulta` tambem e atualizado
- se o registro for `ConsultaProcedimento`, o indice da chave composta e os indices dos dois sentidos do relacionamento `N:N` sao atualizados
- se o registro for `Procedimento`, o indice B+ por nome e reconstruido para refletir a nova ordem

### Leitura

Na leitura por ID:

- o sistema consulta o indice primario
- com o endereco obtido, acessa diretamente o registro no arquivo

### Atualizacao

Na atualizacao:

- se o novo conteudo couber no mesmo espaco, o endereco e preservado
- se o registro precisar ser movido, o indice primario e atualizado com o novo endereco
- no caso de `Consulta`, se houver mudanca de paciente, o relacionamento `1:N` tambem e ajustado
- no caso de `ConsultaProcedimento`, se houver mudanca em `idConsulta` ou `idProcedimento`, a chave composta antiga e removida e a nova chave e inserida nos indices
- no caso de `Procedimento`, alteracoes no nome atualizam a Arvore B+ de ordenacao

### Exclusao

Na exclusao logica:

- o registro recebe lapide
- a chave e removida do indice primario
- o espaco fica disponivel para reutilizacao
- se for uma `Consulta`, o vinculo com o paciente tambem e removido
- se for uma `ConsultaProcedimento`, seus vinculos sao removidos do indice composto e dos indices auxiliares
- nao e permitido excluir um procedimento que ainda possua consultas vinculadas

## 8. Relacionamento 1:N

O relacionamento `Paciente 1:N Consulta` foi implementado com Hash Extensivel.

Foram utilizadas duas estruturas:

### 8.1 Hash Extensivel por paciente

- chave: `idPaciente`
- valor: ponteiro para o inicio da lista de consultas desse paciente

### 8.2 Lista invertida encadeada

Cada no da lista armazena:

- `idConsulta`
- ponteiro para o proximo no

## 9. Logica de Navegacao do Relacionamento 1:N

O acesso ao relacionamento ocorre assim:

1. o sistema recebe um `idPaciente`
2. consulta o hash do relacionamento
3. recupera o ponteiro inicial da lista daquele paciente
4. percorre a lista dos `idConsulta`
5. usa o indice primario da tabela `Consulta` para recuperar os registros completos

Esse modelo evita percorrer todo o arquivo de consultas para descobrir quais pertencem a um paciente.

## 10. Relacionamento N:N

O relacionamento `Consulta N:N Procedimento` foi implementado pela tabela associativa `ConsultaProcedimento`.

Uma consulta pode conter varios procedimentos realizados, e um mesmo procedimento pode aparecer em varias consultas. Por isso, o relacionamento foi representado por uma tabela intermediaria com os campos:

- `idConsulta`
- `idProcedimento`
- `observacao`

A chave primaria logica da tabela associativa e composta por:

- `idConsulta`
- `idProcedimento`

Para manter compatibilidade com a classe generica `Arquivo<T>`, o registro ainda possui um `id` interno. Esse `id` e usado apenas para localizar fisicamente o registro no arquivo, enquanto a regra de unicidade do relacionamento e controlada pelo indice da chave composta.

## 11. Navegacao do Relacionamento N:N

O relacionamento pode ser acessado a partir das duas entidades principais:

- `Consulta -> Procedimentos`: rota `/consulta_procedimentos/consulta/{idConsulta}`
- `Procedimento -> Consultas`: rota `/consulta_procedimentos/procedimento/{idProcedimento}`

Foram criados tres indices para a associativa:

- Hash Extensivel da chave composta `idConsulta + idProcedimento`, usado para impedir duplicidade e buscar um vinculo especifico
- Hash Extensivel por `idConsulta`, apontando para uma lista invertida com os vinculos daquela consulta
- Hash Extensivel por `idProcedimento`, apontando para uma lista invertida com os vinculos daquele procedimento

Essa estrutura evita varredura completa no arquivo `consulta_procedimentos.db` ao navegar pelos dois lados do relacionamento.

## 12. Consulta Ordenada com Arvore B+

A Arvore B+ foi aplicada na tabela `Procedimento` para permitir a listagem de procedimentos em ordem alfabetica pelo nome do exame.

O indice fica persistido em:

- `data/indices/procedimentos_nome.bplus`

A funcionalidade e exposta na interface web pelo botao de ordenacao por nome na tela de procedimentos e pela rota:

- `/procedimentos/ordenado/nome`

Na consulta ordenada, a aplicacao percorre as folhas da Arvore B+ em sequencia e recupera os procedimentos pelos IDs encontrados no indice. Assim, a listagem ordenada nao depende de ordenar a lista em memoria principal no momento da consulta.

## 13. Integridade Referencial

As principais regras de integridade adotadas foram:

- nao criar consulta para paciente inexistente
- nao criar consulta para usuario inexistente
- nao criar vinculo `ConsultaProcedimento` para consulta inexistente
- nao criar vinculo `ConsultaProcedimento` para procedimento inexistente
- nao permitir dois vinculos com a mesma chave composta `idConsulta + idProcedimento`
- ao excluir consulta, remover seus vinculos do relacionamento `Paciente -> Consulta`
- ao excluir consulta, remover tambem seus vinculos com procedimentos
- ao trocar o paciente de uma consulta, remover o vinculo antigo e criar o novo
- nao permitir exclusao de paciente que ainda possua consultas vinculadas
- nao permitir exclusao de procedimento que ainda possua consultas vinculadas

## 14. Estrutura do Projeto no GitHub

O repositorio foi organizado da seguinte forma:

- `src/main/java/model`: entidades e serializacao
- `src/main/java/dao`: persistencia, indices e acesso aos arquivos
- `src/main/java/principal`: inicializacao da aplicacao
- `src/main/resources/public`: front-end web
- `data`: arquivos de dados e indices locais
- `docs`: documentacao e material de apoio

Essa organizacao preserva separacao entre dominio, persistencia e interface, coerente com a arquitetura adotada.

## 15. Decisoes de Projeto

As principais decisoes tomadas foram:

- manter armazenamento binario manual para atender ao objetivo da disciplina
- usar IDs inteiros sequenciais como chave primaria
- implementar exclusao logica com lapide para preservar consistencia dos arquivos
- usar Hash Extensivel no indice primario por permitir crescimento dinamico
- usar Hash Extensivel tambem no relacionamento `1:N`, associado a lista encadeada, para acesso eficiente aos filhos
- usar Hash Extensivel no relacionamento `N:N`, pois as consultas principais sao por igualdade de chave
- usar Arvore B+ na listagem ordenada de procedimentos por nome
- reconstruir automaticamente os indices quando seus arquivos ainda nao existem

## 16. Respostas ao Formulario da Fase Anterior

### a) Qual a estrutura usada para representar os registros?

Os registros foram representados como registros binarios de tamanho variavel. Cada registro possui lapide, tamanho e uma carga util serializada manualmente em bytes.

### b) Como atributos multivalorados do tipo string foram tratados?

Os atributos multivalorados do tipo string foram tratados como listas. No registro binario, primeiro e gravada a quantidade de elementos da lista e depois cada string individualmente.

### c) Como foi implementada a exclusao logica?

A exclusao logica foi implementada por lapide. O registro nao e removido fisicamente do arquivo no momento da exclusao; ele apenas recebe a marcacao de removido e seu espaco pode ser reutilizado posteriormente.

### d) Alem das PKs, quais outras chaves foram utilizadas nesta etapa?

Foi utilizada a chave estrangeira `idPaciente` para implementar o relacionamento `Paciente 1:N Consulta`. Tambem existe `idUsuario` em `Consulta`, usado para vincular a consulta a um usuario do sistema.

### e) Como a estrutura hash foi implementada para cada chave de pesquisa?

Foi utilizada uma implementacao de Hash Extensivel persistida em disco com diretorio e buckets. No indice primario, a hash relaciona `id -> endereco do registro`. No relacionamento `1:N`, a hash relaciona `idPaciente -> ponteiro para a lista de consultas do paciente`.

### f) Como foi implementado o relacionamento 1:N?

O relacionamento `Paciente 1:N Consulta` foi implementado com Hash Extensivel e lista invertida encadeada. O hash localiza rapidamente o inicio da lista de consultas de um paciente, e a lista guarda os IDs das consultas vinculadas.

### g) Como os indices sao persistidos em disco? Formato, atualizacao, sincronizacao com os dados.

Os indices sao persistidos em arquivos separados dentro de `data/indices/`. O diretorio da hash armazena a profundidade global e os ponteiros para buckets. Os buckets armazenam profundidade local, quantidade de entradas e pares `chave -> valor`. A cada insercao, alteracao ou exclusao, os indices sao atualizados para refletir o estado dos dados.

### h) Como esta estruturado o projeto no GitHub? Pastas, modulos, arquitetura.

O projeto esta organizado em camadas. As entidades ficam em `model`, a persistencia em `dao`, a inicializacao da aplicacao em `principal`, os arquivos web em `src/main/resources/public`, os dados persistidos em `data` e a documentacao em `docs`. A arquitetura segue a separacao entre dominio, persistencia e interface.

## 17. Conclusao

O projeto atende aos requisitos principais da etapa ao combinar persistencia binaria, CRUD completo, exclusao logica, indices primarios baseados em PK, relacionamento `1:N`, relacionamento `N:N` com chave composta e consulta ordenada com Arvore B+. A documentacao acima resume as decisoes tecnicas da implementacao atual.
