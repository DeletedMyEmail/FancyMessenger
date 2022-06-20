package ServerSide;

import SQL.SQLManager;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.sql.*;
import java.util.List;

/**
 *  Server backend for Kmes messenger
 *
 * @version 18.06.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
class InputHandler extends Thread {

    private final SocketAcceptor socketAcceptor;
    private final SQLManager sqlmanager;

    List<List<Object>> clients_in_out = SocketAcceptor.getSockets();

    private boolean running;

    protected InputHandler() throws IOException, SQLException, ClassNotFoundException {
        socketAcceptor = new SocketAcceptor();
        socketAcceptor.start();
        sqlmanager = new SQLManager("src/main/java/SQL/kmes.db");
        running = true;
    }

    private void handleLoginRequest(int socket_index, String request[]) throws IOException {
        PasswordHasher hasher = new PasswordHasher();
        String username = request[2];
        String password = hasher.getHash(request[3]);

        if (sqlmanager.check_login(username, password))
        {
            clients_in_out.get(socket_index).set(3, username);
            writeToSocket(socket_index, "KMES;loggedIn;"+username);
        }
    }

    private void writeToSocket(int socket_index, String str) throws IOException {
        try {
            ((DataOutputStream)clients_in_out.get(socket_index).get(1)).writeUTF(str);
        } catch (IOException e) {
            SocketAcceptor.closeSocket(socket_index);
        }
    }

    /**
     *
     * */
    public void run() {
        while (running)
        {
            for (int i = 0; i < clients_in_out.toArray().length; i++)
            {
                Socket current_socket = ((Socket)clients_in_out.get(i).get(0));
                DataInputStream current_input_stream = ((DataInputStream)clients_in_out.get(i).get(2));
                DataInputStream current_output_stream = ((DataInputStream)clients_in_out.get(i).get(2));

                // Closing socket if disconnected
                System.out.printf("Checking socket [%d]..\n", i+1);
                try {
                    if (current_socket.isClosed() || !current_socket.isConnected() || current_input_stream.readLine() == null)
                    {
                        SocketAcceptor.closeSocket(i);
                        System.out.println("Socket closed");
                        i--;
                        continue;
                    }
                    else
                    {
                        System.out.println("Socket connected");
                    }
                } catch (IOException e) {
                    clients_in_out.remove(i);
                    System.out.println("Socket closed");
                    i--;
                    continue;
                }

                // Client requests
                try
                {
                    String[] request = current_input_stream.readUTF().split(";");
                    if (!request[0].equals("KMES"))
                    {
                        current_socket.close();
                        clients_in_out.remove(i);
                        continue;
                    }
                    switch (request[1])
                    {
                        case "login": handleLoginRequest(i, request);
                    }

                } catch (IOException e) {e.printStackTrace();}

            }
        }
    }

    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        InputHandler inputHandler = new InputHandler();
        inputHandler.run();
    }
}
