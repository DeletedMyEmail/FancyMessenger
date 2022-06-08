package de.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class HTController {

    @FXML
    private Button show_login_button;

    public void onShowLoginButtonClick(ActionEvent actionEvent) {
        show_login_button.setStyle("-fx-background-color: #873");
    }
}