package client;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.control.Button;
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
import java.io.ByteArrayInputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Objects;

/**
 * Controller for the GUI home scene
 *
 * @version stabel-1.1.1 | last edit: 01.11.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class  HomeSceneController {

    private ClientBackend backend;

    @FXML
    public Text titleText;

    @FXML
    public TextField messageTextField;

    @FXML
    public ListView<Label> contactsList;

    @FXML
    public ScrollPane messagesScrollpane;

    @FXML
    public Button sendButton;

    HashMap<String, VBox> messageLists;

    @FXML
    public void initialize()
    {
        backend = SceneManager.getBackend();
        messageLists = new HashMap<>();
        contactsList.getSelectionModel().selectedItemProperty().addListener((observableValue, o, t1) -> Platform.runLater(() -> {
            if (t1 != null) {
                switchMessageList((t1).getText());
                t1.setStyle("");
            }
        }));
        messageTextField.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                sendButton.fire();
                event.consume();
            }
        });
    }


    @FXML
    protected void onSendButtonClick(ActionEvent actionEvent)
    {
        Label selectedContact = contactsList.getSelectionModel().getSelectedItem();
        if (selectedContact == null) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "Please select a contact", "Error occurred while sending the message", ButtonType.OK);
            return;
        }

        String lReceiver = selectedContact.getText();
        String lMsg = messageTextField.getText();
        backend.sendMessageToOtherUser(lReceiver, lMsg);
    }

    @FXML
    protected void onAddContactButtonClick(ActionEvent actionEvent) {
        SceneManager.showAddContactWindow();
    }

    protected void clearMessagesAndContacts()
    {
        Platform.runLater(() -> {
            messageLists.clear();
            messagesScrollpane.setContent(null);
            contactsList.getSelectionModel().clearSelection();
            contactsList.getItems().clear();
        });
    }

    @FXML
    public void onFileButtonClick(ActionEvent actionEvent) {
        Label selectedContact = contactsList.getSelectionModel().getSelectedItem();
        if (selectedContact == null) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "Please select a contact", "Error occurred while sending the message", ButtonType.OK);
            return;
        }

        String lReceiver = selectedContact.getText();
        backend.sendFile(lReceiver);
    }

    @FXML
    protected void onAccountButtonClick() {
        if (!backend.getUsername().isEmpty().get())
            SceneManager.switchToSettingsScene();
        else
            SceneManager.switchToLoginScene();
    }

    private void addNewMessageListIfAbsent(String pContact)
    {
        VBox lVBox = new VBox();
        lVBox.setMaxHeight(480);
        messageLists.putIfAbsent(pContact, lVBox);
    }

    private void switchMessageList(String pContact) {
        addNewMessageListIfAbsent(pContact);
        messagesScrollpane.setContent(messageLists.get(pContact));
    }

    private VBox createMessageBox(String pContent, Extention pFileExtention, boolean pReceiving)
    {
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
                    backend.saveFile(((ImageView)event.getSource()).getImage());
                });
            }

            lTextflow.getChildren().add(lImgView);
        }

        lVBox.getChildren().add(lTextflow);
        return lVBox;
    }

    public void showNotification(String pContactName) {
        Label lContactLabel = getContactLabel(pContactName);
        Label lSelectedContact = contactsList.getSelectionModel().getSelectedItem();

        if (lContactLabel != null && (lSelectedContact == null || !lSelectedContact.equals(lContactLabel)))
            lContactLabel.setStyle(
                    "-fx-border-style: hidden hidden hidden solid; -fx-border-color: #6bc490; -fx-border-width: 1.5px;"
            );
    }

    protected void showNewMessage(String pUsername, String pMessage, Extention pFileExtention, boolean pReceived) {
        addNewMessageListIfAbsent(pUsername);
        messageLists.get(pUsername).getChildren().add(createMessageBox(pMessage, pFileExtention, pReceived));
        messagesScrollpane.vvalueProperty().setValue(1);
    }

    private Label getContactLabel(String pUsername)
    {
        ObservableList<Label> lContacts = contactsList.getItems();
        for (Label contact : lContacts) {
            if (contact.getText().equals(pUsername)) return contact;
        }
        return null;
    }

    protected void showNewContact(String pUsername)
    {
        Platform.runLater(() -> {
            if (getContactLabel(pUsername) == null) contactsList.getItems().add(new Label(pUsername));
        });
    }
}
