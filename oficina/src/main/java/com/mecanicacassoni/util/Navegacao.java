
package com.mecanicacassoni.util;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Navegacao {

    public static void trocarTela(ActionEvent event, String arquivo)
            throws IOException {

        FXMLLoader loader = new FXMLLoader(
                Navegacao.class.getResource(
                        "/com/mecanicacassoni/view/" + arquivo
                )
        );

        Parent root = loader.load();

        Stage stage = (Stage) ((Node) event.getSource())
                .getScene()
                .getWindow();

        stage.setScene(new Scene(root));
    }
}
