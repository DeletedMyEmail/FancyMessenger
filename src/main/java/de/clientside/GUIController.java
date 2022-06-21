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

    private static Scene loginScene = null;
    private static Scene settingsScene = null;
    private static Scene mainScene = null;

    private static Stage stage;

    private static boolean logedIn = false;

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

    protected void init() throws IOException {
    }

    protected static void setScenes() throws IOException {
        stage =
        loginScene = new Scene(FXMLLoader.load(Objects.requireNonNull(GUIController.class.getResource("login_scene.fxml"))));
        settingsScene = new Scene(FXMLLoader.load(Objects.requireNonNull(GUIController.class.getResource("settings_scene.fxml"))));
        mainScene = ;
    }

    protected static void setLoginState(boolean bool)
    {
        logedIn = bool;
    }

    @FXML
    public void switchToAccScene() throws IOException
    {
        if (logedIn)
        {
            stage.setScene(settingsScene);
            usernameText.setText("Current User"+ClientBackend.getUsername());
        }
        else
        {
            stage.setScene(loginScene);
        }
        stage.show();
}

    @FXML
    public void switchToMainScene(ActionEvent event) throws IOException {
        stage.setScene(mainScene);
        stage.show();
    }

    public void onAccountButtonClick(ActionEvent actionEvent) {

    }

    public void onLoginButtonClick(ActionEvent actionEvent) throws IOException {
        ClientBackend.setLastActionEvent(actionEvent);
        ClientBackend.sendToServer("KMES;login;"+usernameField.getText()+";"+passwordField.getText());
    }

}
