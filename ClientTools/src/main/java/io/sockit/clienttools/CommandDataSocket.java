/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.clienttools;

import java.net.URI;

/**
 *
 * @author h
 */
public interface CommandDataSocket {
    
    void close();

    boolean isClosed();

    boolean isLocal();
    
    void setCommandDataReadListener(CommandDataReadListener commandDataReadListener);

    void setEncryptorDecryptor(Encryptor encryptor, Decryptor decryptor);

    void write(String command, String data);
    
    void write(String command, byte[] data);
    
}
