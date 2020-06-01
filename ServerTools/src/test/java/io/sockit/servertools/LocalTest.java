/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author h
 */
public class LocalTest {
    static class ReadListener implements CommandDataReadListener{
        CommandDataSocket commandDataSocket;
        String lastCommandRead;
        String lastStringDataRead;
        byte[] lastByteDataRead;

        @Override
        public void commandDataRead(CommandDataSocket socket,String command, String data) {
            lastCommandRead=command;
            lastStringDataRead=data;
        }

        @Override
        public void commandDataRead(CommandDataSocket socket,String command, byte[] data) {
            lastCommandRead=command;
            lastByteDataRead=data;
        }
        
    }
    public LocalTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void test() {
         LocalCommandDataSocket client1=LocalCommandDataSocket.newInstance();
         LocalCommandDataSocket client2=client1.getOpposite();
         ReadListener client2Reader=new ReadListener();
         client2.setCommandDataReadListener(client2Reader);
         client1.write("abc", "1");
         client1.write("efg", "2");
         client1.write("xyz", "3");
         try{Thread.sleep(50);}catch(Exception ex){}
         assertTrue("xyz".equals(client2Reader.lastCommandRead));
         assertTrue("3".equals(client2Reader.lastStringDataRead));
         client1.write("pqr", new byte[1]);
         try{Thread.sleep(50);}catch(Exception ex){}
         assertTrue("pqr".equals(client2Reader.lastCommandRead));
     }
}
