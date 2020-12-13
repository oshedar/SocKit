/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.sockitserver;

import io.sockit.servertools.CommandDataSocket;

/**
 *
 * @author Hoshedar Irani
 */
class CommandData {
    final String command;
    final Object data;
    final boolean isBinary;
    final CommandDataSocket socket;
    CommandData(CommandDataSocket socket, String command, String data) {
        this.command = command;
        this.data = data;
        this.isBinary=false;
        this.socket=socket;
    }

    CommandData(CommandDataSocket socket,String command, byte[] data) {
        this.command = command;
        this.data = data;
        this.isBinary=true;
        this.socket=socket;
    }
    
}
