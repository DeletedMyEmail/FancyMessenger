package de.clientside;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class GUIController {

    private static boolean logedIn = false;

    private ClientBackend backend;

    @FXML
    public Text usernameText;

    @FXML
    public TextField usernameField;

    @FXML
    public PasswordField passwordField;

    @FXML
    public void initialize() throws IOException {
        init();
    }

    protected void init() throws IOException
    {
        new Thread() {
            @Override
            public void run() {
                new ClientBackend().run();
            }
        }.start();
    }


    protected static void setLoginState(boolean bool)
    {
        logedIn = bool;
    }

    @FXML
    public void switchToAccScene() throws IOException
    {

}

    @FXML
    public void switchToMainScene(ActionEvent event) throws IOException {

    }

    public void onAccountButtonClick(ActionEvent actionEvent) {

    }

    public void onLoginButtonClick(ActionEvent actionEvent) throws IOException {
        ClientBackend.sendToServer("KMES;login;"+usernameField.getText()+";"+passwordField.getText());
    }

}
