/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

import java.net.InetAddress;

/**
 *
 * @author h
 */
public interface CommandDataSocket extends AbstractSocket {
    
    boolean isLocal();
    
    void setCommandDataReadListener(CommandDataReadListener commandDataReadListener);

    void setEncryptorDecryptor(Encryptor encryptor, Decryptor decryptor);

    void write(String command, String data);
    
    void write(String command, byte[] data);
    
    InetAddress getRemoteAddress();
    
    int getRemotePort();
       
}
