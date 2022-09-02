package client;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;

/**
 * Controller for the GUI home scene
 *
 * @version v2.0.0 | last edit: 24.08.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class HomeSceneController {

    private ClientBackend backend;

    @FXML
    public Text titleText;

    @FXML
    public TextField messageTextField;

    @FXML
    public ListView contactsList;

    @FXML
    public ScrollPane messagesScrollpane;

    HashMap<String, VBox> messageLists;

    private void switchMessageList(String pContact) {
        messageLists.putIfAbsent(pContact, new VBox());
        messagesScrollpane.setContent(messageLists.get(pContact));
    }

    @FXML
    public void initialize()
    {
        backend = SceneManager.getBackend();
        messageLists = new HashMap<>();
        contactsList.getSelectionModel().selectedItemProperty().addListener((observableValue, o, t1) -> Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (t1 != null) switchMessageList(((Text)t1).getText());
            }
        }));
    }

    @FXML
    protected void onAccountButtonClick()
    {
        if (!backend.getUsername().equals("")) SceneManager.switchToSettingsScene();
        else SceneManager.switchToLoginScene();
    }

    private VBox createMessageBox(String pContent, boolean pReceiving)
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

        if (pContent.startsWith("[image]")) {
            byte[] lImageBytes = Base64.getDecoder().decode(pContent.substring(7));
            Image lImg = new Image(new ByteArrayInputStream(lImageBytes));
            ImageView lImgView = new ImageView(lImg);
            lImgView.setCache(true);
            lImgView.setPreserveRatio(true);

            if (lImg.getHeight() > 500 || lImg.getWidth() > 840) {
                lImgView.setFitHeight(500);
                lImgView.setFitWidth(840);
            }

            lTextflow.getChildren().add(lImgView);
        }
        else {
            Text lText = new Text();
            lText.setFont(Font.font("System", FontWeight.NORMAL, 14));
            lText.setText(pContent);
            lTextflow.getChildren().add(lText);
        }
        lVBox.getChildren().add(lTextflow);
        return lVBox;
    }

    protected void showNewMessage(String pAuthor, String pMessage, boolean pReceived) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                if (messageLists.get(pAuthor) == null) messageLists.put(pAuthor, new VBox());
                messageLists.get(pAuthor).getChildren().add(createMessageBox(pMessage, pReceived));
            }
        });
    }

    protected void showNewContact(String pUsername)
    {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                contactsList.getItems().add(new Text(pUsername));
            }
        });
    }

    @FXML
    protected void onSendButtonClick(ActionEvent actionEvent) throws IOException
    {
        Text selectedContact = ((Text)contactsList.getSelectionModel().getSelectedItem());
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
        messageLists.clear();
        messagesScrollpane.setContent(null);
        contactsList.getSelectionModel().clearSelection();
        contactsList.getItems().clear();
    }

    @FXML
    public void onFileButtonClick(ActionEvent actionEvent) {
        Text selectedContact = ((Text)contactsList.getSelectionModel().getSelectedItem());
        if (selectedContact == null) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "Please select a contact", "Error occurred while sending the message", ButtonType.OK);
            return;
        }

        String lReceiver = selectedContact.getText();
        backend.fileButtonClick(lReceiver);
    }

}
