package client.model;

import client.ClientApp;
import client.controller.HomeSceneController;
import client.controller.LoginSceneController;
import client.controller.SettingsSceneController;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * @version stabel-1.1.1 | last edit: 18.10.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class SceneAndControllerModel {

    private static SceneAndControllerModel instance;

    private final Scene homeScene, settingsScene, loginScene;
    private final SettingsSceneController settingsController;
    private final LoginSceneController loginController;
    private final HomeSceneController homeController;
    private final Stage addContactStage;

    private Stage mainStage;

    private SceneAndControllerModel() throws IOException {
        FXMLLoader loader = new FXMLLoader();

        loader.setLocation(ClientApp.class.getResource("view/home_scene.fxml"));
        homeScene = new Scene(loader.load());
        homeController = loader.getController();

        loader = new FXMLLoader();
        loader.setLocation(ClientApp.class.getResource("view/settings_scene.fxml"));
        settingsScene = new Scene(loader.load());
        settingsController = loader.getController();

        loader = new FXMLLoader();
        loader.setLocation(ClientApp.class.getResource("view/login_scene.fxml"));
        loginScene = new Scene(loader.load());
        loginController = loader.getController();

        addContactStage = new Stage();
        loader = new FXMLLoader();
        loader.setLocation(ClientApp.class.getResource("view/addContact_scene.fxml"));
        addContactStage.setScene(loader.load());
    }

    public void setMainStage(Stage pStage) {
        mainStage = pStage;
    }

    public LoginSceneController getLoginSceneController()
    {
        return loginController;
    }

    public Scene getLoginScene() {
        return loginScene;
    }

    public SettingsSceneController getSettingsSceneController() {
        return settingsController;
    }

    public Scene getSettingsScene() {
        return settingsScene;
    }

    public HomeSceneController getHomeSceneController()
    {
        return homeController;
    }

    public Scene getHomeScene() {
        return homeScene;
    }

    public Stage getMainStage() {
        return mainStage;
    }

    public Stage getAddContactStage() {
        return addContactStage;
    }

    public static SceneAndControllerModel getInstance() {
        if (instance == null) {
            try {
                instance = new SceneAndControllerModel();
            } catch (IOException e) {
                e.printStackTrace();
                System.exit(1);
            }
        }
        return instance;
    }
}
