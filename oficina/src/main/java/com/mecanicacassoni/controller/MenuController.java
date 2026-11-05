package com.mecanicacassoni.controller;

import java.io.IOException;

import com.mecanicacassoni.util.Navegacao;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;

public class MenuController {

    @FXML
    private void abrirClientes(ActionEvent event) {
        abrirTela(event, "Cliente.fxml");
    }

    @FXML
    private void abrirFuncionarios(ActionEvent event) {
        abrirTela(event, "Funcionario.fxml");
    }

    @FXML
    private void abrirServicos(ActionEvent event) {
        abrirTela(event, "Servico.fxml");
    }

    private void abrirTela(ActionEvent event, String arquivo) {
        try {
            Navegacao.trocarTela(event, arquivo);
        } catch (IOException e) {
            Alert alerta = new Alert(Alert.AlertType.ERROR);
            alerta.setTitle("Navegação");
            alerta.setHeaderText(null);
            alerta.setContentText("Não foi possível abrir a tela.");
            alerta.showAndWait();
            e.printStackTrace();
        }
    }
}