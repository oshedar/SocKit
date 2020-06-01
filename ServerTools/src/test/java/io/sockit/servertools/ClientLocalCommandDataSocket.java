/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

/**
 *
 * @author hoshi
 */
public class ClientLocalCommandDataSocket implements io.sockit.clienttools.CommandDataSocket, io.sockit.servertools.CommandDataReadListener, io.sockit.servertools.Encryptor,io.sockit.servertools.Decryptor{

    private io.sockit.servertools.LocalCommandDataSocket localCommandDataSocket;
    private io.sockit.clienttools.CommandDataReadListener clientCommandDataReadListener;
    private io.sockit.clienttools.Encryptor clientEncryptor;
    private io.sockit.clienttools.Decryptor clientDecryptor;
    private io.sockit.clienttools.SocketConnectionListener clientConnectionListener;
    
    class ClosedListener implements SocketClosedListener{

        @Override
        public void socketClosed(AbstractSocket socket) {
            io.sockit.clienttools.SocketConnectionListener clientConnectionListener=ClientLocalCommandDataSocket.this.clientConnectionListener;
            if(clientConnectionListener!=null)
                clientConnectionListener.socketClosed(ClientLocalCommandDataSocket.this);
        }
        
    }
    
    private ClientLocalCommandDataSocket(LocalCommandDataSocket localCommandDataSocket, io.sockit.clienttools.SocketConnectionListener clientConnectionListener,io.sockit.clienttools.CommandDataReadListener clientCommandDataReadListener,io.sockit.clienttools.Encryptor clientEncryptor,io.sockit.clienttools.Decryptor clientDecryptor) {
        this.localCommandDataSocket = localCommandDataSocket;
        if(clientCommandDataReadListener!=null)
            setCommandDataReadListener(clientCommandDataReadListener);
        if(clientEncryptor!=null || clientDecryptor!=null)
            setEncryptorDecryptor(clientEncryptor, clientDecryptor);
        this.clientConnectionListener=clientConnectionListener;
        localCommandDataSocket.setSocketClosedListener(new ClosedListener());
    }
    
    public static ClientLocalCommandDataSocket newInstance(LocalCommandDataSocket localCommandDataSocket, io.sockit.clienttools.SocketConnectionListener clientConnectionListener,io.sockit.clienttools.CommandDataReadListener clientCommandDataReadListener,io.sockit.clienttools.Encryptor clientEncryptor,io.sockit.clienttools.Decryptor clientDecryptor){
        ClientLocalCommandDataSocket clientLocalCommandDataSocket=new ClientLocalCommandDataSocket(localCommandDataSocket, clientConnectionListener, clientCommandDataReadListener, clientEncryptor, clientDecryptor);
        if(clientConnectionListener!=null){
            clientConnectionListener.socketConnecting(clientLocalCommandDataSocket);
            clientConnectionListener.socketConnected(clientLocalCommandDataSocket);
        }
        return clientLocalCommandDataSocket;
    }
    
    public static ClientLocalCommandDataSocket newInstance(LocalCommandDataSocket localCommandDataSocket, io.sockit.clienttools.SocketConnectionListener clientConnectionListener,io.sockit.clienttools.CommandDataReadListener clientCommandDataReadListener){
        return newInstance(localCommandDataSocket, clientConnectionListener, clientCommandDataReadListener, null, null);
    }
    
    
    @Override
    public void close() {
        this.localCommandDataSocket.close();
    }

    @Override
    public boolean isClosed() {
        return this.localCommandDataSocket.isClosed();
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public void setCommandDataReadListener(io.sockit.clienttools.CommandDataReadListener clientCommandDataReadListener) {
        this.localCommandDataSocket.setCommandDataReadListener(this);
        this.clientCommandDataReadListener=clientCommandDataReadListener;
    }

    @Override
    public void setEncryptorDecryptor(io.sockit.clienttools.Encryptor clientEncryptor, io.sockit.clienttools.Decryptor clientDecryptor) {
        this.clientEncryptor=clientEncryptor;
        this.clientDecryptor=clientDecryptor;
        this.localCommandDataSocket.setEncryptorDecryptor(this, this);
    }

    @Override
    public void write(String command, String data) {
        this.localCommandDataSocket.write(command, data);
    }

    @Override
    public void write(String command, byte[] data) {
        this.localCommandDataSocket.write(command, data);
    }

    @Override
    public void commandDataRead(CommandDataSocket socket, String command, String data) {
        io.sockit.clienttools.CommandDataReadListener clientCommandDataReadListener=this.clientCommandDataReadListener;
        if(clientCommandDataReadListener!=null)
            clientCommandDataReadListener.commandDataRead(this, command, data);
    }

    @Override
    public void commandDataRead(CommandDataSocket socket, String command, byte[] data) {
        io.sockit.clienttools.CommandDataReadListener clientCommandDataReadListener=this.clientCommandDataReadListener;
        if(clientCommandDataReadListener!=null)
            clientCommandDataReadListener.commandDataReadBytes(this, command, data);
        
    }

    @Override
    public byte[] encrypt(byte[] bytes) {
        io.sockit.clienttools.Encryptor clientEncryptor=this.clientEncryptor;
        if(clientEncryptor!=null)
            return clientEncryptor.encrypt(bytes);
        return bytes;
    }

    @Override
    public String encrypt(String txt) {
        io.sockit.clienttools.Encryptor clientEncryptor=this.clientEncryptor;
        if(clientEncryptor!=null)
            return clientEncryptor.encrypt(txt);
        return txt;
    }

    @Override
    public byte[] decrypt(byte[] bytes) {
        io.sockit.clienttools.Decryptor clientDecryptor=this.clientDecryptor;
        if(clientDecryptor!=null)
            return clientDecryptor.decrypt(bytes);
        return bytes;
    }

    @Override
    public String decrypt(String txt) {
        io.sockit.clienttools.Decryptor clientDecryptor=this.clientDecryptor;
        if(clientDecryptor!=null)
            return clientDecryptor.decrypt(txt);
        return txt;
    }
    
}
