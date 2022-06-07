package com.example.healthtracker;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Background;

public class HTController {

    @FXML
    private Button analyticsButton;

    public void onAnalyticsButtonClick(ActionEvent actionEvent) {
        analyticsButton.setStyle("-fx-background-color: #112");
    }
}