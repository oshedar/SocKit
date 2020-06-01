/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author 
 */
public final class HttpClientResponse {
    public int code;
    public final String body;
    public final byte[] binaryBody;
    public final InputStream binaryStream;
    public final boolean isBinary;
    public final boolean isStream;
    final Map<String,List<String>> headerFields;
    
    public HttpClientResponse(int code, String body,Map<String,List<String>> headerFields) {
        this.code = code;
        this.body = body;
        this.binaryBody=null;
        isBinary=false;
        isStream=false;
        binaryStream=null;
        this.headerFields=headerFields;
    }

    public HttpClientResponse(int code, byte[] body,Map<String,List<String>> headerFields) {
        this.code = code;
        if(code>=400) {
            this.body = new String(body);
        }
        else {
            this.body = null;
        }
        this.binaryBody=body;
        isBinary=true;
        isStream=false;
        binaryStream=null;
        this.headerFields=headerFields;
    }

    public HttpClientResponse(int code, InputStream inputStream,Map<String,List<String>> headerFields) {
        this.code = code;
        this.body=null;
        this.binaryBody=null;
        isBinary=true;
        isStream=true;
        this.binaryStream=inputStream;
        this.headerFields=headerFields;
    }

    public boolean hasError(){
        return code>=400;
    }
    
    public String getHeaderValue(String headerName){
        if(headerFields==null)
            return null;
        List<String> values=headerFields.get(headerName);
        if(values!=null && !values.isEmpty())
            return values.get(0);
        return null;
    }

    public List<String> getHeaderValues(String headerName){
        if(headerFields==null)
            return null;
        return headerFields.get(headerName);
    }
    
    public Set<String> getHeaderNames(){
        if(headerFields==null)
            return null;
        return headerFields.keySet();
    }
}
