package de.clientside;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Class which contains all scenes and stages for the GUI
 *
 * @version 22.06.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class SceneManager extends Application {

    private static Stage stage;
    private static Scene settingsScene, loginScene, homeScene;
    private static SettingsSceneController settingsController;
    private static LoginSceneController loginController;
    private static HomeSceneController homeController;
    private static ClientBackend backend;

    private void setScenes() throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(SceneManager.class.getResource("settings_scene.fxml"));
        settingsScene = new Scene(loader.load());
        settingsController = loader.getController();

        loader = new FXMLLoader();
        loader.setLocation(SceneManager.class.getResource("home_scene.fxml"));
        homeScene = new Scene(loader.load());
        homeController = loader.getController();

        loader = new FXMLLoader();
        loader.setLocation(SceneManager.class.getResource("login_scene.fxml"));
        loginScene = new Scene(loader.load());
        loginController = loader.getController();
    }

    protected static LoginSceneController getLoginScene()
    {
        return loginController;
    }

    protected static SettingsSceneController getSettingsScene()
    {
        return settingsController;
    }

    protected static HomeSceneController getHomeScene()
    {
        return homeController;
    }

    protected static void switchToSettingsScene()
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                stage.setScene(settingsScene);
                stage.show();
            }
        });
    }

    protected static void switchToLoginScene()
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                stage.setScene(loginScene);
                stage.show();
            }
        });
    }

    protected static void switchToHomeScene()
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                stage.setScene(homeScene);
                stage.show();
            }
        });
    }

    protected static void showError(Alert.AlertType pType, String pErrorMessage, ButtonType... pButtons)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                Alert alert = new Alert(pType, pErrorMessage, pButtons);
                alert.showAndWait();
            }
        });
    }

    protected static ClientBackend getBackend() { return backend;}

    @Override
    public void start(Stage pStage) throws Exception {
        backend = new ClientBackend();
        backend.listenForServerInput();
        stage = pStage;
        setScenes();
        stage.setScene(homeScene);
        stage.show();
    }

    public static void main(String[] args) {
        SceneManager.launch();
    }
}
