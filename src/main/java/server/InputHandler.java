package server;

// Own Library https://github.com/KaitoKunTatsu/KLibrary
import KLibrary.Utils.EncryptionUtils;
import KLibrary.Utils.SQLUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.List;

/**
 * This class acts as a backend for the KMes Server and processes all inputs from user sockets.
 *
 * @version v2.0.2 | last edit: 31.08.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
class InputHandler extends Thread {

    private final SocketManager socketManager;
    private final SQLUtils sqlUtils;

    private final List<List<Object>> clientConnnectionsAndStreams;

    private boolean running;

    protected InputHandler(SocketManager pSocketManager) throws IOException, SQLException {
        socketManager = pSocketManager;
        socketManager.start();
        clientConnnectionsAndStreams = socketManager.getSockets();
        sqlUtils = new SQLUtils("src/main/resources/kmes.db");
        running = true;
    }

    protected InputHandler() throws IOException, SQLException {
        this(new SocketManager());
    }


    private void handleSendRequest(int pAuthorSocketIndex, String pReveiver, String pMessage) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        for (List<Object> client : clientConnnectionsAndStreams)
        {
            if (client.get(3).equals(pReveiver))
            {
                if (!clientConnnectionsAndStreams.get(pAuthorSocketIndex).get(3).equals(pReveiver))
                {
                    socketManager.writeToSocket(clientConnnectionsAndStreams.indexOf(client), "message;;"+
                            clientConnnectionsAndStreams.get(pAuthorSocketIndex).get(3)+";;"+pMessage);
                }
                else
                {
                    socketManager.writeToSocket(pAuthorSocketIndex, "error;;You can't message yourself;;Couldn't send message");
                }
                return;
            }
        }
        socketManager.writeToSocket(pAuthorSocketIndex, "error;;User not found;;Couldn't send message");
    }

    private void handleRegistrationRequest(int pSocketIndex, String pUsername, String pPassword) throws IOException, SQLException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        if (clientConnnectionsAndStreams.get(pSocketIndex).get(3).equals(""))
        {
            if (!userExists(pUsername))
            {
                try
                {
                    byte[] lSalt = EncryptionUtils.generateSalt();
                    String lHashedPassword = EncryptionUtils.getHash(pPassword, lSalt);
                    sqlUtils.onExecute("INSERT INTO User VALUES(?, ?, ?)", pUsername, lHashedPassword, lSalt);
                    socketManager.writeToSocket(pSocketIndex, "loggedIn;;"+pUsername);
                }
                catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                    socketManager.writeToSocket(pSocketIndex, "error;;Couldn't encrypt your password;;Error occurred during registration process");
                }
            }
            else
            {
                socketManager.writeToSocket(pSocketIndex, "error;;This username is already taken, please choose another one;;Registration failed");
            }
        }
        else
        {
            socketManager.writeToSocket(pSocketIndex, "error;;Log out before registration;;Registration failed");
        }
    }

    private void handleLoginRequest(int pSocketIndex, String pUsername, String pPassword) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        byte[] lSalt;
        try {
            lSalt = sqlUtils.onQuery("SELECT salt FROM User WHERE username=?", pUsername).getBytes(1);
        } catch (SQLException sqlEx) {
            socketManager.writeToSocket(pSocketIndex, "error;;User not found;;Login failed");
            return;
        }

        if (verifyLogin(pUsername, pPassword, lSalt))
        {
            clientConnnectionsAndStreams.get(pSocketIndex).set(3, pUsername);
            socketManager.writeToSocket(pSocketIndex, "loggedIn;;"+pUsername);
        }
        else {
            socketManager.writeToSocket(pSocketIndex, "error;;Password or username incorrect;;Login failed");
        }
    }


    /**
     *
     * */
    public void run() {
        while (running)
        {
            for (int i = 0; i < clientConnnectionsAndStreams.toArray().length; i++)
            {
                Socket lCurrentSocket = ((Socket) clientConnnectionsAndStreams.get(i).get(0));
                try
                {
                    String lInput = socketManager.readFromSocket(i);

                    if (lCurrentSocket.isClosed() || !lCurrentSocket.isConnected())
                    {
                        socketManager.closeSocket(i);
                        System.out.printf("[%d]Socket closed\n", i+1);
                        i--;
                    }
                    else
                    {
                        String[] lRequest = lInput.split(";;");
                        switch (lRequest[0])
                        {
                            case "login" -> handleLoginRequest(i, lRequest[1], lRequest[2]);
                            case "register" -> handleRegistrationRequest(i, lRequest[1], lRequest[2]);
                            case "send" -> handleSendRequest(i, lRequest[1], lRequest[2]);
                            case "logout" -> clientConnnectionsAndStreams.get(i).set(3,"");
                            case "doesUserExist" -> {
                                    String lUsername = lRequest[1];
                                    if (userExists(lUsername)) socketManager.writeToSocket(i, "userExists;;"+lUsername);
                                    else socketManager.writeToSocket(i, "error;; ;;User \""+lUsername+"\" does not exist");
                                }
                            case "Unable to decrypt message" -> socketManager.writeToSocket(i, "error;; ;;Your message was not properly encrypted");
                            default -> socketManager.writeToSocket(i, "error;; ;;Unknown input");
                        }
                    }
                }
                catch (SocketTimeoutException ignored) {}
                catch (IndexOutOfBoundsException ioobEx)
                {
                    try {
                        socketManager.writeToSocket(i, "error;; ;;Missing argument");
                    } catch (IOException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException e) {
                        e.printStackTrace();
                    }
                }
                catch (IOException ioEx)
                {
                    clientConnnectionsAndStreams.remove(i);
                    System.out.printf("[%d]Socket closed due to IOException\n", i+1);
                    i--;
                }
                catch (Exception ex) {
                    System.out.println("Deadly exception");
                    ex.printStackTrace();
                    System.exit(1);
                }
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

    public SocketManager getSocketManager() { return socketManager;}

}
