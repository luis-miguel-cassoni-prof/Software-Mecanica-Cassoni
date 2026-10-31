package com.mecanicacassoni.controller;

import java.sql.SQLException;
import java.util.List;

import com.mecanicacassoni.dao.FuncionarioDAO;
import com.mecanicacassoni.model.Funcionario;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
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

    private final FuncionarioDAO DAO = new FuncionarioDAO();

    @FXML
    private void inserirFuncionario() {
        String nome = txtNome.getText().trim();

        if (nome.isEmpty() || nome.length() > 50) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Cadastro");
            alerta.setHeaderText(null);
            alerta.setContentText("Erro! O nome está vazio ou excede 50 letras");
            alerta.showAndWait();
            return;
        }

        Funcionario funcionario = new Funcionario();
        funcionario.setNome(nome);
        try {
            int id = DAO.inserir(funcionario);
            System.out.println("Inserido: " + id);
            Alert alerta = new Alert(Alert.AlertType.INFORMATION);
            alerta.setTitle("Cadastro");
            alerta.setHeaderText(null);
            alerta.setContentText("Funcionário cadastrado com sucesso!");
            alerta.showAndWait();

            txtNome.clear();
            txtId.clear();

            carregarFuncionario();
        } catch (SQLException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Cadastro");
            alerta.setHeaderText(null);
            alerta.setContentText("Erro ao inserir Funcionário");
            alerta.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void buscarFuncionarioPorId() {

    }

    @FXML
    private void atualizarFuncionario() {

    }

    @FXML
    private void deletarFuncionario() {

    }

    private void carregarFuncionario() {
        try {
            List<Funcionario> lista = DAO.listarTodos();
            ObservableList<Funcionario> listaObservavel = FXCollections.observableArrayList(lista);
            tabelaFuncionarios.setItems(listaObservavel);
        } catch (SQLException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Listagem");
            alerta.setHeaderText(null);
            alerta.setContentText("Erro carregar funcionários na lista");
            alerta.showAndWait();
            e.printStackTrace();
        }
    }

    @FXML
    private void initialize() {
        colunaId.setCellValueFactory(new PropertyValueFactory<>("idFuncionario"));

        colunaNome.setCellValueFactory(new PropertyValueFactory<>("nome"));

        carregarFuncionario();
    }
}
