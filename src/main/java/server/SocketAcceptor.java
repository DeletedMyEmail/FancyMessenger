package server;

// Own Library https://github.com/KaitoKunTatsu/KLibrary
import KLibrary.Utils.EncryptionUtils;
import KLibrary.Utils.SQLUtils;

import java.io.IOException;
import java.net.ServerSocket;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;


/**
 * Thread accepting new clients connecting to the KMes Server
 *
 * @version stabel-1.0.2 | last edit: 11.10.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
class SocketAcceptor extends Thread {

    public static final int PORT = 4242;

    private final HashMap<String, InputHandler> clients;
    private final HashMap<String, List<String>> queuedMessages;
    private final ServerSocket serverSocket;

    private final EncryptionUtils encryptionUtils;

    private final SQLUtils sqlUtils;
    private boolean running;

    protected SocketAcceptor() throws IOException, SQLException {
        clients = new HashMap<>();
        queuedMessages = new HashMap<>();
        serverSocket = new ServerSocket(PORT);
        encryptionUtils = new EncryptionUtils();
        sqlUtils = new SQLUtils("kmes_server.db");
        sqlUtils.onExecute("""
                CREATE TABLE IF NOT EXISTS User
                (
                    username TEXT primary key,
                    hashedPassword TEXT,
                    salt BLOB
                );
                """);
        sqlUtils.onExecute("""
                CREATE TABLE IF NOT EXISTS Session (
                	ip BLOB,
                	username TEXT,
                	FOREIGN KEY("username") REFERENCES "User"("username"),
                	PRIMARY KEY("ip")
                );
                """);
    }

    /**
     * Accepts new sockets and establishes encryption via RSA + AES key transfer
     * */
    @Override
    public void run()
    {
        running = true;
        System.out.println("Server is now listening for clients on port "+PORT);
        while (running)
        {
            try
            {
                // Init new socket and streams
                SocketWrapper lNewSocket = new SocketWrapper(serverSocket.accept());
                if (!running) {
                    lNewSocket.close();
                    break;
                }

                new InputHandler(lNewSocket, clients, queuedMessages, sqlUtils, encryptionUtils).start();
                System.out.println("Client socket accepted");

            }
            catch (IOException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    protected void stopAcceptingSockets() { running = false; }

    public static void main(String[] args) throws SQLException, IOException {
        new SocketAcceptor().start();
    }
}
