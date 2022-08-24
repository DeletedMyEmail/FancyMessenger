package server;

// Own Library
import KLibrary.Utils.EncryptionUtils;
import KLibrary.Utils.SQLUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.*;
import java.util.List;

/**
 * Server backend for the KMes server<br/>
 * Manages all inputs from user clients
 *
 * @version 23.08.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
class InputHandler extends Thread {

    private final SocketManager socketManager;
    private final SQLUtils sqlUtils;

    private final List<List<Object>> clientConnnectionsAndStreams;

    private boolean running;

    protected InputHandler() throws IOException, SQLException {
        socketManager = new SocketManager();
        socketManager.start();
        clientConnnectionsAndStreams = socketManager.getSockets();
        sqlUtils = new SQLUtils("src/main/resources/kmes.db");
        running = true;
    }


    private void handleSendRequest(int pAuthorSocketIndex, String pReveiver, String pMessage) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        for (List<Object> client : clientConnnectionsAndStreams)
        {
            if (client.get(3).equals(pReveiver))
            {
                if (!clientConnnectionsAndStreams.get(pAuthorSocketIndex).get(3).equals(pReveiver))
                {
                    socketManager.writeToSocket(clientConnnectionsAndStreams.indexOf(client), "KMES;message;"+
                            clientConnnectionsAndStreams.get(pAuthorSocketIndex).get(3)+";"+pMessage);
                }
                else
                {
                    socketManager.writeToSocket(pAuthorSocketIndex, "KMES;error;You can't message yourself;Couldn't send message");
                }
                return;
            }
        }
        socketManager.writeToSocket(pAuthorSocketIndex, "KMES;error;User not found;Couldn't send message");
    }

    private void handleRegistrationRequest(int pSocketIndex, String pUsername, String pPassword) throws IOException, SQLException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        if (clientConnnectionsAndStreams.get(pSocketIndex).get(3).equals(""))
        {
            if (!userExists(pUsername))
            {
                try
                {
                    byte[] lSalt = EncryptionUtils.generateSalt();
                    String lHashedPassword = EncryptionUtils.getHash(pPassword, lSalt);
                    sqlUtils.onExecute("INSERT INTO User VALUES(?, ?, ?)", pUsername, lHashedPassword, lSalt);
                    socketManager.writeToSocket(pSocketIndex, "KMES;loggedIn;"+pUsername);
                }
                catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                    socketManager.writeToSocket(pSocketIndex, "KMES;error;Couldn't encrypt your password;Error occurred during registration process");
                }
            }
            else
            {
                socketManager.writeToSocket(pSocketIndex, "KMES;error;This username is already taken, please choose another one;Registration failed");
            }
        }
        else
        {
            socketManager.writeToSocket(pSocketIndex, "KMES;error;Log out before registration;Registration failed");
        }
    }

    private void handleLoginRequest(int pSocketIndex, String pUsername, String pPassword) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] lSalt;
        try {
            lSalt = sqlUtils.onQuery("SELECT salt FROM User WHERE username=?", pUsername).getBytes(1);
        } catch (SQLException sqlEx) {
            socketManager.writeToSocket(pSocketIndex, "KMES;error;User not found;Login failed");
            return;
        }

        System.out.println("Password validation..");
        if (verifyLogin(pUsername, pPassword, lSalt))
        {
            clientConnnectionsAndStreams.get(pSocketIndex).set(3, pUsername);
            socketManager.writeToSocket(pSocketIndex, "KMES;loggedIn;"+pUsername);
            System.out.println("Password valid");
        }
        else
        {
            socketManager.writeToSocket(pSocketIndex, "KMES;error;Password or username incorrect;Login failed");
            System.out.println("Password invalid");
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

                        switch (lRequest[1])
                        {
                            case "login":
                                handleLoginRequest(i, lRequest[2], lRequest[3]);
                                break;
                            case "register":
                                handleRegistrationRequest(i, lRequest[2], lRequest[3]);
                                break;
                            case "send":
                                handleSendRequest(i, lRequest[2], lRequest[3]);
                                break;
                            case "logout":
                                clientConnnectionsAndStreams.get(i).set(3,"");
                                break;
                            case "doesUserExist":
                                String lUsername = lRequest[2];
                                if (userExists(lUsername))
                                {
                                    socketManager.writeToSocket(i, "KMES;userExists;"+lUsername);
                                }
                                else
                                {
                                    socketManager.writeToSocket(i, "KMES;error;User does not exist: "+lUsername+";Adding contact failed");
                                }
                            default:
                                break;
                        }
                    }
                }
                catch (SocketTimeoutException ignored) {}
                catch (IndexOutOfBoundsException ioobEx)
                {
                    try {
                        socketManager.writeToSocket(i, "KMES;error;Missing argument;Error occurred");
                    } catch (IOException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
                        e.printStackTrace();
                    }
                }
                catch (IOException ex)
                {
                    clientConnnectionsAndStreams.remove(i);
                    System.out.printf("[%d]Socket closed due to IOExcception\n", i+1);
                    i--;
                }
                catch (Exception ex) {
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

    public static void main(String[] args) throws IOException, SQLException { new InputHandler().start(); }
}
