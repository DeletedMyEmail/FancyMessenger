package client;

// Own Library https://github.com/KaitoKunTatsu/KLibrary
import KLibrary.Utils.EncryptionUtils;

import javafx.embed.swing.SwingFXUtils;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.stage.FileChooser;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.*;
import javax.imageio.ImageIO;
import java.util.List;

/**
 * Client backend for KMes Messenger<br/>
 * Handles input from the KMes Server
 *
 * @version v2.1.0 | last edit: 03.09.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
public class ClientBackend {

    // Hostname/IP from KMes Server
    private static final String HOST = "localhost";

    // Port of the KMes Server which accepts clients
    private static final int PORT = 4242;

    private final HashMap<String, List<String>> messages;
    private final EncryptionUtils encryptionUtils;

    // connection to KMes Server
    private Socket server;
    private DataInputStream inStream;
    private DataOutputStream outStream;
    private SecretKey AESKey;

    // private BufferedReader reader;
    // private BufferedWriter writer;

    private String currentUser = "";

    /**
     * Creates a ClientBackend instance, iniitializes attributes and transfers old saved messages into a HashMap
     * in conjunction with the receiver/sender
     * */
    public ClientBackend() {
        messages = new HashMap<>();
        encryptionUtils = new EncryptionUtils();
        /*
        reader = new BufferedReader(new FileReader("src/main/contacts.txt"));
        writer = new BufferedWriter(new FileWriter("src/main/contacts.txt"));

        contacts = new ArrayList<>();
        String in;
        while((in = reader.readLine()) != null)
        {
            //TODO
        }*/
    }

    /**
     * @param pUsername Name of the user whose sent/received messages are to be returned
     * @return Returns a list of strings including all sent messages with the specified user
     * */
    protected List<String> getMessagesForUser(String pUsername)
    {
        return messages.get(pUsername);
    }

    /**
     * @return Returns the username from the current user or an empty string if not logged in.
     * */
    protected String getUsername() { return currentUser; }

    /**
     * Logs out the current user.<br/>
     * Both server and client will no longer associate a specific user with this socket.
     * */
    protected void logOut() {
        try
        {
            sendToServer("logout");
            messages.clear();
            SceneManager.getHomeScene().clearMessagesAndContacts();
            currentUser = "";
        }
        catch (IOException ex)
        {
            SceneManager.showAlert(Alert.AlertType.ERROR, "", "Can't reach the KMesServer", ButtonType.OK);
        }
    }

    /**
     * @return Returns the connection status to the KMes Server
     * */
    protected boolean isConnected()
    {
        return server != null && !server.isClosed();
    }

    /**
     * Updates the current user, the GUI and loads the account settings scene
     *
     * @param pUsername Username of the new logged-in user
     * */
    private void updateCurrentUser(String pUsername) {
        currentUser = pUsername;
        SceneManager.getSettingsScene().changeUsernameText(currentUser);
        SceneManager.switchToSettingsScene();
    }

    /**
     * Adds a new message to the list of messages with a specific user.
     * If there is no such list in the HashMap in conjunction with this user, a new one will be added.
     *
     * @param pUser Name of the user who sent/received this message
     * @param pMessage Message content
     * */
    protected void addNewMessage(String pUser, String pMessage)
    {
        if (messages.get(pUser) == null)
        {
            messages.put(pUser, new ArrayList<>() {{
                add(pMessage);
            }});
            SceneManager.getHomeScene().showNewContact(pUser);
        }
        else
        {
            messages.get(pUser).add(pMessage);
        }

        if (pMessage.startsWith("Received: ")) SceneManager.getHomeScene().showNewMessage(pUser, pMessage.substring(10), true);
        else SceneManager.getHomeScene().showNewMessage(pUser, pMessage.substring(6), false);
    }

    /**
     * Creates a new Thread which listens for server inputs and handles them
     * */
    protected void listenForServerInput () {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                try {
                    while (true) // do not criticize obvious stupidity
                    {
                        // Connects to the KMes Server and iniitializes attributes for communication
                        server = new Socket();
                        server.connect(new InetSocketAddress(HOST, PORT), 4000);
                        outStream = new DataOutputStream(server.getOutputStream());
                        inStream = new DataInputStream(server.getInputStream());

                        establishEncryption();

                        // Handles inputs as long as the connection exists
                        while (isConnected())
                        {
                            String[] lInput = readFromServer().split(";;");

                            switch (lInput[0]) {
                                case "loggedIn" -> updateCurrentUser(lInput[1]);
                                case "error" -> SceneManager.showAlert(Alert.AlertType.ERROR, lInput[1], lInput[2], ButtonType.OK);
                                case "message" -> addNewMessage(lInput[1], "Received: " + lInput[2]);
                                case "userExists" -> {
                                    messages.computeIfAbsent(lInput[1], k -> new ArrayList<>());
                                    SceneManager.getHomeScene().showNewContact(lInput[1]);
                                    SceneManager.showAlert(Alert.AlertType.CONFIRMATION, "Successfully added" +
                                            " new contact: " + lInput[1], "New contact", ButtonType.OK);
                                }
                            }
                        }
                    }
                }
                catch (SocketTimeoutException | SocketException socketException)
                {
                    socketException.printStackTrace();
                    SceneManager.showAlert(Alert.AlertType.ERROR, "",
                            "Connection to the KMesServer couldn't be established",
                            event -> System.exit(0), ButtonType.OK);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();

    }

    /**
     * Sends a string to the KMes Server
     *
     * @param pMessage Message to be sent
     * */
    protected void sendToServer(String pMessage) throws IOException
    {
        try
        {
            byte[] lEncrpytedMessage = EncryptionUtils.encryptAES(pMessage, AESKey);
            byte[] lMessageSizeAsBytes = ByteBuffer.allocate(4).putInt(lEncrpytedMessage.length).array();
            byte[] lConcatenated = ByteBuffer.allocate(lMessageSizeAsBytes.length+lEncrpytedMessage.length).put(lMessageSizeAsBytes).put(lEncrpytedMessage).array();

            outStream.write(lConcatenated, 0 , lConcatenated.length);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String readFromServer() throws IOException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        int lSize = inStream.readInt();
        byte[] lEncryptedInput = new byte[lSize];
        inStream.readFully(lEncryptedInput);
        return EncryptionUtils.decryptAES(lEncryptedInput, AESKey);
    }

    private void establishEncryption() throws IOException, InvalidKeySpecException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        // RSA
        outStream.write(encryptionUtils.getPublicKey().getEncoded());
        byte[] lKeyBytes = new byte[294];
        inStream.read(lKeyBytes);
        PublicKey lServerRSAKey = EncryptionUtils.decodeRSAKey(lKeyBytes);

        // AES
        AESKey = EncryptionUtils.generateSymmetricKey();
        byte[] lEncryptedAESKey = encryptionUtils.encryptRSA(AESKey.getEncoded(), lServerRSAKey);
        outStream.write(lEncryptedAESKey);
    }

    public void sendMessageToOtherUser(String pReceiver, String pMessage)
    {
        try
        {
            char[] lMessage = pMessage.toCharArray();
            if (lMessage[0] == ';') lMessage[0] = ',';
            if (lMessage[lMessage.length-1] == ';') lMessage[lMessage.length-1] = ',';

            sendToServer("send;;"+pReceiver+";;"+String.valueOf(lMessage));
            addNewMessage(pReceiver, "Sent: "+String.valueOf(lMessage));
        }
        catch (UTFDataFormatException sizeEx) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "", "Input is too large", ButtonType.OK);
        }
        catch (IOException ioEx) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "", "Can't reach the KMes Server", ButtonType.OK);
        }
    }

    public void sendFileButtonClick(String pReceiver) {
        FileChooser lChooser = new FileChooser();
        lChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        lChooser.setTitle("Choose an image");
        File lFile = lChooser.showOpenDialog(SceneManager.getStage());

        if (lFile == null) return;

        try {
            FileInputStream lFileStream = new FileInputStream(lFile);
            byte[] lImageBytes = new byte[(int) lFile.length()];
            lFileStream.read(lImageBytes);
            lFileStream.close();
            sendMessageToOtherUser(pReceiver, "[image]"+Base64.getEncoder().encodeToString(lImageBytes));
        }
        catch (IOException ex) {
            SceneManager.showAlert(Alert.AlertType.ERROR, "", "Can not convert this file");
        }
    }

    public void saveFile(Image pImage) {
        FileChooser lChooser = new FileChooser();
        lChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif"));
        File lFileToSaveTo = lChooser.showSaveDialog(SceneManager.getStage());
        if (lFileToSaveTo == null) SceneManager.showAlert(Alert.AlertType.ERROR, "", "Please select a file");
        else
        {
            try
            {
                lFileToSaveTo.createNewFile();
                BufferedImage lBufferedImage = SwingFXUtils.fromFXImage(pImage, null);
                BufferedImage imageRGB = new BufferedImage(lBufferedImage.getWidth(), lBufferedImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
                imageRGB.createGraphics().drawImage(lBufferedImage, 0, 0, null);

                String lFileExtension = lFileToSaveTo.getName();
                lFileExtension = lFileExtension.substring(lFileExtension.lastIndexOf('.')+1);

                ImageIO.write(imageRGB, lFileExtension, lFileToSaveTo);
            }
            catch (IOException ioEx) {
                SceneManager.showAlert(Alert.AlertType.ERROR, "", "Error occured while saving");
            }
            catch (SecurityException secEx) {
                SceneManager.showAlert(Alert.AlertType.ERROR, "", "Access denied");
            }
        }
    }
}
