CREATE DATABASE IF NOT EXISTS mecanica_cassoni
DEFAULT CHARACTER SET utf8mb4;

USE mecanica_cassoni;

CREATE TABLE funcionario (
    id_funcionario INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(50) NOT NULL
);

CREATE TABLE cliente (
    id_cliente INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(50) NOT NULL,
    cpf VARCHAR(14) NOT NULL UNIQUE
);

CREATE TABLE servico (
    id_servico INT PRIMARY KEY AUTO_INCREMENT,
    nome VARCHAR(100) NOT NULL,
    id_cliente INT NOT NULL,
    valor_final DECIMAL(10,2) NULL,
    FOREIGN KEY (id_cliente) REFERENCES cliente(id_cliente)
);

CREATE TABLE prestacao (
    id_prestacao INT PRIMARY KEY AUTO_INCREMENT,
    id_servico INT NOT NULL,
    id_funcionario INT NOT NULL,
    data DATE NOT NULL,
    hora_inicio TIME NOT NULL,
    hora_fim TIME NOT NULL,
    vlr_material DECIMAL(10,2) NOT NULL,
    valor_hora DECIMAL(10,2) NOT NULL DEFAULT 80.00,
    FOREIGN KEY (id_servico) REFERENCES servico(id_servico),
    FOREIGN KEY (id_funcionario) REFERENCES funcionario(id_funcionario)
);