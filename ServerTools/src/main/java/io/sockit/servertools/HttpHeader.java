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
    public final String name;
    public final String value;

    public HttpHeader(String headerName, String headerValue) {
        this.name = headerName;
        this.value = headerValue;
    }
}
