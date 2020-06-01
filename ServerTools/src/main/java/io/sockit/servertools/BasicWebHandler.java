/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * A Basic Web Handler for web requests. This Class implements the WebHandler interface.
 * @author Hoshedar Irani
 */
public class BasicWebHandler implements WebHandler{
    private File rootDir;

    /**
     * Constructs a new BasicWebHandler object with the specified folder as the web root folder
     * @param webRoootFolder - the web root folder
     */
    public BasicWebHandler(File webRoootFolder) {
        this.rootDir = webRoootFolder;
        if(!webRoootFolder.exists())
            throw new IllegalArgumentException("webRootFolder does not exist");
        if(!webRoootFolder.isDirectory())
            throw new IllegalArgumentException("webRootFolder is not a folder/directory");
        if(!webRoootFolder.isDirectory())
            throw new IllegalArgumentException("webRootFolder is not accessible");
    }
    
    /**
     * Constructs a new BasicWebHandler object with the specified folder as the web root folder
     * @param webRoootFolder - the web root folder
     */
    public BasicWebHandler(String webRoootFolder) {
        this.rootDir = new File(webRoootFolder);
    }
    
    private void processRequest(WebRequest webRequest,WebResponse webResponse){
        File file;    
        String webPath=webRequest.getResourcePath();
        if(webPath.equals("/")){
            file=new File(rootDir, "index.html");
            if(!file.exists())
                file=new File(rootDir, "index.htm");
            if(!file.exists()){
                webResponse.setStatus(404);
                return;
            }
            webResponse.setBody(file);
            return;
        }
        
        file= new File(rootDir, webPath.substring(1));
        if(file.exists()){
            if(file.isFile()){
                webResponse.setBody(file);
                return;
            }
            File parentFolder=file;
            file=new File(parentFolder,"index.html");
            if(file.exists()){
                if(!webPath.endsWith("/"))
                    webPath+="/index.html";
                else
                    webPath+="index.html";
                webResponse.addHttpResponseHeader("Location", webPath + webRequest.getQueryString());
                webResponse.setStatus(301);
                return;
            }
            file=new File(parentFolder,"index.htm");
            if(file.exists()){
                if(!webPath.endsWith("/"))
                    webPath+="/index.htm";
                else
                    webPath+="index.htm";
                webResponse.addHttpResponseHeader("Location", webPath + webRequest.getQueryString());
                webResponse.setStatus(301);
                return;
            }
        }
        webResponse.setStatus(404);//not found        
    }
    
    byte[] notFoundErrorPrefix=("<html><head>\n" +
        "<title>404 Not Found</title>\n" +
        "</head><body>\n" +
        "<h1>Not Found</h1>\n" +
        "<p>The requested resource ").getBytes(Utils.utf8Charset);
    byte[] notFoundErrorSuffix=(" was not found on this server.</p>\n" +
            "</body></html>").getBytes(Utils.utf8Charset);
    
    @Override
    public void processWebRequest(WebRequest webRequest,WebResponse webResponse) {
        processRequest(webRequest, webResponse);
        if(webResponse.getStatus()==404 && webResponse.getByteCount()<1){
            try{
                OutputStream outputStream=webResponse.getOutputStream();
                outputStream.write(notFoundErrorPrefix);
                outputStream.write(webRequest.getResourcePath().getBytes(Utils.utf8Charset));
                outputStream.write(notFoundErrorSuffix);
            }catch(IOException ex){
                throw new RuntimeException(ex);
            }
        }
    }
    
}
