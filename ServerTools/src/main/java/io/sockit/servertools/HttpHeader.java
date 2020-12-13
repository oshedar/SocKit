/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

/**
 *
 * @author 
 */
public final class HttpHeader {
    public final String headerName;
    public final String headerValue;

    public HttpHeader(String headerName, String headerValue) {
        this.headerName = headerName;
        this.headerValue = headerValue;
    }
}
