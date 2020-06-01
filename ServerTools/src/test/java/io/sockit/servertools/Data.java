/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

/**
 *
 * @author Hoshedar Irani
 */
public class Data {
    public final String textData;
    public final byte[] binaryData;

    public Data(String textData) {
        this.textData = textData;
        this.binaryData=null;
    }

    public Data(byte[] binaryData) {
        this.textData=null;
        this.binaryData = binaryData;
    }
    
    public boolean isBinary(){
        return binaryData!=null;
    }
    
    public boolean isText(){
        return textData!=null;
    }
}
