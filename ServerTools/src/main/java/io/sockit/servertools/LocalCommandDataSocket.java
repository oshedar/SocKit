/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 *
 * @author h
 */
public class LocalCommandDataSocket implements CommandDataSocket{
    volatile private CommandDataReadListener commandDataReadListener;
    volatile private LocalCommandDataSocket oppositeLocalCommandDataSocket;
    private AtomicBoolean closed=new AtomicBoolean(false);
    volatile private SocketClosedListener socketClosedListener;
    public final boolean isInitiator;
    private AtomicBoolean connectionListenerInvoked=new AtomicBoolean(false);
    Lock instanceLock=new ReentrantLock();

    private LocalCommandDataSocket() {
        this.isInitiator=true;
    }
    
    private LocalCommandDataSocket(LocalCommandDataSocket oppositeSocket){
        this.isInitiator=false;
        this.oppositeLocalCommandDataSocket=oppositeSocket;
    }
    
    public static LocalCommandDataSocket newInstance(){
        LocalCommandDataSocket socket=new LocalCommandDataSocket();
        socket.oppositeLocalCommandDataSocket=new LocalCommandDataSocket(socket);
        SocketConnectionListener connectionListener=LocalCommandDataSocket.connectionListener;
        if(connectionListener!=null)
            connectionListener.newConnection(socket.oppositeLocalCommandDataSocket);
        return socket;
    }
    public LocalCommandDataSocket getOpposite(){
        return oppositeLocalCommandDataSocket;
    }
    
    @Override
    public void close() {
        if(closed.compareAndSet(false, true)){
            LocalCommandDataSocket oppLocalCommandDataSocket=this.oppositeLocalCommandDataSocket;
            if(oppLocalCommandDataSocket!=null)
                oppLocalCommandDataSocket.oppositeLocalCommandDataSocket=null;
            //call closed listener
            SocketClosedListener closedListener=this.socketClosedListener;
            if(closedListener!=null){
                try{
                    closedListener.socketClosed(this);
                }catch(Exception ex){Utils.log(ex);}
            }
            //close opposite command data socket
            if(oppLocalCommandDataSocket!=null && oppLocalCommandDataSocket.closed.get()==false)
                Executor.execute(new CloseOpposite(oppLocalCommandDataSocket));
        }
    }
    
    private static class CloseOpposite implements Runnable{
        LocalCommandDataSocket socketToclose;

        CloseOpposite(LocalCommandDataSocket socketToclose) {
            this.socketToclose = socketToclose;
        }

        @Override
        public void run() {
            socketToclose.close();
        }
        
    }

    @Override
    public boolean isClosed() {
        return closed.get();
    }

    @Override
    public void setCommandDataReadListener(CommandDataReadListener commandDataReadListener) {
        if(closed.get())
            return;
        this.commandDataReadListener=commandDataReadListener;
    }

    @Override
    public void setEncryptorDecryptor(Encryptor byteEncryptor, Decryptor byteDecryptor) {
        return;
    }

    @Override
    public void setSocketClosedListener(SocketClosedListener socketClosedListener) {
        if(closed.get())
            return;
        this.socketClosedListener=socketClosedListener;
    }

    @Override
    public void write(String command, String data) {
        if(!closed.get()){
//            if(isInitiator && connectionListenerInvoked.compareAndSet(false, true)==true){
//                SocketConnectionListener connectionListener=LocalCommandDataSocket.connectionListener;
//                if(connectionListener!=null)
//                    connectionListener.newConnection(this.oppositeLocalCommandDataSocket);
//            }
            LocalCommandDataSocket oppositeLocalCommandDataSocket=this.oppositeLocalCommandDataSocket;
            if(oppositeLocalCommandDataSocket!=null && !oppositeLocalCommandDataSocket.closed.get()){
                oppositeLocalCommandDataSocket.processData(command, data);
            }
        }
    }
    
    @Override
    public void write(String command, byte[] data) {
        if(!closed.get()){
            LocalCommandDataSocket oppositeLocalCommandDataSocket=this.oppositeLocalCommandDataSocket;
            if(oppositeLocalCommandDataSocket!=null && !oppositeLocalCommandDataSocket.closed.get()){
                oppositeLocalCommandDataSocket.processData(command, data);
            }
        }
    }

    @Override
    public boolean isLocal() {
        return true;
    }

    @Override
    public InetAddress getRemoteAddress() {
        return null;
    }

    @Override
    public int getRemotePort() {
        return -1;
    }
    
    
    private static class DataToProcess{
        private String command;
        private String stringData;
        private byte[] byteData;
        private byte dataType;
        private CommandDataReadListener commandDataReadListener;

        public DataToProcess(String command, String stringData, byte[] byteData, byte dataType, CommandDataReadListener commandDataReadListener) {
            this.command = command;
            this.stringData = stringData;
            this.byteData = byteData;
            this.dataType = dataType;
            this.commandDataReadListener = commandDataReadListener;
        }
        
    }
    
    private Queue<DataToProcess> dataToProcessQueue=new ConcurrentLinkedQueue();
    private void processData(String command, String data){
        CommandDataReadListener commandDataReadListener=this.commandDataReadListener;
        if(commandDataReadListener==null){
            return;
        }
        dataToProcessQueue.add(new DataToProcess(command, data, null, (byte)1, commandDataReadListener));
        Executor.execute(processDataRunnable);
    }

    private void processData(String command, byte[] data){
        CommandDataReadListener commandDataReadListener=this.commandDataReadListener;
        if(commandDataReadListener==null)
            return;
        dataToProcessQueue.add(new DataToProcess(command, null, data, (byte)0, commandDataReadListener));
        Executor.execute(processDataRunnable);
    }
    
    private final ProcessData processDataRunnable=new ProcessData();
    private class ProcessData implements Runnable{

        @Override
        public void run() {
            DataToProcess dataToProcess;
            instanceLock.lock();
            try{
                if(isClosed())
                    return;
                if((dataToProcess=dataToProcessQueue.poll())!=null){
                    if(dataToProcess.dataType==1)
                        dataToProcess.commandDataReadListener.commandDataRead(LocalCommandDataSocket.this,dataToProcess.command, dataToProcess.stringData);
                    else
                        dataToProcess.commandDataReadListener.commandDataRead(LocalCommandDataSocket.this,dataToProcess.command, dataToProcess.byteData);
                }
            }finally{instanceLock.unlock();}
        }
        
    }
    
    private static volatile SocketConnectionListener connectionListener;
    static void setConnectionListener(SocketConnectionListener connectionListener){
        LocalCommandDataSocket.connectionListener=connectionListener;
    }
    
}
