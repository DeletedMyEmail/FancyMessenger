package server;

import SQL.SQLUtils;

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
 * @version 27.06.2022
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
        sqlUtils = new SQLUtils("src/main/java/SQL/kmes.db");
        running = true;
    }


    private void handleSendRequest(int pAuthorSocketIndex, String[] pRequest) throws IOException
    {
        String receiver = pRequest[2];
        for (List<Object> client : clientConnnectionsAndStreams)
        {
            if (client.get(3).equals(receiver))
            {
                if (!clientConnnectionsAndStreams.get(pAuthorSocketIndex).get(3).equals(receiver))
                {
                    writeToSocket(clientConnnectionsAndStreams.indexOf(client), "KMES;message;"+ clientConnnectionsAndStreams.get(pAuthorSocketIndex).get(3)+";"+pRequest[3]);
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

    private void handleRegistrationRequest(int pSocketIndex, String[] pRequest) throws IOException, SQLException {
        if (pRequest.length<4)
        {
            writeToSocket(pSocketIndex, "KMES;error;Please fill out every text field;Registration failed");
        }
        else if (clientConnnectionsAndStreams.get(pSocketIndex).get(3).equals(""))
        {
            String username = pRequest[2];
            if (!userExists(username))
            {
                String lHashedPassword;
                try {
                    lHashedPassword = EncryptionUtils.getHash(pRequest[3], new byte[]{'s','a','l','t'});
                    sqlUtils.onExecute("INSERT INTO User VALUES(?, ?)", new String[]{ username, lHashedPassword});
                } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                    writeToSocket(pSocketIndex, "KMES;error;Couldn't encrypt your password;Error occurred during registration process");
                    return;
                }
                writeToSocket(pSocketIndex, "KMES;loggedIn;"+username);
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

    private void handleLoginRequest(int pSocketIndex, String[] pRequest) throws IOException {
        if (pRequest.length<4)
        {
            writeToSocket(pSocketIndex, "KMES;error;Please fill out every text field;Login failed");
            System.out.println("Password invalid");
            return;
        }

        String username = pRequest[2];
        String password;
        try {
            password = EncryptionUtils.getHash(pRequest[3], new byte[]{'s','a','l','t'});
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            writeToSocket(pSocketIndex, "KMES;error;Couldn't encrypt your password;Error occurred during login process");
            return;
        }

        System.out.println("Password validation..");
        if (checkLogin(username, password))
        {
            clientConnnectionsAndStreams.get(pSocketIndex).set(3, username);
            writeToSocket(pSocketIndex, "KMES;loggedIn;"+username);
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
                                    handleLoginRequest(i, request);
                                    break;
                                case "register":
                                    handleRegistrationRequest(i, request);
                                    break;
                                case "send":
                                    handleSendRequest(i, request);
                                    break;
                                case "logout":
                                    clientConnnectionsAndStreams.get(i).set(3,"");
                                    break;
                                case "doesUserExist":
                                    String username = request[2];
                                    if (userExists(username))
                                    {
                                        writeToSocket(i, "KMES;userExists;"+username);
                                    }
                                    else
                                    {
                                        writeToSocket(i, "KMES;error;User does not exist: "+username+";Adding contact failed");
                                    }
                                default:
                            }
                        }
                    }

                }
                catch (SQLException | SocketTimeoutException ignored) {}
                catch (IOException e)
                {
                    clientConnnectionsAndStreams.remove(i);
                    System.out.printf("[%d]Socket closed due to IOExcception\n", i+1);
                    i--;
                }

            }
        }
    }


    public boolean checkLogin(String pUsername, String pHashedPassword)
    {
        try
        {
            ResultSet rs = sqlUtils.onQuery("SELECT username FROM User WHERE username=? AND password_hash=?",
                    new String[]{ pUsername, pHashedPassword});

            return !(rs.isClosed() || rs.getString(1).equals(""));

        }
        catch (SQLException e) {
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

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        InputHandler inputHandler = new InputHandler();
        inputHandler.start();
    }
}
