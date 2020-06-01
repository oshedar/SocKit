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
public final class HttpParam {
    public final String paramName;
    public final String paramvalue;

    public HttpParam(String paramName, String paramvalue) {
        this.paramName = paramName;
        this.paramvalue = paramvalue;
    }    
}
