package client;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Class which contains and handles everything in conjunction with stages and scene switches for the GUI
 *
 * @version v2.0.2 | last edit: 31.08.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class SceneManager extends Application {

    private static Stage stage, addContactStage;
    private static Scene settingsScene, loginScene, homeScene;
    private static SettingsSceneController settingsController;
    private static LoginSceneController loginController;
    private static HomeSceneController homeController;
    private static ClientBackend backend;

    private void setScenes() throws IOException {
        FXMLLoader loader = new FXMLLoader();

        loader.setLocation(SceneManager.class.getResource("home_scene.fxml"));
        homeScene = new Scene(loader.load());
        homeController = loader.getController();

        loader = new FXMLLoader();
        loader.setLocation(SceneManager.class.getResource("settings_scene.fxml"));
        settingsScene = new Scene(loader.load());
        settingsController = loader.getController();

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

    protected static Stage getStage()
    {
        return stage;
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

    protected static void showAlert(Alert.AlertType pType, String pContent, String pHeader, ButtonType... pButtons)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                Alert alert = new Alert(pType, pContent, pButtons);
                alert.setHeaderText(pHeader);
                alert.showAndWait();
            }
        });
    }

    protected static void showAlert(Alert.AlertType pType, String pContent, String pHeader, EventHandler pEventHandler, ButtonType... pButtons)
    {
        Platform.runLater(new Runnable()
        {
            @Override
            public void run()
            {
                Alert alert = new Alert(pType, pContent, pButtons);
                alert.setHeaderText(pHeader);
                alert.setOnCloseRequest(pEventHandler);
                alert.showAndWait();
            }
        });
    }

    protected static ClientBackend getBackend() { return backend;}

    protected static void closeAddContactWindow()
    {
        if (addContactStage != null)
        {
            addContactStage.close();
        }
    }

    protected static void showAddContactWindow()
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run()
            {
                try
                {
                    addContactStage = new Stage();
                    addContactStage.setTitle("Add Contact");
                    Scene scene = new Scene(FXMLLoader.load(SceneManager.class.getResource("addContact_scene.fxml")));
                    addContactStage.setScene(scene);
                    addContactStage.showAndWait();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void start(Stage pStage)
    {
        try
        {
            backend = new ClientBackend();
            backend.listenForServerInput();

            stage = pStage;
            setScenes();
            stage.setScene(homeScene);
            stage.setResizable(false);
            stage.getIcons().add(new Image(SceneManager.class.getResourceAsStream("/images/logo.png")));
            stage.show();
        }
        catch (Exception ex) { ex.printStackTrace();}
    }

    @Override
    public void stop()
    {
        System.exit(0);
    }

    public static void main(String[] args) {
        SceneManager.launch();
    }
}
