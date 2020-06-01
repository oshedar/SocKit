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
    public interface Logger {

    /**
     * Logs a message
     * @param mesg - the message to be logged
     */
    public void log(String mesg);

    /**
     * logs an error
     * @param throwable - the error to be logged
     */
    public void log(Throwable throwable);

    /**
     * Destroys the log handler and frees up resources.
     */
    public void destroy();
}
