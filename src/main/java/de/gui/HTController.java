package de.gui;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class HTController {

    @FXML
    private Button show_login_button;

    @FXML
    private Label top_label;

    public void onShowLoginButtonClick(ActionEvent actionEvent) {
        show_login_button.setStyle("-fx-background-color: #873");
        top_label.setText("Test");
    }
}