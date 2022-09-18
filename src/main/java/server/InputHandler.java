package server;

// Own Library https://github.com/KaitoKunTatsu/KLibrary
import KLibrary.Utils.EncryptionUtils;
import KLibrary.Utils.SQLUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.net.SocketTimeoutException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * this class processes the requests of a client socket in a new thread
 *
 * @version v3.0.0 | last edit: 18.09.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 *
 * @see SocketWrapper
 * */
class InputHandler extends Thread {

    private final SQLUtils sqlUtils;
    private final SocketWrapper client;
    private final HashMap<String, SocketWrapper> allConnectedClients;
    private final HashMap<String, List<String>> queuedMessages;

    private String currentUser;
    private boolean running;

    /**
     * Creates an instance of this class, initializes utility classes like {@link SQLUtils} and connets the database
     *
     * @param pClient               {@link SocketWrapper} with the client socket whose request will be processed
     * @param pAllConnectedClients  {@link HashMap} containing all users connected to the server and linked to their username
     * @param pQueuedMessages       {@link HashMap} containing all messages which couldn't be sent due to user inactivity
     *
     * @see SocketAcceptor
     * */
    public InputHandler(SocketWrapper pClient, HashMap<String, SocketWrapper> pAllConnectedClients, HashMap<String, List<String>> pQueuedMessages) throws SQLException {
        this.allConnectedClients = pAllConnectedClients;
        this.queuedMessages = pQueuedMessages;
        this.sqlUtils = new SQLUtils("src/main/resources/kmes_server.db");
        this.client = pClient;
    }

    /**
     *  Reads and processes client inputs like:<br>
     *  - login<br>
     *  - registration<br>
     *  - logout<br>
     *  - send message<br>
     *  - check other user's existance<br>
     *  requests
     * */
    @Override
    public void run() {
        running = true;
        while (running)
        {
            if (client.isClosed()) {
                running = false;
                break;
            }

            try
            {
                String lInput = client.readAES();

                String[] lRequest = lInput.split(";;");
                switch (lRequest[0])
                {
                    case "login" -> handleLoginRequest(lRequest[1], lRequest[2]);
                    case "register" -> handleRegistrationRequest(lRequest[1], lRequest[2]);
                    case "send" -> handleSendRequest(lRequest[1], lRequest[2]);
                    case "logout" ->
                            {
                                currentUser = "";
                                client.writeAES("loggedOut");
                            }
                    case "doesUserExist" ->
                            {
                                String lUsername = lRequest[1];
                                if (userExists(lUsername)) client.writeAES("userExists;;"+lUsername);
                                else client.writeAES("error;; ;;User \""+lUsername+"\" does not exist");
                            }
                    case "Unable to decrypt message" -> client.writeAES( "error;; ;;Your message was not properly encrypted");
                    default -> client.writeAES("error;; ;;Unknown input");
                }

            }
            catch (SocketTimeoutException ignored) {}
            catch (IndexOutOfBoundsException ioobEx)
            {
                try {
                    client.writeAES("error;; ;;Missing argument");
                } catch (IOException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
                    e.printStackTrace();
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                client.close();
                running = false;
                break;
            }

        }
    }

    /**
     * @param pReveiver Name of the user to be messaged
     * @param pMessage  Message to be sent
     * */
    private void handleSendRequest(String pReveiver, String pMessage) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidKeyException {
        if (!userExists(pReveiver))
            client.writeAES("error;;User does not exist;;Couldn't send message");
        else if (pReveiver.equals(currentUser))
            client.writeAES("error;;You can't text yourself;;Couldn't send message");
        else if (currentUser.equals(""))
            client.writeAES("error;;You have to be logged in before sending messages;;Couldn't send message");
        else
        {
            SocketWrapper lReceiverWrapper = getWrapper(pReveiver);
            if (lReceiverWrapper == null) queueMessage(pReveiver, pMessage);
            else
            {
                lReceiverWrapper.writeAES("message;;"+
                        currentUser+";;"+pMessage);
            }
        }
    }

    /**
     * Notifies the user if the entered username or password is invalid, otherwise creates a new user in the database
     *
     * @param pUsername Username of the user to be registered
     * @param pPassword User's new password
     * */
    private void handleRegistrationRequest(String pUsername, String pPassword) throws IOException, SQLException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        if (!currentUser.equals("")) {
            client.writeAES("error;;Log out before registration;;Registration failed");
        }
        else
        {
            if (!userExists(pUsername))
            {
                try
                {
                    byte[] lSalt = EncryptionUtils.generateSalt();
                    String lHashedPassword = EncryptionUtils.getHash(pPassword, lSalt);
                    sqlUtils.onExecute("INSERT INTO User VALUES(?, ?, ?)", pUsername, lHashedPassword, lSalt);
                    client.writeAES("loggedIn;;"+pUsername);
                    changeBindingWithCurrentUser();
                    sendQueuedMessages();
                }
                catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                    client.writeAES("error;;Couldn't encrypt your password;;Error occurred during registration process");
                }
            }
            else
            {
                client.writeAES("error;;This username is already taken, please choose another one;;Registration failed");
            }
        }
    }

    /**
     * Notifies the user if the entered username or password is invalid, otherwise links this socket to the entered username
     *
     * @param pUsername Username
     * @param pPassword User's password
     * */
    private void handleLoginRequest(String pUsername, String pPassword) throws IOException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        byte[] lSalt;
        try {
            lSalt = sqlUtils.onQuery("SELECT salt FROM User WHERE username=?", pUsername).getBytes(1);
        } catch (SQLException sqlEx) {
            client.writeAES("error;;User not found;;Login failed");
            return;
        }

        if (verifyLogin(pUsername, pPassword, lSalt))
        {
            currentUser = pUsername;
            client.writeAES("loggedIn;;"+pUsername);
            changeBindingWithCurrentUser();
            sendQueuedMessages();
        }
        else {
            client.writeAES("error;;Password or username incorrect;;Login failed");
        }
    }

    /**
     * Queues a message which will be sent if the user gets back online
     *
     * @param pReveiver Name of the user to be messaged
     * @param pMessage  Message to be sent
     * */
    private void queueMessage(String pReveiver, String pMessage)
    {
        queuedMessages.putIfAbsent(pReveiver, new ArrayList<>());
        queuedMessages.get(pReveiver).add(currentUser+";;"+pMessage);
    }

    /**
     * Looks for queued messages linked to the user currently connected with this socket and sends them
     * */
    private void sendQueuedMessages() throws InvalidAlgorithmParameterException, IllegalBlockSizeException, IOException, BadPaddingException, InvalidKeyException {
        List<String> lMessages = queuedMessages.get(currentUser);
        if (lMessages == null) return;
        while (lMessages.size() != 0) {
            System.out.println(lMessages.size());
            client.writeAES("message;;"+lMessages.get(0));
            lMessages.remove(0);
        }
    }

    /**
     * Validates login credentials
     *
     * @param pUsername Username
     * @param pPassword Unhashed password - will be hashed with the corresponding salt from the database
     * */
    private boolean verifyLogin(String pUsername, String pPassword)
    {
        byte[] lSalt;
        try {
            lSalt = sqlUtils.onQuery("SELECT salt FROM User WHERE username=?", pUsername).getBytes(1);
        }
        catch (SQLException sqlEx) { return false;}

        return verifyLogin(pUsername, pPassword, lSalt);
    }

    /**
     * Validates login credentials
     *
     * @param pUsername Username
     * @param pPassword Unhashed password
     * @param pSalt     User's salt used to hash the password
     * */
    private boolean verifyLogin(String pUsername, String pPassword, byte[] pSalt)
    {
        try
        {
            ResultSet lResultSet = sqlUtils.onQuery("SELECT hashedPassword FROM User WHERE username=?", pUsername);

            if (!lResultSet.isClosed() && lResultSet.next()) {
                return EncryptionUtils.validate(pPassword, lResultSet.getString("hashedPassword"), pSalt);
            }
            return false;

        }
        catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Validates the existence of a database entry for a specific user
     * @param pUsername Name of the user
     * */
    private boolean userExists(String pUsername)
    {
        try {
            ResultSet lResultSet = sqlUtils.onQuery("SELECT username FROM User WHERE username=?", pUsername);
            return !(lResultSet.isClosed() || lResultSet.getString(1).equals(""));
        }
        catch (SQLException ex)
        {
            return false;
        }
    }

    /**
     * Links this socket to the current user and logs out old connections
     * */
    private void changeBindingWithCurrentUser()
    {
        SocketWrapper lCurrentBindedWrapperToThisUsername = allConnectedClients.get(currentUser);
        if (lCurrentBindedWrapperToThisUsername != null) {
            try {
                lCurrentBindedWrapperToThisUsername.writeAES("loggedOut");
            }
            catch (InvalidAlgorithmParameterException | IOException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException ignored) {ignored.printStackTrace();}
        }
        allConnectedClients.put(currentUser, client);
    }

    /**
     * @return {@link SocketWrapper} for a specific user
     * */
    private SocketWrapper getWrapper(String pUsername) { return allConnectedClients.get(pUsername); }

    public void stopListening() {running=false;}
}
