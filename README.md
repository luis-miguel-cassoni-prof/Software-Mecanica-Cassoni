## PROJETO DE SOFTWARE PARA A MECÂNICA CASSONI

## TECNOLOGIAS E FERRAMENTAS UTILIZADAS

- Java 21: O core da aplicação.

- JavaFX: Biblioteca gráfica que permite a criação de aplicativos desktop.

- Maven: Ferramenta de build e gerenciamento automático de Dependências.

- Scene Builder: Ferramenta para gerenciar Telas do JavaFX, reduzindo quantidade de código.

- MySQL: Banco de dados relacional utilizado para persistir todos os dados.

- JDBC: Biblioteca do java que gerencia a conexão com Banco de Dados, e executa as consultas SQL.

- ChatGPT: Apoio à implementação, revisão de código, depuração e geração de código repetitivo (boilerplate).

## DEPENDÊNCIAS UTILIZADAS
- JavaFX: A dependência da própria biblioteca gráfica

- MySQL Driver for JDBC (MySQL Connector J): Biblioteca do driver responsável por estabelecer a comunicação entre o Java e o Banco de Dados MySQL.

- Lombok: Biblioteca usada nos Models para evitar código repetitivo.

## FUNCIONAMENTO DO SISTEMA

O sistema Mecânica Cassoni é uma aplicação desktop desenvolvida em Java para auxiliar no gerenciamento das atividades de uma oficina mecânica.

O software permite cadastrar, consultar, atualizar e excluir funcionários, clientes, serviços e prestações de trabalho.

Cada serviço está vinculado a um cliente e pode possuir diversas prestações, que registram o funcionário responsável, a data, os horários de início e término e o valor dos materiais utilizados.

O sistema calcula automaticamente o valor dos serviços com base no tempo trabalhado, utilizando uma tarifa padrão de R$ 80,00 por hora, acrescida do valor dos materiais. Também permite definir manualmente o valor final de um serviço, quando necessário.

A aplicação utiliza JavaFX para a interface gráfica, JDBC para comunicação com o banco de dados e MySQL para persistência das informações.

## PRÉ-REQUISITOS

Para executar o projeto a partir do código-fonte, são necessários:

- JDK 21 ou superior, compatível com a versão do JavaFX utilizada.
- Apache Maven.
- MySQL Server.
- Um editor ou IDE com suporte a Java, como VS Code, IntelliJ IDEA ou Eclipse.

As dependências Java são gerenciadas pelo Maven e estão declaradas no arquivo `pom.xml`.

O Scene Builder não é necessário para executar o programa, sendo utilizado apenas para criar ou modificar as interfaces gráficas.

## INSTALAÇÃO E EXECUÇÃO

### 1. Obter o projeto

Clone o repositório:

```bash
git clone https://github.com/luis-miguel-cassoni-prof/Software-Mecanica-Cassoni.git
```

Acesse a pasta que contém o arquivo `pom.xml`.

### 2. Configurar o MySQL

Inicie o MySQL Server e execute o script SQL de criação do banco de dados disponibilizado no repositório.

O script cria o banco `mecanica_cassoni` e as tabelas:

- `funcionario`
- `cliente`
- `servico`
- `prestacao`

As tabelas possuem relacionamentos por meio de chaves estrangeiras.

### 3. Configurar a conexão

No arquivo `src/main/resources/database.properties`, configure os parâmetros de conexão conforme o formato esperado pela classe `ConnectionFactory`.

Utilize o endereço do servidor MySQL, a porta, o nome do banco de dados e as credenciais de um usuário com as permissões necessárias.

Não publique senhas reais ou outras credenciais no repositório.

### 4. Executar o projeto

Abra um terminal na pasta que contém o `pom.xml` e execute:

```bash
mvn clean javafx:run
```

Para empacotar o projeto e fazer um executável utilize:
```bash
mvn clean package
```

O Maven realizará a compilação e iniciará a aplicação JavaFX.

### 5. Utilizar o sistema

Na tela inicial, selecione uma das opções:

- **Clientes:** gerenciamento dos clientes cadastrados.
- **Funcionários:** gerenciamento dos funcionários.
- **Serviços:** cadastro de serviços, associação com clientes e consulta dos valores.
- **Prestações:** acessadas a partir de um serviço, permitindo registrar e consultar o trabalho realizado.

Para cadastrar uma prestação, é necessário possuir previamente um serviço e um funcionário cadastrados.

## LOG DE VERSÕES

- 0.9.1 - Adicionados filtros de busca para serviços, utilizando o Cliente e o Ano.

- 0.9.0 - Lançamento.
