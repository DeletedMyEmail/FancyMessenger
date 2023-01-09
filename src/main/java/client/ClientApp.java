package client;

import client.model.SceneAndControllerModel;
import javafx.application.Application;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;

public class ClientApp extends Application {

    private final SceneAndControllerModel model;

    public ClientApp() throws IOException {
        model = SceneAndControllerModel.getInstance();
    }

    @Override
    public void start(Stage pStage) {
        try {
            model.setMainStage(pStage);
            pStage.setScene(model.getHomeScene());
            pStage.setResizable(false);
            pStage.getIcons().add(new Image(SceneAndControllerModel.class.getResourceAsStream("/images/logo.png")));
            pStage.show();

            ServerController.getInstance().listenForServerInput();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void stop()
    {
        System.exit(0);
    }

    public static void main(String[] args) {
        launch();
    }
}
