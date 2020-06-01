/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.net.SocketAddress;
import java.util.List;

/**
 *
 * @author oshedar
 */
class BasicWebRequest implements WebRequest{
    final SocketAddress remoteAddress;
    final List<HttpHeader> requestHeaders;
    final String hostName;
    final String resourcePath;
    final String queryString;

    public BasicWebRequest(SocketAddress remoteAddress, List<HttpHeader> requestHeaders, String hostName, String resourcePath, String queryString) {
        this.remoteAddress = remoteAddress;
        this.requestHeaders = requestHeaders;
        this.hostName = hostName;
        this.resourcePath = resourcePath;
        this.queryString = queryString;
    }

    @Override
    public SocketAddress getRemoteAddress() {
        return remoteAddress;
    }

    @Override
    public List<HttpHeader> getRequestHeaders() {
        return requestHeaders;
    }

    @Override
    public String getHostName() {
        return hostName;
    }

    @Override
    public String getResourcePath() {
        return resourcePath;
    }

    @Override
    public String getQueryString() {
        return queryString;
    }

    @Override
    public String getHeaderValue(String headerName) {
        if(requestHeaders==null)
            return null;
        for(HttpHeader header:requestHeaders){
            if(header.name.equalsIgnoreCase(headerName))
                return header.value;
        }
        return null;        
    }

}
