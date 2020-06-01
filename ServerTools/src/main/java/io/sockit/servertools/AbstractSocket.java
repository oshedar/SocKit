/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.sockit.servertools;

/**
 *
 * @author finclusion
 */
public interface AbstractSocket {

    void close();

    boolean isClosed();

    void setSocketClosedListener(SocketClosedListener socketClosedListener);

}
