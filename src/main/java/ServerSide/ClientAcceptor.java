package ServerSide;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;


/**
 * Thread accepting new clients connecting to the Kmes messenger
 *
 * @version 1.0 | 13.12.2021
 * @author Joshua Hartjes | KaitoKunTatsu
 * */
class ClientAcceptor extends Thread{

    protected ArrayList<ArrayList<Object>> clients;
    protected ArrayList<DataInputStream> inputs;
    protected ArrayList<DataOutputStream> outputs;

    private ServerSocket socket;

    protected ClientAcceptor() throws IOException
    {
        socket = new ServerSocket(3141);
        clients = new ArrayList<>();
        outputs = new ArrayList<>();
        inputs = new ArrayList<>();
    }

    public void run()
    {
        while (true)
        {
            System.out.println("Acccepting client...");
            try
            {
                Socket client = socket.accept();
                clients.add(new ArrayList<Object>() {{add(client); add("");}});
                inputs.add(new DataInputStream(client.getInputStream()));
                outputs.add(new DataOutputStream(client.getOutputStream()));
            } catch (IOException e) { e.printStackTrace(); }
        }
    }

}
