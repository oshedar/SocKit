/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.sockit.servertools;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author hoshi2
 */
public class RemoteCommandDataSocket implements CommandDataSocket{
    private volatile ChannelPipeline channelPipeline;
    private volatile Encryptor encryptor;
    private volatile Decryptor decryptor;
    private volatile CommandDataReadListener commandDataReadListener;
    private volatile SocketClosedListener socketClosedListener;
    private static final Charset UTF8charset=Charset.forName("UTF-8");
    
    private RemoteCommandDataSocket(ChannelPipeline channelPipeline) {
        this.channelPipeline=channelPipeline;
        channelPipeline.channel().closeFuture().addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture f) throws Exception {
                SocketClosedListener socketClosedListener=RemoteCommandDataSocket.this.socketClosedListener;
                if(socketClosedListener!=null)
                    socketClosedListener.socketClosed(RemoteCommandDataSocket.this);
            }
        });
    }
    
    static RemoteCommandDataSocket newInstance(ChannelPipeline channelPipeline){
        if(channelPipeline==null){
            return null;
        }
        return new RemoteCommandDataSocket(channelPipeline);
    }

    @Override
    public void setEncryptorDecryptor(Encryptor encryptor,Decryptor decryptor){
        this.encryptor=encryptor;
        this.decryptor=decryptor;
    }
    
    @Override
    public boolean isClosed(){
        return channelPipeline==null || channelPipeline.channel().isOpen()==false;
    }
    
    @Override
    public void close(){
        instanceLock.lock();
        try{
            ChannelPipeline channelPipeline=this.channelPipeline;
            if(channelPipeline==null)
                return;
            this.channelPipeline=null;
            channelPipeline.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
//            channelPipeline.close();
        }finally{instanceLock.unlock();}
    }
    
    @Override
    public void setSocketClosedListener(SocketClosedListener socketClosedListener){
        this.socketClosedListener=socketClosedListener;
    }

    @Override
    public void setCommandDataReadListener(CommandDataReadListener commandDataReadListener) {
        this.commandDataReadListener = commandDataReadListener;
    }
    
    private ConcurrentLinkedQueue<byte[]> messageQ=new ConcurrentLinkedQueue();
    private Lock instanceLock=new ReentrantLock();
    
    public void bytesRead(byte[] bytes){
        messageQ.add(bytes);
        Executor.execute(qProcessor);
    }
    
    private Runnable qProcessor=new QProcessor();
    private class QProcessor implements Runnable{

        @Override
        public void run() {
            processMessageQ();
        }
        
    }
    
    private void processMessageQ(){
        instanceLock.lock();
        try{
            byte[] bytes=messageQ.poll();
            if(bytes==null || isClosed() || commandDataReadListener==null)
                return;
            if(decryptor!=null)
                bytes=decryptor.decrypt(bytes);
            int commandLength=bytes[0] & 127;            
            boolean isText=(bytes[0] & 128)==128;
            String command=new String(bytes, 1, commandLength, UTF8charset);
            byte[] data=bytes.length>commandLength+1?Arrays.copyOfRange(bytes, commandLength+1, bytes.length):null;
            if(isText){
                commandDataReadListener.commandDataRead(this, command,data==null?null:new String(data, UTF8charset));
            }
            else{
                commandDataReadListener.commandDataRead(this,command,data);
            }
        }catch(Exception ex){
            Utils.log(ex);
        }
        finally{instanceLock.unlock();}
    }
     
    @Override
    public void write(String command,String data) {
        if(this.isClosed())
            return;
        write(command, data==null?null:data.getBytes(UTF8charset), true);
    }

    @Override
    public void write(String command,byte[] data) {
        if(this.isClosed())
            return;
        write(command, data, false);
    }
    
    private void write(String command,byte[] data,boolean isText){
        byte[] commandBytes=command.getBytes(UTF8charset);
        byte[] bytes=new byte[commandBytes.length+1 + (data==null?0:data.length)];
        bytes[0]=(byte)(commandBytes.length | (isText?128:0));
        System.arraycopy(commandBytes, 0, bytes, 1, commandBytes.length);
        if(data!=null)
            System.arraycopy(data, 0, bytes, commandBytes.length+1, data.length);
        if(encryptor!=null)
            bytes=encryptor.encrypt(bytes);
        ChannelPipeline channelPipeline=this.channelPipeline;
        if(channelPipeline!=null){
            channelPipeline.writeAndFlush(new BinaryWebSocketFrame(Unpooled.wrappedBuffer(bytes)));
        }
    } 
    
    private static final int maxBytesSize=(int)(Math.pow(2, 24) - 1);

    @Override
    public boolean isLocal() {
        return false;
    }

    @Override
    public InetAddress getRemoteAddress() {
        ChannelPipeline channelPipeline=this.channelPipeline;
        if(channelPipeline==null)
            return null;
        return ((InetSocketAddress)channelPipeline.channel().remoteAddress()).getAddress();
    }

    @Override
    public int getRemotePort() {
        ChannelPipeline channelPipeline=this.channelPipeline;
        if(channelPipeline==null)
            return 0;
        return ((InetSocketAddress)channelPipeline.channel().remoteAddress()).getPort();
    }

    private static int toInt(byte[] bytes){ // converts 3 bytes to int. big endian - for c# (little endian) reverse the bytes
        return ( ((bytes[0]&0xFF)<<16) | ((bytes[1]&0xFF)<<8) | ((bytes[2]&0xFF)) ) ;
    }
        
    private static void copyIntToByteArray(int value,byte[] bytes){
        bytes[2]=(byte)(value & 0xFF);
        bytes[1]=(byte)((value>>>8) & 0xFF);
        bytes[0]=(byte)((value>>>16) & 0xFF);        
    }
    
}
