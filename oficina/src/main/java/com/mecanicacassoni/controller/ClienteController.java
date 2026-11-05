
package com.mecanicacassoni.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.mecanicacassoni.dao.ClienteDAO;
import com.mecanicacassoni.model.Cliente;
import com.mecanicacassoni.util.Navegacao;

import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class ClienteController {

    @FXML private TextField txtId;
    @FXML private TextField txtNome;
    @FXML private TextField txtCpf;

    @FXML private TableView<Cliente> tabelaClientes;

    @FXML private TableColumn<Cliente, Integer> colunaId;
    @FXML private TableColumn<Cliente, String> colunaNome;
    @FXML private TableColumn<Cliente, String> colunaCpf;

    private final ClienteDAO dao = new ClienteDAO();

    @FXML
    private void initialize() {
        colunaId.setCellValueFactory(new PropertyValueFactory<>("idCliente"));
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));
        colunaCpf.setCellValueFactory(new PropertyValueFactory<>("cpf"));

        tabelaClientes.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, antigo, novo) -> {
                    if (novo != null) {
                        txtId.setText(String.valueOf(novo.getIdCliente()));
                        txtNome.setText(novo.getNome());
                        txtCpf.setText(novo.getCpf());
                    }
                });

        carregarClientes();
    }

    @FXML
    private void inserirCliente() {
        String nome = txtNome.getText().trim();
        String cpf = normalizarCpf(txtCpf.getText());

        if (!validarNome(nome, "Cadastro") || !validarCpf(cpf, "Cadastro")) {
            return;
        }

        Cliente cliente = new Cliente();
        cliente.setNome(nome);
        cliente.setCpf(cpf);

        try {
            int id = dao.inserir(cliente);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Cadastro",
                    "Cliente cadastrado com sucesso! ID: " + id);

            limparCampos();
            carregarClientes();

        } catch (SQLException e) {
            mostrarErroSql("Cadastro", "Não foi possível cadastrar o cliente.", e);
        }
    }

    @FXML
    private void buscarClientePorId() {
        Integer id = lerIdValido("Buscar");

        if (id == null) {
            return;
        }

        try {
            Cliente cliente = dao.buscarPorId(id);

            if (cliente == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Buscar",
                        "Cliente não encontrado.");
                return;
            }

            txtNome.setText(cliente.getNome());
            txtCpf.setText(cliente.getCpf());

        } catch (SQLException e) {
            mostrarErroSql("Buscar", "Não foi possível buscar o cliente.", e);
        }
    }

    @FXML
    private void atualizarCliente() {
        Integer id = lerIdValido("Atualizar");

        if (id == null) {
            return;
        }

        String nome = txtNome.getText().trim();
        String cpf = normalizarCpf(txtCpf.getText());

        if (!validarNome(nome, "Atualizar") || !validarCpf(cpf, "Atualizar")) {
            return;
        }

        try {
            Cliente cliente = dao.buscarPorId(id);

            if (cliente == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Atualizar",
                        "Cliente não encontrado.");
                return;
            }

            cliente.setNome(nome);
            cliente.setCpf(cpf);

            boolean atualizado = dao.atualizar(cliente);

            if (!atualizado) {
                mostrarAlerta(Alert.AlertType.WARNING, "Atualizar",
                        "Nenhuma atualização foi confirmada.");
                return;
            }

            mostrarAlerta(Alert.AlertType.INFORMATION, "Atualizar",
                    "Cliente atualizado com sucesso!");

            limparCampos();
            carregarClientes();

        } catch (SQLException e) {
            mostrarErroSql("Atualizar", "Não foi possível atualizar o cliente.", e);
        }
    }

    @FXML
    private void deletarCliente() {
        Integer id = lerIdValido("Excluir");

        if (id == null) {
            return;
        }

        try {
            Cliente cliente = dao.buscarPorId(id);

            if (cliente == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Excluir",
                        "Cliente não encontrado.");
                return;
            }

            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Excluir cliente");
            confirmacao.setHeaderText("Confirmar exclusão");
            confirmacao.setContentText(
                    "Deseja realmente excluir " + cliente.getNome()
                    + " (ID: " + id + ")?"
            );

            boolean confirmou = confirmacao.showAndWait()
                    .filter(ButtonType.OK::equals)
                    .isPresent();

            if (!confirmou) {
                return;
            }

            boolean excluido = dao.deletarPorId(id);

            if (!excluido) {
                mostrarAlerta(Alert.AlertType.WARNING, "Excluir",
                        "Nenhum cliente foi excluído.");
                return;
            }

            mostrarAlerta(Alert.AlertType.INFORMATION, "Excluir",
                    "Cliente excluído com sucesso!");

            limparCampos();
            carregarClientes();

        } catch (SQLException e) {
            mostrarErroSql("Excluir", "Não foi possível excluir o cliente.", e);
        }
    }

    private void carregarClientes() {
        try {
            tabelaClientes.setItems(
                    FXCollections.observableArrayList(dao.listarTodos())
            );

        } catch (SQLException e) {
            mostrarErroSql("Listagem", "Não foi possível carregar os clientes.", e);
        }
    }

    private Integer lerIdValido(String operacao) {
        try {
            int id = Integer.parseInt(txtId.getText().trim());

            if (id <= 0) {
                mostrarAlerta(Alert.AlertType.ERROR, operacao,
                        "O ID deve ser maior que zero.");
                return null;
            }

            return id;

        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, operacao,
                    "Digite um ID numérico inteiro válido.");
            return null;
        }
    }

    private boolean validarNome(String nome, String operacao) {
        if (nome.isEmpty() || nome.length() > 50) {
            mostrarAlerta(Alert.AlertType.ERROR, operacao,
                    "O nome deve conter entre 1 e 50 caracteres.");
            return false;
        }

        return true;
    }

    private String normalizarCpf(String cpf) {
        String digitos = cpf.trim().replaceAll("[.\\-\\s]", "");

        if (!digitos.matches("\\d{11}")) {
            return cpf.trim();
        }

        return digitos.substring(0, 3) + "."
                + digitos.substring(3, 6) + "."
                + digitos.substring(6, 9) + "-"
                + digitos.substring(9, 11);
    }

    private boolean validarCpf(String cpf, String operacao) {
        if (!cpf.matches("\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}")) {
            mostrarAlerta(Alert.AlertType.ERROR, operacao,
                    "Digite um CPF com 11 dígitos, no formato 000.000.000-00.");
            return false;
        }

        return true;
    }

    private void limparCampos() {
        tabelaClientes.getSelectionModel().clearSelection();
        txtId.clear();
        txtNome.clear();
        txtCpf.clear();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void mostrarErroSql(String operacao, String mensagem, SQLException e) {
        if (e.getErrorCode() == 1062) {
            mostrarAlerta(Alert.AlertType.WARNING, operacao,
                    "Já existe um cliente cadastrado com esse CPF.");
        } else {
            mostrarAlerta(Alert.AlertType.ERROR, operacao, mensagem);
        }

        e.printStackTrace();
    }

    @FXML
    private void voltarMenu(ActionEvent event) {
        try {
            Navegacao.trocarTela(event, "Menu.fxml");
        } catch (IOException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Navegação");
            alerta.setHeaderText(null);
            alerta.setContentText("Não foi possível voltar ao menu.");
            alerta.showAndWait();
            e.printStackTrace();
        }
    }
}