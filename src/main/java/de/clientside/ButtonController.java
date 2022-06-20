package de.clientside;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.IOException;

public class ButtonController {

    private static Stage stage;
    private static Scene scene;
    private static Parent root;
    private static boolean logedIn = false;

    @FXML
    public Text usernameText;

    @FXML
    public TextField usernameField;

    @FXML
    public PasswordField passwordField;

    @FXML
    public void initialize()
    {
        init();
    }

    protected void init()
    {

    }

    protected static void setLoginState(boolean bool)
    {
        logedIn = bool;
    }

    private void switchScene(ActionEvent event, String pXMLFileName) throws IOException {
        root = FXMLLoader.load(ButtonController.class.getResource(pXMLFileName));
        stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        scene = new Scene(root);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    public void switchToAccScene(ActionEvent event) throws IOException {
        String filename;
        if (logedIn)
        {
            filename = "settings_scene.fxml";
        }
        else
        {
            filename = "login_scene.fxml";
        }

        switchScene(event, filename);

        if (logedIn)
        {
            usernameText.setText("Current User"+ClientBackend.getUsername());
        }
    }

    @FXML
    public void switchToMainScene(ActionEvent event) throws IOException {
        switchScene(event, "kmes_main.fxml");
    }

    public void onAccountButtonClick(ActionEvent actionEvent) {

    }

    public void onLoginButtonClick(ActionEvent actionEvent) throws IOException {
        ClientBackend.setLastActionEvent(actionEvent);
        ClientBackend.sendToServer("KMES;login;"+usernameField.getText()+";"+passwordField.getText());
    }

}
