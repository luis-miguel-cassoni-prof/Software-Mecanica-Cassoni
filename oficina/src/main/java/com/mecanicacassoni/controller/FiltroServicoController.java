package com.mecanicacassoni.controller;

import java.sql.SQLException;
import java.util.List;
import java.util.function.BiPredicate;

import com.mecanicacassoni.dao.ClienteDAO;
import com.mecanicacassoni.model.Cliente;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class FiltroServicoController {

        @FXML
    private ComboBox<Cliente> comboFiltroClientes;

    @FXML
    private TextField txtAno;

    private final ClienteDAO clienteDAO = new ClienteDAO();

    private BiPredicate<Integer, Integer> pesquisa;

    @FXML
    private void initialize() {
        comboFiltroClientes.setConverter(
                new StringConverter<Cliente>() {
                    @Override
                    public String toString(Cliente cliente) {
                        return cliente == null
                                ? ""
                                : cliente.getNome();
                    }

                    @Override
                    public Cliente fromString(String texto) {
                        return null;
                    }
                }
        );

        carregarClientes();
    }

    public void definirPesquisa(
            BiPredicate<Integer, Integer> pesquisa
    ) {
        this.pesquisa = pesquisa;
    }

    private void carregarClientes() {
        try {
            List<Cliente> clientes = clienteDAO.listarTodos();

            comboFiltroClientes.setItems(
                    FXCollections.observableArrayList(clientes)
            );

        } catch (SQLException e) {
            mostrarErro(
                    "Não foi possível carregar os clientes."
            );
            e.printStackTrace();
        }
    }

    @FXML
    private void realizarBuscaFiltrada() {
        Cliente cliente = comboFiltroClientes.getValue();

        if (cliente == null) {
            mostrarErro("Selecione um cliente.");
            return;
        }

        String textoAno = txtAno.getText().trim();

        if (!textoAno.matches("\\d{4}")) {
            mostrarErro("Informe um ano com 4 dígitos.");
            return;
        }

        int ano = Integer.parseInt(textoAno);

        if (ano < 1) {
            mostrarErro("Informe um ano válido.");
            return;
        }

        if (pesquisa == null) {
            mostrarErro("A pesquisa não foi configurada.");
            return;
        }

        boolean sucesso = pesquisa.test(
                cliente.getIdCliente(),
                ano
        );

        if (sucesso) {
            Stage stage = (Stage) txtAno
                    .getScene()
                    .getWindow();

            stage.close();
        }
    }

    private void mostrarErro(String mensagem) {
        Alert alerta = new Alert(Alert.AlertType.ERROR);
        alerta.setTitle("Busca Filtrada");
        alerta.setHeaderText(null);
        alerta.setContentText(mensagem);
        alerta.showAndWait();
    }
}
