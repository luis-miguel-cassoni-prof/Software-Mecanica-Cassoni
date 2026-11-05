package com.mecanicacassoni.controller;

import java.io.IOException;
import java.sql.SQLException;

import com.mecanicacassoni.dao.FuncionarioDAO;
import com.mecanicacassoni.model.Funcionario;
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

public class FuncionarioController {

    @FXML
    private TextField txtId;

    @FXML
    private TextField txtNome;

    @FXML
    private TableView<Funcionario> tabelaFuncionarios;

    @FXML
    private TableColumn<Funcionario, Integer> colunaId;

    @FXML
    private TableColumn<Funcionario, String> colunaNome;

    private final FuncionarioDAO dao = new FuncionarioDAO();

    @FXML
    private void initialize() {
        colunaId.setCellValueFactory(new PropertyValueFactory<>("idFuncionario"));
        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        tabelaFuncionarios.getSelectionModel().selectedItemProperty().addListener((observable, antigo, novo) -> {

            if (novo != null) {
                txtId.setText(String.valueOf(novo.getIdFuncionario()));

                txtNome.setText(novo.getNome());
            }
        });

        carregarFuncionario();
    }

    @FXML
    private void inserirFuncionario() {
        String nome = txtNome.getText().trim();

        if (!validarNome(nome, "Cadastro")) {
            return;
        }

        Funcionario funcionario = new Funcionario();
        funcionario.setNome(nome);

        try {
            int id = dao.inserir(funcionario);

            mostrarAlerta(Alert.AlertType.INFORMATION, "Cadastro",
                    "Funcionário cadastrado com sucesso! ID: " + id);

            limparCampos();
            carregarFuncionario();

        } catch (SQLException e) {
            mostrarErroSql("Cadastro", "Não foi possível cadastrar o funcionário.", e);
        }
    }

    @FXML
    private void buscarFuncionarioPorId() {
        Integer id = lerIdValido("Buscar");

        if (id == null) {
            txtNome.clear();
            return;
        }

        try {
            Funcionario funcionario = dao.buscarPorId(id);

            if (funcionario == null) {
                txtNome.clear();
                mostrarAlerta(Alert.AlertType.WARNING, "Buscar",
                        "Funcionário não encontrado.");
                return;
            }

            txtNome.setText(funcionario.getNome());

        } catch (SQLException e) {
            txtNome.clear();
            mostrarErroSql("Buscar", "Não foi possível buscar o funcionário.", e);
        }
    }

    @FXML
    private void atualizarFuncionario() {
        Integer id = lerIdValido("Atualizar");

        if (id == null) {
            return;
        }

        String nome = txtNome.getText().trim();

        if (!validarNome(nome, "Atualizar")) {
            return;
        }

        try {
            Funcionario funcionario = dao.buscarPorId(id);

            if (funcionario == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Atualizar",
                        "Funcionário não encontrado.");
                return;
            }

            funcionario.setNome(nome);

            boolean atualizado = dao.atualizar(funcionario);

            if (!atualizado) {
                mostrarAlerta(Alert.AlertType.WARNING, "Atualizar",
                        "Nenhuma alteração foi confirmada.");
                return;
            }

            mostrarAlerta(Alert.AlertType.INFORMATION, "Atualizar",
                    "Funcionário atualizado com sucesso!");

            limparCampos();
            carregarFuncionario();

        } catch (SQLException e) {
            mostrarErroSql("Atualizar", "Não foi possível atualizar o funcionário.", e);
        }
    }

    @FXML
    private void deletarFuncionario() {
        Integer id = lerIdValido("Excluir");

        if (id == null) {
            return;
        }

        try {
            Funcionario funcionario = dao.buscarPorId(id);

            if (funcionario == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Excluir",
                        "Funcionário não encontrado.");
                return;
            }

            Alert confirmacao = new Alert(Alert.AlertType.CONFIRMATION);
            confirmacao.setTitle("Excluir funcionário");
            confirmacao.setHeaderText("Confirmar exclusão");
            confirmacao.setContentText(
                    "Deseja realmente excluir " + funcionario.getNome()
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
                        "Nenhum funcionário foi excluído.");
                return;
            }

            mostrarAlerta(Alert.AlertType.INFORMATION, "Excluir",
                    "Funcionário excluído com sucesso!");

            limparCampos();
            carregarFuncionario();

        } catch (SQLException e) {
            mostrarErroSql("Excluir", "Não foi possível excluir o funcionário.", e);
        }
    }

    private void carregarFuncionario() {
        try {
            tabelaFuncionarios.setItems(
                    FXCollections.observableArrayList(dao.listarTodos())
            );

        } catch (SQLException e) {
            mostrarErroSql("Listagem", "Não foi possível carregar os funcionários.", e);
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

    private void limparCampos() {
        txtId.clear();
        txtNome.clear();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensagem) {
        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }

    private void mostrarErroSql(String operacao, String mensagem, SQLException e) {
        mostrarAlerta(Alert.AlertType.ERROR, operacao, mensagem);
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