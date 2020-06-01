/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedDeque;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author h
 */
public class CommandDataTest {
    
    public CommandDataTest() {
    }
    
    @BeforeAll
    public static void setUpClass() throws IOException {
        WebSocketServer.startAsHttp(8080, new ServerConnectionListener());
    }
    
    @AfterAll
    public static void tearDownClass() {
        WebSocketServer.shutdown();
    }

    static class ServerConnectionListener implements SocketConnectionListener{
        @Override
        public void newConnection(AbstractSocket socket) {
            Console.log("new connection on server");
            CommandDataSocket commandDataSocket=(CommandDataSocket)socket;
            commandDataSocket.setCommandDataReadListener(serverReader);
        }        
    }
    
    static ConcurrentLinkedDeque<String> commandsReadOnServer=new ConcurrentLinkedDeque();
    static String lastCommandReadOnServer(){
        if(!commandsReadOnServer.isEmpty())
            return commandsReadOnServer.getLast();
        return null;
    }
    
    final static ServerReader serverReader=new ServerReader();
    static class ServerReader implements CommandDataReadListener{
        
        @Override
        public void commandDataRead(CommandDataSocket socket,String command, String data) {
            commandsReadOnServer.add(command);
            socket.write(command, data);
        }

        @Override
        public void commandDataRead(CommandDataSocket socket,String command, byte[] data) {
        }
        
    }
    
    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void testRemoteClient() throws Exception{
         Console.log("in testRemoteClient");
         Thread.sleep(100);         
         Client client1=new Client("ws://localhost:8080","client1");
         Client client2=new Client("ws://localhost:8080","client2");
         Thread.sleep(10);
         assertTrue(client1.isConnected());
         client1.write("test", "client1");
         client1.write("test1", "client1");
         client1.write("test2", "client1");
         Thread.sleep(50);
         assertTrue("test2".equals(lastCommandReadOnServer()));
         assertTrue("test2".equals(client1.lastCommandRead));
         client2.write("test", "client2");
         client2.write("test1", "client2");
         Thread.sleep(50);
         assertTrue("test1".equals(lastCommandReadOnServer()));
         assertTrue("test1".equals(client2.lastCommandRead));
         client2.write("test2", "client2");
         client2.write("test3", (String)null);
         Thread.sleep(20);
         client2.close();
         client1.write("test3", "client1");
         Thread.sleep(20);
         client1.close();
         Thread.sleep(50);
         assertTrue("test3".equals(lastCommandReadOnServer()));
         assertTrue("test3".equals(client1.lastCommandRead));
         assertTrue("client1".equals(client1.lastDataRead.textData));
         assertTrue("test3".equals(client2.lastCommandRead));
         assertTrue(client2.lastDataRead.textData==null);
     }
     
     @Test
     public void testLocalClient() throws Exception{
         Console.log("in testLocalClient");
         Thread.sleep(100);         
         Client client1=new Client(LocalCommandDataSocket.newInstance(),"client1");
         Client client2=new Client(LocalCommandDataSocket.newInstance(),"client2");
         Thread.sleep(10);
         assertTrue(client1.isConnected());
         client1.write("test", "client1");
         client1.write("test1", "client1");
         client1.write("test2", "client1");
         Thread.sleep(50);
         assertTrue("test2".equals(lastCommandReadOnServer()));
         Console.log(client1.lastCommandRead);
         assertTrue("test2".equals(client1.lastCommandRead));
         client2.write("test", "client2");
         client2.write("test1", "client2");
         Thread.sleep(50);
         assertTrue("test1".equals(lastCommandReadOnServer()));
         assertTrue("test1".equals(client2.lastCommandRead));
         client2.write("test2", "client2");
         client2.write("test3", (String)null);
         Thread.sleep(20);
         client2.close();
         client1.write("test3", "client1");
         Thread.sleep(20);
         client1.close();
         Thread.sleep(50);
         assertTrue("test3".equals(lastCommandReadOnServer()));
         assertTrue("test3".equals(client1.lastCommandRead));
         assertTrue("client1".equals(client1.lastDataRead.textData));
         assertTrue("test3".equals(client2.lastCommandRead));
         assertTrue(client2.lastDataRead.textData==null);
     }
          
}
