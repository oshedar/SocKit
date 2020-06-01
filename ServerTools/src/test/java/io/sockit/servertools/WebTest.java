/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author oshedar
 */
public class WebTest {
    
    public WebTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
       
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
     @Test
     public void testWeb() throws URISyntaxException,IOException {
         File webRootFolder=new File(WebTest.class.getResource("/web").toURI());
         assertTrue(webRootFolder.isDirectory());        
         WebSocketServer.addWebHandler(".*",new BasicWebHandler(webRootFolder));
         WebSocketServer.startAsHttp(8080, null);
         try{
             HttpClientResponse response=HttpClient.doGet("http://localhost:8080/test.txt", null);
             assertTrue(response.body.equals("test"));
         }finally{
             WebSocketServer.shutdown();
         }
        
     }
}
