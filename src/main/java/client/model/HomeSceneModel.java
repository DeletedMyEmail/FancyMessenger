package client.model;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableStringValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

public class HomeSceneModel {

    private static HomeSceneModel instance;

    private final ObservableList<Label> contactLabels;
    private final ObservableMap<String, VBox> messageLists;
    private final SimpleStringProperty messageToSend;
    private ScrollPane messagesScrollpane;

    private HomeSceneModel() {
        super();
        messageLists = FXCollections.observableHashMap();
        contactLabels = FXCollections.observableArrayList();
        messageToSend = new SimpleStringProperty("");
    }

    public void setMessagesScrollpane(ScrollPane messagesScrollpane) {
        this.messagesScrollpane = messagesScrollpane;
    }

    public ScrollPane getMessagesScrollpane() {
        return messagesScrollpane;
    }

    public ObservableMap<String, VBox> getMessageLists() {
        return messageLists;
    }

    public ObservableList<Label> getContactLabels() {
        return contactLabels;
    }

    public static HomeSceneModel getInstance() {
        if (instance == null) {
            instance = new HomeSceneModel();
        }
        return instance;
    }

    public SimpleStringProperty getMessageToSend() {
        return messageToSend;
    }
}
