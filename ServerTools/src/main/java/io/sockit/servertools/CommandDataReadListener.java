/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.sockit.servertools;

/**
 *
 * @author hoshi2
 */
public interface CommandDataReadListener {
    void commandDataRead(CommandDataSocket socket,String command,String data);
    void commandDataRead(CommandDataSocket socket,String command,byte[] data);
}
