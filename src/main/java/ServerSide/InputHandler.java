package ServerSide;

import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

/**
 *  Server backend for Kmes messenger
 *
 * @version 1.0 | 13.12.2021
 * @author Joshua Hartjes | KaitoKunTatsu
 * */
class InputHandler extends Thread {

    private ClientAcceptor clientManager;
    private SQLManager sqlmanager;

    protected InputHandler() throws IOException, SQLException {
        sqlmanager = new SQLManager();
        clientManager = new ClientAcceptor();
        clientManager.start();
    }

    /**
     *  Main of the server, iterating through all connected clients and handling requests
     * */
    public void run() {

        while (true) {
            try {
                wait(5);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < clientManager.clients.size(); i++) {
                try {
                    String mes = clientManager.inputs.get(i).readUTF();
                    if (mes.startsWith("WannaSendMessageKmesServer;")) {
                        handleClientRequest(i, mes.split(";",0));
                    }
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Handles client requests based on the incoming String
     *
     * @param index Index of the client in list from {@link ClientAcceptor}
     * @param message Input as String Array
     * */
    private void handleClientRequest(int index, String[] message) throws SQLException {
        try {
            switch (message[1]) {
                // User wants to log in
                case "login":
                    if (message.length < 4)
                    {
                        sendMessageToUser(index,"FromKmesServer;loginFailed");
                        return;
                    }
                    if (sqlmanager.onQuery("SELECT username FROM kmes.kmesuser WHERE (username = ? AND pw = ?);", new String[] {message[2],message[3]}).next())
                    {
                        sendMessageToUser(index,"FromKmesServer;loginSuccessful;"+message[2]);
                        clientManager.clients.get(index).set(1, message[2]);
                    }
                    else
                    {
                        sendMessageToUser(index,"FromKmesServer;loginFailed");
                    }
                    break;
                // User asks for a registration
                case "register":

                    // If username doesn't exist in table: insert
                    if (!sqlmanager.onQuery("SELECT * FROM kmes.kmesuser WHERE username=?;", new String[] {message[2]}).next())
                    {
                        sqlmanager.onExecute("INSERT INTO kmes.kmesuser (username, pw) VALUES (?, ?);", new String[] {message[2],message[3]});
                        sendMessageToUser(index,"FromKmesServer;registrationSuccessful;"+message[2]);
                    }
                    // Else: send fail message to client
                    else
                    {
                        sendMessageToUser(index, "FromKmesServer;registrationFailed;");
                    }
                    break;
                //
                case "send":
                    if (message.length == 3)
                    {
                        String usn = clientManager.clients.get(index).get(1).toString();
                        if (!usn.equals(""))
                        {
                            for (ArrayList<Object> x : clientManager.clients)
                            {
                                if (x.get(1).equals(usn))
                                {
                                    clientManager.outputs.get(clientManager.clients.indexOf(x.get(0))).writeUTF(
                                            "FromKmesServer;sent;"+x.get(1)+message[2]);
                                    return;
                                }
                            }
                        }
                    }
                    break;
            }
        }
        catch (IndexOutOfBoundsException | IOException ignored) { }
    }

    static protected void incomingMessage(String from, String msg)
    {

    }

    private void sendMessageToUser(int index, String message) throws IOException {
        clientManager.outputs.get(index).writeUTF(message);
    }
}
