/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.util.Date;

/**
 *
 * @author 
 */
public class HttpCookie {
    public final String name;
    public final String value;
    public final Date expiresOn;

    public HttpCookie(String name, String value, Date expiresOn) {
        this.name = name;
        this.value = value;
        this.expiresOn = expiresOn;
    }

    public HttpCookie(String name, String value) {
        this(name, value, null);
    }
    
}
