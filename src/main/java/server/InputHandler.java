package server;

// Own Library
import KLibrary.Utils.EncryptionUtils;
import KLibrary.Utils.SQLUtils;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;
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

    private final SocketAcceptor socketAcceptor;
    private final SQLUtils sqlUtils;

    private final List<List<Object>> clientConnnectionsAndStreams;

    private boolean running;

    protected InputHandler() throws IOException, SQLException {
        clientConnnectionsAndStreams = SocketAcceptor.getSockets();
        socketAcceptor = new SocketAcceptor();
        socketAcceptor.start();
        sqlUtils = new SQLUtils("src/main/resources/kmes.db");
        running = true;
    }


    private void handleSendRequest(int pAuthorSocketIndex, String pReveiver, String pMessage) throws IOException
    {
        for (List<Object> client : clientConnnectionsAndStreams)
        {
            if (client.get(3).equals(pReveiver))
            {
                if (!clientConnnectionsAndStreams.get(pAuthorSocketIndex).get(3).equals(pReveiver))
                {
                    writeToSocket(clientConnnectionsAndStreams.indexOf(client), "KMES;message;"+
                            clientConnnectionsAndStreams.get(pAuthorSocketIndex).get(3)+";"+pMessage);
                }
                else
                {
                    writeToSocket(pAuthorSocketIndex, "KMES;error;You can't message yourself;Couldn't send message");
                }
                return;
            }
        }
        writeToSocket(pAuthorSocketIndex, "KMES;error;User not found;Couldn't send message");
    }

    private void handleRegistrationRequest(int pSocketIndex, String pUsername, String pPassword) throws IOException, SQLException {
        if (clientConnnectionsAndStreams.get(pSocketIndex).get(3).equals(""))
        {
            if (!userExists(pUsername))
            {
                try {
                    byte[] lSalt = EncryptionUtils.generateSalt();
                    String lHashedPassword = EncryptionUtils.getHash(pPassword, lSalt);
                    sqlUtils.onExecute("INSERT INTO User VALUES(?, ?, ?)", pUsername, lHashedPassword, lSalt);
                    writeToSocket(pSocketIndex, "KMES;loggedIn;"+pUsername);
                } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                    writeToSocket(pSocketIndex, "KMES;error;Couldn't encrypt your password;Error occurred during registration process");
                }
            }
            else
            {
                writeToSocket(pSocketIndex, "KMES;error;This username is already taken, please choose another one;Registration failed");
            }
        }
        else
        {
            writeToSocket(pSocketIndex, "KMES;error;Log out before registration;Registration failed");
        }
    }

    private void handleLoginRequest(int pSocketIndex, String pUsername, String pPassword) throws IOException {
        byte[] lSalt;
        try {
            lSalt = sqlUtils.onQuery("SELECT salt FROM User WHERE username=?", pUsername).getBytes(1);
        } catch (SQLException sqlEx) {
            writeToSocket(pSocketIndex, "KMES;error;User not found;Login failed");
            return;
        }

        System.out.println("Password validation..");
        if (verifyLogin(pUsername, pPassword, lSalt))
        {
            clientConnnectionsAndStreams.get(pSocketIndex).set(3, pUsername);
            writeToSocket(pSocketIndex, "KMES;loggedIn;"+pUsername);
            System.out.println("Password valid");
        }
        else
        {
            writeToSocket(pSocketIndex, "KMES;error;Password or username incorrect;Login failed");
            System.out.println("Password invalid");
        }
    }

    private void writeToSocket(int pSocketIndex, String pStr) throws IOException {
        try {
            ((DataOutputStream) clientConnnectionsAndStreams.get(pSocketIndex).get(1)).writeUTF(pStr);
        } catch (IOException e) {
            SocketAcceptor.closeSocket(pSocketIndex);
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
                Socket current_socket = ((Socket) clientConnnectionsAndStreams.get(i).get(0));
                DataInputStream reader = ((DataInputStream) clientConnnectionsAndStreams.get(i).get(2));

                System.out.printf("Checking socket[%d]..\n", i+1);
                try {
                    String input = reader.readUTF();
                    if (current_socket.isClosed() || !current_socket.isConnected())
                    {
                        SocketAcceptor.closeSocket(i);
                        System.out.printf("[%d]Socket closed\n", i+1);
                        i--;
                    }
                    else
                    {
                        System.out.printf("[%d]Socket still connected\n", i+1);
                        String[] request = input.split(";");
                        if (!request[0].equals("KMES"))
                        {
                            current_socket.close();
                            clientConnnectionsAndStreams.remove(i);
                        }
                        else
                        {
                            switch (request[1])
                            {
                                case "login":
                                    handleLoginRequest(i, request[2], request[3]);
                                    break;
                                case "register":
                                    handleRegistrationRequest(i, request[2], request[3]);
                                    break;
                                case "send":
                                    handleSendRequest(i, request[2], request[3]);
                                    break;
                                case "logout":
                                    clientConnnectionsAndStreams.get(i).set(3,"");
                                    break;
                                case "doesUserExist":
                                    String lUsername = request[2];
                                    if (userExists(lUsername))
                                    {
                                        writeToSocket(i, "KMES;userExists;"+lUsername);
                                    }
                                    else
                                    {
                                        writeToSocket(i, "KMES;error;User does not exist: "+lUsername+";Adding contact failed");
                                    }
                                default:
                            }
                        }
                    }

                }
                catch (SocketTimeoutException ignored) {}
                catch (SQLException sqlEx) { sqlEx.printStackTrace();}
                catch (IndexOutOfBoundsException ioobEx)
                {
                    try {
                        writeToSocket(i, "KMES;error;Missing argument;Error occurred");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                catch (IOException ex)
                {
                    clientConnnectionsAndStreams.remove(i);
                    System.out.printf("[%d]Socket closed due to IOExcception\n", i+1);
                    i--;
                }

            }
        }
    }


    public boolean verifyLogin(String pUsername, String pPassword, byte[] pSalt)
    {
        try
        {
            ResultSet rs = sqlUtils.onQuery("SELECT hashedPassword FROM User WHERE username=?",
                    new String[]{ pUsername, });

            if (!rs.isClosed() && rs.next()) {
                return EncryptionUtils.validate(pPassword, rs.getString("hashedPassword"), pSalt);
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
            ResultSet rs = sqlUtils.onQuery("SELECT username FROM User WHERE username=?", new String[]{pUsername});
            return !(rs.isClosed() || rs.getString(1).equals(""));
        }
        catch (SQLException ex)
        {
            return false;
        }
    }

    public void stopListeningForInput() {running = false;}

    public static void main(String[] args) throws IOException, SQLException {
        InputHandler inputHandler = new InputHandler();
        inputHandler.start();
    }
}
