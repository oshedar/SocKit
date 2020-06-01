/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import io.sockit.clienttools.ClientCommandDataSocket;
import io.sockit.clienttools.CommandDataReadListener;
import io.sockit.clienttools.CommandDataSocket;
import io.sockit.clienttools.SocketConnectionListener;
import java.io.IOException;

/**
 *
 * @author Hoshedar Irani
 */
public class Client implements CommandDataReadListener,SocketConnectionListener{
    CommandDataSocket commandDataSocket;
    volatile String lastCommandRead;
    volatile Data lastDataRead;
    final String name;
    public Client(String url,String name) throws IOException{ //remote client
        this.name=name;
        this.commandDataSocket=ClientCommandDataSocket.newInstance(url, this,this);
    }

    public Client(LocalCommandDataSocket localCommandDataSocket,String name) { //local client
        this.name = name;        
        this.commandDataSocket = ClientLocalCommandDataSocket.newInstance(localCommandDataSocket, this, this);
    }
    
    @Override
    public void commandDataRead(CommandDataSocket socket, String command, String data) {
        this.lastCommandRead=command;
        this.lastDataRead=new Data(data);
    }

    @Override
    public void commandDataReadBytes(CommandDataSocket socket, String command, byte[] data) {
        this.lastCommandRead=command;
        this.lastDataRead=new Data(data);
    }

    public void write(String command,String data){
        this.commandDataSocket.write(command, data);
    }
    
    public void write(String command,byte[] data){
        this.commandDataSocket.write(command, data);
    }
    public void close(){
        this.commandDataSocket.close();
    }

    public boolean isConnected(){
        if(this.commandDataSocket instanceof ClientCommandDataSocket)
            return ((ClientCommandDataSocket)this.commandDataSocket).isOpen();
        return !this.commandDataSocket.isClosed();
    }

    @Override
    public void socketConnected(CommandDataSocket socket) {
        Console.log(name + " connected");
    }

    @Override
    public void connectionFailed(CommandDataSocket socket, Exception exception) {
        System.err.println(name + " connection failed");
    }

    @Override
    public void socketConnecting(CommandDataSocket socket) {
        Console.log(name + " connecting");
    }

    @Override
    public void socketDisconnected(CommandDataSocket socket) {
        System.err.println(name + " disconnected");
    }

    @Override
    public void socketClosed(CommandDataSocket socket) {
        Console.log(" connection closed");
    }
}
