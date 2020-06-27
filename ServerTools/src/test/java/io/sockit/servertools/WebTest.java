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
         WebSocketServer.addWebHandler("*",".*",new BasicWebHandler(webRootFolder));
         WebSocketServer.startAsHttp(8080,-1,false,null);
         try{
             HttpClientResponse response=HttpClient.doGet("http://localhost:8080/test.txt", null);
             assertTrue(response.body.equals("test"));
         }finally{
             WebSocketServer.shutdown();
         }        
     }
     
     //Before running this test edit hosts file and add entry to point sockit.io to 127.0.0.1 and funwithcode.in to 127.0.0.1
//     @Test
     public void testSslWeb() throws URISyntaxException,IOException, Exception{
         File webRootFolder=new File(WebTest.class.getResource("/web").toURI());
         assertTrue(webRootFolder.isDirectory());        
         WebSocketServer.addWebHandler("sockit.io",".*",new BasicWebHandler(webRootFolder));
         WebSocketServer.addWebHandler("funwithcode.in",".*",new BasicWebHandler(webRootFolder));
         WebSocketServer.addSslCertificate("sockit.io",new File("/home/hoshi/sites/sockit/keys/sockit.pfx"), "test@123",null);
         WebSocketServer.addSslCertificate("funwithcode.in",new File("/home/hoshi/sites/funwithcode/key.pfx"), "fun@123",null);
         WebSocketServer.startAsHttps(8080, -1,null,false);
         try{
             HttpClientResponse response;
             response=HttpClient.doGet("https://sockit.io:8080/test.txt", null);
             assertTrue(response.body.equals("test"));
             response=HttpClient.doGet("https://funwithcode.in:8080/test.txt", null);
             assertTrue(response.body.equals("test"));
         }finally{
             WebSocketServer.shutdown();
         }        
         
     }
     
}
