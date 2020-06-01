/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.sockit.clienttools;

import com.neovisionaries.ws.client.WebSocket;
import com.neovisionaries.ws.client.WebSocketAdapter;
import com.neovisionaries.ws.client.WebSocketException;
import com.neovisionaries.ws.client.WebSocketFactory;
import com.neovisionaries.ws.client.WebSocketFrame;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 *
 * @author hoshi2
 */
public class ClientCommandDataSocket extends WebSocketAdapter implements CommandDataSocket{
    private final URI wsUrl;
    private final ConcurrentLinkedQueue<byte[]> msgQ=new ConcurrentLinkedQueue();
    private final SocketConnectionListener connectionListener;
    private volatile CommandDataReadListener commandDataReadListener;
    private volatile WebSocket ws=null;
    private volatile Encryptor encryptor;
    private volatile Decryptor decryptor;
    private volatile long openTime=0;
    private volatile int openAttempt=0;
    private volatile boolean opened=false;
    private boolean closeCalled=false;
    private volatile boolean closed=false;
    private static final Charset UTF8charset=Charset.forName("UTF-8");
    
    private ClientCommandDataSocket(String url,SocketConnectionListener connectionListener,CommandDataReadListener commandDataReadListener,Encryptor encryptor,Decryptor decryptor) {
        this.connectionListener=connectionListener;
        try{
            StringBuilder sb=new StringBuilder(54);            
            this.wsUrl=new URI(url);
            this.commandDataReadListener=commandDataReadListener;
            this.encryptor=encryptor;
            this.decryptor=decryptor;
        }catch(URISyntaxException ex){
            throw new RuntimeException(ex);
        }
    }
    
    @Override
    public void onConnected(WebSocket websocket, Map<String, List<String>> headers) throws Exception {
        if(!this.opened){
            this.opened=true;
            if(this.connectionListener!=null)
                this.connectionListener.socketConnected(this);
        }
        //remove all but last 3 messages from mesg queue
        while(this.msgQ.size()>3){
            this.msgQ.poll();
        }
        //send mesgs left To send
        while(!this.msgQ.isEmpty()){
            this.ws.sendBinary(this.msgQ.poll());
            this.ws.flush();
        }        
    }

    @Override
    public void onBinaryMessage(WebSocket websocket, byte[] bytes) throws Exception {
        CommandDataReadListener commandDataReadListener=this.commandDataReadListener;
        if(bytes==null || isClosed() || commandDataReadListener==null)
            return;
        if(decryptor!=null)
            bytes=decryptor.decrypt(bytes);
        int commandBytesLength=bytes[0] & 127;
        boolean isText=(bytes[0] & 128)==128;
        String command=new String(bytes, 1, commandBytesLength, UTF8charset);
        byte[] data=bytes.length>commandBytesLength+1?Arrays.copyOfRange(bytes, commandBytesLength+1, bytes.length):null;
        if(isText){
            commandDataReadListener.commandDataRead(this, command,data==null?null:new String(data, UTF8charset));
        }
        else{
            commandDataReadListener.commandDataReadBytes(this,command,data);
        }
    }

    @Override
    public void onConnectError(WebSocket websocket, WebSocketException exception) throws Exception {
        this.onClose(exception);
    }

    @Override
    public void onDisconnected(WebSocket websocket, WebSocketFrame serverCloseFrame, WebSocketFrame clientCloseFrame, boolean closedByServer) throws Exception {
        this.onClose(null);
    }

    @Override
    public boolean isClosed(){
        return this.closed;
    }
    
    private void onClose(Exception ex){
        if(this.closed)
            return;
        if(!this.closeCalled && !this.opened && (this.openTime-System.currentTimeMillis())<10000 && this.openAttempt<3){
            this.createWebSocket();
            return;
        }
        if(this.closed)
            return;
        while(!this.msgQ.isEmpty())
            this.msgQ.poll();
        this.closed=true;		
        if(this.connectionListener!=null){
            if(this.closeCalled)
                this.connectionListener.socketClosed(this);
            else if(this.opened)
                this.connectionListener.socketDisconnected(this);
            else
                this.connectionListener.connectionFailed(this,ex);
        }        
    }
    
    private static WebSocketFactory webSocketFactory=new WebSocketFactory();
    private void createWebSocket(){
        if(this.closeCalled||this.opened)
            return;
        if(this.openTime==0){
            this.openTime=System.currentTimeMillis();
            if(this.connectionListener!=null)
                this.connectionListener.socketConnecting(this);
        }
        this.openAttempt++;
        try{
            this.ws=webSocketFactory.createSocket(this.wsUrl);
            this.ws.connect();
        }catch(Exception ex){
            this.onClose(ex);
            return;
        }
        this.ws.addListener(this);
    }
    
    public static ClientCommandDataSocket newInstance(String url,SocketConnectionListener connectionListener,CommandDataReadListener commandDataReadListener,Encryptor encryptor,Decryptor decryptor) throws IOException{
        ClientCommandDataSocket commandDataSocket=new ClientCommandDataSocket(url,connectionListener,commandDataReadListener,encryptor,decryptor);
        commandDataSocket.createWebSocket();
        return commandDataSocket;
    }

    public static ClientCommandDataSocket newInstance(String url,SocketConnectionListener connectionListener,CommandDataReadListener commandDataReadListener) throws IOException{
        return newInstance(url, connectionListener, commandDataReadListener, null, null);
    }
    
    @Override
    public void close(){
        WebSocket wsObj=this.ws;
        if(this.closed)
            return;
        this.ws=null;
        this.closeCalled=true;
        if(wsObj!=null && wsObj.isOpen())
            wsObj.disconnect();
        else
            this.onClose(null);
    }

    @Override
    public void setCommandDataReadListener(CommandDataReadListener commandDataReadListener) {
        this.commandDataReadListener=commandDataReadListener;
    }

    @Override
    public void setEncryptorDecryptor(Encryptor encryptor, Decryptor decryptor) {
        this.encryptor=encryptor;
        this.decryptor=decryptor;
    }
    
    @Override
    public void write(String command,String data){
        if(this.closed)
            throw new RuntimeException("Socket is closed");
        writeBytes(command, data==null?null:data.getBytes(UTF8charset), true);
    }

    @Override
    public void write(String command,byte[] data){
        if(this.closed)
            throw new RuntimeException("Socket is closed");
        writeBytes(command, data, false);
    }
    
    private void writeBytes(String command,byte[] data,boolean isText){
        byte[] commandBytes=command.getBytes(UTF8charset);
        byte[] bytes=new byte[commandBytes.length+1 + (data==null?0:data.length)];
        bytes[0]=(byte)(commandBytes.length | (isText?128:0));
        System.arraycopy(commandBytes, 0, bytes, 1, commandBytes.length);
        if(data!=null)
            System.arraycopy(data, 0, bytes, commandBytes.length+1, data.length);
        if(encryptor!=null)
            bytes=encryptor.encrypt(bytes);
        if(this.ws==null){
            this.msgQ.add(bytes);
            this.createWebSocket();
            return;
        }
        if(this.ws.isOpen()){
            this.ws.sendBinary(bytes);
            this.ws.flush();
        }
        else {
            this.msgQ.add(bytes);
        }
    } 
    
    public boolean isOpen(){
        WebSocket clientSocket=this.ws;
        return clientSocket!=null && clientSocket.isOpen();
    }
    
    @Override
    public boolean isLocal() {
        return false;
    }
}
