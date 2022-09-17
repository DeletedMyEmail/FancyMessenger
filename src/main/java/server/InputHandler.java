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
 * This class acts as a backend for the KMes Server and processes all inputs from user sockets.
 *
 * @version v3.0.0 | last edit: 16.09.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
class InputHandler extends Thread {

    private final SQLUtils sqlUtils;
    private final SocketWrapper client;
    private final HashMap<String, SocketWrapper> allConnectedClients;
    private final HashMap<String, List<String>> queuedMessages;

    private String currentUser;
    private boolean running;

    /**
     *
     * @param pAllConnectedClients
     *
     * @param pClient               {@link SocketWrapper}
     *
     * @see EncryptionUtils
     * @see SQLUtils
     * */
    protected InputHandler(SocketWrapper pClient, HashMap<String, SocketWrapper> pAllConnectedClients, HashMap<String, List<String>> pQueuedMessages) throws SQLException {
        this.allConnectedClients = pAllConnectedClients;
        this.queuedMessages = pQueuedMessages;
        this.sqlUtils = new SQLUtils("kmes_server.db");
        this.client = pClient;
    }

    private SocketWrapper getWrapper(String pUsername) { return allConnectedClients.get(pUsername); }

    private void handleSendRequest(String pReveiver, String pMessage) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, IOException, InvalidKeyException {
        System.err.println(pReveiver);
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

    private void queueMessage(String pReveiver, String pMessage)
    {
        queuedMessages.putIfAbsent(pReveiver, new ArrayList<>());
        queuedMessages.get(pReveiver).add(currentUser+";;"+pMessage);
        System.out.println(queuedMessages.get(pReveiver).size());
    }

    private void sendQueuedMessages() throws InvalidAlgorithmParameterException, IllegalBlockSizeException, IOException, BadPaddingException, InvalidKeyException {
        List<String> lMessages = queuedMessages.get(currentUser);
        if (lMessages == null) return;
        while (lMessages.size() != 0) {
            System.out.println(lMessages.size());
            client.writeAES("message;;"+lMessages.get(0));
            lMessages.remove(0);
        }
    }

    private void changeBindingWithCurrentUser()
    {
        SocketWrapper lCurrentBindedWrapperToThisUsername = allConnectedClients.get(currentUser);
        if (lCurrentBindedWrapperToThisUsername != null) {
            try {
                lCurrentBindedWrapperToThisUsername.writeAES("loggedOut");
            }
            catch (InvalidAlgorithmParameterException | IOException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException ignored) {}
        }
        allConnectedClients.put(currentUser, client);
    }

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
     *
     * */
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


    public boolean verifyLogin(String pUsername, String pPassword, byte[] pSalt)
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

    public boolean userExists(String pUsername)
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

    public void stopListeningForInput() {running = false;}
}
