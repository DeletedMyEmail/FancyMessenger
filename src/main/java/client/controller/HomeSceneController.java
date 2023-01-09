package client.controller;

import client.ServerController;
import client.Extention;
import client.model.HomeSceneModel;
import client.model.SceneAndControllerModel;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.FileChooser;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Base64;
import java.util.Objects;

/**
 * Controller for the GUI home scene
 *
 * @version stabel-1.1.1 | last edit: 01.11.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class HomeSceneController extends ControllerUtils {

    private final HomeSceneModel model;
    private final ServerController serverController;

    @FXML
    public TextField messageTextField;

    @FXML
    public ListView<Label> contactView;

    @FXML
    public ScrollPane messagesScrollpane;

    public HomeSceneController() {
        serverController = ServerController.getInstance();
        model = HomeSceneModel.getInstance();
    }

    @FXML
    public void initialize() {
        messagesScrollpane = model.getMessagesScrollpane();
        contactView.setItems(model.getContactLabels());
        contactView.getSelectionModel().selectedItemProperty().addListener((observableValue, o, t1) -> Platform.runLater(() -> {
            if (t1 != null) {
                switchMessageList((t1).getText());
                t1.setStyle("");
            }
        }));

        messageTextField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                onSendButtonClick(null);
                event.consume();
            }
        });
    }


    @FXML
    public void onSendButtonClick(ActionEvent pActionEvent) {
        Label selectedContact = contactView.getSelectionModel().getSelectedItem();
        if (selectedContact == null) {
            showAlert(Alert.AlertType.ERROR, "Please select a contact", "Error occurred while sending the message", ButtonType.OK);
            return;
        }

        model.getMessageToSend().set(selectedContact.getText() + ";;" + messageTextField.getText());
    }

    @FXML
    public void onAddContactButtonClick(ActionEvent actionEvent) {
        SceneAndControllerModel.getInstance().getAddContactStage().showAndWait();
    }

    public void clearMessagesAndContacts()
    {
        Platform.runLater(() -> {
            model.getMessageLists().clear();
            model.getMessagesScrollpane().setContent(null);
            contactView.getSelectionModel().clearSelection();
            contactView.getItems().clear();
        });
    }

    @FXML
    public void onFileButtonClick(ActionEvent actionEvent) {
        Label selectedContact = contactView.getSelectionModel().getSelectedItem();
        if (selectedContact == null) {
            showAlert(Alert.AlertType.ERROR, "Please select a contact", "Error occurred while sending the message", ButtonType.OK);
            return;
        }

        FileChooser lChooser = new FileChooser();
        lChooser.setTitle("Choose a file");
        lChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Files", "*.png", "*.jpg", "*.jpeg", "*.gif", "*.pdf", "*.txt")
        );
        File lFile = lChooser.showOpenDialog(SceneAndControllerModel.getInstance().getMainStage());

        serverController.sendFile(lFile, selectedContact.getText());
    }

    @FXML
    protected void onAccountButtonClick() {
        SceneAndControllerModel lModel = SceneAndControllerModel.getInstance();

        if (!serverController.getUsername().isEmpty().get()) {
            lModel.getMainStage().setScene(lModel.getSettingsScene());
        }
        else {
            lModel.getMainStage().setScene(lModel.getLoginScene());
        }
    }

    private void addNewMessageListIfAbsent(String pContact) {
        VBox lVBox = new VBox();
        lVBox.setMaxHeight(480);
        model.getMessageLists().putIfAbsent(pContact, lVBox);
    }

    private void switchMessageList(String pContact) {
        addNewMessageListIfAbsent(pContact);
        model.getMessagesScrollpane().setContent(model.getMessageLists().get(pContact));
    }

    private VBox createMessageBox(String pContent, Extention pFileExtention, boolean pReceiving) {
        VBox lVBox = new VBox();
        lVBox.setPrefWidth(860);
        lVBox.setPadding(new Insets(3));
        VBox.setMargin(lVBox, new Insets(0, 0, 15, 0));
        TextFlow lTextflow = new TextFlow();
        Text lSentOrReceived = new Text();


        if (pReceiving) {
            lVBox.setStyle("-fx-background-color: #c7c9c7; -fx-margin-bottom: 10px;");
            lSentOrReceived.setText("Received: ");
        }
        else {
            lVBox.setStyle("-fx-background-color: #d6f4cd;");
            lSentOrReceived.setText("Sent: ");
        }
        lSentOrReceived.setFont(Font.font("System", FontWeight.BOLD, 15));
        lTextflow.getChildren().add(lSentOrReceived);

        if (pFileExtention == Extention.NONE)
        {
            Text lText = new Text();
            lText.setFont(Font.font("System", FontWeight.NORMAL, 14));
            lText.setText(pContent);
            lTextflow.getChildren().add(lText);
        }
        else
        {
            ImageView lImgView = new ImageView();
            lImgView.setCache(true);
            lImgView.setPreserveRatio(true);

            if (pFileExtention == Extention.TXT || pFileExtention == Extention.PDF)
            {
                Image lImg = new Image(Objects.requireNonNull(HomeSceneController.class.getResourceAsStream(
                        "/images/" + pFileExtention.name().toLowerCase() + ".png")));
                lImgView.setImage(lImg);
                lImgView.setFitHeight(50);
                lImgView.setFitWidth(50);
            }
            else if (pFileExtention == Extention.UNKNOWN)
                lImgView.setImage(new Image(Objects.requireNonNull(HomeSceneController.class.getResourceAsStream("/images/unknown.png"))));
            else
            {
                byte[] lImageBytes = Base64.getDecoder().decode(pContent);
                Image lImg = new Image(new ByteArrayInputStream(lImageBytes));
                lImgView.setImage(lImg);

                if (lImg.getHeight() > 500 || lImg.getWidth() > 840) {
                    lImgView.setFitHeight(500);
                    lImgView.setFitWidth(840);
                }

                lImgView.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
                    serverController.saveFile(((ImageView)event.getSource()).getImage());
                });
            }

            lTextflow.getChildren().add(lImgView);
        }

        lVBox.getChildren().add(lTextflow);
        return lVBox;
    }

    public void showNotification(String pContactName) {
        Label lContactLabel = getContactLabel(pContactName);
        Label lSelectedContact = contactView.getSelectionModel().getSelectedItem();

        if (lContactLabel != null && (lSelectedContact == null || !lSelectedContact.equals(lContactLabel)))
            lContactLabel.setStyle(
                    "-fx-border-style: hidden hidden hidden solid; -fx-border-color: #6bc490; -fx-border-width: 1.5px;"
            );
    }

    public void showNewMessage(String pUsername, String pMessage, Extention pFileExtention, boolean pReceived) {
        addNewMessageListIfAbsent(pUsername);
        model.getMessageLists().get(pUsername).getChildren().add(createMessageBox(pMessage, pFileExtention, pReceived));
        model.getMessagesScrollpane().vvalueProperty().setValue(1);
    }

    private Label getContactLabel(String pUsername)
    {
        ObservableList<Label> lContacts = contactView.getItems();
        for (Label contact : lContacts) {
            if (contact.getText().equals(pUsername)) return contact;
        }
        return null;
    }

    public void showNewContact(String pUsername)
    {
        Platform.runLater(() -> {
            if (getContactLabel(pUsername) == null) contactView.getItems().add(new Label(pUsername));
        });
    }
}
