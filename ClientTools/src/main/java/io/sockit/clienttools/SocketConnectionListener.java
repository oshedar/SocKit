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
public interface SocketConnectionListener {
    public void socketConnecting(CommandDataSocket socket);
    public void socketConnected(CommandDataSocket socket);
    public void connectionFailed(CommandDataSocket socket, Exception exception);
    public void socketDisconnected(CommandDataSocket socket);
    public void socketClosed(CommandDataSocket socket);
}
