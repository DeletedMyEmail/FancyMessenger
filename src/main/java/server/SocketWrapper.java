package server;

// Own Library https://github.com/KaitoKunTatsu/KLibrary
import KLibrary.Utils.EncryptionUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PrivateKey;

/**
 * Thread accepting new clients connecting to the KMes messenger
 *
 * @version v3.0.0 | last edit: 16.09.2022
 * @author Joshua H. | KaitoKunTatsu#3656
 * */
class SocketWrapper {

    private Socket client;
    private DataInputStream reader;
    private DataOutputStream writer;

    private SecretKey AESKey;

    private boolean running;

    public SocketWrapper(Socket pSocket, SecretKey pAESKey) throws IOException
    {
        client = pSocket;
        reader = new DataInputStream(client.getInputStream());
        writer = new DataOutputStream(client.getOutputStream());

        AESKey = pAESKey;
    }

    public SocketWrapper(Socket pSocket) throws IOException {
        this(pSocket, null);
    }

    protected void writeAES(String pMessage) throws InvalidAlgorithmParameterException, IllegalBlockSizeException, BadPaddingException, InvalidKeyException, IOException {
        if (AESKey == null) return;

        byte[] lEncrpytedMessage = EncryptionUtils.encryptAES(pMessage, AESKey);
        byte[] lMessageSizeAsBytes = ByteBuffer.allocate(4).putInt(lEncrpytedMessage.length).array();
        byte[] lConcatenated = ByteBuffer.allocate(lMessageSizeAsBytes.length+lEncrpytedMessage.length).put(lMessageSizeAsBytes).put(lEncrpytedMessage).array();
        writer.write(lConcatenated, 0, lConcatenated.length);
    }

    protected void writeUnencrypted(String pMessage) throws IOException {
        writer.writeUTF(pMessage);
    }

    protected void writeUnencrypted(byte[] pMessage) throws IOException {
        writer.write(pMessage);
    }

    public byte[] readAllBytes() throws IOException {
        int lSize = reader.readInt();
        if (lSize <= 0) return new byte[0];

        byte[] lInput = new byte[lSize];
        reader.readFully(lInput);

        return lInput;
    }

    public String readAES() throws IOException
    {
        if (AESKey == null) return "";

        try
        {
            int lSize = reader.readInt();
            if (lSize <= 0) return "";
            byte[] lEncryptedInput = new byte[lSize];

            reader.readFully(lEncryptedInput);

            return EncryptionUtils.decryptAES(lEncryptedInput, AESKey);
        }
        catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | InvalidAlgorithmParameterException ex) {
            ex.printStackTrace();
            return "Unable to decrypt message";
        }
    }


    protected void close() {
        try
        {
            reader.close();
            writer.close();
            client.close();
        }
        catch (IOException ignored)
        {
            reader = null;
            writer = null;
            client = null;
        }
    }

    protected void setAESKey(SecretKey pKey) { AESKey = pKey; }

    protected boolean isClosed() { return client == null || client.isClosed();}

    protected SecretKey getAESKey() {return AESKey;}

    protected Socket getSocket() {return client;}

    protected DataOutputStream getOutStream() {return writer;}

    protected DataInputStream getInStream() {return reader;}
}
