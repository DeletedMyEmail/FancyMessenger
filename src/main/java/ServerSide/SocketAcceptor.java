package ServerSide;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


/**
 * Thread accepting new clients connecting to the KMes messenger
 *
 * @version 22.06.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
class SocketAcceptor extends Thread{

    private static List<List<Object>> clients_in_out = new ArrayList<>();;

    private ServerSocket serverSocket;

    protected SocketAcceptor() throws IOException
    {
        serverSocket = new ServerSocket(3141);
    }

    protected static List<List<Object>> getSockets() { return clients_in_out; };

    protected static void closeSocket(int index) throws IOException {
        ((Socket)clients_in_out.get(index).get(0)).close();
        clients_in_out.remove(index);
    }

    public void run()
    {
        while (true)
        {
            try
            {
                Socket new_socket = serverSocket.accept();
                new_socket.setSoTimeout(100);
                clients_in_out.add(new ArrayList<>()
                {{
                    add(new_socket);
                    add(new DataOutputStream(new_socket.getOutputStream()));
                    add(new DataInputStream(new_socket.getInputStream()));
                    add("");
                }});
                System.out.printf("[%d]Client socket accepted\n", clients_in_out.toArray().length);
            } catch (IOException e) { e.printStackTrace(); }

        }
    }
}
