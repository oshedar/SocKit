/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.sockit.clienttools;

/**
 *
 * @author hoshi2
 */
public interface CommandDataReadListener {
    public void commandDataRead(CommandDataSocket socket,String command,String data);
    public void commandDataReadBytes(CommandDataSocket socket,String command,byte[] data);
}
