package client.controller;

import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

public class ControllerUtils {

    public void showAlert(Alert.AlertType pType, String pContent, String pHeader, ButtonType... pButtons)
    {
        Platform.runLater(() -> {
            Alert alert = new Alert(pType, pContent, pButtons);
            alert.setHeaderText(pHeader);
            alert.showAndWait();
        });
    }

    public void showAlert(Alert.AlertType pType, String pContent, String pHeader, EventHandler pEventHandler, ButtonType... pButtons)
    {
        Platform.runLater(() -> {
            Alert alert = new Alert(pType, pContent, pButtons);
            alert.setHeaderText(pHeader);
            alert.setOnCloseRequest(pEventHandler);
            alert.showAndWait();
        });
    }
}
