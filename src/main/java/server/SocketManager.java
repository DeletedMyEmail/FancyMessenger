package server;

import KLibrary.Utils.EncryptionUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * Thread accepting new clients connecting to the KMes messenger
 *
 * @version 24.08.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
class SocketManager extends Thread{

    private final List<List<Object>> socketConnectionsAndStreams = new ArrayList<>();;
    private final ServerSocket serverSocket;
    private final EncryptionUtils encryptionUtils;

    protected SocketManager() throws IOException
    {
        serverSocket = new ServerSocket(3141);
        encryptionUtils = new EncryptionUtils();
    }

    protected List<List<Object>> getSockets() { return socketConnectionsAndStreams; };

    protected void closeSocket(int pIndex) throws IOException {
        ((Socket) socketConnectionsAndStreams.get(pIndex).get(0)).close();
        socketConnectionsAndStreams.remove(pIndex);
    }

    protected void writeToSocket(int pIndex, String pMessage) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        System.out.println(pMessage);
        writeToSocket(
                (DataOutputStream)socketConnectionsAndStreams.get(pIndex).get(1),
                (PublicKey) socketConnectionsAndStreams.get(pIndex).get(4),
                pMessage);
    }

    protected String readFromSocket(int pIndex) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        return readFromSocket((DataInputStream)socketConnectionsAndStreams.get(pIndex).get(2));
    }

    private void writeToSocket(DataOutputStream pOutStream, PublicKey pSocketsPublicKey, String pMessage) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] lEncryptedMessage = encryptionUtils.encryptRSA(pMessage, pSocketsPublicKey);
        System.out.println(pMessage);
        pOutStream.write(lEncryptedMessage);
    }

    private String readFromSocket(DataInputStream pInStream) throws IOException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException {
        byte[] lInput = new byte[256];
        pInStream.read(lInput);
        return encryptionUtils.decryptRSA(lInput);
    }

    /**
     * Accepts new sockets and establishes secure RSA encryption via key transfer
     * */
    public void run()
    {
        System.out.println("Server online\n");
        while (true)
        {
            try
            {
                // Init new socket and streams

                Socket lNewSocket = serverSocket.accept();
                DataOutputStream lNewOutStream = new DataOutputStream(lNewSocket.getOutputStream());
                DataInputStream lNewInStream = new DataInputStream(lNewSocket.getInputStream());
                lNewSocket.setSoTimeout(400);

                byte[] lInput = new byte[294];
                lNewInStream.read(lInput);

                // RSA key handshake

                PublicKey lForeignPubKey = encryptionUtils.getPublicKeyFromBytes(lInput);

                if (lForeignPubKey == null)  {
                    lNewSocket.close();
                    lNewInStream.close();
                    lNewOutStream.close();
                    continue;
                }

                socketConnectionsAndStreams.add(new ArrayList<>()
                {{
                    add(lNewSocket);
                    add(lNewOutStream);
                    add(lNewInStream);
                    add("");
                    add(lForeignPubKey);
                }});
                byte[] lEncodedOwnKey = encryptionUtils.getPublicKey().getEncoded();
                lNewOutStream.write(lEncodedOwnKey, 0, lEncodedOwnKey.length);

                System.out.printf("[%d]Client socket accepted\n", socketConnectionsAndStreams.toArray().length);
                lNewSocket.setSoTimeout(100);
            }
            catch (IOException e) { e.printStackTrace(); }

        }
    }
}
