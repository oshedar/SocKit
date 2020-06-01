/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * A basic implementation of the WeResource interface. An instance of this class represents a web resource that will be sent by the server to the web client such as your web browser.
 * 
 */
class BasicWebResponse implements WebResponse{
    private File file;
    private String mimeType;
    private List<HttpHeader> responseHeaders;
    private int responseStatusCode;
    private FastGrowingOutputStream outputStream=null;
    private Writer writer=null;
    
    /**
     * Constructs a new empty Web Resource - without any body 
     */
    public BasicWebResponse() {
    }
    
    @Override
    public void setBody(File file) {
        if(outputStream!=null){
            if(writer!=null)
                throw new IllegalStateException("Cant be called after getWriter()");
            throw new IllegalStateException("Cant be called after getOutputStream()");
        }
        this.file = file;
        String resourceName=file.getName();
        int indextOfDot=resourceName.lastIndexOf('.');
        String extension=null;
        if(indextOfDot>=0)
            extension=resourceName.substring(indextOfDot);
        if(this.mimeType==null)
            this.mimeType=MimeTypes.getMimeType(extension);
    }

    @Override
    public OutputStream getOutputStream() {
        if(outputStream!=null){
            if(writer!=null)
                throw new IllegalStateException("Cant be called after getWriter()");
            return outputStream;
        }
        if(file!=null)
            throw new IllegalStateException("Cant be called after setBody()");
        outputStream=new FastGrowingOutputStream();
        return outputStream;
    }

    @Override
    public Writer getWriter() {
        if(writer!=null){
            return writer;
        }
        if(outputStream!=null)
            throw new IllegalStateException("Cant be called after getWriter()");
        if(file != null)
            throw new IllegalStateException("Cant be called after setBody()");
        
        outputStream=new FastGrowingOutputStream();
        writer=new OutputStreamWriter(outputStream);
        return writer;
    }

    @Override
    public boolean isFile() {
        return file!=null?true:outputStream!=null && outputStream.isFile();
    }

    @Override
    public File getFile() {
        if(file!=null)
            return file;
        return file!=null?file:outputStream!=null?outputStream.getFile():null;
    }
    
    void closeOutputStream(){
        if(outputStream!=null){
            try{
                outputStream.close();
            }catch(IOException ex){
                throw new RuntimeException(ex);
            }
        }
    }

    @Override
    public InputStream getStream() {
        try{
            return file!=null?new FileInputStream(file):outputStream!=null?outputStream.getInputStream():null;
        }catch(IOException ex){
            throw new RuntimeException(ex);
        }
    }
   
    @Override
    public String getMimeType() {
        return this.mimeType;
    }

    @Override
    public long getByteCount() {
        if(file!=null)
            return file.length();
        if(outputStream!=null)
            return outputStream.getSize();
        return 0;
    }

    @Override
    public List<HttpHeader> getHttpResponseHeaders() {
        return this.responseHeaders;
    }
    
    @Override
    public void addHttpResponseHeader(String name,String value){
        if(name.equalsIgnoreCase("content-type")){
            if(value!=null)
                this.mimeType=value;
            return;
        }
        if(responseHeaders==null)
            responseHeaders=new ArrayList(4);
        responseHeaders.add(new HttpHeader(name, value));
    }

    @Override
    public void setStatus(int statusCode) {
        this.responseStatusCode = statusCode;
    }

    @Override
    public int getStatus() {
        return responseStatusCode;
    }

    @Override
    public void setMimeType(String mimeType) {
        this.mimeType=mimeType;
    }
    
}
