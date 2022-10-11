package server;

// Own Library https://github.com/KaitoKunTatsu/KLibrary
import KLibrary.Utils.EncryptionUtils;
import KLibrary.Utils.SQLUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.IOException;
import java.net.ServerSocket;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
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

    private final HashMap<String, SocketWrapper> clients;
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
        sqlUtils = new SQLUtils("src/main/resources/kmes_server.db");
        sqlUtils.onExecute("""
                CREATE TABLE IF NOT EXISTS User
                (
                    username TEXT primary key,
                    hashedPassword TEXT,
                    salt BLOB
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

                byte[] lInput = new byte[294];
                lNewSocket.getInStream().read(lInput);

                // Key handshake

                PublicKey lForeignPubKey = EncryptionUtils.decodeRSAKey(lInput);
                if (lForeignPubKey == null)  {
                    lNewSocket.close();
                    continue;
                }

                byte[] lEncodedOwnKey = encryptionUtils.getPublicKey().getEncoded();
                lNewSocket.writeUnencrypted(lEncodedOwnKey);

                lInput = new byte[128];
                lNewSocket.getInStream().read(lInput);
                SecretKey lSocketsAESKey = EncryptionUtils.decodeAESKey(
                        encryptionUtils.decryptRSAToBytes(lInput));

                lNewSocket.setAESKey(lSocketsAESKey);

                lNewSocket.getSocket().setSoTimeout(0);

                new InputHandler(lNewSocket, clients, queuedMessages, sqlUtils).start();
                System.out.println("Client socket accepted");

            }
            catch (IOException | InvalidKeySpecException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException | SQLException e) {
                e.printStackTrace();
            }
        }
    }

    protected void closeSocket(String pUsername) {
        if (clients.get(pUsername) != null) clients.get(pUsername).close();
        clients.remove(pUsername);
    }

    protected void stopAcceptingSockets() { running = false; }

    protected void close()
    {
        running = false;
        clients.forEach((username, socketWrapper) -> socketWrapper.close());
        clients.clear();
    }

    public static void main(String[] args) throws SQLException, IOException {
        new SocketAcceptor().start();
    }
}
