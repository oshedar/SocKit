/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.List;

/**
 * An instance of this interface represents an http response that will be sent by the server to the web client such as your web browser.
 * 
 */
public interface WebResponse {
    /**
     * Returns whether the resource is a File
     * @return - true if the resource is a File 
     */
    boolean isFile();

    /**
     * Returns the content of this resource as a File. Should return null if resource is not a file or if content is empty
     * @return - the content of this resource as a File. Should return null if resource is not a file  or if content is empty
     */
    File getFile();

    /**
     * Returns the content of this resource as an InputStream. Should return null if resource is a File or if content is empty
     * @return - the content of this resource as an InputStream. Should return null if resource is a File or if content is empty
     */
    InputStream getStream();

    /**
     * Returns the mime type for the specified file extension
     * @return - the mime type
     */
    String getMimeType();

    /**
     * Returns the total number of bytes in the response body
     * @return - the total number of bytes in the resource
     */
    long getByteCount();

    /**
     * Returns the list of headers to be added to the response
     * @return - the list of headers to be added to the response
     */
    List<HttpHeader> getHttpResponseHeaders();
    
    /**
     * Add an http response header to this resource. This header will be added to the http response sent to the web client
     * @param name - the header name
     * @param value - the header value
     */
    void addHttpResponseHeader(String name,String value);

        /**
     * Returns the http response status code
     * @return - the response status code
     */
    int getStatus();
    
    /**
     * Sets the http response status code of this response
     * @param statusCode - the http status code 
     */
    void setStatus(int statusCode);
    
    
    /**
     * Sets the mime type of this response
     * @param mimeType - the mime type of the response
     */
    void setMimeType(String mimeType);
    
    /**
     * Sets the body of the response to a file
     * @param file - the file to which the body is set
     */
    void setBody(File file);
    
    /**
     * Returns an OutputStream suitable for writing binary data to the response body
     * @return - the outputstream to write binary data to the response body
     */
    OutputStream getOutputStream();
    
    /**
     * Returns a Writer suitable for writing character data to the response body
     * @return - the writer to write character data to the response body
     */
    Writer getWriter();
    
}
